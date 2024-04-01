package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.estr.EstrClient;
import com.walmart.realestate.crystal.storereview.client.estr.model.EstrFact;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewEntity;
import com.walmart.realestate.crystal.storereview.model.StoreAssetReview;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Component
public class StoreAssetReviewOrchestrationService {

    private final StoreAssetReviewService storeAssetReviewService;

    private final EstrClient estrClient;

    private final TaskExecutor taskExecutor;

    @Logger
    public CompletableFuture<List<StoreAssetReview>> createStoreAssetReviews(UUID uuid, Long storeNumber, String storeReviewId) {
        log.info("Create store asset reviews store number {} store review {} uuid {}", storeNumber, storeReviewId, uuid);
        return CompletableFuture
                .runAsync(() -> {
                    log.info("Start creating store asset reviews store number {} store review {}", storeNumber, storeReviewId);
                    estrClient.updateFactStatus(uuid, "startAssetReviewsCreation", EstrFact.builder().build());
                }, taskExecutor)
                .thenApplyAsync(nothing -> storeAssetReviewService.createStoreAssetReviews(storeNumber, storeReviewId), taskExecutor)
                .thenApplyAsync(storeAssetReviews -> {
                    log.info("Successfully created store asset reviews");
                    completeAssetReviewCreation(uuid);
                    return storeAssetReviews;

                }, taskExecutor)
                .handle((storeAssetReviews, e) -> {
                    if (Objects.nonNull(e)) {
                        log.error("Error creating store asset reviews", e);
                        AuthenticationHelper authenticationHelper = new AuthenticationHelper("system", false);
                        try {
                            failAssetReviewCreation(uuid);
                        } finally {
                            authenticationHelper.resetAuthentication();
                        }
                    }
                    return storeAssetReviews;
                });
    }

    @Logger
    @Retry(name = "estr-update")
    public void completeAssetReviewCreation(UUID uuid) {
        estrClient.updateFactStatus(uuid, "completeAssetReviewsCreation", EstrFact.builder().build());
    }

    @Logger
    @Retry(name = "estr-update")
    public void failAssetReviewCreation(UUID uuid) {
        estrClient.updateFactStatus(uuid, "failAssetReviewsCreation", EstrFact.builder().build());
    }

    @Logger
    public CompletableFuture<List<StoreAssetReview>> refreshStoreAssetReviews(UUID uuid, Long storeNumber, String storeReviewId) {
        log.info("Refresh store asset reviews store number {} store review {} uuid {}", storeNumber, storeReviewId, uuid);
        return CompletableFuture
                .runAsync(() -> {
                    log.info("Refresh store asset reviews store number {} store review {}", storeNumber, storeReviewId);
                    estrClient.updateFactStatus(uuid, "refreshAssetReviews", EstrFact.builder().build());
                }, taskExecutor)
                .thenApplyAsync(nothing -> storeAssetReviewService.createStoreAssetReviews(storeNumber, storeReviewId), taskExecutor);
    }

    @Logger
    public void checkAndFixStoreReviewInvalidState(StoreReviewEntity entity) {
        if ((entity.getFlow().equalsIgnoreCase("default") || entity.getFlow().equalsIgnoreCase("assetReviews|default:assigned")) && entity.getState().equalsIgnoreCase("assigned")) {
            if (entity.getCreatedAt().isBefore(Instant.now().minusSeconds(60))) {
                log.error("Review creation failed for {} moving to failed state", entity.getId());
                AuthenticationHelper authenticationHelper = new AuthenticationHelper("system", true);
                try {
                    failAssetReviewCreation(UUID.fromString(entity.getUuid()));
                } finally {
                    authenticationHelper.resetAuthentication();
                }
            }
        }
    }

}
