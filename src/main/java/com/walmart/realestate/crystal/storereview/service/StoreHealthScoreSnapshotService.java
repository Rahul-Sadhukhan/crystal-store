package com.walmart.realestate.crystal.storereview.service;

import com.google.common.collect.Lists;
import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationStoreTimeInTarget;
import com.walmart.realestate.crystal.storereview.entity.StoreHealthScoreSnapshotEntity;
import com.walmart.realestate.crystal.storereview.properties.StoreHealthScoreSnapshotProperties;
import com.walmart.realestate.crystal.storereview.repository.StoreHealthScoreSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class StoreHealthScoreSnapshotService {

    private final StoreHealthScoreSnapshotRepository storeHealthScoreSnapshotRepository;

    private final HealthMetricsClient healthMetricsClient;

    private final StoreHealthScoreSnapshotProperties storeHealthScoreSnapshotProperties;

    @Logger
    public void updateStoreHealthScoreSnapshot() {
        CollectionModel<EntityModel<RefrigerationStoreTimeInTarget>> refrigerationStoreTimeInTargetList = healthMetricsClient.getAllStoreHealthScore();
        List<StoreHealthScoreSnapshotEntity> storeHealthScoreSnapshotEntityList = refrigerationStoreTimeInTargetList.getContent().stream()
                .map(EntityModel::getContent)
                .filter(Objects::nonNull)
                .map(this::buildStoreHealthScoreSnapshotEntity)
                .collect(Collectors.toList());
        Lists.partition(storeHealthScoreSnapshotEntityList, storeHealthScoreSnapshotProperties.getPartitionSize())
                .forEach(storeHealthScoreSnapshotRepository::saveAllAndFlush);
    }

    private StoreHealthScoreSnapshotEntity buildStoreHealthScoreSnapshotEntity(RefrigerationStoreTimeInTarget refrigerationStoreTimeInTarget) {
        return StoreHealthScoreSnapshotEntity.builder()
                .storeNumber(refrigerationStoreTimeInTarget.getStoreNumber())
                .rowId(refrigerationStoreTimeInTarget.getRowId())
                .value(refrigerationStoreTimeInTarget.getTimeInTarget())
                .runTime(refrigerationStoreTimeInTarget.getRunTime())
                .build();
    }

}
