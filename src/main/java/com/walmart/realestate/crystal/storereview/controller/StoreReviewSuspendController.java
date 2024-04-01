package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.storereview.model.StoreReviewSuspend;
import com.walmart.realestate.crystal.storereview.service.StoreReviewSuspendService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/store-suspend")
public class StoreReviewSuspendController {

    private final StoreReviewSuspendService storeReviewSuspendService;

    @PostMapping
    @PreAuthorize("hasPolicy('createStoreReview')")
    public StoreReviewSuspend suspendStore(@RequestBody @Valid StoreReviewSuspend storeReviewSuspend) {
        return storeReviewSuspendService.suspendStore(storeReviewSuspend);
    }

    @PutMapping("{rowNumber}")
    @PreAuthorize("hasPolicy('createStoreReview')")
    public StoreReviewSuspend updateSuspended(@RequestBody @Valid StoreReviewSuspend storeReviewSuspend, @PathVariable Long rowNumber) {
        return storeReviewSuspendService.updateSuspended(storeReviewSuspend, rowNumber);
    }

    @GetMapping("/count")
    public Long getSuspendCount() {
        return storeReviewSuspendService.getSuspendedCount();
    }

}
