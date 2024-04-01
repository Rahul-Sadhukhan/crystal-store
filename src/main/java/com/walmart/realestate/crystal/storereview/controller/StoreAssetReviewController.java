package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.storereview.command.StoreAssetReviewCommand;
import com.walmart.realestate.crystal.storereview.controller.hypermedia.StoreAssetReviewAssembler;
import com.walmart.realestate.crystal.storereview.model.AggregatedStoreAssetReview;
import com.walmart.realestate.crystal.storereview.model.StoreAssetReview;
import com.walmart.realestate.crystal.storereview.model.UpdateStoreAssetReviewStatus;
import com.walmart.realestate.crystal.storereview.service.StoreAssetReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/store-asset-reviews")
public class StoreAssetReviewController {

    private final StoreAssetReviewService storeAssetReviewService;

    private final StoreAssetReviewAssembler storeAssetReviewAssembler;

    @GetMapping("{storeAssetReviewId}")
    @PostAuthorize("hasPolicy(returnObject.getContent(),'viewStoreReview')")
    public EntityModel<StoreAssetReview> getStoreAssetReview(@PathVariable String storeAssetReviewId) {
        return storeAssetReviewAssembler.toModel(storeAssetReviewService.getStoreAssetReview(storeAssetReviewId));
    }

    @GetMapping
    public CollectionModel<EntityModel<StoreAssetReview>> getStoreAssetReviews(@RequestParam String storeReviewId) {
        return storeAssetReviewAssembler.toCollectionModel(storeAssetReviewService.getStoreAssetReviews(storeReviewId));
    }

    @GetMapping("{storeNumber}/{storeReviewId}")
    public List<AggregatedStoreAssetReview> getAggregatedStoreAssetReviews(@PathVariable Long storeNumber, @PathVariable String storeReviewId, @RequestParam(required = false) List<String> fields) {
        return storeAssetReviewService.getAggregatedStoreAssetReviews(storeNumber, storeReviewId, fields);
    }

    @PostMapping("{storeAssetReviewId}/status")
    @PreAuthorize("hasPolicy(#storeAssetReviewId,'StoreAssetReview','editStoreReview')")
    public EntityModel<StoreAssetReview> updateAssetReviewStatus(
            @PathVariable String storeAssetReviewId,
            @RequestParam String action,
            @RequestBody(required = false) StoreAssetReviewCommand command) {
        return storeAssetReviewAssembler.toModel(storeAssetReviewService.updateStoreAssetReviewStatus(storeAssetReviewId, action, command));
    }

    @PostMapping("bulk/updateStatus")
    @PreAuthorize("hasPolicy('editStoreReview')")
    public CollectionModel<EntityModel<StoreAssetReview>> updateAssetReviewsStatus(
            @RequestParam String action,
            @RequestBody UpdateStoreAssetReviewStatus updateStoreAssetReviewStatus) {
        return storeAssetReviewAssembler.toCollectionModel(storeAssetReviewService.updateStoreAssetReviewsStatus(updateStoreAssetReviewStatus.getStoreAssetReviewIdList(), action));
    }

}
