package com.walmart.realestate.crystal.storereview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationCaseTimeInTarget;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationRackTimeInTarget;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationStoreTimeInTarget;
import com.walmart.realestate.crystal.storereview.client.storeinfo.model.StoreDetail;
import com.walmart.realestate.crystal.storereview.client.storeinfo.model.facilitydetails.*;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewAssetHealthScoreEntity;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewStoreHealthScoreEntity;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.model.StoreReviewAssetHealthScore;
import com.walmart.realestate.crystal.storereview.model.StoreReviewStoreHealthScore;
import com.walmart.realestate.crystal.storereview.properties.StoreReviewProperties;
import com.walmart.realestate.crystal.storereview.repository.StoreReviewAssetHealthScoreRepository;
import com.walmart.realestate.crystal.storereview.repository.StoreReviewStoreHealthScoreRepository;
import com.walmart.realestate.crystal.storereview.service.healthscoredetailshandler.PostMaintenanceDetailsProcessor;
import com.walmart.realestate.crystal.storereview.service.healthscoredetailshandler.PostReviewDetailsProcessor;
import com.walmart.realestate.crystal.storereview.service.healthscoredetailshandler.PreReviewDetailsProcessor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.walmart.realestate.crystal.storereview.service.StoreReviewHealthScoreService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StoreReviewHealthScoreService.class, StoreReviewProperties.class, PreReviewDetailsProcessor.class, PostReviewDetailsProcessor.class, PostMaintenanceDetailsProcessor.class})
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@ActiveProfiles("test")
class StoreReviewHealthScoreServiceTest {

    @Autowired
    private StoreReviewHealthScoreService storeReviewHealthScoreService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostReviewDetailsProcessor postReviewDetailsProcessor;

    @Autowired
    private PostMaintenanceDetailsProcessor postMaintenanceDetailsProcessor;

    @MockBean
    private StoreReviewService storeReviewService;

    @MockBean
    private StoreAssetService storeAssetService;

    @MockBean
    private StoreService storeService;

    @MockBean
    private StoreReviewStoreHealthScoreRepository storeReviewStoreHealthScoreRepository;

    @MockBean
    private StoreReviewAssetHealthScoreRepository storeReviewAssetHealthScoreRepository;

    @BeforeEach
    void setup() {
        setupCurrentTime(Instant.parse("2021-12-01T00:00:00Z"));
    }

    private void setupCurrentTime(Instant now) {
        postReviewDetailsProcessor.setTimeSupplier(() -> now);
        postMaintenanceDetailsProcessor.setTimeSupplier(() -> now);
    }

    @Test
    void testConsumeEventForCreate() throws JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        when(storeReviewService.getStoreReviewByAggregateId(uuid.toString())).thenReturn(StoreReview.builder()
                .id("SR-76")
                .storeNumber(143L)
                .build());

        Instant timestamp = Instant.parse("2021-09-10T11:00:00Z");
        when(storeAssetService.getStoreHealthScore(143L, timestamp)).thenReturn(RefrigerationStoreTimeInTarget.builder()
                .storeNumber(143L)
                .timeInTarget(72.51)
                .runTime(timestamp.minusSeconds(720))
                .build());

        when(storeAssetService.getAssetHealthScore(143L, timestamp)).thenReturn(Arrays.asList(
                RefrigerationRackTimeInTarget.builder()
                        .assetMappingId("2631")
                        .timeInTarget(23.59)
                        .runTime(timestamp.minusSeconds(900))
                        .build(),
                RefrigerationCaseTimeInTarget.builder()
                        .assetMappingId("2638")
                        .timeInTarget(27.56)
                        .runTime(timestamp.minusSeconds(1200))
                        .build()));

        storeReviewHealthScoreService.processEvent(objectMapper.readTree(buildJson(STARTED_EVENT, uuid, timestamp)));

        verify(storeReviewService).getStoreReviewByAggregateId(uuid.toString());
        verify(storeAssetService).getStoreHealthScore(143L, timestamp);

        verify(storeReviewAssetHealthScoreRepository).findByStoreReviewId("SR-76");

        StoreReviewStoreHealthScoreEntity storeHealthScoreEntity = StoreReviewStoreHealthScoreEntity.builder()
                .storeReviewId("SR-76")
                .storeNumber(143L)
                .reviewStartTimestamp(timestamp)
                .healthScoreStart(72.51)
                .timestampStart(timestamp.minusSeconds(720))
                .build();
        verify(storeReviewStoreHealthScoreRepository).save(storeHealthScoreEntity);

        verify(storeReviewAssetHealthScoreRepository).findByStoreReviewId("SR-76");

