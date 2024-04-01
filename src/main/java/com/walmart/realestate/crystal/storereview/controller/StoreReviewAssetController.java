package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationSensor;
import com.walmart.realestate.crystal.storereview.service.StoreReviewAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/sensor-assets")
public class StoreReviewAssetController {

    private final StoreReviewAssetService storeReviewAssetService;

    @Logger
    @GetMapping
    @PreAuthorize("hasPolicy('viewStoreReview')")
    public List<RefrigerationSensor> getAssetsForStore(@RequestParam Long storeNumber) {
        return storeReviewAssetService.getAssetsForStore(storeNumber);
    }

}
