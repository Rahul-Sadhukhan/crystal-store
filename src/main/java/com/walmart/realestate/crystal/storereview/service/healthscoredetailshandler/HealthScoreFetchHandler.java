package com.walmart.realestate.crystal.storereview.service.healthscoredetailshandler;

import com.walmart.realestate.crystal.storereview.entity.StoreReviewAssetHealthScoreEntity;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewStoreHealthScoreEntity;
import com.walmart.realestate.crystal.storereview.model.StoreReview;

import java.time.ZoneId;
import java.util.List;

public interface HealthScoreFetchHandler {

    Integer getPriority();

    void populate(StoreReview storeReview,
                  ZoneId storeTimeZone,
                  StoreReviewStoreHealthScoreEntity storeReviewStoreHealthScoreEntity,
                  List<StoreReviewAssetHealthScoreEntity> storeReviewAssetHealthScoreEntities,
                  boolean healthScoresOverride);

    void clear(StoreReviewStoreHealthScoreEntity storeReviewStoreHealthScoreEntity,
               List<StoreReviewAssetHealthScoreEntity> storeReviewAssetHealthScoreEntities);

}
