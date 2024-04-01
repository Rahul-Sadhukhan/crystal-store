package com.walmart.realestate.crystal.storereview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.walmart.realestate.crystal.storereview.client.asset.model.Asset;
import com.walmart.realestate.crystal.storereview.client.estr.EstrClient;
import com.walmart.realestate.crystal.storereview.client.estr.model.EstrFact;
import com.walmart.realestate.crystal.storereview.client.estr.model.EstrTransition;
import com.walmart.realestate.crystal.storereview.client.estr.model.EstrWorkflow;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationCaseTimeInTarget;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationRackMetric;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationRackTimeInTarget;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationSensor;
import com.walmart.realestate.crystal.storereview.command.CreateStoreAssetReviewCommand;
import com.walmart.realestate.crystal.storereview.config.TestAsyncConfig;
import com.walmart.realestate.crystal.storereview.entity.StoreAssetReviewEntity;
import com.walmart.realestate.crystal.storereview.model.*;
import com.walmart.realestate.crystal.storereview.properties.StoreReviewRetryProperties;
import com.walmart.realestate.crystal.storereview.repository.StoreAssetReviewRepository;
import com.walmart.realestate.idn.model.Identifier;
import com.walmart.realestate.idn.model.IdentifiersContainer;
import com.walmart.realestate.idn.service.IdentifierOperationsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StoreAssetReviewService.class, ObjectMapper.class, TestAsyncConfig.class, ThreadPoolTaskExecutor.class, TaskExecutorBuilder.class, RetryTemplate.class, StoreReviewRetryProperties.class})
@ActiveProfiles("test")
class StoreAssetReviewServiceTest {

    @Autowired
    private StoreAssetReviewService storeAssetReviewService;

    @MockBean
    private IdentifierOperationsService identifierOperationsService;

    @MockBean
    private StoreAssetService storeAssetService;

    @MockBean
    private StoreReviewAssetService storeReviewAssetService;

    @MockBean
    private AssetService assetService;

    @MockBean
    private StoreAssetReviewRepository storeAssetReviewRepository;

    @MockBean
    private EstrClient estrClient;

    @MockBean
    private HealthMetricsClient healthMetricsClient;

