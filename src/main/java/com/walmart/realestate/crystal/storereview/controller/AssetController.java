package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.asset.model.Asset;
import com.walmart.realestate.crystal.storereview.client.asset.model.StoreDataQuality;
import com.walmart.realestate.crystal.storereview.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Size;
import java.util.List;

@RequiredArgsConstructor
@Validated
@RestController
public class AssetController {

    private final AssetService assetService;

    @Logger
    @GetMapping("assets")
    @PreAuthorize("hasPolicy('viewStoreReview')")
    public List<Asset> getAssetsForStore(@RequestParam Long storeNumber, @RequestParam(required = false) List<String> fields) {
        return assetService.getAssetsForStore(storeNumber, fields);
    }

    @Logger
    @PutMapping("assets")
    @PreAuthorize("hasPolicy('createStoreReview')")
    public Object updateAsset(@RequestBody Asset asset) {
        return assetService.updateAsset(asset);
    }

    @Logger
    @GetMapping("store-data-quality-score")
    @PreAuthorize("hasPolicy('viewStoreReview')")
    public List<StoreDataQuality> getStoreDataQuality(@RequestParam @Size(max = 1000) List<Long> storeNumbers) {
        return assetService.getStoreDataQuality(storeNumbers);
    }

}
