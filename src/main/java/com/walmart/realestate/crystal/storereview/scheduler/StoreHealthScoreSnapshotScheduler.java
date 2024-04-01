package com.walmart.realestate.crystal.storereview.scheduler;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationTimeInTargetSummary;
import com.walmart.realestate.crystal.storereview.exception.StoreHealthScoreSnapshotException;
import com.walmart.realestate.crystal.storereview.repository.StoreHealthScoreSnapshotRepository;
import com.walmart.realestate.crystal.storereview.service.StoreHealthScoreSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class StoreHealthScoreSnapshotScheduler {

    private final StoreHealthScoreSnapshotRepository storeHealthScoreSnapshotRepository;

    private final HealthMetricsClient healthMetricsClient;

    private final StoreHealthScoreSnapshotService storeHealthScoreSnapshotService;

    @Logger
    @Scheduled(fixedDelayString = "${store-health-score-snapshot.schedule-value}", initialDelayString = "${store-health-score-snapshot.initial-delay-value}")
    public void updateStoreHealthScoreSnapshot() {
        EntityModel<RefrigerationTimeInTargetSummary> refrigerationTimeInTargetSummaryEntityModel = healthMetricsClient.findTopByOrderByRunTimeDesc();
        var storeHealthScoreSnapshotEntity = storeHealthScoreSnapshotRepository.findTopByOrderByRunTimeDesc();

        if (Objects.nonNull(refrigerationTimeInTargetSummaryEntityModel.getContent()) && Objects.nonNull(refrigerationTimeInTargetSummaryEntityModel.getContent().getRunTime())) {
            Instant latestRunTime = refrigerationTimeInTargetSummaryEntityModel.getContent().getRunTime();

            if (Objects.nonNull(storeHealthScoreSnapshotEntity) && Objects.nonNull(storeHealthScoreSnapshotEntity.getRunTime())) {
                Instant latestRunTimeInStoreHealthScoreSnapshot = storeHealthScoreSnapshotEntity.getRunTime();
                if (Objects.nonNull(latestRunTime) && Objects.nonNull(latestRunTimeInStoreHealthScoreSnapshot)
                        && latestRunTime.isAfter(latestRunTimeInStoreHealthScoreSnapshot)) {
                    storeHealthScoreSnapshotService.updateStoreHealthScoreSnapshot();
                }
            } else {
                storeHealthScoreSnapshotService.updateStoreHealthScoreSnapshot();
            }
        } else {
            throw new StoreHealthScoreSnapshotException();
        }
    }

}
