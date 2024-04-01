package com.walmart.realestate.crystal.storereview.service.selfheal;

import com.walmart.realestate.crystal.storereview.entity.StoreReviewEntity;

import java.util.List;

public interface SelfHealHandler {

    Integer getPriority();

    void handle(List<StoreReviewEntity> storeReviewList);

    void handle(StoreReviewEntity storeReviewList);

}
