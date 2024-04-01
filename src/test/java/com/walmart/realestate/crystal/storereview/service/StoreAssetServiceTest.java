package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.*;
import com.walmart.realestate.crystal.storereview.config.TestAsyncConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StoreAssetService.class, TestAsyncConfig.class})
@ActiveProfiles("test")
class StoreAssetServiceTest {

    @Autowired
    private StoreAssetService storeAssetService;

    @MockBean
    private HealthMetricsClient healthMetricsClient;

    @MockBean
    private StoreReviewAssetService storeReviewAssetService;

    @MockBean
    private AssetService assetService;

    @Test
    void testGetStoreHealthScore() {
        when(healthMetricsClient.getStoreHealthScores(singletonList(115L)))
                .thenReturn(CollectionModel.wrap(singletonList(
                        RefrigerationStoreTimeInTarget.builder()
                                .storeNumber(115L)
                                .timeInTarget(98.2)
                                .build())));

        Map<Long, RefrigerationStoreTimeInTarget> healthScores = storeAssetService.getStoreHealthScore(singletonList(115L));

        assertThat(healthScores).hasSize(1);
        assertThat(healthScores.get(115L).getStoreNumber()).isEqualTo(115L);
        assertThat(healthScores.get(115L).getTimeInTarget()).isEqualTo(98.2);

        verify(healthMetricsClient).getStoreHealthScores(singletonList(115L));
    }

    @Test
    void testGetStoreHealthScoreWithDuplicates() {
        when(healthMetricsClient.getStoreHealthScores(singletonList(115L)))
                .thenReturn(CollectionModel.wrap(Arrays.asList(
                        RefrigerationStoreTimeInTarget.builder()
                                .storeNumber(115L)
                                .timeInTarget(98.2)
                                .build(),
                        RefrigerationStoreTimeInTarget.builder()
                                .storeNumber(115L)
                                .timeInTarget(93.52)
                                .build())));

        Map<Long, RefrigerationStoreTimeInTarget> healthScores = storeAssetService.getStoreHealthScore(singletonList(115L));

        assertThat(healthScores).hasSize(1);
        assertThat(healthScores.get(115L).getStoreNumber()).isEqualTo(115L);
        assertThat(healthScores.get(115L).getTimeInTarget()).isEqualTo(98.2);

        verify(healthMetricsClient).getStoreHealthScores(singletonList(115L));
    }

    @Test
    void testGetStoreHealthScoreWithTimestamp() {
        Instant timestamp = LocalDateTime.of(2021, 9, 10, 16, 30, 55).toInstant(ZoneOffset.UTC);

        when(healthMetricsClient.getStoreHealthScore(115L, timestamp, timestamp.atZone(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ISO_DATE)))
                .thenReturn(EntityModel.of(RefrigerationStoreTimeInTarget.builder()
                        .storeNumber(115L)
                        .timeInTarget(98.2)
                        .build()));

        RefrigerationStoreTimeInTarget healthScore = storeAssetService.getStoreHealthScore(115L, timestamp);

        assertThat(healthScore).isNotNull();
        assertThat(healthScore.getStoreNumber()).isEqualTo(115L);
        assertThat(healthScore.getTimeInTarget()).isEqualTo(98.2);

        verify(healthMetricsClient).getStoreHealthScore(115L, timestamp, timestamp.atZone(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ISO_DATE));
    }

    @Test
    void testGetAssetHealthScore() {
        List<RefrigerationRackTimeInTarget> rackHealthScores = Arrays.asList(
                RefrigerationRackTimeInTarget.builder()
                        .storeNumber(116L)
                        .rackCallLetter("A")
                        .timeInTarget(82.29)
                        .equipmentId(44831L)
                        .build(),
                RefrigerationRackTimeInTarget.builder()
                        .storeNumber(116L)
                        .rackCallLetter("BS")
                        .timeInTarget(32.01)
                        .build());

        when(healthMetricsClient.getRackHealthScore(116L)).thenReturn(CollectionModel.wrap(rackHealthScores));

        List<RefrigerationCaseTimeInTarget> caseHealthScores = Arrays.asList(
                RefrigerationCaseTimeInTarget.builder()
                        .storeNumber(116L)
                        .rackName("Rack A-1")
                        .caseName("Case A-1-1")
                        .timeInTarget(56.1)
                        .equipmentId(24884L)
                        .temperatureSensorId("92663")
                        .build(),
                RefrigerationCaseTimeInTarget.builder()
                        .storeNumber(116L)
                        .rackName("Rack A-1")
                        .caseName("Case A-1-2")
                        .equipmentId(24886L)
                        .temperatureSensorId("92664")
                        .timeInTarget(82.93)
                        .build());

        when(healthMetricsClient.getCaseHealthScore(116L)).thenReturn(CollectionModel.wrap(caseHealthScores));

        List<RefrigerationSensor> assets = Arrays.asList(
                RefrigerationSensor.builder()
                        .id("A-116")
                        .storeNumber("116")
                        .rackCallLetter("A")
                        .type("rack")
                        .build(),
                RefrigerationSensor.builder()
                        .id("92663")
                        .type("case")
                        .build(),
                RefrigerationSensor.builder()
                        .id("BS-116")
                        .type("rack")
                        .storeNumber("116")
                        .rackCallLetter("BS")
                        .build(),
                RefrigerationSensor.builder()
                        .id("92664")
                        .type("case")
                        .build());

        when(storeReviewAssetService.getAssetsForStore(116L)).thenReturn(assets);

        List<RefrigerationAssetTimeInTarget> healthScores = storeAssetService.getAssetHealthScore(116L);

        assertThat(healthScores).hasSize(4);

        assertThat(healthScores.get(0)).isInstanceOf(RefrigerationRackTimeInTarget.class);
        RefrigerationRackTimeInTarget rack1 = (RefrigerationRackTimeInTarget) healthScores.get(0);
        assertThat(rack1.getAssetMappingId()).isEqualTo("A-116");
        assertThat(rack1.getStoreNumber()).isEqualTo(116L);
        assertThat(rack1.getEquipmentId()).isEqualTo(44831L);
        assertThat(rack1.getTimeInTarget()).isEqualTo(82.29);

        assertThat(healthScores.get(3)).isInstanceOf(RefrigerationCaseTimeInTarget.class);
        RefrigerationCaseTimeInTarget case3 = (RefrigerationCaseTimeInTarget) healthScores.get(3);
        assertThat(case3.getAssetMappingId()).isEqualTo("92664");
        assertThat(case3.getStoreNumber()).isEqualTo(116L);
        assertThat(case3.getEquipmentId()).isEqualTo(24886L);
        assertThat(case3.getRackName()).isEqualTo("Rack A-1");
        assertThat(case3.getCaseName()).isEqualTo("Case A-1-2");
        assertThat(case3.getTimeInTarget()).isEqualTo(82.93);

        verify(healthMetricsClient).getRackHealthScore(116L);
        verify(healthMetricsClient).getCaseHealthScore(116L);
        verify(storeReviewAssetService).getAssetsForStore(116L);
    }

