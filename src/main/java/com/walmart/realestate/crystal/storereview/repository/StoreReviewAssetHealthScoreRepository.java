package com.walmart.realestate.crystal.storereview.repository;

import com.walmart.realestate.crystal.storereview.entity.StoreReviewAssetHealthScoreEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreReviewAssetHealthScoreRepository extends MongoRepository<StoreReviewAssetHealthScoreEntity, String> {

    List<StoreReviewAssetHealthScoreEntity> findByStoreReviewId(String storeReviewId);

    List<StoreReviewAssetHealthScoreEntity> findByStoreReviewIdIn(List<String> storeReviewIds);

    StoreReviewAssetHealthScoreEntity findTopByStoreReviewId(String id);
}
