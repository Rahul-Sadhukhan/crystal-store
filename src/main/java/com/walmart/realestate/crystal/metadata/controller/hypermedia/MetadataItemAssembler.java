package com.walmart.realestate.crystal.metadata.controller.hypermedia;

import com.walmart.realestate.crystal.metadata.model.MetadataItem;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class MetadataItemAssembler implements SimpleRepresentationModelAssembler<MetadataItem> {

    @Override
    public void addLinks(EntityModel<MetadataItem> resource) {
        // metadataItems are collection
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<MetadataItem>> resources) {
        // link added in controller
    }
}
