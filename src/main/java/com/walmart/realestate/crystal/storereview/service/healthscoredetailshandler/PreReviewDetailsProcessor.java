package com.walmart.realestate.crystal.storereview.service.healthscoredetailshandler;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationAssetTimeInTarget;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewAssetHealthScoreEntity;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewStoreHealthScoreEntity;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.service.StoreAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PreReviewDetailsProcessor implements HealthScoreFetchHandler {

    private final StoreAssetService storeAssetService;

    @Override
    public Integer getPriority() {
        return 1;
    }

    @Logger
    @Override
    public void populate(StoreReview storeReview, ZoneId storeTimeZone, StoreReviewStoreHealthScoreEntity storeReviewStoreHealthScore, List<StoreReviewAssetHealthScoreEntity> storeReviewAssetHealthScores, boolean healthScoresOverride) {
        if (storeReviewStoreHealthScore.getPreReviewTimestamp() == null || healthScoresOverride) {
            populatePreReviewDetails(storeReview, storeTimeZone, storeReviewStoreHealthScore, storeReviewAssetHealthScores);
        }
    }

    @Logger
    @Override
    public void clear(StoreReviewStoreHealthScoreEntity storeReviewStoreHealthScore, List<StoreReviewAssetHealthScoreEntity> storeReviewAssetHealthScores) {
        // No-op! Pre-review timestamp does not change, should we clear it?
    }

    private void populatePreReviewDetails(StoreReview storeReview, ZoneId storeTimeZone, StoreReviewStoreHealthScoreEntity storeReviewStoreHealthScore, List<StoreReviewAssetHealthScoreEntity> storeReviewAssetHealthScores) {
        Instant preReviewStartAt = storeReview.getStartedAt()
                .atZone(storeTimeZone)
                .toLocalDate()
                .atStartOfDay()
                .atZone(storeTimeZone)
                .toInstant();

        Optional.ofNullable(storeAssetService.getStoreHealthScore(storeReview.getStoreNumber(), preReviewStartAt))
                .ifPresent(refrigerationStoreTimeInTarget -> {
                    storeReviewStoreHealthScore.setPreReviewTimestamp(preReviewStartAt);
                    storeReviewStoreHealthScore.setPreReviewScore(refrigerationStoreTimeInTarget.getTimeInTarget());
                    storeReviewStoreHealthScore.setPreReviewScoreTimestamp(refrigerationStoreTimeInTarget.getRunTime());

                    Map<String, RefrigerationAssetTimeInTarget> assetHealthScoresPreReviewMap = storeAssetService.getAssetHealthScore(storeReview.getStoreNumber(), preReviewStartAt).stream()
                            .collect(Collectors.toMap(RefrigerationAssetTimeInTarget::getAssetMappingId, Function.identity()));

                    storeReviewAssetHealthScores.forEach(storeReviewAssetHealthScore -> {
                        RefrigerationAssetTimeInTarget refrigerationAssetTimeInTarget = assetHealthScoresPreReviewMap.get(storeReviewAssetHealthScore.getAssetMappingId());
                        if (refrigerationAssetTimeInTarget != null) {
                            storeReviewAssetHealthScore.setPreReviewTimestamp(preReviewStartAt);
                            storeReviewAssetHealthScore.setPreReviewScore(refrigerationAssetTimeInTarget.getTimeInTarget());
                            storeReviewAssetHealthScore.setPreReviewScoreTimestamp(refrigerationAssetTimeInTarget.getRunTime());
                        }
                    });
                });
    }
}
