package com.walmart.realestate.crystal.storereview.controller.hypermedia;

import com.walmart.realestate.crystal.storereview.controller.*;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class StoreReviewAssembler extends PagedModelAssembler<StoreReview> {

    public StoreReviewAssembler(PagedResourcesAssembler<StoreReview> pagedResourcesAssembler) {
        super(pagedResourcesAssembler);
    }

    @Override
    public void addLinks(EntityModel<StoreReview> resource) {
        StoreReview storeReview = Objects.requireNonNull(resource.getContent());

        resource.add(linkTo(methodOn(StoreReviewController.class)
                .getStoreReview(storeReview.getId()))
                .withSelfRel());

        resource.add(linkTo(methodOn(StoreReviewWorkflowController.class)
                .getWorkflow(storeReview.getId()))
                .withRel("workflow"));

        resource.add(linkTo(methodOn(StoreReviewProgressController.class)
                .getStoreReviewProgress(storeReview.getId()))
                .withRel("progress"));

        resource.add(linkTo(methodOn(StoreReviewTimelineController.class)
                .getStoreReviewTimeline(storeReview.getId()))
                .withRel("timeline"));

        resource.add(linkTo(methodOn(StoreAssetReviewController.class)
                .getStoreAssetReviews(storeReview.getId()))
                .withRel("store-asset-reviews"));

        resource.add(linkTo(methodOn(StoreController.class)
                .getStoreInfo(storeReview.getStoreNumber()))
                .withRel("store-info"));

        resource.add(linkTo(methodOn(StoreAssetController.class)
                .getAssetHealthScore(storeReview.getStoreNumber(), null))
                .withRel("asset-health-scores"));
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<StoreReview>> resources) {
        List<Long> storeNumbers = resources.getContent().stream()
                .map(EntityModel::getContent)
                .filter(Objects::nonNull)
                .map(StoreReview::getStoreNumber)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));

        resources.add(linkTo(methodOn(AssetController.class)
                .getStoreDataQuality(storeNumbers))
                .withRel("store-data-quality-scores"));

        resources.add(linkTo(methodOn(StoreAssetController.class)
                .getStoreHealthScore(storeNumbers))
                .withRel("store-health-scores"));
    }

}
