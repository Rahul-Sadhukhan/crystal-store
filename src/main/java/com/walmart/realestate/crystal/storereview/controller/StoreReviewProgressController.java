package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.storereview.controller.hypermedia.StoreReviewProgressAssembler;
import com.walmart.realestate.crystal.storereview.model.StoreReviewProgress;
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
@RequestMapping("/store-reviews/{storeReviewId}/progress")
public class StoreReviewProgressController {

    private final StoreAssetReviewService storeAssetReviewService;

    private final StoreReviewProgressAssembler storeReviewProgressAssembler;

    @GetMapping
    @PostAuthorize("hasPolicy(returnObject.getContent(),'viewStoreReview')")
    public EntityModel<StoreReviewProgress> getStoreReviewProgress(@PathVariable String storeReviewId) {
        return storeReviewProgressAssembler.toModel(storeAssetReviewService.getStoreReviewProgress(storeReviewId));
    }

}
