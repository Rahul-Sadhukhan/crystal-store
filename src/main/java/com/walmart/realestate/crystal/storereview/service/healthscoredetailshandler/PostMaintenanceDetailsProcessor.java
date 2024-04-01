package com.walmart.realestate.crystal.storereview.service.healthscoredetailshandler;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationAssetTimeInTarget;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewAssetHealthScoreEntity;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewStoreHealthScoreEntity;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.service.StoreAssetService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostMaintenanceDetailsProcessor implements HealthScoreFetchHandler, InitializingBean {

    private final StoreAssetService storeAssetService;

    @Setter
    private Supplier<Instant> timeSupplier;

    @Override
    public void afterPropertiesSet() {
        timeSupplier = Instant::now;
    }

    @Override
    public Integer getPriority() {
        return 3;
    }

    @Logger
    @Override
    public void populate(StoreReview storeReview, ZoneId storeTimeZone, StoreReviewStoreHealthScoreEntity storeReviewStoreHealthScore, List<StoreReviewAssetHealthScoreEntity> storeReviewAssetHealthScores, boolean healthScoresOverride) {
        if (storeReviewStoreHealthScore.getPostMaintenanceTimestamp() == null || healthScoresOverride) {
            populatePostMaintenanceDetails(storeReview, storeTimeZone, storeReviewStoreHealthScore, storeReviewAssetHealthScores);
        }
    }

    @Logger
    @Override
    public void clear(StoreReviewStoreHealthScoreEntity storeReviewStoreHealthScore, List<StoreReviewAssetHealthScoreEntity> storeReviewAssetHealthScores) {
        storeReviewStoreHealthScore.setPostMaintenanceTimestamp(null);
        storeReviewStoreHealthScore.setPostMaintenanceScore(null);
        storeReviewStoreHealthScore.setPostMaintenanceScoreTimestamp(null);

        storeReviewAssetHealthScores.forEach(storeReviewAssetHealthScore -> {
            storeReviewAssetHealthScore.setPostMaintenanceTimestamp(null);
            storeReviewAssetHealthScore.setPostMaintenanceScore(null);
            storeReviewAssetHealthScore.setPostMaintenanceScoreTimestamp(null);
        });
    }

    private void populatePostMaintenanceDetails(StoreReview storeReview, ZoneId storeTimeZone, StoreReviewStoreHealthScoreEntity storeReviewStoreHealthScore, List<StoreReviewAssetHealthScoreEntity> storeReviewAssetHealthScores) {
        if (storeReview.getValidationStartedAt() == null || storeReview.getValidationStartedAt().isBefore(storeReview.getMonitoringStartedAt())) {
            return;
        }

        Instant postMaintenanceStartAt = storeReview.getValidationStartedAt()
                .atZone(storeTimeZone)
                .toLocalDate()
                .plusDays(1)
                .atStartOfDay()
                .atZone(storeTimeZone)
                .toInstant();
        if (postMaintenanceStartAt.isAfter(timeSupplier.get())) {
            return;
        }

        Optional.ofNullable(storeAssetService.getStoreHealthScore(storeReview.getStoreNumber(), postMaintenanceStartAt))
                .ifPresent(refrigerationStoreTimeInTarget -> {
                    storeReviewStoreHealthScore.setPostMaintenanceTimestamp(postMaintenanceStartAt);
                    storeReviewStoreHealthScore.setPostMaintenanceScore(refrigerationStoreTimeInTarget.getTimeInTarget());
                    storeReviewStoreHealthScore.setPostMaintenanceScoreTimestamp(refrigerationStoreTimeInTarget.getRunTime());

                    Map<String, RefrigerationAssetTimeInTarget> assetHealthScoresPostMaintenanceMap = storeAssetService.getAssetHealthScore(storeReview.getStoreNumber(), postMaintenanceStartAt).stream()
                            .collect(Collectors.toMap(RefrigerationAssetTimeInTarget::getAssetMappingId, Function.identity()));

                    storeReviewAssetHealthScores.forEach(storeReviewAssetHealthScore -> {
                        RefrigerationAssetTimeInTarget refrigerationAssetTimeInTarget = assetHealthScoresPostMaintenanceMap.get(storeReviewAssetHealthScore.getAssetMappingId());
                        if (refrigerationAssetTimeInTarget != null) {
                            storeReviewAssetHealthScore.setPostMaintenanceTimestamp(postMaintenanceStartAt);
                            storeReviewAssetHealthScore.setPostMaintenanceScore(refrigerationAssetTimeInTarget.getTimeInTarget());
                            storeReviewAssetHealthScore.setPostMaintenanceScoreTimestamp(refrigerationAssetTimeInTarget.getRunTime());
                        }
                    });
                });
    }

}
