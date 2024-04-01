package com.walmart.realestate.crystal.storereview.controller.hypermedia;

import com.walmart.realestate.crystal.storereview.model.StoreReviewSummary;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class StoreReviewSummaryAssembler implements SimpleRepresentationModelAssembler<StoreReviewSummary> {
    @Override
    public void addLinks(EntityModel<StoreReviewSummary> resource) {
        //AssetDetailsMaintenance is a collection
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<StoreReviewSummary>> resources) {
        //links to be added in controller
    }

}
