package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.storereview.controller.hypermedia.StoreReviewTimelineAssembler;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.model.Timeline;
import com.walmart.realestate.crystal.storereview.service.StoreReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RequiredArgsConstructor
@RestController
@RequestMapping("/store-reviews/{storeReviewId}/timeline")
public class StoreReviewTimelineController {

    private final StoreReviewService storeReviewService;

    private final StoreReviewTimelineAssembler storeReviewTimelineAssembler;

    @GetMapping
    @PostAuthorize("hasPolicy('viewStoreReview')")
    public EntityModel<Timeline<StoreReview>> getStoreReviewTimeline(@PathVariable String storeReviewId) {
        return storeReviewTimelineAssembler.toModel(storeReviewService.getStoreReviewTimeline(storeReviewId))
                .add(linkTo(methodOn(StoreReviewTimelineController.class)
                        .getStoreReviewTimeline(storeReviewId))
                        .withSelfRel());
    }

}
