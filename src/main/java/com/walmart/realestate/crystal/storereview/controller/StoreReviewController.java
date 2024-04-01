package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.storereview.command.StoreReviewCommand;
import com.walmart.realestate.crystal.storereview.controller.hypermedia.StoreReviewAssembler;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.model.StoreReviewFilters;
import com.walmart.realestate.crystal.storereview.service.StoreReviewService;
import com.walmart.realestate.soteria.model.UserContext;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RequiredArgsConstructor
@RestController
@RequestMapping("/store-reviews")
public class StoreReviewController {

    private final StoreReviewService storeReviewService;

    private final StoreReviewAssembler storeReviewAssembler;

    @PostMapping
    @PreAuthorize("hasPolicy(#storeReview, 'createStoreReview')")
    public EntityModel<StoreReview> createStoreReview(@RequestBody @Valid StoreReview storeReview, @AuthenticationPrincipal UserContext userContext) {
        return storeReviewAssembler.toModel(storeReviewService.createStoreReview(storeReview, userContext));
    }

    @PutMapping("{storeReviewId}")
    @PreAuthorize("hasPolicy(#storeReview, 'editStoreReview')")
    public EntityModel<StoreReview> updateStoreReview(@PathVariable String storeReviewId, @RequestBody @Valid StoreReview storeReview) {
        return storeReviewAssembler.toModel(storeReviewService.updateStoreReview(storeReviewId, storeReview));
    }

    @GetMapping
    @PreAuthorize("hasPolicy('viewStoreReview')")
    @PageableAsQueryParam
    public CollectionModel<EntityModel<StoreReview>> getStoreReviews(@Parameter(hidden = true) @PageableDefault(size = 1000, sort = "assignedAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                     @ModelAttribute StoreReviewFilters filters) {
        return storeReviewAssembler.toPagedModel(storeReviewService.getStoreReviews(pageable, filters))
                .add(linkTo(methodOn(StoreReviewController.class)
                        .getStoreReviews(pageable, filters))
                        .withSelfRel());
    }

    @GetMapping("{storeReviewId}")
    @PostAuthorize("hasPolicy(returnObject.getContent(), 'viewStoreReview')")
    public EntityModel<StoreReview> getStoreReview(@PathVariable String storeReviewId) {
        return storeReviewAssembler.toModel(storeReviewService.getStoreReview(storeReviewId));
    }

    @PostMapping("{storeReviewId}/status")
    @PreAuthorize("hasPolicy(#storeReviewId, 'StoreReview', 'editStoreReview')")
    public EntityModel<StoreReview> updateStatus(
            @PathVariable String storeReviewId,
            @RequestParam String action,
            @RequestBody(required = false) StoreReviewCommand command) {
        return storeReviewAssembler.toModel(storeReviewService.updateStoreReviewStatus(storeReviewId, action, command));
    }

    @PostMapping("{storeReviewId}/store-asset-reviews")
    @PreAuthorize("hasPolicy(#storeReviewId, 'StoreReview', 'createStoreReview')")
    public EntityModel<StoreReview> createStoreAssetReviews(@PathVariable String storeReviewId) {
        return storeReviewAssembler.toModel(storeReviewService.refreshStoreAssetReviews(storeReviewId));
    }

}
