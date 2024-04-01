package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationAssetTimeInTarget;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationStoreTimeInTarget;
import com.walmart.realestate.crystal.storereview.service.StoreAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Validated
@RestController
public class StoreAssetController {

    private final StoreAssetService storeAssetService;

    @Logger
    @GetMapping("store-health-score")
    @PreAuthorize("hasPolicy('viewHealthScore')")
    public List<RefrigerationStoreTimeInTarget> getStoreHealthScore(@RequestParam @Size(max = 1000) List<Long> storeNumbers) {
        return new ArrayList<>(storeAssetService.getStoreHealthScore(storeNumbers).values());
    }

    @Logger
    @GetMapping("asset-health-score")
    @PreAuthorize("hasPolicy('viewHealthScore')")
    public List<RefrigerationAssetTimeInTarget> getAssetHealthScore(@RequestParam Long storeNumber, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate requestDate) {
        return storeAssetService.getAssetHealthScoreByStoreNumberDate(storeNumber, requestDate);
    }

}
