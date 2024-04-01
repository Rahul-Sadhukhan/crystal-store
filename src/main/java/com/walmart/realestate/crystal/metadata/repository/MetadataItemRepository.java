package com.walmart.realestate.crystal.metadata.repository;

import com.walmart.realestate.crystal.metadata.entity.MetadataItemEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MetadataItemRepository extends MongoRepository<MetadataItemEntity, String> {

    List<MetadataItemEntity> findByIsEnabledTrue();

    Optional<MetadataItemEntity> findByIdAndIsEnabledTrue(String id);

    List<MetadataItemEntity> findByIdInAndIsEnabledTrue(List<String> ids);

    List<MetadataItemEntity> findByTypeInAndIsEnabledTrue(List<String> types);

    List<MetadataItemEntity> findByAssetTypesInAndIsEnabledTrue(List<String> assetTypes);

    List<MetadataItemEntity> findByTypeInAndAssetTypesInAndIsEnabledTrue(List<String> types, List<String> assetTypes);

    List<MetadataItemEntity> findByTypeIn(List<String> types);

    List<MetadataItemEntity> findByAssetTypesIn(List<String> assetTypes);

    List<MetadataItemEntity> findByTypeInAndAssetTypesIn(List<String> types, List<String> assetTypes);
}
