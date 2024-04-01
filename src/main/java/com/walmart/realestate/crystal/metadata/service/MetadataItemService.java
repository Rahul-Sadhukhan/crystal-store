package com.walmart.realestate.crystal.metadata.service;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.metadata.entity.MetadataItemEntity;
import com.walmart.realestate.crystal.metadata.exception.MetadataItemInvalidException;
import com.walmart.realestate.crystal.metadata.exception.MetadataTypeInvalidException;
import com.walmart.realestate.crystal.metadata.model.MetadataItem;
import com.walmart.realestate.crystal.metadata.model.MetadataType;
import com.walmart.realestate.crystal.metadata.properties.MetadataProperties;
import com.walmart.realestate.crystal.metadata.repository.MetadataItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MetadataItemService {

    private final MetadataItemRepository metadataItemRepository;

    private final MetadataTypeService metadataTypeService;

    private final MetadataProperties metadataProperties;

    @Logger
    public MetadataItem createMetadataItem(MetadataItem metadataItem) {
        MetadataType metadataType = metadataTypeService.getMetadataTypeById(metadataItem.getMetadataType());
        if (Objects.isNull(metadataType)) {
            throw new MetadataTypeInvalidException();
        }

        MetadataItemEntity metadataItemEntity = metadataItemRepository.findById(metadataItem.getId())
                .map(metadataItemEntity0 -> {
                    metadataItemEntity0.setAssetTypes(metadataItem.getAssetTypes());
                    metadataItemEntity0.setDefaultValue(metadataItem.getDefaultValue());
                    metadataItemEntity0.setIndex(metadataItem.getIndex());
                    metadataItemEntity0.setIsEnabled(metadataItem.getIsEnabled());
                    metadataItemEntity0.setValues(metadataItem.getValues());
                    metadataItemEntity0.setUnit(metadataItem.getUnit());
                    metadataItemEntity0.setMaxValue(metadataItem.getMaxValue());
                    metadataItemEntity0.setMinValue(metadataItem.getMinValue());
                    metadataItemEntity0.setOffsetValue(metadataItem.getOffsetValue());
                    return metadataItemEntity0;
                })
                .orElseGet(() -> MetadataItemEntity.builder()
                        .id(metadataItem.getId().toLowerCase())
                        .index(metadataItem.getIndex())
                        .assetTypes(metadataItem.getAssetTypes())
                        .offsetValue(metadataItem.getOffsetValue())
                        .defaultValue(metadataItem.getDefaultValue())
                        .isEnabled(metadataItem.getIsEnabled())
                        .type(metadataType.getId())
                        .unit(metadataItem.getUnit())
                        .maxValue(metadataItem.getMaxValue())
                        .minValue(metadataItem.getMinValue())
                        .values(metadataItem.getValues()).build());

        return getMetadataItemFromEntity(metadataItemRepository.save(metadataItemEntity));
    }

    @Logger
    @PostFilter("hasPolicy(filterObject, 'viewStoreReview')")
    public List<MetadataItem> getMetadataItems(List<String> type, List<String> assetTypes, Boolean includeDisabled) {
        List<String> assetTypesMapped = Optional.ofNullable(assetTypes)
                .orElseGet(Collections::emptyList).stream()
                .map(assetType -> metadataProperties.getAssetTypes().getOrDefault(assetType, assetType))
                .distinct()
                .collect(Collectors.toList());

        return getMetadataItemEntities(type, assetTypesMapped, includeDisabled).stream()
                .map(this::getMetadataItemFromEntity)
                .collect(Collectors.toList());
    }

    @Logger
    public MetadataItem getMetadataItem(String id) {
        return metadataItemRepository.findByIdAndIsEnabledTrue(id)
                .map(this::getMetadataItemFromEntity)
                .orElseThrow(() -> new MetadataItemInvalidException("MetadataItem does not exist!"));
    }

    @Logger
    public List<MetadataItem> getMetadataItems(List<String> ids) {
        return metadataItemRepository.findByIdInAndIsEnabledTrue(ids)
                .stream()
                .map(this::getMetadataItemFromEntity)
                .collect(Collectors.toList());
    }

    private MetadataItem getMetadataItemFromEntity(MetadataItemEntity metadataItemEntity) {

        return MetadataItem.builder()
                .createdAt(metadataItemEntity.getCreatedAt())
                .id(metadataItemEntity.getId())
                .assetTypes(metadataItemEntity.getAssetTypes())
                .index(metadataItemEntity.getIndex())
                .defaultValue(metadataItemEntity.getDefaultValue())
                .isEnabled(metadataItemEntity.getIsEnabled())
                .metadataType(metadataItemEntity.getType())
                .unit(metadataItemEntity.getUnit())
                .offsetValue(metadataItemEntity.getOffsetValue())
                .maxValue(metadataItemEntity.getMaxValue())
                .minValue(metadataItemEntity.getMinValue())
                .createdBy(metadataItemEntity.getCreatedBy())
                .lastModifiedAt(metadataItemEntity.getLastModifiedAt())
                .lastModifiedBy(metadataItemEntity.getLastModifiedBy())
                .lastModifiedAt(metadataItemEntity.getLastModifiedAt())
                .values(metadataItemEntity.getValues())
                .build();
    }

    private List<MetadataItemEntity> getMetadataItemEntities(List<String> types, List<String> assetTypes, boolean includeDisabled) {
        if (!includeDisabled) {
            if (CollectionUtils.isEmpty(types)) {
                if (CollectionUtils.isEmpty(assetTypes)) return metadataItemRepository.findByIsEnabledTrue();
                else return metadataItemRepository.findByAssetTypesInAndIsEnabledTrue(assetTypes);
            } else {
                if (CollectionUtils.isEmpty(assetTypes))
                    return metadataItemRepository.findByTypeInAndIsEnabledTrue(types);
                else return metadataItemRepository.findByTypeInAndAssetTypesInAndIsEnabledTrue(types, assetTypes);
            }
        } else {
            if (CollectionUtils.isEmpty(types)) {
                if (CollectionUtils.isEmpty(assetTypes)) return metadataItemRepository.findAll();
                else return metadataItemRepository.findByAssetTypesIn(assetTypes);
            } else {
                if (CollectionUtils.isEmpty(assetTypes)) return metadataItemRepository.findByTypeIn(types);
                else return metadataItemRepository.findByTypeInAndAssetTypesIn(types, assetTypes);
            }
        }
    }

}
