package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.storereview.model.StoreReviewTally;
import com.walmart.realestate.crystal.storereview.service.StoreReviewTallyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/store-reviews")
public class StoreReviewTallyController {

    private final StoreReviewTallyService storeReviewTallyService;

    @GetMapping("/tally/state")
    @PreAuthorize("hasPolicy('viewStoreReview')")
    public List<StoreReviewTally> getStoreReviewTallyByState(@RequestParam Optional<String> user) {
        return storeReviewTallyService.getStoreReviewTallyByState(user);
    }

}
