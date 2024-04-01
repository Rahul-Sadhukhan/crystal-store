package com.walmart.realestate.crystal.storereview.controller.hypermedia;

import com.walmart.realestate.crystal.storereview.controller.StoreReviewProgressController;
import com.walmart.realestate.crystal.storereview.model.StoreReviewProgress;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class StoreReviewProgressAssembler implements SimpleRepresentationModelAssembler<StoreReviewProgress> {

    @Override
    public void addLinks(EntityModel<StoreReviewProgress> resource) {
        StoreReviewProgress storeReviewProgress = Objects.requireNonNull(resource.getContent());

        resource.add(linkTo(methodOn(StoreReviewProgressController.class)
                .getStoreReviewProgress(storeReviewProgress.getStoreReviewId()))
                .withSelfRel());
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<StoreReviewProgress>> resources) {
        // progress is singular
    }

}
