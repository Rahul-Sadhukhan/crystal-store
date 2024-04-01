package com.walmart.realestate.crystal.metadata.service;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.metadata.entity.MetadataTypeEntity;
import com.walmart.realestate.crystal.metadata.model.MetadataType;
import com.walmart.realestate.crystal.metadata.repository.MetadataTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MetadataTypeService {

    private final MetadataTypeRepository metadataTypeRepository;

    @Logger
    public MetadataType createMetadataType(MetadataType metadataType) {
        MetadataTypeEntity metadataTypeEntity = metadataTypeRepository.findById(metadataType.getId())
                .map(metadataTypeEntity0 -> {
                    metadataTypeEntity0.setName(metadataType.getName());
                    return metadataTypeEntity0;
                })
                .orElseGet(() -> MetadataTypeEntity.builder()
                        .id(metadataType.getId())
                        .name(metadataType.getName())
                        .build());

        return getMetadataTypeFromEntity(metadataTypeRepository.save(metadataTypeEntity));
    }

    @Logger
    public MetadataType getMetadataTypeById(String metadataType) {
        return metadataTypeRepository.findById(metadataType)
                .map(this::getMetadataTypeFromEntity)
                .orElse(null);
    }

    @Logger
    public List<MetadataType> getMetadataTypes() {
        return getMetadataTypeFromEntities(metadataTypeRepository.findAll());
    }

    private List<MetadataType> getMetadataTypeFromEntities(List<MetadataTypeEntity> metadataTypeEntities) {
        return metadataTypeEntities.stream()
                .map(this::getMetadataTypeFromEntity)
                .collect(Collectors.toList());
    }

    private MetadataType getMetadataTypeFromEntity(MetadataTypeEntity metadataTypeEntity) {
        return MetadataType.builder()
                .id(metadataTypeEntity.getId())
                .name(metadataTypeEntity.getName())
                .createdAt(metadataTypeEntity.getCreatedAt())
                .lastModifiedAt(metadataTypeEntity.getLastModifiedAt())
                .lastModifiedBy(metadataTypeEntity.getLastModifiedBy())
                .createdBy(metadataTypeEntity.getCreatedBy())
                .build();
    }

}
