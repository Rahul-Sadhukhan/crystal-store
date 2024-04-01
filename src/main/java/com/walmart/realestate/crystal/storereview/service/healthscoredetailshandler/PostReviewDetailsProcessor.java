package com.walmart.realestate.crystal.storereview.service.healthscoredetailshandler;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationAssetTimeInTarget;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewAssetHealthScoreEntity;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewStoreHealthScoreEntity;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.properties.StoreReviewProperties;
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
public class PostReviewDetailsProcessor implements HealthScoreFetchHandler, InitializingBean {

    private final StoreAssetService storeAssetService;

    private final StoreReviewProperties storeReviewProperties;

    @Setter
    private Supplier<Instant> timeSupplier;

    @Override
    public void afterPropertiesSet() {
        timeSupplier = Instant::now;
    }

    @Override
    public Integer getPriority() {
        return 2;
    }

    @Logger
    @Override
    public void populate(StoreReview storeReview, ZoneId storeTimeZone, StoreReviewStoreHealthScoreEntity storeReviewStoreHealthScore, List<StoreReviewAssetHealthScoreEntity> storeReviewAssetHealthScores, boolean healthScoresOverride) {
        if (storeReviewStoreHealthScore.getPostReviewTimestamp() == null || healthScoresOverride) {
            populatePostReviewDetails(storeReview, storeTimeZone, storeReviewStoreHealthScore, storeReviewAssetHealthScores);
        }
    }

    @Logger
    @Override
    public void clear(StoreReviewStoreHealthScoreEntity storeReviewStoreHealthScore, List<StoreReviewAssetHealthScoreEntity> storeReviewAssetHealthScores) {
        storeReviewStoreHealthScore.setPostReviewTimestamp(null);
        storeReviewStoreHealthScore.setPostReviewScore(null);
        storeReviewStoreHealthScore.setPostReviewScoreTimestamp(null);

        storeReviewAssetHealthScores.forEach(storeReviewAssetHealthScore -> {
            storeReviewAssetHealthScore.setPostReviewTimestamp(null);
            storeReviewAssetHealthScore.setPostReviewScore(null);
            storeReviewAssetHealthScore.setPostReviewScoreTimestamp(null);
        });
    }

    private void populatePostReviewDetails(StoreReview storeReview, ZoneId storeTimeZone, StoreReviewStoreHealthScoreEntity storeReviewStoreHealthScore, List<StoreReviewAssetHealthScoreEntity> storeReviewAssetHealthScores) {
        int postReviewHours = storeReviewProperties.getReport().getPostReviewHours();
        Instant postReviewStartAt = storeReview.getMonitoringStartedAt()
                .atZone(storeTimeZone)
                .toLocalDateTime()
                .plusHours(postReviewHours)
                .toLocalDate()
                .plusDays(1)
                .atStartOfDay()
                .atZone(storeTimeZone)
                .toInstant();
        if (postReviewStartAt.isAfter(timeSupplier.get())) {
            return;
        }

        Optional.ofNullable(storeAssetService.getStoreHealthScore(storeReview.getStoreNumber(), postReviewStartAt))
                .ifPresent(refrigerationStoreTimeInTarget -> {
                    storeReviewStoreHealthScore.setPostReviewTimestamp(postReviewStartAt);
                    storeReviewStoreHealthScore.setPostReviewScore(refrigerationStoreTimeInTarget.getTimeInTarget());
                    storeReviewStoreHealthScore.setPostReviewScoreTimestamp(refrigerationStoreTimeInTarget.getRunTime());

                    Map<String, RefrigerationAssetTimeInTarget> assetHealthScoresPostReviewMap = storeAssetService.getAssetHealthScore(storeReview.getStoreNumber(), postReviewStartAt).stream()
                            .collect(Collectors.toMap(RefrigerationAssetTimeInTarget::getAssetMappingId, Function.identity()));

                    storeReviewAssetHealthScores.forEach(storeReviewAssetHealthScore -> {
                        RefrigerationAssetTimeInTarget refrigerationAssetTimeInTarget = assetHealthScoresPostReviewMap.get(storeReviewAssetHealthScore.getAssetMappingId());
                        if (refrigerationAssetTimeInTarget != null) {
                            storeReviewAssetHealthScore.setPostReviewTimestamp(postReviewStartAt);
                            storeReviewAssetHealthScore.setPostReviewScore(refrigerationAssetTimeInTarget.getTimeInTarget());
                            storeReviewAssetHealthScore.setPostReviewScoreTimestamp(refrigerationAssetTimeInTarget.getRunTime());
                        }
                    });
                });
    }

}
