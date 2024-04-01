package com.walmart.realestate.crystal.storereview.client.estr;

import com.walmart.realestate.crystal.storereview.client.estr.model.EstrFact;
import com.walmart.realestate.crystal.storereview.client.estr.model.EstrTimeline;
import com.walmart.realestate.crystal.storereview.client.estr.model.EstrWorkflow;
import com.walmart.realestate.crystal.storereview.client.estr.model.StoreAssetReviewStateResponse;
import com.walmart.realestate.crystal.storereview.model.UpdateStoreAssetReviewStatusEstrFact;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(value = "estr", url = "${estr-api.url}", configuration = EstrClientConfiguration.class)
public interface EstrClient {

    @PostMapping
    EstrFact createFact(EstrFact fact);

    @GetMapping("workflow")
    @Retry(name = "estr")
    EstrWorkflow getWorkflow(@RequestParam String ticketType, @RequestParam String state, @RequestParam String flow);

    @GetMapping("{id}/workflow")
    @Retry(name = "estr")
    EstrWorkflow getWorkflow(@PathVariable UUID id);

    @GetMapping("{id}/timeline")
    @Retry(name = "estr")
    EstrTimeline getTimeline(@PathVariable UUID id);

    @PostMapping("{id}/status")
    EstrFact updateFactStatus(@PathVariable UUID id, @RequestParam String action, EstrFact fact);

    @PostMapping("bulk/status")
    List<EstrFact> updateFactsStatus(@RequestParam String action, @RequestBody UpdateStoreAssetReviewStatusEstrFact updateStoreAssetReviewStatusEstrFact);

    @GetMapping("{storeReviewId}/store-asset-reviews")
    @Retry(name = "estr-bulk-fetch")
    List<StoreAssetReviewStateResponse> getAssetReviews(@PathVariable String storeReviewId);

}
