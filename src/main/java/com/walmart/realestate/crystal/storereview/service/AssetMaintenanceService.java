package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.amg.AmgClient;
import com.walmart.realestate.crystal.storereview.client.amg.model.AmgNote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@CacheConfig(cacheManager = "cloudCacheManager")
public class AssetMaintenanceService {

    private final AmgClient amgClient;

    @Logger
    @Cacheable("sdm")
    @Deprecated
    public AmgNote getServiceModel(Long storeNumber) {
        log.info("Get store service delivery model for store number {}", storeNumber);
        return amgClient.getNote(storeNumber, "SDM")
                .orElse(AmgNote.builder()
                        .value("NA")
                        .build());
    }

}