    @Test
    void testCreateStoreAssetReviews() {
        List<RefrigerationSensor> testAssets = List.of(
                RefrigerationSensor.builder()
                        .id("87147")
                        .type("Rack")
                        .equipmentId(987147L)
                        .build(),
                RefrigerationSensor.builder()
                        .id("87148")
                        .type("Controller")
                        .equipmentId(987148L)
                        .build(),
                RefrigerationSensor.builder()
                        .id("87149")
                        .type("Controller")
                        .build());
        when(storeReviewAssetService.getAssetsForStore(2763L)).thenReturn(testAssets);

        List<Asset> assets = List.of(
                Asset.builder()
                        .id(87147L)
                        .assetType("Rack")
                        .dwEquipmentId(987147L)
                        .build(),
                Asset.builder()
                        .id(87148L)
                        .assetType("Controller")
                        .dwEquipmentId(987148L)
                        .build(),
                Asset.builder()
                        .id(87149L)
                        .assetType("Controller")
                        .dwEquipmentId(987149L)
                        .build());
        when(assetService.getAssetsForStore(2763L)).thenReturn(assets);

        List<Identifier> identifiers = Arrays.asList(
                Identifier.builder()
                        .key("SR-2763-1-SAR")
                        .value(1L)
                        .build(),
                Identifier.builder()
                        .key("SR-2763-1-SAR")
                        .value(2L)
                        .build(),
                Identifier.builder()
                        .key("SR-2763-1-SAR")
                        .value(3L)
                        .build());
        IdentifiersContainer identifiersContainer = IdentifiersContainer.builder()
                .key("SR-2763-1-SAR")
                .count(3)
                .start(10L)
                .end(12L)
                .identifiers(identifiers)
                .build();
        when(identifierOperationsService.generateIdentifiers("SR-2763-1-SAR", 3)).thenReturn(identifiersContainer);

        ObjectNode attributes1 = JsonNodeFactory.instance.objectNode()
                .put("@class", CreateStoreAssetReviewCommand.class.getName())
                .put("storeAssetReviewId", "SR-2763-1-SAR-1")
                .put("storeReviewId", "SR-2763-1")
                .put("storeNumber", 2763L)
                .put("assetId", 87147L)
                .put("assetMappingId", "87147")
                .put("assetType", "Rack");
        EstrFact estrFact1 = EstrFact.builder()
                .type("crystalStoreAssetReview")
                .attributes(attributes1)
                .build();
        UUID uuid1 = UUID.randomUUID();
        when(estrClient.createFact(estrFact1)).thenReturn(EstrFact.builder()
                .id(uuid1)
                .state("state0")
                .flow("flow0")
                .attributes(attributes1)
                .build());

        ObjectNode attributes2 = JsonNodeFactory.instance.objectNode()
                .put("@class", CreateStoreAssetReviewCommand.class.getName())
                .put("storeAssetReviewId", "SR-2763-1-SAR-2")
                .put("storeReviewId", "SR-2763-1")
                .put("storeNumber", 2763L)
                .put("assetId", 87148L)
                .put("assetMappingId", "87148")
                .put("assetType", "Controller");
        EstrFact estrFact2 = EstrFact.builder()
                .type("crystalStoreAssetReview")
                .attributes(attributes2)
                .build();
        UUID uuid2 = UUID.randomUUID();
        when(estrClient.createFact(estrFact2)).thenReturn(EstrFact.builder()
                .id(uuid2)
                .state("state0")
                .flow("flow0")
                .attributes(attributes2)
                .build());

        ObjectNode attributes3 = JsonNodeFactory.instance.objectNode()
                .put("@class", CreateStoreAssetReviewCommand.class.getName())
                .put("storeAssetReviewId", "SR-2763-1-SAR-3")
                .put("storeReviewId", "SR-2763-1")
                .put("storeNumber", 2763L)
                .put("assetId", (Long) null)
                .put("assetMappingId", "87149")
                .put("assetType", "Controller");
        EstrFact estrFact3 = EstrFact.builder()
                .type("crystalStoreAssetReview")
                .attributes(attributes3)
                .build();
        UUID uuid3 = UUID.randomUUID();
        when(estrClient.createFact(estrFact3)).thenReturn(EstrFact.builder()
                .id(uuid3)
                .state("state0")
                .flow("flow0")
                .attributes(attributes3)
                .build());

        List<StoreAssetReview> storeAssetReviews = storeAssetReviewService.createStoreAssetReviews(2763L, "SR-2763-1");

        assertThat(storeAssetReviews).isNotNull();
        assertThat(storeAssetReviews).hasSize(3);

        StoreAssetReview storeAssetReview1 = storeAssetReviews.get(0);
        assertThat(storeAssetReview1.getId()).isEqualTo("SR-2763-1-SAR-1");
        assertThat(storeAssetReview1.getUuid()).isEqualTo(uuid1);
        assertThat(storeAssetReview1.getAssetId()).isEqualTo(87147L);
        assertThat(storeAssetReview1.getAssetMappingId()).isEqualTo("87147");
        assertThat(storeAssetReview1.getState()).isEqualTo("state0");
        assertThat(storeAssetReview1.getFlow()).isEqualTo("flow0");

        StoreAssetReview storeAssetReview2 = storeAssetReviews.get(1);
        assertThat(storeAssetReview2.getId()).isEqualTo("SR-2763-1-SAR-2");
        assertThat(storeAssetReview2.getUuid()).isEqualTo(uuid2);
        assertThat(storeAssetReview2.getAssetId()).isEqualTo(87148L);
        assertThat(storeAssetReview2.getAssetMappingId()).isEqualTo("87148");
        assertThat(storeAssetReview2.getState()).isEqualTo("state0");
        assertThat(storeAssetReview2.getFlow()).isEqualTo("flow0");

        StoreAssetReview storeAssetReview3 = storeAssetReviews.get(2);
        assertThat(storeAssetReview3.getId()).isEqualTo("SR-2763-1-SAR-3");
        assertThat(storeAssetReview3.getUuid()).isEqualTo(uuid3);
        assertThat(storeAssetReview3.getAssetId()).isNull();
        assertThat(storeAssetReview3.getAssetMappingId()).isEqualTo("87149");
        assertThat(storeAssetReview3.getState()).isEqualTo("state0");
        assertThat(storeAssetReview3.getFlow()).isEqualTo("flow0");

        verify(storeAssetReviewRepository).findByStoreReviewId("SR-2763-1");
        verify(assetService).getAssetsForStore(2763L);
        verify(storeReviewAssetService).getAssetsForStore(2763L);
        verify(identifierOperationsService).generateIdentifiers("SR-2763-1-SAR", 3);
        verify(estrClient).createFact(estrFact1);
        verify(estrClient).createFact(estrFact2);
        verify(estrClient).createFact(estrFact3);
    }

