package com.walmart.realestate.crystal.settingchangelog.repository;

import com.walmart.realestate.crystal.settingchangelog.entity.SettingChangeLogEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SettingChangeLogRepository extends MongoRepository<SettingChangeLogEntity, String> {

    List<SettingChangeLogEntity> findByStoreNumber(Long storeNumber);

    List<SettingChangeLogEntity> findByAssetMappingId(String assetId);

    List<SettingChangeLogEntity> findByStoreNumberAndAssetMappingId(Long storeNumber, String assetId);

    List<SettingChangeLogEntity> findByReferenceId(String storeReviewId);
}
