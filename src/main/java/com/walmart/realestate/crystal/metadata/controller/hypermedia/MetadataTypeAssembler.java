package com.walmart.realestate.crystal.metadata.controller.hypermedia;

import com.walmart.realestate.crystal.metadata.model.MetadataType;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class MetadataTypeAssembler implements SimpleRepresentationModelAssembler<MetadataType> {

    @Override
    public void addLinks(EntityModel<MetadataType> resource) {
        // no links for type to be added
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<MetadataType>> resources) {
        // no links for type to be added
    }
}