    @Test
    void testCreateStoreAssetReviewsMultipleCalls() {
        List<RefrigerationSensor> testAssets1 = Arrays.asList(
                RefrigerationSensor.builder()
                        .id("87147")
                        .type("Rack")
                        .build(),
                RefrigerationSensor.builder()
                        .id("87148")
                        .type("Controller")
                        .build());
        List<RefrigerationSensor> testAssets2 = Collections.singletonList(
                RefrigerationSensor.builder()
                        .id("87149")
                        .type("Rack")
                        .build());
        when(storeReviewAssetService.getAssetsForStore(2763L)).thenReturn(testAssets1)
                .thenReturn(testAssets2);

        List<StoreAssetReviewEntity> testAssetReviews = Arrays.asList(
                StoreAssetReviewEntity.builder()
                        .assetMappingId("87147")
                        .build(),
                StoreAssetReviewEntity.builder()
                        .assetMappingId("87148")
                        .build());
        when(storeAssetReviewRepository.findByStoreReviewId("SR-2763-1")).thenReturn(Collections.emptyList())
                .thenReturn(testAssetReviews);

        List<Identifier> identifiers1 = Arrays.asList(
                Identifier.builder()
                        .key("SR-2763-1-SAR")
                        .value(1L)
                        .build(),
                Identifier.builder()
                        .key("SR-2763-1-SAR")
                        .value(2L)
                        .build());
        IdentifiersContainer identifiersContainer1 = IdentifiersContainer.builder()
                .key("SR-2763-1-SAR")
                .count(2)
                .start(1L)
                .end(2L)
                .identifiers(identifiers1)
                .build();
        when(identifierOperationsService.generateIdentifiers("SR-2763-1-SAR", 2)).thenReturn(identifiersContainer1);

        List<Identifier> identifiers2 = Collections.singletonList(
                Identifier.builder()
                        .key("SR-2763-1-SAR")
                        .value(3L)
                        .build());
        IdentifiersContainer identifiersContainer2 = IdentifiersContainer.builder()
                .key("SR-2763-1-SAR")
                .count(1)
                .start(3L)
                .end(3L)
                .identifiers(identifiers2)
                .build();
        when(identifierOperationsService.generateIdentifiers("SR-2763-1-SAR", 1)).thenReturn(identifiersContainer2);

        ObjectNode attributes1 = JsonNodeFactory.instance.objectNode()
                .put("@class", CreateStoreAssetReviewCommand.class.getName())
                .put("storeAssetReviewId", "SR-2763-1-SAR-1")
                .put("storeReviewId", "SR-2763-1")
                .put("storeNumber", 2763L)
                .put("assetId", (Long) null)
                .put("assetMappingId", "87147")
                .put("assetType", "Rack");
        ;
        EstrFact estrFact1 = EstrFact.builder()
                .type("crystalStoreAssetReview")
                .attributes(attributes1)
                .build();
        UUID uuid1 = UUID.randomUUID();
        when(estrClient.createFact(estrFact1)).thenReturn(EstrFact.builder()
                .id(uuid1)
                .state("state0")
                .flow("flow0")
                .attributes(attributes1)
                .build());

        ObjectNode attributes2 = JsonNodeFactory.instance.objectNode()
                .put("@class", CreateStoreAssetReviewCommand.class.getName())
                .put("storeAssetReviewId", "SR-2763-1-SAR-2")
                .put("storeReviewId", "SR-2763-1")
                .put("storeNumber", 2763L)
                .put("assetId", (Long) null)
                .put("assetMappingId", "87148")
                .put("assetType", "Controller");
        EstrFact estrFact2 = EstrFact.builder()
                .type("crystalStoreAssetReview")
                .attributes(attributes2)
                .build();
        UUID uuid2 = UUID.randomUUID();
        when(estrClient.createFact(estrFact2)).thenReturn(EstrFact.builder()
                .id(uuid2)
                .state("state0")
                .flow("flow0")
                .attributes(attributes2)
                .build());

        ObjectNode attributes3 = JsonNodeFactory.instance.objectNode()
                .put("@class", CreateStoreAssetReviewCommand.class.getName())
                .put("storeAssetReviewId", "SR-2763-1-SAR-3")
                .put("storeReviewId", "SR-2763-1")
                .put("storeNumber", 2763L)
                .put("assetId", (Long) null)
                .put("assetMappingId", "87149")
                .put("assetType", "Rack");
        EstrFact estrFact3 = EstrFact.builder()
                .type("crystalStoreAssetReview")
                .attributes(attributes3)
                .build();
        UUID uuid3 = UUID.randomUUID();
        when(estrClient.createFact(estrFact3)).thenReturn(EstrFact.builder()
                .id(uuid3)
                .state("state0")
                .flow("flow0")
                .attributes(attributes3)
                .build());

        List<StoreAssetReview> storeAssetReviews1 = storeAssetReviewService.createStoreAssetReviews(2763L, "SR-2763-1");

        assertThat(storeAssetReviews1).isNotNull();
        assertThat(storeAssetReviews1).hasSize(2);

        StoreAssetReview storeAssetReview1 = storeAssetReviews1.get(0);
        assertThat(storeAssetReview1.getId()).isEqualTo("SR-2763-1-SAR-1");
        assertThat(storeAssetReview1.getUuid()).isEqualTo(uuid1);
        assertThat(storeAssetReview1.getAssetId()).isNull();
        assertThat(storeAssetReview1.getAssetMappingId()).isEqualTo("87147");
        assertThat(storeAssetReview1.getState()).isEqualTo("state0");
        assertThat(storeAssetReview1.getFlow()).isEqualTo("flow0");

        StoreAssetReview storeAssetReview2 = storeAssetReviews1.get(1);
        assertThat(storeAssetReview2.getId()).isEqualTo("SR-2763-1-SAR-2");
        assertThat(storeAssetReview2.getUuid()).isEqualTo(uuid2);
        assertThat(storeAssetReview2.getAssetId()).isNull();
        assertThat(storeAssetReview2.getAssetMappingId()).isEqualTo("87148");
        assertThat(storeAssetReview2.getState()).isEqualTo("state0");
        assertThat(storeAssetReview2.getFlow()).isEqualTo("flow0");

        List<StoreAssetReview> storeAssetReviews2 = storeAssetReviewService.createStoreAssetReviews(2763L, "SR-2763-1");

        assertThat(storeAssetReviews2).isNotNull();
        assertThat(storeAssetReviews2).hasSize(1);

        StoreAssetReview storeAssetReview3 = storeAssetReviews2.get(0);
        assertThat(storeAssetReview3.getId()).isEqualTo("SR-2763-1-SAR-3");
        assertThat(storeAssetReview3.getUuid()).isEqualTo(uuid3);
        assertThat(storeAssetReview3.getAssetId()).isNull();
        assertThat(storeAssetReview3.getAssetMappingId()).isEqualTo("87149");
        assertThat(storeAssetReview3.getState()).isEqualTo("state0");
        assertThat(storeAssetReview3.getFlow()).isEqualTo("flow0");

        verify(storeAssetReviewRepository, times(2)).findByStoreReviewId("SR-2763-1");
        verify(assetService, times(2)).getAssetsForStore(2763L);
        verify(storeReviewAssetService, times(2)).getAssetsForStore(2763L);
        verify(identifierOperationsService).generateIdentifiers("SR-2763-1-SAR", 2);
        verify(identifierOperationsService).generateIdentifiers("SR-2763-1-SAR", 1);
        verify(estrClient).createFact(estrFact1);
        verify(estrClient).createFact(estrFact2);
        verify(estrClient).createFact(estrFact3);
    }

