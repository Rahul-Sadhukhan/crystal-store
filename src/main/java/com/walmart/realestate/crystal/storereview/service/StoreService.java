package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.asset.model.StoreReviewQuery;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.StoreReviewPagedEntity;
import com.walmart.realestate.crystal.storereview.client.storeinfo.StoreClient;
import com.walmart.realestate.crystal.storereview.client.storeinfo.model.StoreDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StoreService {

    private final StoreClient storeClient;

    private final HealthMetricsClient healthMetricsClient;

    @Logger
    @Cacheable("storeDetailsInfo")
    public StoreDetail getStoreInfo(Long storeNumber) {
        return storeClient.getStoreInfo(storeNumber);
    }

    @Logger
    public String getStorePlan(Long storeNumber) {
        return storeClient.getStorePlan(storeNumber);
    }

    @Logger
    public PagedModel<EntityModel<StoreReviewPagedEntity>> getStoreList(StoreReviewQuery query, Pageable pageable) {
        return healthMetricsClient.getStoreList(query.getHealthScore(),
                query.getStoreNumber(),
                query.getStoreType(),
                query.getFmRegion(),
                query.getFmSubRegion(),
                query.getRefrigerationTypes(),
                query.getEmsData(),
                pageable);
    }

}
