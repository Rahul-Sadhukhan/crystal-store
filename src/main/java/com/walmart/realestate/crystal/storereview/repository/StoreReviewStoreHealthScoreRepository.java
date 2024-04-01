package com.walmart.realestate.crystal.storereview.repository;

import com.walmart.realestate.crystal.storereview.entity.StoreReviewStoreHealthScoreEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreReviewStoreHealthScoreRepository extends MongoRepository<StoreReviewStoreHealthScoreEntity, String> {

    Optional<StoreReviewStoreHealthScoreEntity> findByStoreReviewId(String storeReviewId);

}