    @Test
    void testCreateStoreAssetReviewsMultipleCallsIdempotency() {
        List<RefrigerationSensor> testAssets = Arrays.asList(
                RefrigerationSensor.builder()
                        .id("87147")
                        .type("Rack")
                        .build(),
                RefrigerationSensor.builder()
                        .id("87148")
                        .type("Controller")
                        .build());
        when(storeReviewAssetService.getAssetsForStore(2763L)).thenReturn(testAssets);

        List<StoreAssetReviewEntity> testAssetReviews = Arrays.asList(
                StoreAssetReviewEntity.builder()
                        .assetMappingId("87147")
                        .build(),
                StoreAssetReviewEntity.builder()
                        .assetMappingId("87148")
                        .build());
        when(storeAssetReviewRepository.findByStoreReviewId("SR-1")).thenReturn(Collections.emptyList())
                .thenReturn(testAssetReviews);

        List<Identifier> identifiers = Arrays.asList(
                Identifier.builder()
                        .key("SR-1-SAR")
                        .value(1L)
                        .build(),
                Identifier.builder()
                        .key("SR-1-SAR")
                        .value(2L)
                        .build());
        IdentifiersContainer identifiersContainer = IdentifiersContainer.builder()
                .key("SR-1-SAR")
                .count(2)
                .start(1L)
                .end(2L)
                .identifiers(identifiers)
                .build();
        when(identifierOperationsService.generateIdentifiers("SR-1-SAR", 2)).thenReturn(identifiersContainer);

        ObjectNode attributes1 = JsonNodeFactory.instance.objectNode()
                .put("@class", CreateStoreAssetReviewCommand.class.getName())
                .put("storeAssetReviewId", "SR-1-SAR-1")
                .put("storeReviewId", "SR-1")
                .put("storeNumber", 2763L)
                .put("assetId", (Long) null)
                .put("assetMappingId", "87147")
                .put("assetType", "Rack");
        EstrFact estrFact1 = EstrFact.builder()
                .type("crystalStoreAssetReview")
                .attributes(attributes1)
                .build();
        UUID uuid1 = UUID.randomUUID();
        when(estrClient.createFact(estrFact1)).thenReturn(EstrFact.builder()
                .id(uuid1)
                .state("state0")
                .flow("flow0")
                .attributes(attributes1)
                .build());

        ObjectNode attributes2 = JsonNodeFactory.instance.objectNode()
                .put("@class", CreateStoreAssetReviewCommand.class.getName())
                .put("storeAssetReviewId", "SR-1-SAR-2")
                .put("storeReviewId", "SR-1")
                .put("storeNumber", 2763L)
                .put("assetId", (Long) null)
                .put("assetMappingId", "87148")
                .put("assetType", "Controller");
        ;
        EstrFact estrFact2 = EstrFact.builder()
                .type("crystalStoreAssetReview")
                .attributes(attributes2)
                .build();
        UUID uuid2 = UUID.randomUUID();
        when(estrClient.createFact(estrFact2)).thenReturn(EstrFact.builder()
                .id(uuid2)
                .state("state0")
                .flow("flow0")
                .attributes(attributes2)
                .build());

        List<StoreAssetReview> storeAssetReviews1 = storeAssetReviewService.createStoreAssetReviews(2763L, "SR-1");

        assertThat(storeAssetReviews1).isNotNull();
        assertThat(storeAssetReviews1).hasSize(2);

        StoreAssetReview storeAssetReview1 = storeAssetReviews1.get(0);
        assertThat(storeAssetReview1.getId()).isEqualTo("SR-1-SAR-1");
        assertThat(storeAssetReview1.getUuid()).isEqualTo(uuid1);
        assertThat(storeAssetReview1.getAssetId()).isNull();
        assertThat(storeAssetReview1.getAssetMappingId()).isEqualTo("87147");
        assertThat(storeAssetReview1.getState()).isEqualTo("state0");
        assertThat(storeAssetReview1.getFlow()).isEqualTo("flow0");

        StoreAssetReview storeAssetReview2 = storeAssetReviews1.get(1);
        assertThat(storeAssetReview2.getId()).isEqualTo("SR-1-SAR-2");
        assertThat(storeAssetReview2.getUuid()).isEqualTo(uuid2);
        assertThat(storeAssetReview2.getAssetId()).isNull();
        assertThat(storeAssetReview2.getAssetMappingId()).isEqualTo("87148");
        assertThat(storeAssetReview2.getState()).isEqualTo("state0");
        assertThat(storeAssetReview2.getFlow()).isEqualTo("flow0");

        List<StoreAssetReview> storeAssetReviews2 = storeAssetReviewService.createStoreAssetReviews(2763L, "SR-1");

        assertThat(storeAssetReviews2).isNotNull();
        assertThat(storeAssetReviews2).isEmpty();

        verify(storeAssetReviewRepository, times(2)).findByStoreReviewId("SR-1");
        verify(assetService).getAssetsForStore(2763L);
        verify(storeReviewAssetService, times(2)).getAssetsForStore(2763L);
        verify(identifierOperationsService).generateIdentifiers("SR-1-SAR", 2);
        verify(estrClient).createFact(estrFact1);
        verify(estrClient).createFact(estrFact2);
    }

