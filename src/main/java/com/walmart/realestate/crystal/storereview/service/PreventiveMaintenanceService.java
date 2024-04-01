package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.model.PmCustomPage;
import com.walmart.realestate.crystal.storereview.model.PmSearchFilter;
import com.walmart.realestate.crystal.storereview.model.PreventiveMaintenanceContainer;
import com.walmart.realestate.crystal.storereview.model.PreventiveMaintenanceReadyToStart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PreventiveMaintenanceService {

    private final HealthMetricsClient healthMetricsClient;

    private final StoreReviewService storeReviewService;

    private final StoreReviewSuspendService storeReviewSuspendService;

    public Page<PreventiveMaintenanceReadyToStart> getPmReadyToStart(String filters, Boolean isSuspended, Pageable pageable) {

        List<Long> suspendedStoresRecordIds = storeReviewSuspendService.getSuspendedRecordIdNumbers();

        String suspendedFilter;
        if (isSuspended) {
            if (suspendedStoresRecordIds.isEmpty()) {
                return new PmCustomPage<>(new ArrayList<>(), pageable, 0, 0);
            }
            suspendedFilter = "recordIdNbr:in:" + StringUtils.join(suspendedStoresRecordIds, ",");

        } else {
            if (suspendedStoresRecordIds.isEmpty()) {
                suspendedFilter = "";
            } else {
                suspendedFilter = "recordIdNbr:notin:" + StringUtils.join(suspendedStoresRecordIds, ",");
            }
        }
        if (filters.isEmpty()) {
            filters = "storeNumber:notin:" + StringUtils.join(storeReviewService.getActiveStoresForPmReadyToStartExclusion(), ",");
        } else {
            filters = filters + ";" + "storeNumber:notin:" + StringUtils.join(storeReviewService.getActiveStoresForPmReadyToStartExclusion(), ",");
        }
        if (!suspendedFilter.isEmpty()) {
            filters = filters + ";" + suspendedFilter;
        }
        PreventiveMaintenanceContainer preventiveMaintenanceContainer = healthMetricsClient.getPmReadyToStart(PmSearchFilter.builder().filter(filters).build(), pageable);
        Page<PreventiveMaintenanceReadyToStart> page = new PageImpl<>(preventiveMaintenanceContainer.getPmReadyToStartList(), pageable, preventiveMaintenanceContainer.getTotalCount());
        return new PmCustomPage<>(page.getContent(), pageable, page.getTotalElements(), preventiveMaintenanceContainer.getAgingCount());

    }
}
