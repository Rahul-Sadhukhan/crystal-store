package com.walmart.realestate.crystal.storereview.controller.hypermedia;

import com.walmart.realestate.crystal.storereview.controller.StoreReviewController;
import com.walmart.realestate.crystal.storereview.controller.StoreReviewWorkflowController;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.model.Workflow;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class StoreReviewWorkflowAssembler implements SimpleRepresentationModelAssembler<Workflow<StoreReview>> {

    @Override
    public void addLinks(EntityModel<Workflow<StoreReview>> resource) {
        Workflow<StoreReview> workflow = Objects.requireNonNull(resource.getContent());
        StoreReview storeReview = workflow.getEntity();

        if (Objects.nonNull(storeReview)) {
            resource.add(linkTo(methodOn(StoreReviewWorkflowController.class)
                    .getWorkflow(storeReview.getId()))
                    .withSelfRel());

            workflow.getTransitions()
                    .forEach(transition -> resource.add(linkTo(methodOn(StoreReviewController.class)
                            .updateStatus(storeReview.getId(), transition.getAction(), null))
                            .withRel(transition.getAction())));
        } else {
            resource.add(linkTo(methodOn(StoreReviewWorkflowController.class)
                    .getWorkflow(workflow.getState(), workflow.getFlow()))
                    .withSelfRel());
        }
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<Workflow<StoreReview>>> resources) {
        // workflow is singular
    }
}
