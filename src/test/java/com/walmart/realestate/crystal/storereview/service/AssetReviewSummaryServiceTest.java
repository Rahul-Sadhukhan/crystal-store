package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.storereview.model.AssetReviewSummary;
import com.walmart.realestate.crystal.storereview.model.StoreAssetReview;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.model.StoreReviewAssetHealthScore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AssetReviewSummaryService.class})
@ActiveProfiles("test")
class AssetReviewSummaryServiceTest {

    @Autowired
    private AssetReviewSummaryService assetReviewSummaryService;

    @MockBean
    private StoreAssetReviewService storeAssetReviewService;

    @MockBean
    private StoreReviewHealthScoreService storeReviewHealthScoreService;

    @MockBean
    private StoreReviewService storeReviewService;

    @Test
    void testGetAssetReviewSummary() {
        Instant storeReviewStartTimestampFirst = Instant.parse("2021-09-10T11:00:00Z");
        Instant storeReviewEndTimestampFirst = Instant.parse("2021-10-10T11:00:00Z");
        Instant storeReviewStartTimestampSecond = Instant.parse("2021-11-10T11:00:00Z");
        Instant storeReviewEndTimestampSecond = Instant.parse("2021-12-10T11:00:00Z");

        List<StoreAssetReview> storeAssetReviews = Arrays.asList(
                StoreAssetReview.builder()
                        .assetId(1234L)
                        .assetMappingId("1234")
                        .storeReviewId("SR-1")
                        .workOrderId("1234")
                        .build(),
                StoreAssetReview.builder()
                        .assetId(1234L)
                        .assetMappingId("1234")
                        .storeReviewId("SR-2")
                        .workOrderId("2345")
                        .build());
        when(storeAssetReviewService.getStoreAssetReviews(1234L)).thenReturn(storeAssetReviews);

        List<StoreReview> storeReviews = Arrays.asList(
                StoreReview.builder()
                        .id("SR-1")
                        .state("completed")
                        .startedAt(storeReviewStartTimestampFirst)
                        .monitoringStartedAt(storeReviewEndTimestampFirst)
                        .build(),
                StoreReview.builder()
                        .id("SR-2")
                        .state("completed")
                        .startedAt(storeReviewStartTimestampSecond)
                        .monitoringStartedAt(storeReviewEndTimestampSecond)
                        .build());
        when(storeReviewService.getStoreReviews(List.of("SR-1", "SR-2"))).thenReturn(storeReviews);

        List<StoreReviewAssetHealthScore> storeReviewAssetHealthScoreList = Arrays.asList(
                StoreReviewAssetHealthScore.builder()
                        .assetMappingId("1234")
                        .postReviewScore(12.9)
                        .preReviewScore(10.5)
                        .storeReviewId("SR-1")
                        .reviewStartTimestamp(storeReviewStartTimestampFirst)
                        .reviewEndTimestamp(storeReviewEndTimestampFirst)
                        .build(),
                StoreReviewAssetHealthScore.builder()
                        .assetMappingId("1234")
                        .postReviewScore(15.0)
                        .preReviewScore(14.1)
                        .storeReviewId("SR-2")
                        .reviewStartTimestamp(storeReviewStartTimestampSecond)
                        .reviewEndTimestamp(storeReviewEndTimestampSecond)
                        .build(),
                StoreReviewAssetHealthScore.builder()
                        .assetMappingId("5678")
                        .postReviewScore(15.0)
                        .preReviewScore(14.1)
                        .storeReviewId("SR-2")
                        .reviewStartTimestamp(storeReviewStartTimestampSecond)
                        .reviewEndTimestamp(storeReviewEndTimestampSecond)
                        .build());
        when(storeReviewHealthScoreService.getStoreReviewAssetHealthScores(List.of("SR-1", "SR-2"))).thenReturn(storeReviewAssetHealthScoreList);

        List<AssetReviewSummary> assetReviewSummaryList = assetReviewSummaryService.getAssetReviewSummary(1234L);

        assertThat(assetReviewSummaryList.size()).isEqualTo(2);

        assertThat(assetReviewSummaryList.get(0).getAssetMappingId()).isEqualTo("1234");
        assertThat(assetReviewSummaryList.get(0).getStoreReviewId()).isEqualTo("SR-1");
        assertThat(assetReviewSummaryList.get(0).getPostReviewHealthScore()).isEqualTo(12.9);
        assertThat(assetReviewSummaryList.get(0).getPreReviewHealthScore()).isEqualTo(10.5);
        assertThat(assetReviewSummaryList.get(0).getWorkOrder()).isEqualTo("1234");
        assertThat(assetReviewSummaryList.get(0).getReviewStartDate()).isEqualTo(storeReviewStartTimestampFirst);
        assertThat(assetReviewSummaryList.get(0).getReviewEndDate()).isEqualTo(storeReviewEndTimestampFirst);

        assertThat(assetReviewSummaryList.get(1).getAssetMappingId()).isEqualTo("1234");
        assertThat(assetReviewSummaryList.get(1).getStoreReviewId()).isEqualTo("SR-2");
        assertThat(assetReviewSummaryList.get(1).getPostReviewHealthScore()).isEqualTo(15.0);
        assertThat(assetReviewSummaryList.get(1).getPreReviewHealthScore()).isEqualTo(14.1);
        assertThat(assetReviewSummaryList.get(1).getWorkOrder()).isEqualTo("2345");
        assertThat(assetReviewSummaryList.get(1).getReviewStartDate()).isEqualTo(storeReviewStartTimestampSecond);
        assertThat(assetReviewSummaryList.get(1).getReviewEndDate()).isEqualTo(storeReviewEndTimestampSecond);

        verify(storeAssetReviewService).getStoreAssetReviews(1234L);
        verify(storeReviewService).getStoreReviews(List.of("SR-1", "SR-2"));
        verify(storeReviewHealthScoreService).getStoreReviewAssetHealthScores(List.of("SR-1", "SR-2"));
    }

}
