package com.walmart.realestate.crystal.storereview.client.healthmetrics;

import com.fasterxml.jackson.databind.JsonNode;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.*;
import com.walmart.realestate.crystal.storereview.model.PmSearchFilter;
import com.walmart.realestate.crystal.storereview.model.PreventiveMaintenanceContainer;
import com.walmart.realestate.crystal.storereview.model.PreventiveMaintenanceReadyToStart;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@FeignClient(value = "health-metrics", url = "${health-metrics-api.url}", configuration = HealthMetricsClientConfig.class)
public interface HealthMetricsClient {

    @GetMapping("refrigerationStoreTimeInTargets/search/findByStoreNumber")
    @Retry(name = "healthMetrics")
    EntityModel<RefrigerationStoreTimeInTarget> getStoreHealthScore(@RequestParam Long storeNumber);

    @GetMapping("refrigerationStoreTimeInTargets/search/findByStoreNumberAndRunTimeLessThanEqual")
    @Retry(name = "healthMetrics")
    EntityModel<RefrigerationStoreTimeInTarget> getStoreHealthScore(@RequestParam Long storeNumber, @RequestParam Instant runTime, @RequestParam String runDate);

    @GetMapping("refrigerationStoreTimeInTargets/search/findByStoreNumberIn")
    @Retry(name = "healthMetrics")
    CollectionModel<EntityModel<RefrigerationStoreTimeInTarget>> getStoreHealthScores(@RequestParam List<Long> storeNumbers);

    @GetMapping("refrigerationStoreTimeInTargets/search/findAllRunTimeLessThanEqual")
    @Retry(name = "healthMetrics")
    CollectionModel<EntityModel<RefrigerationStoreTimeInTarget>> getAllStoreHealthScore();

    @GetMapping("refrigerationRackTimeInTargets/search/findByStoreNumber")
    @Retry(name = "healthMetrics")
    CollectionModel<EntityModel<RefrigerationRackTimeInTarget>> getRackHealthScore(@RequestParam Long storeNumber);

    @GetMapping("refrigerationRackTimeInTargets/search/findByStoreNumberAndRunTimeLessThanEqual")
    @Retry(name = "healthMetrics")
    CollectionModel<EntityModel<RefrigerationRackTimeInTarget>> getRackHealthScore(@RequestParam Long storeNumber, @RequestParam Instant runTime, @RequestParam String runDate);

    @GetMapping("refrigerationCaseCurrentTimeInTargets/search/findByStoreNumber")
    @Retry(name = "healthMetrics")
    CollectionModel<EntityModel<RefrigerationCaseTimeInTarget>> getCaseHealthScore(@RequestParam Long storeNumber);

    @GetMapping("refrigerationCaseTimeInTargets/search/findByStoreNumberAndRunTimeLessThanEqual")
    @Retry(name = "healthMetrics")
    CollectionModel<EntityModel<RefrigerationCaseTimeInTarget>> getCaseHealthScore(@RequestParam Long storeNumber, @RequestParam Instant runTime, @RequestParam String runDate);

    @GetMapping("refrigerationTimeInTargetSummaries/search/findTopByOrderByRunTimeDesc")
    @Retry(name = "healthMetrics")
    EntityModel<RefrigerationTimeInTargetSummary> findTopByOrderByRunTimeDesc();

    @GetMapping("refrigerationAssetNormalizations/search/findAllCaseTemperatureSensorsByStoreNumber")
    @Retry(name = "healthMetrics")
    CollectionModel<EntityModel<RefrigerationSensor>> getCaseAssets(@RequestParam Long storeNumber);

    @GetMapping("refrigerationAssetNormalizations/search/findAllRacksByStoreNumber")
    @Retry(name = "healthMetrics")
    CollectionModel<EntityModel<RefrigerationSensor>> getRackAssets(@RequestParam Long storeNumber);

    @GetMapping("rackMetrics/{storeNumber}")
    @Retry(name = "healthMetrics")
    List<RefrigerationRackMetric> getRefrigerationRackMetricByStore(@PathVariable Long storeNumber);

    @GetMapping("store-review/stores")
    @Retry(name = "healthMetrics")
    PagedModel<EntityModel<StoreReviewPagedEntity>> getStoreList(@RequestParam String healthScore,
                                                                 @RequestParam String storeNumber,
                                                                 @RequestParam String storeType,
                                                                 @RequestParam String fmRegion,
                                                                 @RequestParam String fmSubRegion,
                                                                 @RequestParam String refrigerationTypes,
                                                                 @RequestParam String emsData,
                                                                 @SpringQueryMap Pageable pageable);

    @GetMapping("fMRealtyAlignments/search/findByStoreNumber")
    @Retry(name = "healthMetrics")
    FMRealtyAlignment getFMRealtyAlignment(@RequestParam String storeNumber);

    @PostMapping("quick-base/schedules/ready")
    @Retry(name = "healthMetrics")
    PreventiveMaintenanceContainer getPmReadyToStart(@RequestBody PmSearchFilter pmSearchFilter, @SpringQueryMap Pageable pageable);

    @PostMapping("quick-base/schedules/ready/v2")
    @Retry(name = "healthMetrics")
    List<PreventiveMaintenanceReadyToStart> getPmReadyToStartList( @RequestBody PmSearchFilter pmSearchFilter);

    @PostMapping("quick-base/schedules/ready/count")
    @Retry(name = "healthMetrics")
    Long getPmReadyToStartListCount( @RequestBody PmSearchFilter pmSearchFilter);
}
