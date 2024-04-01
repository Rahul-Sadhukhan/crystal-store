package com.walmart.realestate.crystal.metadata.controller;

import com.walmart.realestate.crystal.metadata.controller.hypermedia.MetadataItemAssembler;
import com.walmart.realestate.crystal.metadata.model.MetadataItem;
import com.walmart.realestate.crystal.metadata.service.MetadataItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RequiredArgsConstructor
@RestController
@RequestMapping("/metadata-items")
public class MetadataItemController {

    private final MetadataItemService metadataItemService;

    private final MetadataItemAssembler metadataItemAssembler;

    @PostMapping
    @PreAuthorize("hasPolicy(#metadataItem, 'createStoreReview')")
    public EntityModel<MetadataItem> createMetadataItem(@RequestBody @Valid MetadataItem metadataItem) {
        return metadataItemAssembler.toModel(metadataItemService.createMetadataItem(metadataItem));
    }

    @GetMapping
    public CollectionModel<EntityModel<MetadataItem>> getMetadataItems(@RequestParam(required = false) List<String> types,
                                                                       @RequestParam(required = false) List<String> assetTypes,
                                                                       @RequestParam(required = false, defaultValue = "false") Boolean includeDisabled) {
        return metadataItemAssembler.toCollectionModel(metadataItemService.getMetadataItems(types, assetTypes, includeDisabled))
                .add(linkTo(methodOn(MetadataItemController.class)
                        .getMetadataItems(types, assetTypes, includeDisabled))
                        .withSelfRel());
    }

}
