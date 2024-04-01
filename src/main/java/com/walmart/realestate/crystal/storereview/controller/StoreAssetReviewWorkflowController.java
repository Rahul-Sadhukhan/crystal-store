package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.storereview.controller.hypermedia.StoreAssetReviewWorkflowAssembler;
import com.walmart.realestate.crystal.storereview.model.StoreAssetReview;
import com.walmart.realestate.crystal.storereview.model.Workflow;
import com.walmart.realestate.crystal.storereview.service.StoreAssetReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/store-asset-reviews")
public class StoreAssetReviewWorkflowController {

    private final StoreAssetReviewService storeAssetReviewService;

    private final StoreAssetReviewWorkflowAssembler storeAssetReviewWorkflowAssembler;

    @GetMapping("{storeAssetReviewId}/workflow")
    @PostAuthorize("hasPolicy(returnObject.getContent().getEntity(),'viewStoreReview')")
    public EntityModel<Workflow<StoreAssetReview>> getWorkflow(@PathVariable String storeAssetReviewId) {
        return storeAssetReviewWorkflowAssembler.toModel(storeAssetReviewService.getStoreAssetReviewWorkflow(storeAssetReviewId));
    }

}
