package com.walmart.realestate.crystal.storereview.controller.hypermedia;

import com.walmart.realestate.crystal.storereview.controller.StoreAssetReviewController;
import com.walmart.realestate.crystal.storereview.controller.StoreAssetReviewWorkflowController;
import com.walmart.realestate.crystal.storereview.controller.StoreReviewController;
import com.walmart.realestate.crystal.storereview.model.StoreAssetReview;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class StoreAssetReviewAssembler implements SimpleRepresentationModelAssembler<StoreAssetReview> {

    @Override
    public void addLinks(EntityModel<StoreAssetReview> resource) {
        StoreAssetReview storeAssetReview = Objects.requireNonNull(resource.getContent());

        resource.add(linkTo(methodOn(StoreAssetReviewController.class)
                .getStoreAssetReview(storeAssetReview.getId()))
                .withSelfRel());

        resource.add(linkTo(methodOn(StoreAssetReviewWorkflowController.class)
                .getWorkflow(storeAssetReview.getId()))
                .withRel("workflow"));

        resource.add(linkTo(methodOn(StoreReviewController.class)
                .getStoreReview(storeAssetReview.getStoreReviewId()))
                .withRel("store-review"));
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<StoreAssetReview>> resources) {
        StreamSupport.stream(resources.spliterator(), false)
                .findFirst()
                .map(EntityModel::getContent)
                .ifPresent(storeAssetReview -> resources.add(linkTo(methodOn(StoreAssetReviewController.class)
                        .getStoreAssetReviews(storeAssetReview.getStoreReviewId()))
                        .withSelfRel()));
    }

}
