package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.storereview.controller.hypermedia.AssetReviewSummaryAssembler;
import com.walmart.realestate.crystal.storereview.model.AssetReviewSummary;
import com.walmart.realestate.crystal.storereview.service.AssetReviewSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RequiredArgsConstructor
@RestController
@RequestMapping("/asset-review-summaries")
public class AssetReviewSummaryController {

    private final AssetReviewSummaryService assetReviewSummaryService;

    private final AssetReviewSummaryAssembler assetReviewSummaryAssembler;

    @GetMapping
    public CollectionModel<EntityModel<AssetReviewSummary>> getAssetReviewSummary(@RequestParam Long assetId) {
        return assetReviewSummaryAssembler
                .toCollectionModel(assetReviewSummaryService.getAssetReviewSummary(assetId))
                .add(linkTo(methodOn(AssetReviewSummaryController.class)
                        .getAssetReviewSummary(assetId)).withSelfRel());
    }

}
