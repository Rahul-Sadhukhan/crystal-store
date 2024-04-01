package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.model.StoreReviewStoreHealthScore;
import com.walmart.realestate.crystal.storereview.model.StoreReviewSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class StoreReviewSummaryService {

    private final StoreReviewHealthScoreService storeReviewHealthScoreService;

    private final StoreReviewService storeReviewService;

    @Logger
    public List<StoreReviewSummary> getStoreReviewSummary(Long storeNumber) {
        List<StoreReview> storeReviews = storeReviewService.getStoreReviewsByStoreNumber(storeNumber);

        return storeReviews.stream()
                .map(storeReview -> {
                    Optional<StoreReviewStoreHealthScore> healthScore = storeReviewHealthScoreService.getStoreReviewStoreHealthScore(storeReview.getId());
                    return StoreReviewSummary.builder()
                            .storeNumber(storeReview.getStoreNumber())
                            .storeReviewId(storeReview.getId())
                            .preReviewHealthScore(mapOrNull(healthScore, StoreReviewStoreHealthScore::getPreReviewScore))
                            .postReviewHealthScore(mapOrNull(healthScore, StoreReviewStoreHealthScore::getPostReviewScore))
                            .reviewStartDate(mapOrNull(healthScore, StoreReviewStoreHealthScore::getReviewStartTimestamp))
                            .reviewEndDate(mapOrNull(healthScore, StoreReviewStoreHealthScore::getReviewEndTimestamp))
                            .status(storeReview.getState())
                            .assignee(storeReview.getAssignee())
                            .assigneeName(storeReview.getAssigneeName())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static <T, U> U mapOrNull(Optional<T> optional, Function<T, U> mapper) {
        return optional.map(mapper)
                .orElse(null);
    }

}
