package com.walmart.realestate.crystal.metadata.controller;

import com.walmart.realestate.crystal.metadata.controller.hypermedia.MetadataTypeAssembler;
import com.walmart.realestate.crystal.metadata.model.MetadataType;
import com.walmart.realestate.crystal.metadata.service.MetadataTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/metadata-types")
public class MetadataTypeController {

    private final MetadataTypeService metadataTypeService;

    private final MetadataTypeAssembler metadataTypeAssembler;

    @PostMapping
    @PreAuthorize("hasPolicy(#metadataType, 'createStoreReview')")
    public EntityModel<MetadataType> createMetadataType(@RequestBody @Valid MetadataType metadataType) {
        return metadataTypeAssembler.toModel(metadataTypeService.createMetadataType(metadataType));
    }

    @GetMapping
    @PreAuthorize("hasPolicy(#metadataType, 'viewStoreReview')")
    public CollectionModel<EntityModel<MetadataType>> getMetadataTypes() {
        return metadataTypeAssembler.toCollectionModel(metadataTypeService.getMetadataTypes());
    }

}
