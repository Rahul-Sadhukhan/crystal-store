package com.walmart.realestate.crystal.storereview.service.selfheal;

import com.walmart.realestate.crystal.storereview.entity.StoreReviewAssetHealthScoreEntity;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewEntity;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewStoreHealthScoreEntity;
import com.walmart.realestate.crystal.storereview.repository.StoreReviewAssetHealthScoreRepository;
import com.walmart.realestate.crystal.storereview.repository.StoreReviewStoreHealthScoreRepository;
import com.walmart.realestate.crystal.storereview.service.StoreReviewHealthScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
public class PostPmSnapshotHandler implements SelfHealHandler {
    private final StoreReviewStoreHealthScoreRepository storeReviewStoreHealthScoreRepository;

    private final StoreReviewAssetHealthScoreRepository storeReviewAssetHealthScoreRepository;

    private final StoreReviewHealthScoreService storeReviewHealthScoreService;


    @Override
    public Integer getPriority() {
        return 4;
    }

    @Override
    public void handle(List<StoreReviewEntity> storeReviewList) {
        log.info("Running scheduled healer check for post pm health ");
        for (StoreReviewEntity storeReview : storeReviewList) {
            handle(storeReview);
        }

    }

    @Override
    public void handle(StoreReviewEntity storeReview) {
        try {
            if (storeReview.getFlow().equalsIgnoreCase("open")) {
                Optional<StoreReviewStoreHealthScoreEntity> storeHealthScoreEntity = storeReviewStoreHealthScoreRepository.findByStoreReviewId(storeReview.getId());
                StoreReviewAssetHealthScoreEntity storeAssetHealthScoreEntity = storeReviewAssetHealthScoreRepository.findTopByStoreReviewId(storeReview.getId());
                if (storeReview.getPostPreventiveMaintenanceStartedAt() != null && (storeHealthScoreEntity.get().getPostPreventiveMaintenanceScore() == null || storeAssetHealthScoreEntity.getPostPreventiveMaintenanceScore() == null)) {
                    log.info("Found review {} with no post pm health, attempting to snapshot post pm health", storeReview.getId());
                    storeReviewHealthScoreService.updateHealthScoreAtPostReviewEnd(storeReview.getId(), storeReview.getStoreNumber(), storeReview.getMonitoringStartedAt());
                }
            }
        } catch (Exception ex) {
            log.info("Exception encountered in self healer for post pm health review {} with error {}", storeReview.getId(), ex.getMessage());
        }
    }
}