    @Test
    void testGetAssetHealthScoreWithTimestamp() {
        Instant timestamp = LocalDateTime.of(2021, 9, 10, 16, 30, 55).toInstant(ZoneOffset.UTC);

        List<RefrigerationRackTimeInTarget> rackHealthScores = Arrays.asList(
                RefrigerationRackTimeInTarget.builder()
                        .storeNumber(116L)
                        .rackCallLetter("A")
                        .timeInTarget(82.29)
                        .equipmentId(44831L)
                        .build(),
                RefrigerationRackTimeInTarget.builder()
                        .storeNumber(116L)
                        .rackCallLetter("BS")
                        .timeInTarget(32.01)
                        .build());

        when(healthMetricsClient.getRackHealthScore(116L, timestamp, timestamp.atZone(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ISO_DATE))).thenReturn(CollectionModel.wrap(rackHealthScores));

        List<RefrigerationCaseTimeInTarget> caseHealthScores = Arrays.asList(
                RefrigerationCaseTimeInTarget.builder()
                        .storeNumber(116L)
                        .rackName("Rack A-1")
                        .caseName("Case A-1-1")
                        .timeInTarget(56.1)
                        .equipmentId(24884L)
                        .temperatureSensorId("92663")
                        .build(),
                RefrigerationCaseTimeInTarget.builder()
                        .storeNumber(116L)
                        .rackName("Rack A-1")
                        .caseName("Case A-1-2")
                        .equipmentId(24886L)
                        .temperatureSensorId("92664")
                        .timeInTarget(82.93)
                        .build());

        when(healthMetricsClient.getCaseHealthScore(116L, timestamp, timestamp.atZone(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ISO_DATE))).thenReturn(CollectionModel.wrap(caseHealthScores));

        List<RefrigerationSensor> assets = Arrays.asList(
                RefrigerationSensor.builder()
                        .id("A-116")
                        .storeNumber("116")
                        .rackCallLetter("A")
                        .type("rack")
                        .build(),
                RefrigerationSensor.builder()
                        .id("92663")
                        .type("case")
                        .build(),
                RefrigerationSensor.builder()
                        .id("BS-116")
                        .type("rack")
                        .storeNumber("116")
                        .rackCallLetter("BS")
                        .build(),
                RefrigerationSensor.builder()
                        .id("92664")
                        .type("case")
                        .build());

        when(storeReviewAssetService.getAssetsForStore(116L)).thenReturn(assets);

        List<RefrigerationAssetTimeInTarget> healthScores = storeAssetService.getAssetHealthScore(116L, timestamp);

        assertThat(healthScores).hasSize(4);

        assertThat(healthScores.get(0)).isInstanceOf(RefrigerationRackTimeInTarget.class);
        RefrigerationRackTimeInTarget rack1 = (RefrigerationRackTimeInTarget) healthScores.get(0);
        assertThat(rack1.getAssetMappingId()).isEqualTo("A-116");
        assertThat(rack1.getStoreNumber()).isEqualTo(116L);
        assertThat(rack1.getEquipmentId()).isEqualTo(44831L);
        assertThat(rack1.getTimeInTarget()).isEqualTo(82.29);

        assertThat(healthScores.get(3)).isInstanceOf(RefrigerationCaseTimeInTarget.class);
        RefrigerationCaseTimeInTarget case3 = (RefrigerationCaseTimeInTarget) healthScores.get(3);
        assertThat(case3.getAssetMappingId()).isEqualTo("92664");
        assertThat(case3.getStoreNumber()).isEqualTo(116L);
        assertThat(case3.getEquipmentId()).isEqualTo(24886L);
        assertThat(case3.getRackName()).isEqualTo("Rack A-1");
        assertThat(case3.getCaseName()).isEqualTo("Case A-1-2");
        assertThat(case3.getTimeInTarget()).isEqualTo(82.93);

        verify(healthMetricsClient).getRackHealthScore(116L, timestamp, timestamp.atZone(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ISO_DATE));
        verify(healthMetricsClient).getCaseHealthScore(116L, timestamp, timestamp.atZone(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ISO_DATE));
        verify(storeReviewAssetService).getAssetsForStore(116L);
    }

}
