package com.walmart.realestate.crystal.settingchangelog.controller;

import com.walmart.realestate.crystal.settingchangelog.service.InsightDeleteService;
import com.walmart.realestate.soteria.model.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/insights")
public class InsightDeleteController {

    private final InsightDeleteService insightDeleteService;

    @PostMapping("{insightId}/delete")
    public void deleteInsight(@PathVariable String insightId,
                                           @RequestParam String storeReviewId,
                                           @RequestParam String storeReviewState,
                                           @AuthenticationPrincipal UserContext userContext) {
        insightDeleteService.deleteInsight(insightId, storeReviewId, storeReviewState, userContext);
    }
}