        List<StoreReviewAssetHealthScoreEntity> assetHealthScoreEntities = Arrays.asList(
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("2631")
                        .reviewStartTimestamp(timestamp)
                        .healthScoreStart(23.59)
                        .timestampStart(timestamp.minusSeconds(900))
                        .build(),
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("2638")
                        .reviewStartTimestamp(timestamp)
                        .healthScoreStart(27.56)
                        .timestampStart(timestamp.minusSeconds(1200))
                        .build());
        verify(storeReviewAssetHealthScoreRepository).saveAll(assetHealthScoreEntities);
    }

    @Test
    void testConsumeEventForUpdate() throws JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        when(storeReviewService.getStoreReviewByAggregateId(uuid.toString())).thenReturn(StoreReview.builder()
                .id("SR-76")
                .storeNumber(143L)
                .build());

        Instant timestampStart = Instant.parse("2021-09-10T11:00:00Z");

        StoreReviewStoreHealthScoreEntity storeHealthScoreEntity = StoreReviewStoreHealthScoreEntity.builder()
                .storeReviewId("SR-76")
                .storeNumber(143L)
                .reviewStartTimestamp(timestampStart)
                .healthScoreStart(72.51)
                .timestampStart(timestampStart.minusSeconds(720))
                .build();
        when(storeReviewStoreHealthScoreRepository.findByStoreReviewId("SR-76")).thenReturn(Optional.of(storeHealthScoreEntity));

        List<StoreReviewAssetHealthScoreEntity> assetHealthScoreEntities = Arrays.asList(
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("2631")
                        .reviewStartTimestamp(timestampStart)
                        .healthScoreStart(23.59)
                        .timestampStart(timestampStart.minusSeconds(900))
                        .build(),
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("2638")
                        .reviewStartTimestamp(timestampStart)
                        .healthScoreStart(27.56)
                        .timestampStart(timestampStart.minusSeconds(1200))
                        .build());
        when(storeReviewAssetHealthScoreRepository.findByStoreReviewId("SR-76")).thenReturn(assetHealthScoreEntities);

        Instant timestamp = Instant.parse("2021-09-15T16:00:00Z");
        when(storeAssetService.getStoreHealthScore(143L, timestamp)).thenReturn(RefrigerationStoreTimeInTarget.builder()
                .storeNumber(143L)
                .timeInTarget(79.47)
                .runTime(timestamp.minusSeconds(120))
                .build());

        when(storeAssetService.getAssetHealthScore(143L, timestamp)).thenReturn(Arrays.asList(
                RefrigerationRackTimeInTarget.builder()
                        .assetMappingId("2631")
                        .timeInTarget(43.52)
                        .runTime(timestamp.minusSeconds(600))
                        .build(),
                RefrigerationCaseTimeInTarget.builder()
                        .assetMappingId("2638")
                        .timeInTarget(47.09)
                        .runTime(timestamp.minusSeconds(300))
                        .build()));

        storeReviewHealthScoreService.processEvent(objectMapper.readTree(buildJson(MONITORING_EVENT, uuid, timestamp)));

        verify(storeReviewService).getStoreReviewByAggregateId(uuid.toString());
        verify(storeAssetService).getStoreHealthScore(143L, timestamp);
        verify(storeAssetService).getAssetHealthScore(143L, timestamp);

        verify(storeReviewStoreHealthScoreRepository).findByStoreReviewId("SR-76");

        StoreReviewStoreHealthScoreEntity storeHealthScoreEntityUpdated = StoreReviewStoreHealthScoreEntity.builder()
                .storeReviewId("SR-76")
                .storeNumber(143L)
                .reviewStartTimestamp(timestampStart)
                .healthScoreStart(72.51)
                .timestampStart(timestampStart.minusSeconds(720))
                .reviewEndTimestamp(timestamp)
                .healthScoreEnd(79.47)
                .timestampEnd(timestamp.minusSeconds(120))
                .build();
        verify(storeReviewStoreHealthScoreRepository).save(storeHealthScoreEntityUpdated);

        verify(storeReviewAssetHealthScoreRepository).findByStoreReviewId("SR-76");

        List<StoreReviewAssetHealthScoreEntity> assetHealthScoreEntitiesUpdated = Arrays.asList(
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("2631")
                        .reviewStartTimestamp(timestampStart)
                        .healthScoreStart(23.59)
                        .timestampStart(timestampStart.minusSeconds(900))
                        .reviewEndTimestamp(timestamp)
                        .healthScoreEnd(43.52)
                        .timestampEnd(timestamp.minusSeconds(600))
                        .build(),
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("2638")
                        .reviewStartTimestamp(timestampStart)
                        .healthScoreStart(27.56)
                        .timestampStart(timestampStart.minusSeconds(1200))
                        .reviewEndTimestamp(timestamp)
                        .healthScoreEnd(47.09)
                        .timestampEnd(timestamp.minusSeconds(300))
                        .build());
        verify(storeReviewAssetHealthScoreRepository).saveAll(assetHealthScoreEntitiesUpdated);
    }

    @Test
    void testConsumeEventForStatuses() throws JsonProcessingException {
        Instant timestamp = Instant.parse("2021-11-01T00:00:00Z");
        setupCurrentTime(timestamp.plus(32, ChronoUnit.HOURS)); // post-review but not validation

        UUID uuid = UUID.randomUUID();
        when(storeReviewService.getStoreReviewByAggregateId(uuid.toString())).thenReturn(StoreReview.builder()
                .id("SR-76")
                .storeNumber(143L)
                .startedAt(timestamp)
                .monitoringStartedAt(timestamp.plusSeconds(300))
                .build());

        StoreReviewStoreHealthScoreEntity storeHealthScoreEntity = StoreReviewStoreHealthScoreEntity.builder()
                .storeReviewId("SR-76")
                .storeNumber(143L)
                .reviewStartTimestamp(timestamp)
                .healthScoreStart(72.51)
                .timestampStart(timestamp.minusSeconds(720))
                .reviewEndTimestamp(timestamp.plusSeconds(300))
                .healthScoreEnd(79.47)
                .timestampEnd(timestamp.minusSeconds(120))
                .build();
        when(storeReviewStoreHealthScoreRepository.findByStoreReviewId("SR-76")).thenReturn(Optional.of(storeHealthScoreEntity));

        List<StoreReviewAssetHealthScoreEntity> assetHealthScoreEntities = Arrays.asList(
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("34877")
                        .reviewStartTimestamp(timestamp)
                        .healthScoreStart(72.51)
                        .timestampStart(timestamp.minusSeconds(720))
                        .reviewEndTimestamp(timestamp.plusSeconds(300))
                        .healthScoreEnd(79.47)
                        .timestampEnd(timestamp.minusSeconds(120))
                        .build(),
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("34884")
                        .reviewStartTimestamp(timestamp)
                        .healthScoreStart(78.53)
                        .timestampStart(timestamp.minusSeconds(600))
                        .reviewEndTimestamp(timestamp.plusSeconds(300))
                        .healthScoreEnd(72.32)
                        .timestampEnd(timestamp.minusSeconds(240))
                        .build());
        when(storeReviewAssetHealthScoreRepository.findByStoreReviewId("SR-76")).thenReturn(assetHealthScoreEntities);

        when(storeAssetService.getStoreHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS))).thenReturn(RefrigerationStoreTimeInTarget.builder()
                .storeNumber(143L)
                .timeInTarget(72.51)
                .runTime(timestamp.minusSeconds(720))
                .build());

        when(storeAssetService.getAssetHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS))).thenReturn(Arrays.asList(
                RefrigerationRackTimeInTarget.builder()
                        .assetMappingId("34877")
                        .timeInTarget(23.59)
                        .runTime(timestamp.minusSeconds(900))
                        .build(),
                RefrigerationCaseTimeInTarget.builder()
                        .assetMappingId("34884")
                        .timeInTarget(27.56)
                        .runTime(timestamp.minusSeconds(1200))
                        .build()));
        when(storeReviewAssetHealthScoreRepository.findTopByStoreReviewId("SR-76")).thenReturn(assetHealthScoreEntities.get(0));
        when(storeAssetService.getStoreHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS))).thenReturn(RefrigerationStoreTimeInTarget.builder()
                .storeNumber(143L)
                .timeInTarget(73.80)
                .runTime(timestamp.plus(30, ChronoUnit.HOURS))
                .build());

        when(storeAssetService.getAssetHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS))).thenReturn(Arrays.asList(
                RefrigerationRackTimeInTarget.builder()
                        .assetMappingId("34877")
                        .timeInTarget(27.36)
                        .runTime(timestamp.plus(30, ChronoUnit.HOURS))
                        .build(),
                RefrigerationCaseTimeInTarget.builder()
                        .assetMappingId("34884")
                        .timeInTarget(23.63)
                        .runTime(timestamp.plus(30, ChronoUnit.HOURS))
                        .build()));

        LocationTimeZone locationTimeZone = LocationTimeZone.builder()
                .dstTimeZone(DstTimeZone.builder()
                        .timeZoneId("America/Los_Angeles")
                        .build())
                .build();
        LocationAddress locationAddress = LocationAddress.builder()
                .addressLine1("Address line 1")
                .city("City")
                .state("ZZ")
                .postalCode("99999-0000")
                .build();
        when(storeService.getStoreInfo(143L)).thenReturn(StoreDetail.builder()
                .facilityDetail(FacilityDetail.builder()
                        .location(Location.builder()
                                .locationTimeZone(locationTimeZone)
                                .locationAddress(locationAddress)
                                .build())
                        .build())
                .build());

        storeReviewHealthScoreService.processEvent(objectMapper.readTree(buildJson(POST_REVIEW_EVENT, uuid, timestamp)));
        assertThat(storeHealthScoreEntity.getPreReviewTimestamp()).isEqualTo(timestamp.minus(17, ChronoUnit.HOURS));
        assertThat(storeHealthScoreEntity.getPreReviewScore()).isEqualTo(72.51);
        assertThat(storeHealthScoreEntity.getPreReviewScoreTimestamp()).isEqualTo(timestamp.minusSeconds(720));
        assertThat(storeHealthScoreEntity.getPostReviewTimestamp()).isEqualTo(timestamp.plus(31, ChronoUnit.HOURS));
        assertThat(storeHealthScoreEntity.getPostReviewScore()).isEqualTo(73.80);
        assertThat(storeHealthScoreEntity.getPostReviewScoreTimestamp()).isEqualTo(timestamp.plus(30, ChronoUnit.HOURS));
        assertThat(storeHealthScoreEntity.getPostMaintenanceTimestamp()).isNull();
        assertThat(storeHealthScoreEntity.getPostMaintenanceScore()).isNull();
        assertThat(storeHealthScoreEntity.getPostMaintenanceScoreTimestamp()).isNull();

        assertThat(assetHealthScoreEntities.get(0).getPreReviewTimestamp()).isEqualTo(timestamp.minus(17, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(0).getPreReviewScore()).isEqualTo(23.59);
        assertThat(assetHealthScoreEntities.get(0).getPreReviewScoreTimestamp()).isEqualTo(timestamp.minusSeconds(900));
        assertThat(assetHealthScoreEntities.get(0).getPostReviewTimestamp()).isEqualTo(timestamp.plus(31, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(0).getPostReviewScore()).isEqualTo(27.36);
        assertThat(assetHealthScoreEntities.get(0).getPostReviewScoreTimestamp()).isEqualTo(timestamp.plus(30, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(0).getPostMaintenanceTimestamp()).isNull();
        assertThat(assetHealthScoreEntities.get(0).getPostMaintenanceScore()).isNull();
        assertThat(assetHealthScoreEntities.get(0).getPostMaintenanceScoreTimestamp()).isNull();
        assertThat(assetHealthScoreEntities.get(1).getPreReviewTimestamp()).isEqualTo(timestamp.minus(17, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(1).getPreReviewScore()).isEqualTo(27.56);
        assertThat(assetHealthScoreEntities.get(1).getPreReviewScoreTimestamp()).isEqualTo(timestamp.minusSeconds(1200));
        assertThat(assetHealthScoreEntities.get(1).getPostReviewTimestamp()).isEqualTo(timestamp.plus(31, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(1).getPostReviewScore()).isEqualTo(23.63);
        assertThat(assetHealthScoreEntities.get(1).getPostReviewScoreTimestamp()).isEqualTo(timestamp.plus(30, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(1).getPostMaintenanceTimestamp()).isNull();
        assertThat(assetHealthScoreEntities.get(1).getPostMaintenanceScore()).isNull();
        assertThat(assetHealthScoreEntities.get(1).getPostMaintenanceScoreTimestamp()).isNull();

        verify(storeReviewService).getStoreReviewByAggregateId(uuid.toString());

        verify(storeAssetService).getStoreHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS));
        verify(storeAssetService).getAssetHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS));
        verify(storeAssetService).getStoreHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS));
        verify(storeAssetService).getAssetHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS));
        verifyNoMoreInteractions(storeAssetService);

        verify(storeReviewStoreHealthScoreRepository).findByStoreReviewId("SR-76");
        verify(storeReviewAssetHealthScoreRepository).findByStoreReviewId("SR-76");
        verify(storeReviewStoreHealthScoreRepository).save(storeHealthScoreEntity);
        verify(storeReviewAssetHealthScoreRepository).saveAll(assetHealthScoreEntities);
    }

    @Test
    void testConsumeEventForRestart() throws JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        when(storeReviewService.getStoreReviewByAggregateId(uuid.toString())).thenReturn(StoreReview.builder()
                .id("SR-76")
                .storeNumber(143L)
                .build());

        Instant timestampStart = Instant.parse("2021-09-10T11:00:00Z");
        Instant timestampEnd = Instant.parse("2021-09-15T16:00:00Z");

        StoreReviewStoreHealthScoreEntity storeHealthScoreEntity = StoreReviewStoreHealthScoreEntity.builder()
                .storeReviewId("SR-76")
                .storeNumber(143L)
                .preReviewTimestamp(timestampStart.minusSeconds(300))
                .preReviewScore(71.23)
                .preReviewScoreTimestamp(timestampStart.minusSeconds(600))
                .reviewStartTimestamp(timestampStart)
                .healthScoreStart(72.51)
                .timestampStart(timestampStart.minusSeconds(720))
                .reviewEndTimestamp(timestampEnd)
                .healthScoreEnd(79.47)
                .timestampEnd(timestampEnd.minusSeconds(120))
                .postReviewTimestamp(timestampEnd.plusSeconds(300))
                .postReviewScore(80.33)
                .postReviewScoreTimestamp(timestampEnd.plusSeconds(120))
                .postMaintenanceTimestamp(timestampEnd.plusSeconds(3000))
                .postMaintenanceScore(85.33)
                .postMaintenanceScoreTimestamp(timestampEnd.plusSeconds(2700))
                .build();
        when(storeReviewStoreHealthScoreRepository.findByStoreReviewId("SR-76")).thenReturn(Optional.of(storeHealthScoreEntity));

        List<StoreReviewAssetHealthScoreEntity> assetHealthScoreEntities = Arrays.asList(
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("2631")
                        .preReviewTimestamp(timestampStart.minusSeconds(1800))
                        .preReviewScore(21.31)
                        .preReviewScoreTimestamp(timestampStart.minusSeconds(1500))
                        .reviewStartTimestamp(timestampStart)
                        .healthScoreStart(23.59)
                        .timestampStart(timestampStart.minusSeconds(900))
                        .reviewEndTimestamp(timestampEnd)
                        .healthScoreEnd(43.52)
                        .timestampEnd(timestampEnd.minusSeconds(600))
                        .targetTemperatureEnd(-8.0)
                        .lowCutInTemperatureEnd(-6.0)
                        .lowCutOutTemperatureEnd(-10.0)
                        .averageTemperatureEnd(-8.1)
                        .postReviewTimestamp(timestampEnd.plusSeconds(480))
                        .postReviewScore(42.25)
                        .postReviewScoreTimestamp(timestampEnd.plusSeconds(180))
                        .postMaintenanceTimestamp(timestampEnd.plusSeconds(3300))
                        .postMaintenanceScore(41.16)
                        .postMaintenanceScoreTimestamp(timestampEnd.plusSeconds(2400))
                        .build(),
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("2638")
                        .preReviewTimestamp(timestampStart.minusSeconds(2100))
                        .preReviewScore(26.92)
                        .preReviewScoreTimestamp(timestampStart.minusSeconds(1800))
                        .reviewStartTimestamp(timestampStart)
                        .healthScoreStart(27.56)
                        .timestampStart(timestampStart.minusSeconds(1200))
                        .reviewEndTimestamp(timestampEnd)
                        .healthScoreEnd(47.09)
                        .timestampEnd(timestampEnd.minusSeconds(300))
                        .targetTemperatureEnd(5.0)
                        .lowCutInTemperatureEnd(7.0)
                        .lowCutOutTemperatureEnd(3.0)
                        .averageTemperatureEnd(4.88)
                        .postReviewTimestamp(timestampEnd.plusSeconds(480))
                        .postReviewScore(48.24)
                        .postReviewScoreTimestamp(timestampEnd.plusSeconds(150))
                        .postMaintenanceTimestamp(timestampEnd.plusSeconds(3600))
                        .postMaintenanceScore(44.43)
                        .postMaintenanceScoreTimestamp(timestampEnd.plusSeconds(2100))
                        .build());
        when(storeReviewAssetHealthScoreRepository.findByStoreReviewId("SR-76")).thenReturn(assetHealthScoreEntities);

        Instant timestamp = Instant.parse("2021-09-15T16:00:00Z");

        storeReviewHealthScoreService.processEvent(objectMapper.readTree(buildJson(DETERIORATED_EVENT, uuid, timestamp)));

        verify(storeReviewService).getStoreReviewByAggregateId(uuid.toString());

        verify(storeAssetService, never()).getStoreHealthScore(anyLong(), any());
        verify(storeAssetService, never()).getAssetHealthScore(anyLong(), any());

        verify(storeReviewStoreHealthScoreRepository).findByStoreReviewId("SR-76");

        StoreReviewStoreHealthScoreEntity storeHealthScoreEntityUpdated = StoreReviewStoreHealthScoreEntity.builder()
                .storeReviewId("SR-76")
                .storeNumber(143L)
                .preReviewTimestamp(timestampStart.minusSeconds(300))
                .preReviewScore(71.23)
                .preReviewScoreTimestamp(timestampStart.minusSeconds(600))
                .reviewStartTimestamp(timestampStart)
                .healthScoreStart(72.51)
                .timestampStart(timestampStart.minusSeconds(720))
                .reviewEndTimestamp(null)
                .healthScoreEnd(null)
                .timestampEnd(null)
                .postReviewTimestamp(null)
                .postReviewScore(null)
                .postReviewScoreTimestamp(null)
                .postMaintenanceTimestamp(null)
                .postMaintenanceScore(null)
                .postMaintenanceScoreTimestamp(null)
                .build();
        verify(storeReviewStoreHealthScoreRepository).save(storeHealthScoreEntityUpdated);

        verify(storeReviewAssetHealthScoreRepository).findByStoreReviewId("SR-76");

        List<StoreReviewAssetHealthScoreEntity> assetHealthScoreEntitiesUpdated = Arrays.asList(
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("2631")
                        .preReviewTimestamp(timestampStart.minusSeconds(1800))
                        .preReviewScore(21.31)
                        .preReviewScoreTimestamp(timestampStart.minusSeconds(1500))
                        .reviewStartTimestamp(timestampStart)
                        .healthScoreStart(23.59)
                        .timestampStart(timestampStart.minusSeconds(900))
                        .reviewEndTimestamp(null)
                        .healthScoreEnd(null)
                        .timestampEnd(null)
                        .targetTemperatureEnd(null)
                        .lowCutInTemperatureEnd(null)
                        .lowCutOutTemperatureEnd(null)
                        .averageTemperatureEnd(null)
                        .postReviewTimestamp(null)
                        .postReviewScore(null)
                        .postReviewScoreTimestamp(null)
                        .postMaintenanceTimestamp(null)
                        .postMaintenanceScore(null)
                        .postMaintenanceScoreTimestamp(null)
                        .build(),
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("2638")
                        .preReviewTimestamp(timestampStart.minusSeconds(2100))
                        .preReviewScore(26.92)
                        .preReviewScoreTimestamp(timestampStart.minusSeconds(1800))
                        .reviewStartTimestamp(timestampStart)
                        .healthScoreStart(27.56)
                        .timestampStart(timestampStart.minusSeconds(1200))
                        .reviewEndTimestamp(null)
                        .healthScoreEnd(null)
                        .timestampEnd(null)
                        .targetTemperatureEnd(null)
                        .lowCutInTemperatureEnd(null)
                        .lowCutOutTemperatureEnd(null)
                        .averageTemperatureEnd(null)
                        .postReviewTimestamp(null)
                        .postReviewScore(null)
                        .postReviewScoreTimestamp(null)
                        .postMaintenanceTimestamp(null)
                        .postMaintenanceScore(null)
                        .postMaintenanceScoreTimestamp(null)
                        .build());
        verify(storeReviewAssetHealthScoreRepository).saveAll(assetHealthScoreEntitiesUpdated);
    }

    @Test
    void testConsumeEventForUnknownEvent() throws JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        Instant timestamp = Instant.parse("2021-09-10T11:00:00Z");

        storeReviewHealthScoreService.processEvent(objectMapper.readTree(buildJson("event0", uuid, timestamp)));

        verify(storeReviewService, never()).getStoreReviewByAggregateId(anyString());
        verify(storeAssetService, never()).getStoreHealthScore(anyLong(), any());
        verify(storeAssetService, never()).getAssetHealthScore(anyLong(), any());
        verify(storeReviewStoreHealthScoreRepository, never()).findByStoreReviewId(anyString());
        verify(storeReviewStoreHealthScoreRepository, never()).save(any());
        verify(storeReviewAssetHealthScoreRepository, never()).findByStoreReviewId(anyString());
        verify(storeReviewAssetHealthScoreRepository, never()).saveAll(anyList());
    }

    @Test
    void testGetStoreReviewStoreHealthScore() {
        Instant timestamp = Instant.parse("2021-09-15T16:00:00Z");
        StoreReviewStoreHealthScoreEntity storeHealthScoreEntity = StoreReviewStoreHealthScoreEntity.builder()
                .storeReviewId("SR-82")
                .storeNumber(143L)
                .reviewStartTimestamp(timestamp)
                .healthScoreStart(72.51)
                .timestampStart(timestamp.minusSeconds(720))
                .reviewEndTimestamp(timestamp.plusSeconds(300))
                .healthScoreEnd(79.47)
                .timestampEnd(timestamp.minusSeconds(120))
                .build();
        when(storeReviewStoreHealthScoreRepository.findByStoreReviewId("SR-82")).thenReturn(Optional.of(storeHealthScoreEntity));

        Optional<StoreReviewStoreHealthScore> storeReviewStoreHealthScoreOptional = storeReviewHealthScoreService.getStoreReviewStoreHealthScore("SR-82");

        assertThat(storeReviewStoreHealthScoreOptional).isPresent();

        StoreReviewStoreHealthScore storeReviewStoreHealthScore = storeReviewStoreHealthScoreOptional.get();
        assertThat(storeReviewStoreHealthScore.getStoreReviewId()).isEqualTo("SR-82");
        assertThat(storeReviewStoreHealthScore.getStoreNumber()).isEqualTo(143L);
        assertThat(storeReviewStoreHealthScore.getReviewStartTimestamp()).isEqualTo(timestamp);
        assertThat(storeReviewStoreHealthScore.getHealthScoreStart()).isEqualTo(72.51);
        assertThat(storeReviewStoreHealthScore.getTimestampStart()).isEqualTo(timestamp.minusSeconds(720));
        assertThat(storeReviewStoreHealthScore.getReviewEndTimestamp()).isEqualTo(timestamp.plusSeconds(300));
        assertThat(storeReviewStoreHealthScore.getHealthScoreEnd()).isEqualTo(79.47);
        assertThat(storeReviewStoreHealthScore.getTimestampEnd()).isEqualTo(timestamp.minusSeconds(120));

        verify(storeReviewStoreHealthScoreRepository).findByStoreReviewId("SR-82");
        verify(storeReviewStoreHealthScoreRepository, never()).save(any());
        verify(storeReviewAssetHealthScoreRepository, never()).findByStoreReviewId(anyString());
        verify(storeReviewAssetHealthScoreRepository, never()).saveAll(anyList());
    }

    @Test
    void testGetStoreReviewAssetHealthScore() {
        Instant timestamp = Instant.parse("2021-09-15T16:00:00Z");
        when(storeReviewAssetHealthScoreRepository.findByStoreReviewId("SR-82")).thenReturn(Arrays.asList(
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-82")
                        .assetMappingId("34877")
                        .reviewStartTimestamp(timestamp)
                        .healthScoreStart(72.51)
                        .timestampStart(timestamp.minusSeconds(720))
                        .reviewEndTimestamp(timestamp.plusSeconds(300))
                        .healthScoreEnd(79.47)
                        .timestampEnd(timestamp.minusSeconds(120))
                        .build(),
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-82")
                        .assetMappingId("34884")
                        .reviewStartTimestamp(timestamp)
                        .healthScoreStart(78.53)
                        .timestampStart(timestamp.minusSeconds(600))
                        .reviewEndTimestamp(timestamp.plusSeconds(300))
                        .healthScoreEnd(72.32)
                        .timestampEnd(timestamp.minusSeconds(240))
                        .build()));
        List<StoreReviewAssetHealthScore> storeReviewAssetHealthScores = storeReviewHealthScoreService.getStoreReviewAssetHealthScores("SR-82");

        assertThat(storeReviewAssetHealthScores).hasSize(2);

        assertThat(storeReviewAssetHealthScores.get(0).getStoreReviewId()).isEqualTo("SR-82");
        assertThat(storeReviewAssetHealthScores.get(0).getAssetMappingId()).isEqualTo("34877");
        assertThat(storeReviewAssetHealthScores.get(0).getReviewStartTimestamp()).isEqualTo(timestamp);
        assertThat(storeReviewAssetHealthScores.get(0).getHealthScoreStart()).isEqualTo(72.51);
        assertThat(storeReviewAssetHealthScores.get(0).getTimestampStart()).isEqualTo(timestamp.minusSeconds(720));
        assertThat(storeReviewAssetHealthScores.get(0).getReviewEndTimestamp()).isEqualTo(timestamp.plusSeconds(300));
        assertThat(storeReviewAssetHealthScores.get(0).getHealthScoreEnd()).isEqualTo(79.47);
        assertThat(storeReviewAssetHealthScores.get(0).getTimestampEnd()).isEqualTo(timestamp.minusSeconds(120));

        assertThat(storeReviewAssetHealthScores.get(1).getStoreReviewId()).isEqualTo("SR-82");
        assertThat(storeReviewAssetHealthScores.get(1).getAssetMappingId()).isEqualTo("34884");
        assertThat(storeReviewAssetHealthScores.get(1).getReviewStartTimestamp()).isEqualTo(timestamp);
        assertThat(storeReviewAssetHealthScores.get(1).getHealthScoreStart()).isEqualTo(78.53);
        assertThat(storeReviewAssetHealthScores.get(1).getTimestampStart()).isEqualTo(timestamp.minusSeconds(600));
        assertThat(storeReviewAssetHealthScores.get(0).getReviewEndTimestamp()).isEqualTo(timestamp.plusSeconds(300));
        assertThat(storeReviewAssetHealthScores.get(1).getHealthScoreEnd()).isEqualTo(72.32);
        assertThat(storeReviewAssetHealthScores.get(1).getTimestampEnd()).isEqualTo(timestamp.minusSeconds(240));

        verify(storeReviewStoreHealthScoreRepository, never()).findByStoreReviewId(anyString());
        verify(storeReviewStoreHealthScoreRepository, never()).save(any());
        verify(storeReviewAssetHealthScoreRepository).findByStoreReviewId("SR-82");
        verify(storeReviewAssetHealthScoreRepository, never()).saveAll(anyList());
    }

    @Test
    void testGetStoreReviewAssetHealthScoreByStoreReviewId() {
        Instant timestamp = Instant.parse("2021-09-15T16:00:00Z");
        when(storeReviewAssetHealthScoreRepository.findByStoreReviewIdIn(anyList())).thenReturn(Arrays.asList(
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-1")
                        .assetMappingId("34877")
                        .reviewStartTimestamp(timestamp)
                        .healthScoreStart(72.51)
                        .timestampStart(timestamp.minusSeconds(720))
                        .reviewEndTimestamp(timestamp.plusSeconds(300))
                        .healthScoreEnd(79.47)
                        .timestampEnd(timestamp.minusSeconds(120))
                        .build(),
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-2")
                        .assetMappingId("34877")
                        .reviewStartTimestamp(timestamp)
                        .healthScoreStart(78.53)
                        .timestampStart(timestamp.minusSeconds(600))
                        .reviewEndTimestamp(timestamp.plusSeconds(300))
                        .healthScoreEnd(72.32)
                        .timestampEnd(timestamp.minusSeconds(240))
                        .build()));
        List<StoreReviewAssetHealthScore> storeReviewAssetHealthScores = storeReviewHealthScoreService.getStoreReviewAssetHealthScores(Arrays.asList("SR-1", "SR-2"));

        assertThat(storeReviewAssetHealthScores).hasSize(2);

        assertThat(storeReviewAssetHealthScores.get(0).getStoreReviewId()).isEqualTo("SR-1");
        assertThat(storeReviewAssetHealthScores.get(0).getAssetMappingId()).isEqualTo("34877");
        assertThat(storeReviewAssetHealthScores.get(0).getReviewStartTimestamp()).isEqualTo(timestamp);
        assertThat(storeReviewAssetHealthScores.get(0).getHealthScoreStart()).isEqualTo(72.51);
        assertThat(storeReviewAssetHealthScores.get(0).getTimestampStart()).isEqualTo(timestamp.minusSeconds(720));
        assertThat(storeReviewAssetHealthScores.get(0).getReviewEndTimestamp()).isEqualTo(timestamp.plusSeconds(300));
        assertThat(storeReviewAssetHealthScores.get(0).getHealthScoreEnd()).isEqualTo(79.47);
        assertThat(storeReviewAssetHealthScores.get(0).getTimestampEnd()).isEqualTo(timestamp.minusSeconds(120));

        assertThat(storeReviewAssetHealthScores.get(1).getStoreReviewId()).isEqualTo("SR-2");
        assertThat(storeReviewAssetHealthScores.get(1).getAssetMappingId()).isEqualTo("34877");
        assertThat(storeReviewAssetHealthScores.get(1).getReviewStartTimestamp()).isEqualTo(timestamp);
        assertThat(storeReviewAssetHealthScores.get(1).getHealthScoreStart()).isEqualTo(78.53);
        assertThat(storeReviewAssetHealthScores.get(1).getTimestampStart()).isEqualTo(timestamp.minusSeconds(600));
        assertThat(storeReviewAssetHealthScores.get(1).getReviewEndTimestamp()).isEqualTo(timestamp.plusSeconds(300));
        assertThat(storeReviewAssetHealthScores.get(1).getHealthScoreEnd()).isEqualTo(72.32);
        assertThat(storeReviewAssetHealthScores.get(1).getTimestampEnd()).isEqualTo(timestamp.minusSeconds(240));

        verify(storeReviewAssetHealthScoreRepository).findByStoreReviewIdIn(Arrays.asList("SR-1", "SR-2"));
        verify(storeReviewAssetHealthScoreRepository, never()).saveAll(anyList());
    }

    @Test
    void testUpdateHealthScoresAtStatusesReviewCreated() {
        when(storeReviewService.getStoreReview("SR-76")).thenReturn(StoreReview.builder()
                .id("SR-76")
                .storeNumber(143L)
                .build());

        storeReviewHealthScoreService.updateHealthScoresAtStatuses("SR-76", ZoneId.of("America/Los_Angeles"), false);

        verify(storeReviewService).getStoreReview("SR-76");
    }

    @Test
    void testUpdateHealthScoresAtStatusesReviewInProgress() {
        when(storeReviewService.getStoreReview("SR-76")).thenReturn(StoreReview.builder()
                .id("SR-76")
                .storeNumber(143L)
                .startedAt(Instant.parse("2021-11-01T00:00:00.000Z"))
                .build());

        storeReviewHealthScoreService.updateHealthScoresAtStatuses("SR-76", ZoneId.of("America/Los_Angeles"), false);

        verify(storeReviewService).getStoreReview("SR-76");
    }

    @Test
    void testUpdateHealthScoresAtStatusesReviewMonitoringPreReview() {
        Instant timestamp = Instant.parse("2021-11-01T00:00:00Z");
        setupCurrentTime(timestamp.plus(30, ChronoUnit.HOURS)); // monitoring but not post-review

        when(storeReviewService.getStoreReview("SR-76")).thenReturn(StoreReview.builder()
                .id("SR-76")
                .storeNumber(143L)
                .startedAt(timestamp)
                .monitoringStartedAt(timestamp.plusSeconds(300))
                .build());

        StoreReviewStoreHealthScoreEntity storeHealthScoreEntity = StoreReviewStoreHealthScoreEntity.builder()
                .storeReviewId("SR-76")
                .storeNumber(143L)
                .reviewStartTimestamp(timestamp)
                .healthScoreStart(72.51)
                .timestampStart(timestamp.minusSeconds(720))
                .reviewEndTimestamp(timestamp.plusSeconds(300))
                .healthScoreEnd(79.47)
                .timestampEnd(timestamp.minusSeconds(120))
                .build();
        when(storeReviewStoreHealthScoreRepository.findByStoreReviewId("SR-76")).thenReturn(Optional.of(storeHealthScoreEntity));

        List<StoreReviewAssetHealthScoreEntity> assetHealthScoreEntities = Arrays.asList(
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("34877")
                        .reviewStartTimestamp(timestamp)
                        .healthScoreStart(72.51)
                        .timestampStart(timestamp.minusSeconds(720))
                        .reviewEndTimestamp(timestamp.plusSeconds(300))
                        .healthScoreEnd(79.47)
                        .timestampEnd(timestamp.minusSeconds(120))
                        .build(),
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("34884")
                        .reviewStartTimestamp(timestamp)
                        .healthScoreStart(78.53)
                        .timestampStart(timestamp.minusSeconds(600))
                        .reviewEndTimestamp(timestamp.plusSeconds(300))
                        .healthScoreEnd(72.32)
                        .timestampEnd(timestamp.minusSeconds(240))
                        .build());
        when(storeReviewAssetHealthScoreRepository.findByStoreReviewId("SR-76")).thenReturn(assetHealthScoreEntities);
        when(storeReviewAssetHealthScoreRepository.findTopByStoreReviewId("SR-76")).thenReturn(assetHealthScoreEntities.get(0));
        when(storeAssetService.getStoreHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS))).thenReturn(RefrigerationStoreTimeInTarget.builder()
                .storeNumber(143L)
                .timeInTarget(72.51)
                .runTime(timestamp.minusSeconds(720))
                .build());

        when(storeAssetService.getAssetHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS))).thenReturn(Arrays.asList(
                RefrigerationRackTimeInTarget.builder()
                        .assetMappingId("34877")
                        .timeInTarget(23.59)
                        .runTime(timestamp.minusSeconds(900))
                        .build(),
                RefrigerationCaseTimeInTarget.builder()
                        .assetMappingId("34884")
                        .timeInTarget(27.56)
                        .runTime(timestamp.minusSeconds(1200))
                        .build()));

        when(storeAssetService.getStoreHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS))).thenReturn(RefrigerationStoreTimeInTarget.builder()
                .storeNumber(143L)
                .timeInTarget(73.80)
                .runTime(timestamp.plus(30, ChronoUnit.HOURS))
                .build());

        when(storeAssetService.getAssetHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS))).thenReturn(Arrays.asList(
                RefrigerationRackTimeInTarget.builder()
                        .assetMappingId("34877")
                        .timeInTarget(27.36)
                        .runTime(timestamp.plus(30, ChronoUnit.HOURS))
                        .build(),
                RefrigerationCaseTimeInTarget.builder()
                        .assetMappingId("34884")
                        .timeInTarget(23.63)
                        .runTime(timestamp.plus(30, ChronoUnit.HOURS))
                        .build()));

        storeReviewHealthScoreService.updateHealthScoresAtStatuses("SR-76", ZoneId.of("America/Los_Angeles"), false);

        assertThat(storeHealthScoreEntity.getPreReviewTimestamp()).isEqualTo(timestamp.minus(17, ChronoUnit.HOURS));
        assertThat(storeHealthScoreEntity.getPreReviewScore()).isEqualTo(72.51);
        assertThat(storeHealthScoreEntity.getPreReviewScoreTimestamp()).isEqualTo(timestamp.minusSeconds(720));
        assertThat(storeHealthScoreEntity.getPostReviewTimestamp()).isNull();
        assertThat(storeHealthScoreEntity.getPostReviewScore()).isNull();
        assertThat(storeHealthScoreEntity.getPostReviewScoreTimestamp()).isNull();
        assertThat(storeHealthScoreEntity.getPostMaintenanceTimestamp()).isNull();
        assertThat(storeHealthScoreEntity.getPostMaintenanceScore()).isNull();
        assertThat(storeHealthScoreEntity.getPostMaintenanceScoreTimestamp()).isNull();

        assertThat(assetHealthScoreEntities.get(0).getPreReviewTimestamp()).isEqualTo(timestamp.minus(17, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(0).getPreReviewScore()).isEqualTo(23.59);
        assertThat(assetHealthScoreEntities.get(0).getPreReviewScoreTimestamp()).isEqualTo(timestamp.minusSeconds(900));
        assertThat(assetHealthScoreEntities.get(0).getPostReviewTimestamp()).isNull();
        assertThat(assetHealthScoreEntities.get(0).getPostReviewScore()).isNull();
        assertThat(assetHealthScoreEntities.get(0).getPostReviewScoreTimestamp()).isNull();
        assertThat(assetHealthScoreEntities.get(0).getPostMaintenanceTimestamp()).isNull();
        assertThat(assetHealthScoreEntities.get(0).getPostMaintenanceScore()).isNull();
        assertThat(assetHealthScoreEntities.get(0).getPostMaintenanceScoreTimestamp()).isNull();
        assertThat(assetHealthScoreEntities.get(1).getPreReviewTimestamp()).isEqualTo(timestamp.minus(17, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(1).getPreReviewScore()).isEqualTo(27.56);
        assertThat(assetHealthScoreEntities.get(1).getPreReviewScoreTimestamp()).isEqualTo(timestamp.minusSeconds(1200));
        assertThat(assetHealthScoreEntities.get(1).getPostReviewTimestamp()).isNull();
        assertThat(assetHealthScoreEntities.get(1).getPostReviewScore()).isNull();
        assertThat(assetHealthScoreEntities.get(1).getPostReviewScoreTimestamp()).isNull();
        assertThat(assetHealthScoreEntities.get(1).getPostMaintenanceTimestamp()).isNull();
        assertThat(assetHealthScoreEntities.get(1).getPostMaintenanceScore()).isNull();
        assertThat(assetHealthScoreEntities.get(1).getPostMaintenanceScoreTimestamp()).isNull();

        verify(storeReviewService).getStoreReview("SR-76");

        verify(storeAssetService).getStoreHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS));
        verify(storeAssetService).getAssetHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS));
        verifyNoMoreInteractions(storeAssetService);

        verify(storeReviewStoreHealthScoreRepository).findByStoreReviewId("SR-76");
        verify(storeReviewAssetHealthScoreRepository).findByStoreReviewId("SR-76");
        verify(storeReviewStoreHealthScoreRepository).save(storeHealthScoreEntity);
        verify(storeReviewAssetHealthScoreRepository).saveAll(assetHealthScoreEntities);
    }

    @Test
    void testUpdateHealthScoresAtStatusesReviewMonitoringPostReview() {
        Instant timestamp = Instant.parse("2021-11-01T00:00:00Z");
        setupCurrentTime(timestamp.plus(32, ChronoUnit.HOURS)); // post-review but not validation

        when(storeReviewService.getStoreReview("SR-76")).thenReturn(StoreReview.builder()
                .id("SR-76")
                .storeNumber(143L)
                .startedAt(timestamp)
                .monitoringStartedAt(timestamp.plusSeconds(300))
                .build());

        StoreReviewStoreHealthScoreEntity storeHealthScoreEntity = StoreReviewStoreHealthScoreEntity.builder()
                .storeReviewId("SR-76")
                .storeNumber(143L)
                .reviewStartTimestamp(timestamp)
                .healthScoreStart(72.51)
                .timestampStart(timestamp.minusSeconds(720))
                .reviewEndTimestamp(timestamp.plusSeconds(300))
                .healthScoreEnd(79.47)
                .timestampEnd(timestamp.minusSeconds(120))
                .build();
        when(storeReviewStoreHealthScoreRepository.findByStoreReviewId("SR-76")).thenReturn(Optional.of(storeHealthScoreEntity));

        List<StoreReviewAssetHealthScoreEntity> assetHealthScoreEntities = Arrays.asList(
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("34877")
                        .reviewStartTimestamp(timestamp)
                        .healthScoreStart(72.51)
                        .timestampStart(timestamp.minusSeconds(720))
                        .reviewEndTimestamp(timestamp.plusSeconds(300))
                        .healthScoreEnd(79.47)
                        .timestampEnd(timestamp.minusSeconds(120))
                        .build(),
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("34884")
                        .reviewStartTimestamp(timestamp)
                        .healthScoreStart(78.53)
                        .timestampStart(timestamp.minusSeconds(600))
                        .reviewEndTimestamp(timestamp.plusSeconds(300))
                        .healthScoreEnd(72.32)
                        .timestampEnd(timestamp.minusSeconds(240))
                        .build());
        when(storeReviewAssetHealthScoreRepository.findByStoreReviewId("SR-76")).thenReturn(assetHealthScoreEntities);

        when(storeAssetService.getStoreHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS))).thenReturn(RefrigerationStoreTimeInTarget.builder()
                .storeNumber(143L)
                .timeInTarget(72.51)
                .runTime(timestamp.minusSeconds(720))
                .build());

        when(storeAssetService.getAssetHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS))).thenReturn(Arrays.asList(
                RefrigerationRackTimeInTarget.builder()
                        .assetMappingId("34877")
                        .timeInTarget(23.59)
                        .runTime(timestamp.minusSeconds(900))
                        .build(),
                RefrigerationCaseTimeInTarget.builder()
                        .assetMappingId("34884")
                        .timeInTarget(27.56)
                        .runTime(timestamp.minusSeconds(1200))
                        .build()));
        when(storeReviewAssetHealthScoreRepository.findTopByStoreReviewId("SR-76")).thenReturn(assetHealthScoreEntities.get(0));
        when(storeAssetService.getStoreHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS))).thenReturn(RefrigerationStoreTimeInTarget.builder()
                .storeNumber(143L)
                .timeInTarget(73.80)
                .runTime(timestamp.plus(30, ChronoUnit.HOURS))
                .build());

        when(storeAssetService.getAssetHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS))).thenReturn(Arrays.asList(
                RefrigerationRackTimeInTarget.builder()
                        .assetMappingId("34877")
                        .timeInTarget(27.36)
                        .runTime(timestamp.plus(30, ChronoUnit.HOURS))
                        .build(),
                RefrigerationCaseTimeInTarget.builder()
                        .assetMappingId("34884")
                        .timeInTarget(23.63)
                        .runTime(timestamp.plus(30, ChronoUnit.HOURS))
                        .build()));

        storeReviewHealthScoreService.updateHealthScoresAtStatuses("SR-76", ZoneId.of("America/Los_Angeles"), false);

        assertThat(storeHealthScoreEntity.getPreReviewTimestamp()).isEqualTo(timestamp.minus(17, ChronoUnit.HOURS));
        assertThat(storeHealthScoreEntity.getPreReviewScore()).isEqualTo(72.51);
        assertThat(storeHealthScoreEntity.getPreReviewScoreTimestamp()).isEqualTo(timestamp.minusSeconds(720));
        assertThat(storeHealthScoreEntity.getPostReviewTimestamp()).isEqualTo(timestamp.plus(31, ChronoUnit.HOURS));
        assertThat(storeHealthScoreEntity.getPostReviewScore()).isEqualTo(73.80);
        assertThat(storeHealthScoreEntity.getPostReviewScoreTimestamp()).isEqualTo(timestamp.plus(30, ChronoUnit.HOURS));
        assertThat(storeHealthScoreEntity.getPostMaintenanceTimestamp()).isNull();
        assertThat(storeHealthScoreEntity.getPostMaintenanceScore()).isNull();
        assertThat(storeHealthScoreEntity.getPostMaintenanceScoreTimestamp()).isNull();

        assertThat(assetHealthScoreEntities.get(0).getPreReviewTimestamp()).isEqualTo(timestamp.minus(17, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(0).getPreReviewScore()).isEqualTo(23.59);
        assertThat(assetHealthScoreEntities.get(0).getPreReviewScoreTimestamp()).isEqualTo(timestamp.minusSeconds(900));
        assertThat(assetHealthScoreEntities.get(0).getPostReviewTimestamp()).isEqualTo(timestamp.plus(31, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(0).getPostReviewScore()).isEqualTo(27.36);
        assertThat(assetHealthScoreEntities.get(0).getPostReviewScoreTimestamp()).isEqualTo(timestamp.plus(30, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(0).getPostMaintenanceTimestamp()).isNull();
        assertThat(assetHealthScoreEntities.get(0).getPostMaintenanceScore()).isNull();
        assertThat(assetHealthScoreEntities.get(0).getPostMaintenanceScoreTimestamp()).isNull();
        assertThat(assetHealthScoreEntities.get(1).getPreReviewTimestamp()).isEqualTo(timestamp.minus(17, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(1).getPreReviewScore()).isEqualTo(27.56);
        assertThat(assetHealthScoreEntities.get(1).getPreReviewScoreTimestamp()).isEqualTo(timestamp.minusSeconds(1200));
        assertThat(assetHealthScoreEntities.get(1).getPostReviewTimestamp()).isEqualTo(timestamp.plus(31, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(1).getPostReviewScore()).isEqualTo(23.63);
        assertThat(assetHealthScoreEntities.get(1).getPostReviewScoreTimestamp()).isEqualTo(timestamp.plus(30, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(1).getPostMaintenanceTimestamp()).isNull();
        assertThat(assetHealthScoreEntities.get(1).getPostMaintenanceScore()).isNull();
        assertThat(assetHealthScoreEntities.get(1).getPostMaintenanceScoreTimestamp()).isNull();

        verify(storeReviewService).getStoreReview("SR-76");

        verify(storeAssetService).getStoreHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS));
        verify(storeAssetService).getAssetHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS));
        verify(storeAssetService).getStoreHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS));
        verify(storeAssetService).getAssetHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS));
        verifyNoMoreInteractions(storeAssetService);

        verify(storeReviewStoreHealthScoreRepository).findByStoreReviewId("SR-76");
        verify(storeReviewAssetHealthScoreRepository).findByStoreReviewId("SR-76");
        verify(storeReviewStoreHealthScoreRepository).save(storeHealthScoreEntity);
        verify(storeReviewAssetHealthScoreRepository).saveAll(assetHealthScoreEntities);
    }

    @Test
    void testUpdateHealthScoresAtStatusesReviewValidationPostReview() {
        Instant timestamp = Instant.parse("2021-11-01T00:00:00Z");
        setupCurrentTime(timestamp.plus(100, ChronoUnit.HOURS)); // validation but not post-maintenance

        when(storeReviewService.getStoreReview("SR-76")).thenReturn(StoreReview.builder()
                .id("SR-76")
                .storeNumber(143L)
                .startedAt(timestamp)
                .monitoringStartedAt(timestamp.plusSeconds(300))
                .validationStartedAt(timestamp.plus(96, ChronoUnit.HOURS))
                .build());

        StoreReviewStoreHealthScoreEntity storeHealthScoreEntity = StoreReviewStoreHealthScoreEntity.builder()
                .storeReviewId("SR-76")
                .storeNumber(143L)
                .reviewStartTimestamp(timestamp)
                .healthScoreStart(72.51)
                .timestampStart(timestamp.minusSeconds(720))
                .reviewEndTimestamp(timestamp.plusSeconds(300))
                .healthScoreEnd(79.47)
                .timestampEnd(timestamp.minusSeconds(120))
                .build();
        when(storeReviewStoreHealthScoreRepository.findByStoreReviewId("SR-76")).thenReturn(Optional.of(storeHealthScoreEntity));

        List<StoreReviewAssetHealthScoreEntity> assetHealthScoreEntities = Arrays.asList(
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("34877")
                        .reviewStartTimestamp(timestamp)
                        .healthScoreStart(72.51)
                        .timestampStart(timestamp.minusSeconds(720))
                        .reviewEndTimestamp(timestamp.plusSeconds(300))
                        .healthScoreEnd(79.47)
                        .timestampEnd(timestamp.minusSeconds(120))
                        .build(),
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("34884")
                        .reviewStartTimestamp(timestamp)
                        .healthScoreStart(78.53)
                        .timestampStart(timestamp.minusSeconds(600))
                        .reviewEndTimestamp(timestamp.plusSeconds(300))
                        .healthScoreEnd(72.32)
                        .timestampEnd(timestamp.minusSeconds(240))
                        .build());
        when(storeReviewAssetHealthScoreRepository.findByStoreReviewId("SR-76")).thenReturn(assetHealthScoreEntities);

        when(storeAssetService.getStoreHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS))).thenReturn(RefrigerationStoreTimeInTarget.builder()
                .storeNumber(143L)
                .timeInTarget(72.51)
                .runTime(timestamp.minusSeconds(720))
                .build());

        when(storeAssetService.getAssetHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS))).thenReturn(Arrays.asList(
                RefrigerationRackTimeInTarget.builder()
                        .assetMappingId("34877")
                        .timeInTarget(23.59)
                        .runTime(timestamp.minusSeconds(900))
                        .build(),
                RefrigerationCaseTimeInTarget.builder()
                        .assetMappingId("34884")
                        .timeInTarget(27.56)
                        .runTime(timestamp.minusSeconds(1200))
                        .build()));
        when(storeReviewAssetHealthScoreRepository.findTopByStoreReviewId("SR-76")).thenReturn(assetHealthScoreEntities.get(0));
        when(storeAssetService.getStoreHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS))).thenReturn(RefrigerationStoreTimeInTarget.builder()
                .storeNumber(143L)
                .timeInTarget(73.80)
                .runTime(timestamp.plus(30, ChronoUnit.HOURS))
                .build());

        when(storeAssetService.getAssetHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS))).thenReturn(Arrays.asList(
                RefrigerationRackTimeInTarget.builder()
                        .assetMappingId("34877")
                        .timeInTarget(27.36)
                        .runTime(timestamp.plus(30, ChronoUnit.HOURS))
                        .build(),
                RefrigerationCaseTimeInTarget.builder()
                        .assetMappingId("34884")
                        .timeInTarget(23.63)
                        .runTime(timestamp.plus(30, ChronoUnit.HOURS))
                        .build()));

        storeReviewHealthScoreService.updateHealthScoresAtStatuses("SR-76", ZoneId.of("America/Los_Angeles"), false);

        assertThat(storeHealthScoreEntity.getPreReviewTimestamp()).isEqualTo(timestamp.minus(17, ChronoUnit.HOURS));
        assertThat(storeHealthScoreEntity.getPreReviewScore()).isEqualTo(72.51);
        assertThat(storeHealthScoreEntity.getPreReviewScoreTimestamp()).isEqualTo(timestamp.minusSeconds(720));
        assertThat(storeHealthScoreEntity.getPostReviewTimestamp()).isEqualTo(timestamp.plus(31, ChronoUnit.HOURS));
        assertThat(storeHealthScoreEntity.getPostReviewScore()).isEqualTo(73.80);
        assertThat(storeHealthScoreEntity.getPostReviewScoreTimestamp()).isEqualTo(timestamp.plus(30, ChronoUnit.HOURS));
        assertThat(storeHealthScoreEntity.getPostMaintenanceTimestamp()).isNull();
        assertThat(storeHealthScoreEntity.getPostMaintenanceScore()).isNull();
        assertThat(storeHealthScoreEntity.getPostMaintenanceScoreTimestamp()).isNull();

        assertThat(assetHealthScoreEntities.get(0).getPreReviewTimestamp()).isEqualTo(timestamp.minus(17, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(0).getPreReviewScore()).isEqualTo(23.59);
        assertThat(assetHealthScoreEntities.get(0).getPreReviewScoreTimestamp()).isEqualTo(timestamp.minusSeconds(900));
        assertThat(assetHealthScoreEntities.get(0).getPostReviewTimestamp()).isEqualTo(timestamp.plus(31, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(0).getPostReviewScore()).isEqualTo(27.36);
        assertThat(assetHealthScoreEntities.get(0).getPostReviewScoreTimestamp()).isEqualTo(timestamp.plus(30, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(0).getPostMaintenanceTimestamp()).isNull();
        assertThat(assetHealthScoreEntities.get(0).getPostMaintenanceScore()).isNull();
        assertThat(assetHealthScoreEntities.get(0).getPostMaintenanceScoreTimestamp()).isNull();
        assertThat(assetHealthScoreEntities.get(1).getPreReviewTimestamp()).isEqualTo(timestamp.minus(17, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(1).getPreReviewScore()).isEqualTo(27.56);
        assertThat(assetHealthScoreEntities.get(1).getPreReviewScoreTimestamp()).isEqualTo(timestamp.minusSeconds(1200));
        assertThat(assetHealthScoreEntities.get(1).getPostReviewTimestamp()).isEqualTo(timestamp.plus(31, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(1).getPostReviewScore()).isEqualTo(23.63);
        assertThat(assetHealthScoreEntities.get(1).getPostReviewScoreTimestamp()).isEqualTo(timestamp.plus(30, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(1).getPostMaintenanceTimestamp()).isNull();
        assertThat(assetHealthScoreEntities.get(1).getPostMaintenanceScore()).isNull();
        assertThat(assetHealthScoreEntities.get(1).getPostMaintenanceScoreTimestamp()).isNull();

        verify(storeReviewService).getStoreReview("SR-76");

        verify(storeAssetService).getStoreHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS));
        verify(storeAssetService).getAssetHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS));
        verify(storeAssetService).getStoreHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS));
        verify(storeAssetService).getAssetHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS));
        verifyNoMoreInteractions(storeAssetService);

        verify(storeReviewStoreHealthScoreRepository).findByStoreReviewId("SR-76");
        verify(storeReviewAssetHealthScoreRepository).findByStoreReviewId("SR-76");
        verify(storeReviewStoreHealthScoreRepository).save(storeHealthScoreEntity);
        verify(storeReviewAssetHealthScoreRepository).saveAll(assetHealthScoreEntities);
    }

    @Test
    void testUpdateHealthScoresAtStatusesReviewValidationPostMaintenance() {
        Instant timestamp = Instant.parse("2021-11-01T00:00:00Z");
        setupCurrentTime(timestamp.plus(136, ChronoUnit.HOURS)); // post-maintenance

        when(storeReviewService.getStoreReview("SR-76")).thenReturn(StoreReview.builder()
                .id("SR-76")
                .storeNumber(143L)
                .startedAt(timestamp)
                .monitoringStartedAt(timestamp.plusSeconds(300))
                .validationStartedAt(timestamp.plus(96, ChronoUnit.HOURS))
                .build());

        StoreReviewStoreHealthScoreEntity storeHealthScoreEntity = StoreReviewStoreHealthScoreEntity.builder()
                .storeReviewId("SR-76")
                .storeNumber(143L)
                .reviewStartTimestamp(timestamp)
                .healthScoreStart(72.51)
                .timestampStart(timestamp.minusSeconds(720))
                .reviewEndTimestamp(timestamp.plusSeconds(300))
                .healthScoreEnd(79.47)
                .timestampEnd(timestamp.minusSeconds(120))
                .build();
        when(storeReviewStoreHealthScoreRepository.findByStoreReviewId("SR-76")).thenReturn(Optional.of(storeHealthScoreEntity));

        List<StoreReviewAssetHealthScoreEntity> assetHealthScoreEntities = Arrays.asList(
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("34877")
                        .reviewStartTimestamp(timestamp)
                        .healthScoreStart(72.51)
                        .timestampStart(timestamp.minusSeconds(720))
                        .reviewEndTimestamp(timestamp.plusSeconds(300))
                        .healthScoreEnd(79.47)
                        .timestampEnd(timestamp.minusSeconds(120))
                        .build(),
                StoreReviewAssetHealthScoreEntity.builder()
                        .storeReviewId("SR-76")
                        .assetMappingId("34884")
                        .reviewStartTimestamp(timestamp)
                        .healthScoreStart(78.53)
                        .timestampStart(timestamp.minusSeconds(600))
                        .reviewEndTimestamp(timestamp.plusSeconds(300))
                        .healthScoreEnd(72.32)
                        .timestampEnd(timestamp.minusSeconds(240))
                        .build());
        when(storeReviewAssetHealthScoreRepository.findByStoreReviewId("SR-76")).thenReturn(assetHealthScoreEntities);

        when(storeAssetService.getStoreHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS))).thenReturn(RefrigerationStoreTimeInTarget.builder()
                .storeNumber(143L)
                .timeInTarget(72.51)
                .runTime(timestamp.minusSeconds(720))
                .build());

        when(storeAssetService.getAssetHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS))).thenReturn(Arrays.asList(
                RefrigerationRackTimeInTarget.builder()
                        .assetMappingId("34877")
                        .timeInTarget(23.59)
                        .runTime(timestamp.minusSeconds(900))
                        .build(),
                RefrigerationCaseTimeInTarget.builder()
                        .assetMappingId("34884")
                        .timeInTarget(27.56)
                        .runTime(timestamp.minusSeconds(1200))
                        .build()));

        when(storeAssetService.getStoreHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS))).thenReturn(RefrigerationStoreTimeInTarget.builder()
                .storeNumber(143L)
                .timeInTarget(73.80)
                .runTime(timestamp.plus(30, ChronoUnit.HOURS))
                .build());

        when(storeAssetService.getAssetHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS))).thenReturn(Arrays.asList(
                RefrigerationRackTimeInTarget.builder()
                        .assetMappingId("34877")
                        .timeInTarget(27.36)
                        .runTime(timestamp.plus(30, ChronoUnit.HOURS))
                        .build(),
                RefrigerationCaseTimeInTarget.builder()
                        .assetMappingId("34884")
                        .timeInTarget(23.63)
                        .runTime(timestamp.plus(30, ChronoUnit.HOURS))
                        .build()));
        when(storeReviewAssetHealthScoreRepository.findTopByStoreReviewId("SR-76")).thenReturn(assetHealthScoreEntities.get(0));
        when(storeAssetService.getStoreHealthScore(143L, timestamp.plus(103, ChronoUnit.HOURS))).thenReturn(RefrigerationStoreTimeInTarget.builder()
                .storeNumber(143L)
                .timeInTarget(73.34)
                .runTime(timestamp.plus(100, ChronoUnit.HOURS))
                .build());

        when(storeAssetService.getAssetHealthScore(143L, timestamp.plus(103, ChronoUnit.HOURS))).thenReturn(Arrays.asList(
                RefrigerationRackTimeInTarget.builder()
                        .assetMappingId("34877")
                        .timeInTarget(23.66)
                        .runTime(timestamp.plus(102, ChronoUnit.HOURS))
                        .build(),
                RefrigerationCaseTimeInTarget.builder()
                        .assetMappingId("34884")
                        .timeInTarget(23.61)
                        .runTime(timestamp.plus(101, ChronoUnit.HOURS))
                        .build()));

        storeReviewHealthScoreService.updateHealthScoresAtStatuses("SR-76", ZoneId.of("America/Los_Angeles"), false);

        assertThat(storeHealthScoreEntity.getPreReviewTimestamp()).isEqualTo(timestamp.minus(17, ChronoUnit.HOURS));
        assertThat(storeHealthScoreEntity.getPreReviewScore()).isEqualTo(72.51);
        assertThat(storeHealthScoreEntity.getPreReviewScoreTimestamp()).isEqualTo(timestamp.minusSeconds(720));
        assertThat(storeHealthScoreEntity.getPostReviewTimestamp()).isEqualTo(timestamp.plus(31, ChronoUnit.HOURS));
        assertThat(storeHealthScoreEntity.getPostReviewScore()).isEqualTo(73.80);
        assertThat(storeHealthScoreEntity.getPostReviewScoreTimestamp()).isEqualTo(timestamp.plus(30, ChronoUnit.HOURS));
        assertThat(storeHealthScoreEntity.getPostMaintenanceTimestamp()).isEqualTo(timestamp.plus(103, ChronoUnit.HOURS));
        assertThat(storeHealthScoreEntity.getPostMaintenanceScore()).isEqualTo(73.34);
        assertThat(storeHealthScoreEntity.getPostMaintenanceScoreTimestamp()).isEqualTo(timestamp.plus(100, ChronoUnit.HOURS));

        assertThat(assetHealthScoreEntities.get(0).getPreReviewTimestamp()).isEqualTo(timestamp.minus(17, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(0).getPreReviewScore()).isEqualTo(23.59);
        assertThat(assetHealthScoreEntities.get(0).getPreReviewScoreTimestamp()).isEqualTo(timestamp.minusSeconds(900));
        assertThat(assetHealthScoreEntities.get(0).getPostReviewTimestamp()).isEqualTo(timestamp.plus(31, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(0).getPostReviewScore()).isEqualTo(27.36);
        assertThat(assetHealthScoreEntities.get(0).getPostReviewScoreTimestamp()).isEqualTo(timestamp.plus(30, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(0).getPostMaintenanceTimestamp()).isEqualTo(timestamp.plus(103, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(0).getPostMaintenanceScore()).isEqualTo(23.66);
        assertThat(assetHealthScoreEntities.get(0).getPostMaintenanceScoreTimestamp()).isEqualTo(timestamp.plus(102, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(1).getPreReviewTimestamp()).isEqualTo(timestamp.minus(17, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(1).getPreReviewScore()).isEqualTo(27.56);
        assertThat(assetHealthScoreEntities.get(1).getPreReviewScoreTimestamp()).isEqualTo(timestamp.minusSeconds(1200));
        assertThat(assetHealthScoreEntities.get(1).getPostReviewTimestamp()).isEqualTo(timestamp.plus(31, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(1).getPostReviewScore()).isEqualTo(23.63);
        assertThat(assetHealthScoreEntities.get(1).getPostReviewScoreTimestamp()).isEqualTo(timestamp.plus(30, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(1).getPostMaintenanceTimestamp()).isEqualTo(timestamp.plus(103, ChronoUnit.HOURS));
        assertThat(assetHealthScoreEntities.get(1).getPostMaintenanceScore()).isEqualTo(23.61);
        assertThat(assetHealthScoreEntities.get(1).getPostMaintenanceScoreTimestamp()).isEqualTo(timestamp.plus(101, ChronoUnit.HOURS));

        verify(storeReviewService).getStoreReview("SR-76");

        verify(storeAssetService).getStoreHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS));
        verify(storeAssetService).getAssetHealthScore(143L, timestamp.minus(17, ChronoUnit.HOURS));
        verify(storeAssetService).getStoreHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS));
        verify(storeAssetService).getAssetHealthScore(143L, timestamp.plus(31, ChronoUnit.HOURS));
        verify(storeAssetService).getStoreHealthScore(143L, timestamp.plus(103, ChronoUnit.HOURS));
        verify(storeAssetService).getAssetHealthScore(143L, timestamp.plus(103, ChronoUnit.HOURS));
        verifyNoMoreInteractions(storeAssetService);

        verify(storeReviewStoreHealthScoreRepository).findByStoreReviewId("SR-76");
        verify(storeReviewAssetHealthScoreRepository).findByStoreReviewId("SR-76");
        verify(storeReviewStoreHealthScoreRepository).save(storeHealthScoreEntity);
        verify(storeReviewAssetHealthScoreRepository).saveAll(assetHealthScoreEntities);
    }

    @SneakyThrows
    private String buildJson(String eventName, UUID id, Instant timestamp) {
        return objectMapper.writeValueAsString(JsonNodeFactory.instance.objectNode()
                .put("eventName", eventName)
                .put("aggregateId", id.toString())
                .put("timestamp", timestamp.toString()));
    }

}
