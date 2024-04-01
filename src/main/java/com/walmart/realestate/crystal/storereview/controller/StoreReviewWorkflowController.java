package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.storereview.controller.hypermedia.StoreReviewWorkflowAssembler;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.model.Workflow;
import com.walmart.realestate.crystal.storereview.service.StoreReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/store-reviews")
public class StoreReviewWorkflowController {

    private final StoreReviewService storeReviewService;

    private final StoreReviewWorkflowAssembler storeReviewWorkflowAssembler;

    @GetMapping("workflow")
    @Deprecated
    @PostAuthorize("hasPolicy(returnObject.getContent().getEntity(),'viewStoreReview')")
    public EntityModel<Workflow<StoreReview>> getWorkflow(@RequestParam String state, @RequestParam String flow) {
        return storeReviewWorkflowAssembler.toModel(storeReviewService.getStoreReviewWorkflow(state, flow));
    }

    @GetMapping("{storeReviewId}/workflow")
    @PostAuthorize("hasPolicy(returnObject.getContent().getEntity(),'viewStoreReview')")
    public EntityModel<Workflow<StoreReview>> getWorkflow(@PathVariable String storeReviewId) {
        return storeReviewWorkflowAssembler.toModel(storeReviewService.getStoreReviewWorkflow(storeReviewId));
    }

}
