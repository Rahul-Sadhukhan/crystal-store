package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.model.AssetReviewSummary;
import com.walmart.realestate.crystal.storereview.model.StoreAssetReview;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.model.StoreReviewAssetHealthScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AssetReviewSummaryService {

    private final StoreAssetReviewService storeAssetReviewService;

    private final StoreReviewHealthScoreService storeReviewHealthScoreService;

    private final StoreReviewService storeReviewService;

    @Logger
    public List<AssetReviewSummary> getAssetReviewSummary(Long assetId) {
        List<StoreAssetReview> storeAssetReviews = storeAssetReviewService.getStoreAssetReviews(assetId);

        List<String> storeReviewsIds = storeAssetReviews.stream()
                .map(StoreAssetReview::getStoreReviewId)
                .collect(Collectors.toList());

        Map<String, StoreReview> storeReviewMap = storeReviewService.getStoreReviews(storeReviewsIds).stream()
                .collect(Collectors.toMap(StoreReview::getId, Function.identity()));

        Map<String, List<StoreReviewAssetHealthScore>> assetHealthScoreMap = storeReviewHealthScoreService.getStoreReviewAssetHealthScores(storeReviewsIds).stream()
                .collect(Collectors.groupingBy(StoreReviewAssetHealthScore::getStoreReviewId));

        return storeAssetReviews.stream()
                .map(storeAssetReview -> {
                    Optional<StoreReviewAssetHealthScore> assetHealthScore = Optional.ofNullable(assetHealthScoreMap.get(storeAssetReview.getStoreReviewId()))
                            .orElseGet(Collections::emptyList).stream()
                            .filter(healthScore -> storeAssetReview.getAssetMappingId().equals(healthScore.getAssetMappingId()))
                            .findFirst();
                    return AssetReviewSummary.builder()
                            .assetMappingId(storeAssetReview.getAssetMappingId())
                            .storeReviewId(storeAssetReview.getStoreReviewId())
                            .preReviewHealthScore(assetHealthScore.map(StoreReviewAssetHealthScore::getPreReviewScore).orElse(null))
                            .postReviewHealthScore(assetHealthScore.map(StoreReviewAssetHealthScore::getPostReviewScore).orElse(null))
                            .reviewStartDate(storeReviewMap.get(storeAssetReview.getStoreReviewId()).getStartedAt())
                            .reviewEndDate(storeReviewMap.get(storeAssetReview.getStoreReviewId()).getMonitoringStartedAt())
                            .workOrder(storeAssetReview.getWorkOrderId())
                            .status(storeReviewMap.get(storeAssetReview.getStoreReviewId()).getState())
                            .build();
                })
                .collect(Collectors.toList());
    }

}