    @Test
    void testGetStoreAssetReview() {
        UUID uuid = UUID.randomUUID();
        when(storeAssetReviewRepository.getOne("SAR-1")).thenReturn(StoreAssetReviewEntity.builder()
                .id("SAR-1")
                .uuid(uuid.toString())
                .storeReviewId("SR-1")
                .storeNumber(90834L)
                .state("state0")
                .flow("flow0")
                .build());

        StoreAssetReview storeAssetReview = storeAssetReviewService.getStoreAssetReview("SAR-1");

        assertThat(storeAssetReview).isNotNull();
        assertThat(storeAssetReview.getId()).isEqualTo("SAR-1");
        assertThat(storeAssetReview.getUuid()).isEqualTo(uuid);
        assertThat(storeAssetReview.getStoreReviewId()).isEqualTo("SR-1");
        assertThat(storeAssetReview.getStoreNumber()).isEqualTo(90834L);
        assertThat(storeAssetReview.getState()).isEqualTo("state0");
        assertThat(storeAssetReview.getFlow()).isEqualTo("flow0");

        verify(storeAssetReviewRepository).getOne("SAR-1");
    }

    @Test
    void testGetStoreAssetReviews() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        List<StoreAssetReviewEntity> testStoreAssetReviewEntities = Arrays.asList(
                StoreAssetReviewEntity.builder()
                        .id("SAR-1")
                        .uuid(uuid1.toString())
                        .storeReviewId("SR-1")
                        .storeNumber(26858L)
                        .state("state0")
                        .flow("flow0")
                        .build(),
                StoreAssetReviewEntity.builder()
                        .id("SAR-2")
                        .uuid(uuid2.toString())
                        .storeReviewId("SR-1")
                        .storeNumber(26123L)
                        .state("state0")
                        .flow("flow0")
                        .build());
        when(storeAssetReviewRepository.findByStoreReviewId("SR-1")).thenReturn(testStoreAssetReviewEntities);

        List<StoreAssetReview> storeAssetReviews = storeAssetReviewService.getStoreAssetReviews("SR-1");

        assertThat(storeAssetReviews).isNotNull();
        assertThat(storeAssetReviews).hasSize(2);

        StoreAssetReview storeAssetReview1 = storeAssetReviews.get(0);
        assertThat(storeAssetReview1.getId()).isEqualTo("SAR-1");
        assertThat(storeAssetReview1.getUuid()).isEqualTo(uuid1);
        assertThat(storeAssetReview1.getStoreReviewId()).isEqualTo("SR-1");
        assertThat(storeAssetReview1.getStoreNumber()).isEqualTo(26858L);
        assertThat(storeAssetReview1.getState()).isEqualTo("state0");
        assertThat(storeAssetReview1.getFlow()).isEqualTo("flow0");

