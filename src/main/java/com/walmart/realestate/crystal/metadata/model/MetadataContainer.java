package com.walmart.realestate.crystal.metadata.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;

@Data
@Builder
@Relation(itemRelation = "metadataContainer", collectionRelation = "metadataContainers")
public class MetadataContainer {

    private String metadataType;

    private String metadataTypeId;

    private List<MetadataItem> metaDataItemList;

}
