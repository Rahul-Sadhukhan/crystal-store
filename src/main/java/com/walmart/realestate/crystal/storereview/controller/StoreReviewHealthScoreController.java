package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.storereview.service.StoreReviewHealthScoreService;
import com.walmart.realestate.crystal.storereview.service.StoreReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.TimeZone;

@RequiredArgsConstructor
@RestController
@RequestMapping("/store-reviews")
public class StoreReviewHealthScoreController {

    private final StoreReviewHealthScoreService storeReviewHealthScoreService;

    @PostMapping("{storeReviewId}/health-scores")
    @PreAuthorize("hasPolicy('editStoreReview')")
    public void updateHealthScores(@PathVariable String storeReviewId,
                                   @RequestParam String storeTimeZone,
                                   @RequestParam boolean overrideHealthScores) {
        storeReviewHealthScoreService.updateHealthScoresAtStatuses(storeReviewId, ZoneId.of(storeTimeZone), overrideHealthScores);
    }

}
