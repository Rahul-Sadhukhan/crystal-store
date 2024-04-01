package com.walmart.realestate.crystal.settingchangelog.service;

import com.walmart.realestate.crystal.metadata.model.MetadataItem;
import com.walmart.realestate.crystal.metadata.service.MetadataItemService;
import com.walmart.realestate.crystal.settingchangelog.entity.InsightEntity;
import com.walmart.realestate.crystal.settingchangelog.model.Insight;
import com.walmart.realestate.crystal.settingchangelog.repository.InsightRepository;
import com.walmart.realestate.crystal.storereview.config.TestAsyncConfig;
import com.walmart.realestate.crystal.storereview.repository.StoreAssetReviewRepository;
import com.walmart.realestate.crystal.storereview.service.StoreAssetReviewService;
import com.walmart.realestate.crystal.storereview.service.UserAccountService;
import com.walmart.realestate.soteria.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {InsightService.class, TestAsyncConfig.class})
@ActiveProfiles("test")
class InsightServiceTest {

    @Autowired
    private InsightService insightService;

    @MockBean
    private InsightRepository insightRepository;

    @MockBean
    private UserAccountService userAccountService;

    @MockBean
    private MetadataItemService metadataItemService;

    @MockBean
    private StoreAssetReviewService storeAssetReviewService;

    @MockBean
    private StoreAssetReviewRepository storeAssetReviewRepository;

    private Insight insight;

    private List<Insight> insightList;

    private InsightEntity insightEntity;

    private List<InsightEntity> insightEntityList;

    private List<MetadataItem> metadataItemRecommendations;

    private List<MetadataItem> metadataItemProbableCauses;

    private MetadataItem metadataItemObservation;

    @BeforeEach
    void setUp() {

        insight = Insight.builder()
                .referenceId("reference1")
                .assetMappingId("1")
                .storeNumber(1L)
                .recommendations(Arrays.asList("recommendation0", "recommendation1"))
                .probableCauses(Arrays.asList("cause0", "cause1"))
                .observation("observation0")
                .source("source0")
                .build();

        insightList = Arrays.asList(insight,
                Insight.builder()
                        .referenceId("reference1")
                        .assetMappingId("2")
                        .storeNumber(2L)
                        .recommendations(Arrays.asList("recommendation0", "recommendation1"))
                        .probableCauses(Arrays.asList("cause0", "cause1"))
                        .observation("observation0")
                        .source("source0")
                        .build());

        insightEntity = InsightEntity.builder()
                .id("123")
                .referenceId("reference1")
                .assetMappingId("1")
                .storeNumber(1L)
                .recommendations(Arrays.asList("recommendation0", "recommendation1"))
                .recommendationValues(Arrays.asList("recommendationValue0", "recommendationValue1"))
                .probableCauses(Arrays.asList("cause0", "cause1"))
                .probableCauseValues(Arrays.asList("causeValue0", "causeValue1"))
                .observation("observation0")
                .observationValue("observationValue0")
                .source("source0")
                .createdBy("user0")
                .createdAt(Instant.ofEpochSecond(1623178393))
                .build();

        insightEntityList = Arrays.asList(insightEntity,
                InsightEntity.builder()
                        .id("124")
                        .referenceId("reference1")
                        .assetMappingId("2")
                        .storeNumber(2L)
                        .recommendations(Arrays.asList("recommendation0", "recommendation1"))
                        .recommendationValues(Arrays.asList("recommendationValue0", "recommendationValue1"))
                        .probableCauses(Arrays.asList("cause0", "cause1"))
                        .probableCauseValues(Arrays.asList("causeValue0", "causeValue1"))
                        .observation("observation0")
                        .observationValue("observationValue0")
                        .source("source0")
                        .createdBy("user0")
                        .createdAt(Instant.ofEpochSecond(1623178393))
                        .build());

        metadataItemRecommendations = Arrays.asList(MetadataItem.builder().id("recommendation0").defaultValue("recommendationValue0").build(),
                MetadataItem.builder().id("recommendation1").defaultValue("recommendationValue1").build());


        metadataItemProbableCauses = Arrays.asList(MetadataItem.builder().id("cause0").defaultValue("causeValue0").build(),
                MetadataItem.builder().id("cause1").defaultValue("causeValue1").build());

        metadataItemObservation = MetadataItem.builder().id("observation0").defaultValue("observationValue0").build();

    }

