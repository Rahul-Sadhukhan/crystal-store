package com.walmart.realestate.crystal.storereview.service.selfheal;

import com.walmart.realestate.crystal.storereview.entity.StoreReviewEntity;
import com.walmart.realestate.crystal.storereview.service.StoreAssetReviewOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;


@RequiredArgsConstructor
@Component
@Slf4j
public class InvalidStoreReviewHandler implements SelfHealHandler {

    private final StoreAssetReviewOrchestrationService storeAssetReviewOrchestrationService;

    @Override
    public Integer getPriority() {
        return 1;
    }

    @Override
    public void handle(List<StoreReviewEntity> storeReviewList) {
        log.info("Running scheduled healer check for Invalid reviews");
        for (StoreReviewEntity storeReview : storeReviewList) {
            handle(storeReview);
        }
    }

    @Override
    public void handle(StoreReviewEntity storeReview) {
        try {
            if ((storeReview.getFlow().equalsIgnoreCase("default") || storeReview.getFlow().equalsIgnoreCase("assetReviews|default:assigned")) && storeReview.getState().equalsIgnoreCase("assigned")) {
                log.info("Found Invalid review {} attempting to fail review ", storeReview.getId());
                storeAssetReviewOrchestrationService.checkAndFixStoreReviewInvalidState(storeReview);
            }
        } catch (Exception ex) {
            log.info("Exception encountered in self healer for invalid review {} with error {}", storeReview.getId(), ex.getMessage());
        }
    }
}
