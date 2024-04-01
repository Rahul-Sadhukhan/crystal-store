package com.walmart.realestate.crystal.settingchangelog.repository;

import com.walmart.realestate.crystal.settingchangelog.entity.InsightEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InsightRepository extends MongoRepository<InsightEntity, String> {

    List<InsightEntity> findByStoreNumber(Long storeNumber);

    List<InsightEntity> findByAssetMappingId(String assetId);

    List<InsightEntity> findByStoreNumberAndAssetMappingId(Long storeNumber, String assetId);

    List<InsightEntity> findByReferenceId(String referenceId);


}
