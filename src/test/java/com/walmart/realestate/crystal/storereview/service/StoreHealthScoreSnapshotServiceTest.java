package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationStoreTimeInTarget;
import com.walmart.realestate.crystal.storereview.config.PropertiesConfig;
import com.walmart.realestate.crystal.storereview.entity.StoreHealthScoreSnapshotEntity;
import com.walmart.realestate.crystal.storereview.properties.StoreHealthScoreSnapshotProperties;
import com.walmart.realestate.crystal.storereview.repository.StoreHealthScoreSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StoreHealthScoreSnapshotService.class, PropertiesConfig.class, StoreHealthScoreSnapshotProperties.class})
@ActiveProfiles("test")
class StoreHealthScoreSnapshotServiceTest {

    @Autowired
    private StoreHealthScoreSnapshotService storeHealthScoreSnapshotService;

    @MockBean
    private StoreHealthScoreSnapshotRepository storeHealthScoreSnapshotRepository;

    @MockBean
    private HealthMetricsClient healthMetricsClient;

    @Test
    void testUpdateStoreHealthScoreSnapshot() {

        CollectionModel<EntityModel<RefrigerationStoreTimeInTarget>> refrigerationTimeInTargetSummaryCollectionModel = CollectionModel.of(
                List.of(EntityModel.of(RefrigerationStoreTimeInTarget.builder()
                                .rowId("123")
                                .storeNumber(1L)
                                .timeInTarget(90.30)
                                .runTime(Instant.ofEpochSecond(1655184485))
                                .build()),
                        EntityModel.of(RefrigerationStoreTimeInTarget.builder()
                                .rowId("124")
                                .storeNumber(2L)
                                .timeInTarget(80.30)
                                .runTime(Instant.ofEpochSecond(1655184485))
                                .build()),
                        EntityModel.of(RefrigerationStoreTimeInTarget.builder()
                                .rowId("125")
                                .storeNumber(3L)
                                .timeInTarget(70.30)
                                .runTime(Instant.ofEpochSecond(1655184485))
                                .build())
                )
        );

        List<StoreHealthScoreSnapshotEntity> storeHealthScoreSnapshotEntityList = List.of(
                StoreHealthScoreSnapshotEntity.builder()
                        .storeNumber(1L)
                        .rowId("123")
                        .value(90.30)
                        .runTime(Instant.ofEpochSecond(1655184485))
                        .build(),
                StoreHealthScoreSnapshotEntity.builder()
                        .storeNumber(2L)
                        .rowId("124")
                        .value(80.30)
                        .runTime(Instant.ofEpochSecond(1655184485))
                        .build(),
                StoreHealthScoreSnapshotEntity.builder()
                        .storeNumber(3L)
                        .rowId("125")
                        .value(70.30)
                        .runTime(Instant.ofEpochSecond(1655184485))
                        .build()
        );

        when(healthMetricsClient.getAllStoreHealthScore()).thenReturn(refrigerationTimeInTargetSummaryCollectionModel);

        storeHealthScoreSnapshotService.updateStoreHealthScoreSnapshot();

        verify(healthMetricsClient).getAllStoreHealthScore();
        verify(storeHealthScoreSnapshotRepository).saveAllAndFlush(storeHealthScoreSnapshotEntityList);

    }

}
