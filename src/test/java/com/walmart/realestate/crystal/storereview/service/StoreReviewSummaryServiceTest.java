package com.walmart.realestate.crystal.storereview.service;


import com.walmart.realestate.crystal.storereview.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StoreReviewSummaryService.class})
@ActiveProfiles("test")
class StoreReviewSummaryServiceTest {

    @Autowired
    private StoreReviewSummaryService storeReviewSummaryService;

    @MockBean
    private StoreReviewService storeReviewService;

    @MockBean
    private StoreReviewHealthScoreService storeReviewHealthScoreService;

    @Test
    void testGetStoreReviewSummary() {

        Instant storeReviewStartTimestampFirst = Instant.parse("2021-09-10T11:00:00Z");
        Instant storeReviewEndTimestampFirst = Instant.parse("2021-10-10T11:00:00Z");
        Instant storeReviewStartTimestampSecond = Instant.parse("2021-11-10T11:00:00Z");
        Instant storeReviewEndTimestampSecond = Instant.parse("2021-12-10T11:00:00Z");

        List<StoreReview> storeReviews = Arrays.asList(
                StoreReview.builder()
                        .storeNumber(1L)
                        .id("SR-1")
                        .state("completed")
                        .startedAt(storeReviewStartTimestampFirst)
                        .closedAt(storeReviewEndTimestampFirst)
                        .build(),
                StoreReview.builder()
                        .storeNumber(1L)
                        .id("SR-2")
                        .state("completed")
                        .startedAt(storeReviewStartTimestampSecond)
                        .closedAt(storeReviewEndTimestampSecond)
                        .build()
        );

        Optional<StoreReviewStoreHealthScore> storeReviewStoreHealthScoreOne = Optional.of(StoreReviewStoreHealthScore.builder()
                        .preReviewScore(10.5)
                        .postReviewScore(12.9)
                        .storeReviewId("SR-1")
                        .reviewStartTimestamp(storeReviewStartTimestampFirst)
                        .reviewEndTimestamp(storeReviewEndTimestampFirst)
                        .build());
        Optional<StoreReviewStoreHealthScore> storeReviewStoreHealthScoreTwo = Optional.of(StoreReviewStoreHealthScore.builder()
                        .preReviewScore(14.1)
                        .postReviewScore(15.0)
                        .storeReviewId("SR-2")
                        .reviewStartTimestamp(storeReviewStartTimestampSecond)
                        .reviewEndTimestamp(storeReviewEndTimestampSecond)
                        .build());

        when(storeReviewService.getStoreReviewsByStoreNumber(1L)).thenReturn(storeReviews);
        when(storeReviewHealthScoreService.getStoreReviewStoreHealthScore("SR-1")).thenReturn(storeReviewStoreHealthScoreOne);
        when(storeReviewHealthScoreService.getStoreReviewStoreHealthScore("SR-2")).thenReturn(storeReviewStoreHealthScoreTwo);

        List<StoreReviewSummary> storeReviewSummaryList = storeReviewSummaryService.getStoreReviewSummary(1L);

        assertThat(storeReviewSummaryList.size()).isEqualTo(2);
        assertThat(storeReviewSummaryList.get(0).getStoreReviewId()).isEqualTo("SR-1");
        assertThat(storeReviewSummaryList.get(0).getPostReviewHealthScore()).isEqualTo(12.9);
        assertThat(storeReviewSummaryList.get(0).getPreReviewHealthScore()).isEqualTo(10.5);
        assertThat(storeReviewSummaryList.get(0).getReviewStartDate()).isEqualTo(storeReviewStartTimestampFirst);
        assertThat(storeReviewSummaryList.get(0).getReviewEndDate()).isEqualTo(storeReviewEndTimestampFirst);
        assertThat(storeReviewSummaryList.get(1).getStoreReviewId()).isEqualTo("SR-2");
        assertThat(storeReviewSummaryList.get(1).getPostReviewHealthScore()).isEqualTo(15.0);
        assertThat(storeReviewSummaryList.get(1).getPreReviewHealthScore()).isEqualTo(14.1);
        assertThat(storeReviewSummaryList.get(1).getReviewStartDate()).isEqualTo(storeReviewStartTimestampSecond);
        assertThat(storeReviewSummaryList.get(1).getReviewEndDate()).isEqualTo(storeReviewEndTimestampSecond);

        verify(storeReviewService).getStoreReviewsByStoreNumber(1L);
        verify(storeReviewHealthScoreService).getStoreReviewStoreHealthScore("SR-1");
        verify(storeReviewHealthScoreService).getStoreReviewStoreHealthScore("SR-2");

    }

}
