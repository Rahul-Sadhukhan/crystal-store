package com.walmart.realestate.crystal.storereview.service;

import com.walmart.core.realestate.cerberus.exception.BadRequestException;
import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewSuspendEntity;
import com.walmart.realestate.crystal.storereview.model.PmSearchFilter;
import com.walmart.realestate.crystal.storereview.model.PreventiveMaintenanceReadyToStart;
import com.walmart.realestate.crystal.storereview.model.StoreReviewSuspend;
import com.walmart.realestate.crystal.storereview.repository.StoreReviewSuspendRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class StoreReviewSuspendService {

    public static final String SUSPENDED = "suspended";
    public static final String EXPIRED = "expired";
    private final StoreReviewSuspendRepository storeReviewSuspendRepository;

    private final HealthMetricsClient healthMetricsClient;

    private final StoreReviewService storeReviewService;

    @Logger
    public StoreReviewSuspend suspendStore(StoreReviewSuspend storeReviewSuspend) {
        Long recordIdNbr = storeReviewSuspend.getRecordIdNbr();

        List<PreventiveMaintenanceReadyToStart> preventiveMaintenanceReadyToStart = healthMetricsClient.getPmReadyToStartList(
                PmSearchFilter.builder()
                        .filter("recordIdNbr:eq:" + recordIdNbr + ";" + "storeNumber:notin:" +
                                StringUtils.join(storeReviewService.getActiveStoresForPmReadyToStartExclusion(), ","))
                        .build()
        );

        if (preventiveMaintenanceReadyToStart.isEmpty() || !preventiveMaintenanceReadyToStart.get(0).getRecordIdNbr().equals(recordIdNbr)) {
            throw new EntityNotFoundException("Requested RecordIdNumber does not exists in quick base ");
        }

        boolean recordExists = storeReviewSuspendRepository.existsById(recordIdNbr);
        if (recordExists) {
            storeReviewSuspendRepository.deleteById(recordIdNbr);
        }

        PreventiveMaintenanceReadyToStart pmReadyToStart = preventiveMaintenanceReadyToStart.get(0);
        StoreReviewSuspendEntity entity = getEntityFromPmReadyToStart(pmReadyToStart);
        StoreReviewSuspendEntity savedEntity = storeReviewSuspendRepository.save(entity);

        return fromStoreReviewSuspendEntity(savedEntity);
    }

    public List<Long> getSuspendedRecordIdNumbers() {
        return storeReviewSuspendRepository.findByState(SUSPENDED).stream().map(StoreReviewSuspendEntity::getRecordIdNbr).collect(Collectors.toList());
    }

    private StoreReviewSuspendEntity getEntityFromPmReadyToStart(PreventiveMaintenanceReadyToStart preventiveMaintenanceReadyToStart) {
        return StoreReviewSuspendEntity.builder()
                .storeNumber(preventiveMaintenanceReadyToStart.getStoreNumber())
                .docPrePmDeliveryDate(preventiveMaintenanceReadyToStart.getDocPrePmDeliveryDate())
                .recordIdNbr(preventiveMaintenanceReadyToStart.getRecordIdNbr())
                .state(SUSPENDED)
                .build();

    }


    private StoreReviewSuspend fromStoreReviewSuspendEntity(StoreReviewSuspendEntity storeReviewSuspendEntity) {

        return StoreReviewSuspend.builder()
                .docPrePmDeliveryDate(storeReviewSuspendEntity.getDocPrePmDeliveryDate())
                .storeNumber(storeReviewSuspendEntity.getStoreNumber())
                .recordIdNbr(storeReviewSuspendEntity.getRecordIdNbr())
                .createdAt(storeReviewSuspendEntity.getCreatedAt())
                .createdBy(storeReviewSuspendEntity.getCreatedBy())
                .state(storeReviewSuspendEntity.getState())
                .lastModifiedBy(storeReviewSuspendEntity.getLastModifiedBy())
                .lastModifiedAt(storeReviewSuspendEntity.getLastModifiedAt())
                .build();
    }

    @Logger
    public StoreReviewSuspend updateSuspended(StoreReviewSuspend storeReviewSuspend, Long rowNumber) {
        StoreReviewSuspendEntity storeReviewSuspendEntity = storeReviewSuspendRepository.findById(rowNumber).get();
        if (storeReviewSuspend.getState().equals(EXPIRED))
            storeReviewSuspendEntity.setState(EXPIRED);
        else throw new BadRequestException("Invalid state name");
        return fromStoreReviewSuspendEntity(storeReviewSuspendRepository.save(storeReviewSuspendEntity));
    }

    public Long getSuspendedCount() {

        List<Long> recordIdList = storeReviewSuspendRepository.findByState(SUSPENDED).stream().map(StoreReviewSuspendEntity::getRecordIdNbr).collect(Collectors.toList());
        if (recordIdList.isEmpty())
            return 0L;
        return healthMetricsClient.getPmReadyToStartListCount(PmSearchFilter.builder()
                .filter("recordIdNbr:in:" + StringUtils.join(recordIdList, ",") + ";" + "storeNumber:notin:"
                        + StringUtils.join(storeReviewService.getActiveStoresForPmReadyToStartExclusion(), ","))
                .build());

    }

    @Scheduled(cron = "0 0 9 * * *", zone = "UTC")
    @SchedulerLock(name = "executeSuspendRefresh")
    @Logger
    public void scheduledExecuteSuspendRefresh() {
        List<Long> recordIdList = storeReviewSuspendRepository.findByState(SUSPENDED).stream()
                .map(StoreReviewSuspendEntity::getRecordIdNbr)
                .collect(Collectors.toList());
        log.info("Running scheduled suspend refresh for {} recordIds ", recordIdList);
        List<PreventiveMaintenanceReadyToStart> pmReadyToStartList = healthMetricsClient
                .getPmReadyToStartList(PmSearchFilter.builder()
                        .filter("recordIdNbr:in:" + StringUtils.join(recordIdList, ","))
                        .build());

        LocalDate cutOffDate = Instant.now().atZone(ZoneId.of("UTC")).toLocalDate().minusDays(60);
        for (PreventiveMaintenanceReadyToStart preventiveMaintenanceReadyToStart : pmReadyToStartList) {
            if (preventiveMaintenanceReadyToStart.getDocPrePmDeliveryDate().isBefore(cutOffDate)) {
                log.info("Found pm with docPrePmDelivery older than 60 days for  {} recordIdNumber, attempting expire ", preventiveMaintenanceReadyToStart.getRecordIdNbr());
                updateSuspended(StoreReviewSuspend.builder().state(EXPIRED).recordIdNbr(preventiveMaintenanceReadyToStart.getRecordIdNbr()).build(),
                        preventiveMaintenanceReadyToStart.getRecordIdNbr());
            }

        }

    }
}
