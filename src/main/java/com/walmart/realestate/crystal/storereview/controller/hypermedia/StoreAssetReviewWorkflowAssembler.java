package com.walmart.realestate.crystal.storereview.controller.hypermedia;

import com.walmart.realestate.crystal.storereview.controller.StoreAssetReviewController;
import com.walmart.realestate.crystal.storereview.controller.StoreAssetReviewWorkflowController;
import com.walmart.realestate.crystal.storereview.model.StoreAssetReview;
import com.walmart.realestate.crystal.storereview.model.Workflow;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class StoreAssetReviewWorkflowAssembler implements SimpleRepresentationModelAssembler<Workflow<StoreAssetReview>> {

    @Override
    public void addLinks(EntityModel<Workflow<StoreAssetReview>> resource) {
        Workflow<StoreAssetReview> workflow = Objects.requireNonNull(resource.getContent());
        StoreAssetReview storeAssetReview = workflow.getEntity();

        resource.add(linkTo(methodOn(StoreAssetReviewWorkflowController.class)
                .getWorkflow(storeAssetReview.getId()))
                .withSelfRel());

        workflow.getTransitions()
                .forEach(transition -> resource.add(linkTo(methodOn(StoreAssetReviewController.class)
                        .updateAssetReviewStatus(storeAssetReview.getId(), transition.getAction(), null))
                        .withRel(transition.getAction())));
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<Workflow<StoreAssetReview>>> resources) {
        // workflow is singular
    }
}
