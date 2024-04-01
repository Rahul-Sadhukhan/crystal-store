package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.storereview.client.estr.EstrClient;
import com.walmart.realestate.crystal.storereview.client.estr.model.EstrFact;
import com.walmart.realestate.crystal.storereview.config.TestAsyncConfig;
import com.walmart.realestate.crystal.storereview.model.StoreAssetReview;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StoreAssetReviewOrchestrationService.class, TestAsyncConfig.class, ThreadPoolTaskExecutor.class, TaskExecutorBuilder.class})
@ActiveProfiles("test")
public class StoreAssetReviewOrchestrationServiceTest {

    @Autowired
    private StoreAssetReviewOrchestrationService storeAssetReviewOrchestrationService;

    @MockBean
    private StoreAssetReviewService storeAssetReviewService;

    @MockBean
    private EstrClient estrClient;

    @Test
    void testCreateStoreAssetReviews() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(estrClient.updateFactStatus(eq(uuid), anyString(), eq(EstrFact.builder().build())))
                .thenReturn(EstrFact.builder()
                        .id(uuid)
                        .build());

        List<StoreAssetReview> testStoreAssetReviews = Collections.singletonList(StoreAssetReview.builder()
                .id("SAR-1")
                .build());
        when(storeAssetReviewService.createStoreAssetReviews(4427L, "SR-1"))
                .thenReturn(testStoreAssetReviews);

        CompletableFuture<List<StoreAssetReview>> storeAssetReviewsFuture =
                storeAssetReviewOrchestrationService.createStoreAssetReviews(uuid, 4427L, "SR-1");

        assertThat(storeAssetReviewsFuture).isNotNull();

        List<StoreAssetReview> storeAssetReviews = storeAssetReviewsFuture.get();
        assertThat(storeAssetReviews).isEqualTo(testStoreAssetReviews);

        verify(estrClient).updateFactStatus(uuid, "startAssetReviewsCreation", EstrFact.builder().build());
        verify(storeAssetReviewService).createStoreAssetReviews(4427L, "SR-1");
        verify(estrClient).updateFactStatus(uuid, "completeAssetReviewsCreation", EstrFact.builder().build());
    }

    @Test
    void testCreateStoreAssetReviewsWithException() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(estrClient.updateFactStatus(eq(uuid), anyString(), eq(EstrFact.builder().build())))
                .thenReturn(EstrFact.builder()
                        .id(uuid)
                        .build());

        when(storeAssetReviewService.createStoreAssetReviews(283L, "SR-1"))
                .thenThrow(new RuntimeException());

        CompletableFuture<List<StoreAssetReview>> storeAssetReviewsFuture =
                storeAssetReviewOrchestrationService.createStoreAssetReviews(uuid, 283L, "SR-1");

        assertThat(storeAssetReviewsFuture).isNotNull();

        List<StoreAssetReview> storeAssetReviews = storeAssetReviewsFuture.get();
        assertThat(storeAssetReviews).isNull();

        verify(estrClient).updateFactStatus(uuid, "startAssetReviewsCreation", EstrFact.builder().build());
        verify(storeAssetReviewService).createStoreAssetReviews(283L, "SR-1");
        verify(estrClient).updateFactStatus(uuid, "failAssetReviewsCreation", EstrFact.builder().build());
    }

    @Test
    void testRefreshStoreAssetReviews() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(estrClient.updateFactStatus(eq(uuid), anyString(), eq(EstrFact.builder().build())))
                .thenReturn(EstrFact.builder()
                        .id(uuid)
                        .build());

        List<StoreAssetReview> testStoreAssetReviews = Collections.singletonList(StoreAssetReview.builder()
                .id("SAR-1")
                .build());
        when(storeAssetReviewService.createStoreAssetReviews(4427L, "SR-1"))
                .thenReturn(testStoreAssetReviews);

        CompletableFuture<List<StoreAssetReview>> storeAssetReviewsFuture =
                storeAssetReviewOrchestrationService.refreshStoreAssetReviews(uuid, 4427L, "SR-1");

        assertThat(storeAssetReviewsFuture).isNotNull();

        List<StoreAssetReview> storeAssetReviews = storeAssetReviewsFuture.get();
        assertThat(storeAssetReviews).isEqualTo(testStoreAssetReviews);

        verify(estrClient).updateFactStatus(uuid, "refreshAssetReviews", EstrFact.builder().build());
        verify(storeAssetReviewService).createStoreAssetReviews(4427L, "SR-1");
    }

    @Test
    void testRefreshStoreAssetReviewsWithStatusException() {
        UUID uuid = UUID.randomUUID();
        when(estrClient.updateFactStatus(eq(uuid), anyString(), eq(EstrFact.builder().build())))
                .thenThrow(new RuntimeException());

        CompletableFuture<List<StoreAssetReview>> storeAssetReviewsFuture =
                storeAssetReviewOrchestrationService.refreshStoreAssetReviews(uuid, 4427L, "SR-1");

        assertThat(storeAssetReviewsFuture).isNotNull();

        assertThatThrownBy(storeAssetReviewsFuture::get).getCause()
                .isInstanceOf(RuntimeException.class);

        verify(estrClient).updateFactStatus(uuid, "refreshAssetReviews", EstrFact.builder().build());
        verify(storeAssetReviewService, never()).createStoreAssetReviews(anyLong(), anyString());
    }

}
