package com.walmart.realestate.crystal.storereview.controller.hypermedia;

import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.model.Timeline;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class StoreReviewTimelineAssembler implements SimpleRepresentationModelAssembler<Timeline<StoreReview>> {

    @Override
    public void addLinks(EntityModel<Timeline<StoreReview>> resource) {
        // links added in controller
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<Timeline<StoreReview>>> resources) {
        // timeline is singular only
    }

}
