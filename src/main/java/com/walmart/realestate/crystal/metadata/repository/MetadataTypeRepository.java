package com.walmart.realestate.crystal.metadata.repository;

import com.walmart.realestate.crystal.metadata.entity.MetadataTypeEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MetadataTypeRepository extends MongoRepository<MetadataTypeEntity, String> {
}
