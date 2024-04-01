package com.walmart.realestate.crystal.storereview.controller.hypermedia;

import com.walmart.realestate.crystal.storereview.model.AssetReviewSummary;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class AssetReviewSummaryAssembler implements SimpleRepresentationModelAssembler<AssetReviewSummary> {
    @Override
    public void addLinks(EntityModel<AssetReviewSummary> resource) {
        //AssetDetailsMaintenance is a collection
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<AssetReviewSummary>> resources) {
        //links to be added in controller
    }

}