    @Test
    void testCreateInsight() {

        when(insightRepository.save(Mockito.any(InsightEntity.class))).thenReturn(insightEntity);
        when(metadataItemService.getMetadataItem("observation0")).thenReturn(metadataItemObservation);
        when(metadataItemService.getMetadataItems(Arrays.asList("recommendation0", "recommendation1"))).thenReturn(metadataItemRecommendations);
        when(metadataItemService.getMetadataItems(Arrays.asList("cause0", "cause1"))).thenReturn(metadataItemProbableCauses);
        when(userAccountService.getUser("user0")).thenReturn(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("Last")
                .build());

        Insight actual = insightService.createInsight(insight);

        assertThat(actual.getId()).isEqualTo("123");
        assertThat(actual.getRecommendationValues()).isEqualTo(Arrays.asList("recommendationValue0", "recommendationValue1"));
        assertThat(actual.getProbableCauseValues()).isEqualTo(Arrays.asList("causeValue0", "causeValue1"));
        assertThat(actual.getCreatedByName()).isEqualTo("First Last");
        assertThat(actual.getObservationValue()).isEqualTo("observationValue0");

        verify(metadataItemService).getMetadataItem("observation0");
        verify(metadataItemService, Mockito.times(2)).getMetadataItems(anyList());
        verify(insightRepository).save(Mockito.any(InsightEntity.class));
        verify(userAccountService).getUser("user0");

    }

    @Test
    void testCreateInsights() {

        List<InsightEntity> entityList = Arrays.asList(
                InsightEntity.builder()
                        .referenceId("reference1")
                        .assetMappingId("1")
                        .storeNumber(1L)
                        .recommendations(Arrays.asList("recommendation0", "recommendation1"))
                        .recommendationValues(Arrays.asList("recommendationValue0", "recommendationValue1"))
                        .probableCauses(Arrays.asList("cause0", "cause1"))
                        .probableCauseValues(Arrays.asList("causeValue0", "causeValue1"))
                        .observation("observation0")
                        .observationValue("observationValue0")
                        .build(),
                InsightEntity.builder()
                        .referenceId("reference1")
                        .assetMappingId("2")
                        .storeNumber(2L)
                        .recommendations(Arrays.asList("recommendation0", "recommendation1"))
                        .recommendationValues(Arrays.asList("recommendationValue0", "recommendationValue1"))
                        .probableCauses(Arrays.asList("cause0", "cause1"))
                        .probableCauseValues(Arrays.asList("causeValue0", "causeValue1"))
                        .observation("observation0")
                        .observationValue("observationValue0")
                        .build());

        when(insightRepository.saveAll(entityList)).thenReturn(insightEntityList);
        when(metadataItemService.getMetadataItem("observation0")).thenReturn(metadataItemObservation);
        when(metadataItemService.getMetadataItems(Mockito.any())).thenReturn(Stream.of(metadataItemRecommendations, metadataItemProbableCauses, Collections.singletonList(metadataItemObservation)).flatMap(Collection::stream).collect(Collectors.toList()));
        when(userAccountService.getUser("user0")).thenReturn(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("Last")
                .build());

        List<Insight> actualList = insightService.createInsights(insightList);

        assertThat(actualList.size()).isEqualTo(2);
        assertThat(actualList.get(0).getId()).isEqualTo("123");
        assertThat(actualList.get(0).getRecommendationValues()).isEqualTo(Arrays.asList("recommendationValue0", "recommendationValue1"));
        assertThat(actualList.get(0).getProbableCauseValues()).isEqualTo(Arrays.asList("causeValue0", "causeValue1"));
        assertThat(actualList.get(0).getCreatedByName()).isEqualTo("First Last");
        assertThat(actualList.get(0).getObservationValue()).isEqualTo("observationValue0");
        assertThat(actualList.get(1).getId()).isEqualTo("124");
        assertThat(actualList.get(1).getAssetMappingId()).isEqualTo("2");


        verify(metadataItemService).getMetadataItems(Mockito.any());
        verify(insightRepository).saveAll(entityList);
        verify(userAccountService, times(2)).getUser("user0");

    }

    @Test
    void testInsight() {

        when(insightRepository.findById("123")).thenReturn(Optional.ofNullable(insightEntity));
        when(userAccountService.getUser("user0")).thenReturn(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("Last")
                .build());

        Insight actual = insightService.getInsight("123");

        assertThat(actual.getId()).isEqualTo("123");
        assertThat(actual.getObservation()).isEqualTo("observation0");
        assertThat(actual.getCreatedByName()).isEqualTo("First Last");

        verify(insightRepository).findById("123");
        verify(userAccountService).getUser("user0");

    }

    @Test
    void testGetInsights() {

        when(insightRepository.findByStoreNumberAndAssetMappingId(1L, "1"))
                .thenReturn(Collections.singletonList(insightEntity));

        List<Insight> actual = insightService.getInsights(1L, "1");

        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual.get(0).getId()).isEqualTo("123");
        assertThat(actual.get(0).getAssetMappingId()).isEqualTo("1");

        verify(insightRepository).findByStoreNumberAndAssetMappingId(1L, "1");

    }

}
