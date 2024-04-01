package com.walmart.realestate.crystal.storereview.repository;

import com.walmart.realestate.crystal.storereview.entity.StoreAssetReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface StoreAssetReviewRepository extends JpaRepository<StoreAssetReviewEntity, String> {

    List<StoreAssetReviewEntity> findByStoreReviewId(String storeReviewId);

    Integer countByStoreReviewId(String storeReviewId);

    Integer countByStoreReviewIdAndState(String storeReviewId, String state);

    List<StoreAssetReviewEntity> findByAssetId(Long assetId);

    StoreAssetReviewEntity findByAssetMappingIdAndStoreReviewId(String assetMappingId, String storeReviewId);

    List<StoreAssetReviewEntity> findByAssetMappingIdInAndStoreReviewId(List<String> assetMappingId, String storeReviewId);
}
