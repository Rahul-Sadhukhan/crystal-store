package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.storereview.controller.hypermedia.StoreReviewSummaryAssembler;
import com.walmart.realestate.crystal.storereview.model.StoreReviewSummary;
import com.walmart.realestate.crystal.storereview.service.StoreReviewSummaryService;
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
@RequestMapping("/store-review-summary")
public class StoreReviewSummaryController {

    private final StoreReviewSummaryService storeReviewSummaryService;

    private final StoreReviewSummaryAssembler storeReviewSummaryAssembler;

    @GetMapping
    public CollectionModel<EntityModel<StoreReviewSummary>> getStoreReviewSummary(@RequestParam Long storeNumber) {
        return storeReviewSummaryAssembler
                .toCollectionModel(storeReviewSummaryService.getStoreReviewSummary(storeNumber))
                .add(linkTo(methodOn(StoreReviewSummaryController.class)
                        .getStoreReviewSummary(storeNumber)).withSelfRel());
    }

}