        StoreAssetReview storeAssetReview2 = storeAssetReviews.get(1);
        assertThat(storeAssetReview2.getId()).isEqualTo("SAR-2");
        assertThat(storeAssetReview2.getUuid()).isEqualTo(uuid2);
        assertThat(storeAssetReview2.getStoreReviewId()).isEqualTo("SR-1");
        assertThat(storeAssetReview2.getStoreNumber()).isEqualTo(26123L);
        assertThat(storeAssetReview2.getState()).isEqualTo("state0");
        assertThat(storeAssetReview2.getFlow()).isEqualTo("flow0");

        verify(storeAssetReviewRepository).findByStoreReviewId("SR-1");
    }

    @Test
    void testUpdateAssetReviewStatus() {
        UUID uuid = UUID.randomUUID();
        when(storeAssetReviewRepository.findById("SAR-1"))
                .thenReturn(Optional.of(StoreAssetReviewEntity.builder()
                        .id("SAR-1")
                        .uuid(uuid.toString())
                        .storeReviewId("SR-1")
                        .storeNumber(334L)
                        .assetId(324672L)
                        .assetMappingId("324672")
                        .state("created")
                        .flow("flow")
                        .build()));

        ObjectNode attributes = JsonNodeFactory.instance.objectNode()
                .put("storeAssetReviewId", "SAR-1")
                .put("storeReviewId", "SR-1")
                .put("storeNumber", 334L)
                .put("assetId", 324672L)
                .put("assetMappingId", "324672");
        when(estrClient.updateFactStatus(uuid, "update", EstrFact.builder().build()))
                .thenReturn(EstrFact.builder()
                        .id(uuid)
                        .state("updated")
                        .flow("flow")
                        .attributes(attributes)
                        .build());

        StoreAssetReview storeAssetReview = storeAssetReviewService.updateStoreAssetReviewStatus("SAR-1", "update", null);

        assertThat(storeAssetReview).isNotNull();
        assertThat(storeAssetReview.getId()).isEqualTo("SAR-1");
        assertThat(storeAssetReview.getUuid()).isEqualTo(uuid);
        assertThat(storeAssetReview.getStoreReviewId()).isEqualTo("SR-1");
        assertThat(storeAssetReview.getStoreNumber()).isEqualTo(334L);
        assertThat(storeAssetReview.getAssetId()).isEqualTo(324672L);
        assertThat(storeAssetReview.getAssetMappingId()).isEqualTo("324672");
        assertThat(storeAssetReview.getState()).isEqualTo("updated");
        assertThat(storeAssetReview.getFlow()).isEqualTo("flow");

        verify(storeAssetReviewRepository).findById("SAR-1");
        verify(estrClient).updateFactStatus(uuid, "update", EstrFact.builder().build());
    }

    @Test
    void testGetStoreReviewWorkflow() {
        UUID uuid = UUID.randomUUID();
        when(storeAssetReviewRepository.getOne("SAR-1")).thenReturn(StoreAssetReviewEntity.builder()
                .id("SAR-1")
                .uuid(uuid.toString())
                .storeReviewId("SR-1")
                .storeNumber(4608L)
                .state("state0")
                .flow("flow0")
                .build());

        EstrWorkflow estrWorkflow = EstrWorkflow.builder()
                .nextAction(EstrTransition.builder()
                        .action("action0")
                        .build())
                .nextAction(EstrTransition.builder()
                        .action("action1")
                        .build())
                .build();
        when(estrClient.getWorkflow(uuid)).thenReturn(estrWorkflow);

        Workflow<StoreAssetReview> workflow = storeAssetReviewService.getStoreAssetReviewWorkflow("SAR-1");

        assertThat(workflow).isNotNull();
        assertThat(workflow.getEntity()).isNotNull();
        assertThat(workflow.getEntity().getId()).isEqualTo("SAR-1");
        assertThat(workflow.getEntity().getStoreReviewId()).isEqualTo("SR-1");
        assertThat(workflow.getEntity().getState()).isEqualTo("state0");
        assertThat(workflow.getEntity().getFlow()).isEqualTo("flow0");

        assertThat(workflow.getTransitions()).hasSize(2);
        assertThat(workflow.getTransitions().get(0).getAction()).isEqualTo("action0");
        assertThat(workflow.getTransitions().get(1).getAction()).isEqualTo("action1");

        verify(storeAssetReviewRepository).getOne("SAR-1");
        verify(estrClient).getWorkflow(uuid);
    }

    @Test
    void testGetStoreReviewProgress() {
        when(storeAssetReviewRepository.countByStoreReviewId("SR-1")).thenReturn(44);
        when(storeAssetReviewRepository.countByStoreReviewIdAndState("SR-1", "completed")).thenReturn(29);

        StoreReviewProgress storeReviewProgress = storeAssetReviewService.getStoreReviewProgress("SR-1");

        assertThat(storeReviewProgress).isNotNull();
        assertThat(storeReviewProgress.getStoreReviewId()).isEqualTo("SR-1");
        assertThat(storeReviewProgress.getTotal()).isEqualTo(44);
        assertThat(storeReviewProgress.getCompleted()).isEqualTo(29);

        verify(storeAssetReviewRepository).countByStoreReviewId("SR-1");
        verify(storeAssetReviewRepository).countByStoreReviewIdAndState("SR-1", "completed");
    }

    @Test
    void getAggregatedStoreAssetReviews() {

        when(storeAssetReviewRepository.findByStoreReviewId("SR-1-1")).thenReturn(List.of(
                StoreAssetReviewEntity.builder()
                        .storeReviewId("SR-1-1")
                        .assetId(101L)
                        .id("id0rack")
                        .assetMappingId("A-1")
                        .build(),
                StoreAssetReviewEntity.builder()
                        .storeReviewId("SR-1-1")
                        .id("id1satrack")
                        .assetMappingId("AS-1")
                        .build(),
                StoreAssetReviewEntity.builder()
                        .storeReviewId("SR-1-1")
                        .assetId(102L)
                        .id("id0case")
                        .assetMappingId("A|1|2|45")
                        .build(),
                StoreAssetReviewEntity.builder()
                        .storeReviewId("SR-1-1")
                        .id("id1case")
                        .assetMappingId("A|1|2|47")
                        .build()
        ));

        when(storeAssetService.getAssetHealthScore(1L)).thenReturn(List.of(
                        RefrigerationRackTimeInTarget.builder()
                                .assetMappingId("A-1")
                                .timeInTarget(12.0)
                                .build(),
                        RefrigerationRackTimeInTarget.builder()
                                .assetMappingId("AS-1")
                                .timeInTarget(13.0)
                                .build(),
                        RefrigerationCaseTimeInTarget.builder()
                                .assetMappingId("A|1|2|45")
                                .timeInTarget(14.0)
                                .build(),
                        RefrigerationCaseTimeInTarget.builder()
                                .assetMappingId("A|1|2|47")
                                .timeInTarget(15.0)
                                .build()
                )
        );

        when(healthMetricsClient.getRefrigerationRackMetricByStore(1L)).thenReturn(List.of(
                RefrigerationRackMetric.builder()
                        .rackCallLetter("A")
                        .storeNumber(1L)
                        .suctionPressureScore(10.0)
                        .superHeatScore(11.0)
                        .build(),
                RefrigerationRackMetric.builder()
                        .rackCallLetter("AS")
                        .storeNumber(1L)
                        .suctionPressureScore(15.0)
                        .superHeatScore(16.0)
                        .build()
        ));

        when(storeReviewAssetService.getAssetsForStore(1L)).thenReturn(List.of(
                RefrigerationSensor.builder()
                        .id("A-1")
                        .rackCallLetter("A")
                        .storeNumber("1")
                        .build(),
                RefrigerationSensor.builder()
                        .id("AS-1")
                        .rackCallLetter("AS")
                        .storeNumber("1")
                        .build(),
                RefrigerationSensor.builder()
                        .id("A|1|2|45")
                        .rackCallLetter("A")
                        .storeNumber("1")
                        .build(),
                RefrigerationSensor.builder()
                        .id("A|1|2|47")
                        .rackCallLetter("A")
                        .storeNumber("1")
                        .build()
        ));

        when(assetService.getAssetsForStore(1L, null)).thenReturn(List.of(
                Asset.builder()
                        .id(101L)
                        .tagId("tag0")
                        .assetType("RACK")
                        .active(true)
                        .build(),
                Asset.builder()
                        .id(102L)
                        .tagId("tag0")
                        .assetType("CASE")
                        .active(true)
                        .build()
        ));


        List<AggregatedStoreAssetReview> actual = storeAssetReviewService.getAggregatedStoreAssetReviews(1L, "SR-1-1", null);

        assertThat(actual.size()).isEqualTo(4);

        assertThat(actual.get(0).getAsset().getId()).isEqualTo(101L);
        assertThat(actual.get(0).getAsset().getTagId()).isEqualTo("tag0");
        assertThat(actual.get(0).getAsset().getAssetType()).isEqualTo("RACK");
        assertThat(actual.get(0).getAsset().getActive()).isEqualTo(true);

        assertThat(actual.get(1).getStoreAssetReview().getStoreReviewId()).isEqualTo("SR-1-1");
        assertThat(actual.get(1).getStoreAssetReview().getId()).isEqualTo("id1satrack");
        assertThat(actual.get(1).getStoreAssetReview().getAssetMappingId()).isEqualTo("AS-1");


        assertThat(actual.get(2).getRefrigerationSensor().getId()).isEqualTo("A|1|2|45");
        assertThat(actual.get(2).getRefrigerationSensor().getRackCallLetter()).isEqualTo("A");
        assertThat(actual.get(2).getRefrigerationSensor().getStoreNumber()).isEqualTo("1");

        assertThat(actual.get(3).getRefrigerationAssetTimeInTarget().getAssetMappingId()).isEqualTo("A|1|2|47");
        assertThat(actual.get(3).getRefrigerationAssetTimeInTarget().getTimeInTarget()).isEqualTo(15.0);


        assertThat(actual.get(0).getRefrigerationRackMetric().getRackCallLetter()).isEqualTo("A");
        assertThat(actual.get(0).getRefrigerationRackMetric().getStoreNumber()).isEqualTo(1L);
        assertThat(actual.get(0).getRefrigerationRackMetric().getSuctionPressureScore()).isEqualTo(10.0);
        assertThat(actual.get(0).getRefrigerationRackMetric().getSuperHeatScore()).isEqualTo(11.0);

        verify(storeAssetReviewRepository).findByStoreReviewId("SR-1-1");

        verify(storeAssetService).getAssetHealthScore(1L);

        verify(healthMetricsClient).getRefrigerationRackMetricByStore(1L);

        verify(storeReviewAssetService).getAssetsForStore(1L);

        verify(assetService).getAssetsForStore(1L, null);

    }

    @Test
    void testUpdateAssetReviewsStatus() {
        List<UUID> uuidList = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        when(storeAssetReviewRepository.findAllById(List.of("SAR-1", "SAR-2", "SAR-3")))
                .thenReturn(List.of(StoreAssetReviewEntity.builder()
                                .id("SAR-1")
                                .uuid(uuidList.get(0).toString())
                                .storeReviewId("SR-1")
                                .storeNumber(334L)
                                .assetId(324672L)
                                .assetMappingId("324672")
                                .state("created")
                                .flow("flow")
                                .build(),
                        StoreAssetReviewEntity.builder()
                                .id("SAR-2")
                                .uuid(uuidList.get(1).toString())
                                .storeReviewId("SR-1")
                                .storeNumber(334L)
                                .assetId(324673L)
                                .assetMappingId("324673")
                                .state("created")
                                .flow("flow")
                                .build(),
                        StoreAssetReviewEntity.builder()
                                .id("SAR-3")
                                .uuid(uuidList.get(2).toString())
                                .storeReviewId("SR-1")
                                .storeNumber(334L)
                                .assetId(324674L)
                                .assetMappingId("324674")
                                .state("created")
                                .flow("flow")
                                .build()));


        when(estrClient.updateFactsStatus(eq("complete"), any(UpdateStoreAssetReviewStatusEstrFact.class)))
                .thenReturn(List.of(EstrFact.builder()
                                .id(uuidList.get(0))
                                .state("completed")
                                .flow("flow")
                                .build(),
                        EstrFact.builder()
                                .id(uuidList.get(1))
                                .state("completed")
                                .flow("flow")
                                .build(),
                        EstrFact.builder()
                                .id(uuidList.get(2))
                                .state("completed")
                                .flow("flow")
                                .build()));

        List<StoreAssetReview> storeAssetReviewList = storeAssetReviewService.updateStoreAssetReviewsStatus(List.of("SAR-1", "SAR-2", "SAR-3"), "complete");

        assertThat(storeAssetReviewList).isNotNull();
        assertThat(storeAssetReviewList.size()).isEqualTo(3);
        assertThat(storeAssetReviewList.get(0).getUuid()).isEqualTo(uuidList.get(0));
        assertThat(storeAssetReviewList.get(1).getStoreReviewId()).isEqualTo("SR-1");
        assertThat(storeAssetReviewList.get(2).getStoreNumber()).isEqualTo(334L);
        assertThat(storeAssetReviewList.get(0).getAssetId()).isEqualTo(324672L);
        assertThat(storeAssetReviewList.get(0).getAssetMappingId()).isEqualTo("324672");
        assertThat(storeAssetReviewList.get(1).getState()).isEqualTo("completed");
        assertThat(storeAssetReviewList.get(1).getFlow()).isEqualTo("flow");

        verify(storeAssetReviewRepository).findAllById(List.of("SAR-1", "SAR-2", "SAR-3"));

        ArgumentCaptor<UpdateStoreAssetReviewStatusEstrFact> argumentCaptor = ArgumentCaptor.forClass(UpdateStoreAssetReviewStatusEstrFact.class);
        ArgumentCaptor<String> argumentCaptorString = ArgumentCaptor.forClass(String.class);

        verify(estrClient).updateFactsStatus(argumentCaptorString.capture(), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getIdList()).isEqualTo(uuidList);
        assertThat(argumentCaptorString.getValue()).isEqualTo("complete");

    }

}
