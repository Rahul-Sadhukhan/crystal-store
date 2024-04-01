package com.walmart.realestate.crystal.storereview.scheduler;

import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationTimeInTargetSummary;
import com.walmart.realestate.crystal.storereview.entity.StoreHealthScoreSnapshotEntity;
import com.walmart.realestate.crystal.storereview.exception.StoreHealthScoreSnapshotException;
import com.walmart.realestate.crystal.storereview.repository.StoreHealthScoreSnapshotRepository;
import com.walmart.realestate.crystal.storereview.service.StoreHealthScoreSnapshotService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StoreHealthScoreSnapshotScheduler.class})
@ActiveProfiles("test")
class StoreHealthScoreSnapshotSchedulerTest {

    @Autowired
    private StoreHealthScoreSnapshotScheduler storeHealthScoreSnapshotScheduler;

    @MockBean
    private StoreHealthScoreSnapshotService storeHealthScoreSnapshotService;

    @MockBean
    private HealthMetricsClient healthMetricsClient;

    @MockBean
    private StoreHealthScoreSnapshotRepository storeHealthScoreSnapshotRepository;

    @Test
    void testUpdateStoreHealthScoreSnapshotWhenRunTimeIsPresentInRefrigerationTimeInTargetSummaryAndRunTimeIsPresentInStoreHealthScoreSnapshotAndIsLess() {

        EntityModel<RefrigerationTimeInTargetSummary> refrigerationTimeInTargetSummaryEntityModel = EntityModel.of(RefrigerationTimeInTargetSummary.builder()
                .runTime(Instant.ofEpochSecond(1655184485))
                .build());

        StoreHealthScoreSnapshotEntity storeHealthScoreSnapshotEntity = StoreHealthScoreSnapshotEntity.builder()
                .runTime(Instant.ofEpochSecond(1655184485).minusSeconds(60))
                .build();

        when(healthMetricsClient.findTopByOrderByRunTimeDesc()).thenReturn(refrigerationTimeInTargetSummaryEntityModel);
        when(storeHealthScoreSnapshotRepository.findTopByOrderByRunTimeDesc()).thenReturn(storeHealthScoreSnapshotEntity);

        storeHealthScoreSnapshotScheduler.updateStoreHealthScoreSnapshot();

        verify(healthMetricsClient).findTopByOrderByRunTimeDesc();
        verify(storeHealthScoreSnapshotRepository).findTopByOrderByRunTimeDesc();
        verify(storeHealthScoreSnapshotService).updateStoreHealthScoreSnapshot();

    }

    @Test
    void testUpdateStoreHealthScoreSnapshotWhenRunTimeIsPresentInRefrigerationTimeInTargetSummaryAndRunTimeIsPresentInStoreHealthScoreSnapshotAndIsEqual() {

        EntityModel<RefrigerationTimeInTargetSummary> refrigerationTimeInTargetSummaryEntityModel = EntityModel.of(RefrigerationTimeInTargetSummary.builder()
                .runTime(Instant.ofEpochSecond(1655184485))
                .build());

        StoreHealthScoreSnapshotEntity storeHealthScoreSnapshotEntity = StoreHealthScoreSnapshotEntity.builder()
                .runTime(Instant.ofEpochSecond(1655184485))
                .build();

        when(healthMetricsClient.findTopByOrderByRunTimeDesc()).thenReturn(refrigerationTimeInTargetSummaryEntityModel);
        when(storeHealthScoreSnapshotRepository.findTopByOrderByRunTimeDesc()).thenReturn(storeHealthScoreSnapshotEntity);

        storeHealthScoreSnapshotScheduler.updateStoreHealthScoreSnapshot();

        verify(healthMetricsClient).findTopByOrderByRunTimeDesc();
        verify(storeHealthScoreSnapshotRepository).findTopByOrderByRunTimeDesc();

    }

    @Test
    void testUpdateStoreHealthScoreSnapshotWhenRunTimeIsPresentInRefrigerationTimeInTargetSummaryAndRunTimeIsAbsentInStoreHealthScoreSnapshot() {

        EntityModel<RefrigerationTimeInTargetSummary> refrigerationTimeInTargetSummaryEntityModel = EntityModel.of(RefrigerationTimeInTargetSummary.builder()
                .runTime(Instant.ofEpochSecond(1655184485))
                .build());

        StoreHealthScoreSnapshotEntity storeHealthScoreSnapshotEntity = StoreHealthScoreSnapshotEntity.builder()
                .runTime(null)
                .build();

        when(healthMetricsClient.findTopByOrderByRunTimeDesc()).thenReturn(refrigerationTimeInTargetSummaryEntityModel);
        when(storeHealthScoreSnapshotRepository.findTopByOrderByRunTimeDesc()).thenReturn(storeHealthScoreSnapshotEntity);

        storeHealthScoreSnapshotScheduler.updateStoreHealthScoreSnapshot();

        verify(healthMetricsClient).findTopByOrderByRunTimeDesc();
        verify(storeHealthScoreSnapshotRepository).findTopByOrderByRunTimeDesc();
        verify(storeHealthScoreSnapshotService).updateStoreHealthScoreSnapshot();

    }

    @Test
    void testUpdateStoreHealthScoreSnapshotWhenRunTimeIsAbsentInRefrigerationTimeInTargetSummaryAndRunTimeIsAbsentInStoreHealthScoreSnapshot() {

        EntityModel<RefrigerationTimeInTargetSummary> refrigerationTimeInTargetSummaryEntityModel = EntityModel.of(RefrigerationTimeInTargetSummary.builder()
                .runTime(null)
                .build());

        StoreHealthScoreSnapshotEntity storeHealthScoreSnapshotEntity = StoreHealthScoreSnapshotEntity.builder()
                .runTime(null)
                .build();

        when(healthMetricsClient.findTopByOrderByRunTimeDesc()).thenReturn(refrigerationTimeInTargetSummaryEntityModel);
        when(storeHealthScoreSnapshotRepository.findTopByOrderByRunTimeDesc()).thenReturn(storeHealthScoreSnapshotEntity);

        Assertions.assertThrows(StoreHealthScoreSnapshotException.class, () -> storeHealthScoreSnapshotScheduler.updateStoreHealthScoreSnapshot());

        verify(healthMetricsClient).findTopByOrderByRunTimeDesc();
        verify(storeHealthScoreSnapshotRepository).findTopByOrderByRunTimeDesc();
        verify(storeHealthScoreSnapshotService, never()).updateStoreHealthScoreSnapshot();

    }

}
