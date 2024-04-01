package com.walmart.realestate.crystal.storereview.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.walmart.core.realestate.cerberus.bean.CerberusUserInformation;
import com.walmart.realestate.crystal.storereview.client.amg.model.AmgNote;
import com.walmart.realestate.crystal.storereview.client.asset.model.Asset;
import com.walmart.realestate.crystal.storereview.client.estr.EstrClient;
import com.walmart.realestate.crystal.storereview.client.estr.model.EstrFact;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.command.CreateStoreReviewCommand;
import com.walmart.realestate.crystal.storereview.config.TestAsyncConfig;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewEntity;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.repository.StoreReviewRepository;
import com.walmart.realestate.crystal.storereview.repository.UserRepository;
import com.walmart.realestate.idn.model.Identifier;
import com.walmart.realestate.idn.model.IdentifiersContainer;
import com.walmart.realestate.idn.service.IdentifierOperationsService;
import com.walmart.realestate.soteria.model.User;
import com.walmart.realestate.soteria.model.UserContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StoreReviewService.class, StoreAssetReviewOrchestrationService.class, TestAsyncConfig.class, ThreadPoolTaskExecutor.class, TaskExecutorBuilder.class, RetryTemplate.class})
@AutoConfigureJson
@ActiveProfiles("test")
class StoreReviewWithAsyncAssetReviewTest {

    @Autowired
    private StoreReviewService storeReviewService;

    @MockBean
    private StoreReviewUserService storeReviewUserService;

    @MockBean
    private StoreAssetReviewService storeAssetReviewService;

    @MockBean
    private AssetMaintenanceService assetMaintenanceService;

    @MockBean
    private IdentifierOperationsService identifierOperationsService;

    @MockBean
    private AssetService assetService;

    @MockBean
    private UserAccountService userAccountService;

    @MockBean
    private StoreService storeService;

    @MockBean
    private StoreReviewRepository storeReviewRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EstrClient estrClient;

    @MockBean
    private HealthMetricsClient healthMetricsClient;

    @Test
    void testCreateStoreReview() {

        UserContext userContext = new UserContext(new CerberusUserInformation("user0", null, null, null, null, null), Set.of("reviewer"));

        when(storeReviewUserService.getReviewers(userContext)).thenReturn(List.of(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("Last")
                .build()));
        when(assetService.getAssetsForStore(192L)).thenReturn(Collections.singletonList(Asset.builder()
                .id(123L)
                .assetType("Rack")
                .build()));

        Identifier identifier = Identifier.builder()
                .key("HR-192")
                .value(11L)
                .build();
        IdentifiersContainer identifiersContainer = IdentifiersContainer.builder()
                .key("HR-192")
                .count(1)
                .start(11L)
                .end(11L)
                .identifiers(Collections.singletonList(identifier))
                .build();
        when(identifierOperationsService.generateIdentifier("HR-192")).thenReturn(identifiersContainer);
        when(assetMaintenanceService.getServiceModel(192L)).thenReturn(new AmgNote(192L, null, "sdm0", null));

        ObjectNode attributes = JsonNodeFactory.instance.objectNode()
                .put("@class", CreateStoreReviewCommand.class.getName())
                .put("storeReviewId", "HR-192-11")
                .put("storeNumber", 192L)
                .put("assignee", "user0")
                .put("sdm", "NA")
                .put("reviewType", "Health Review")
                .put("fmRegion", (String) null)
                .put("refrigerantType", "test")
                .put("startDate", (String) null);
        EstrFact estrFact = EstrFact.builder()
                .type("crystalStoreReview")
                .attributes(attributes)
                .build();
        UUID uuid = UUID.randomUUID();
        when(estrClient.createFact(estrFact)).thenReturn(EstrFact.builder()
                .id(uuid)
                .state("state0")
                .flow("flow0")
                .attributes(attributes)
                .build());
        when(estrClient.updateFactStatus(eq(uuid), anyString(), eq(EstrFact.builder().build())))
                .thenReturn(EstrFact.builder()
                        .id(uuid)
                        .build());

        StoreReview storeReview = storeReviewService.createStoreReview(StoreReview.builder()
                .storeNumber(192L)
                .assignee("user0")
                .refrigerantType("test")
                .sdm("NA")
                .reviewType("Health Review")
                .build(), userContext);

        assertThat(storeReview).isNotNull();
        assertThat(storeReview.getId()).isEqualTo("HR-192-11");
        assertThat(storeReview.getUuid()).isEqualTo(uuid);
        assertThat(storeReview.getStoreNumber()).isEqualTo(192L);
        assertThat(storeReview.getState()).isEqualTo("state0");
        assertThat(storeReview.getFlow()).isEqualTo("flow0");

        verify(storeReviewUserService).getReviewers(userContext);
        verify(estrClient).createFact(estrFact);
        verify(userAccountService, never()).getUser("user0");
    }

    @Test
    void testCreateStoreReviewWithException() {

        UserContext userContext = new UserContext(new CerberusUserInformation("user0", null, null, null, null, null), Set.of("reviewer"));

        when(storeReviewUserService.getReviewers(userContext)).thenReturn(List.of(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("Last")
                .build()));
        when(assetService.getAssetsForStore(192L)).thenReturn(Collections.singletonList(Asset.builder()
                .id(123L)
                .assetType("Rack")
                .build()));

        Identifier identifier = Identifier.builder()
                .key("HR-192")
                .value(11L)
                .build();
        IdentifiersContainer identifiersContainer = IdentifiersContainer.builder()
                .key("HR-192")
                .count(1)
                .start(11L)
                .end(11L)
                .identifiers(Collections.singletonList(identifier))
                .build();
        when(identifierOperationsService.generateIdentifier("HR-192")).thenReturn(identifiersContainer);
        when(assetMaintenanceService.getServiceModel(192L)).thenReturn(new AmgNote(192L, null, "sdm0", null));

        ObjectNode attributes = JsonNodeFactory.instance.objectNode()
                .put("@class", CreateStoreReviewCommand.class.getName())
                .put("storeReviewId", "HR-192-11")
                .put("storeNumber", 192L)
                .put("reviewType", "Health Review")
                .put("fmRegion", (String) null)
                .put("sdm", "NA")
                .put("refrigerantType", "test")
                .put("assignee", "user0")
                .put("startDate", (String) null);
        EstrFact estrFact = EstrFact.builder()
                .type("crystalStoreReview")
                .attributes(attributes)
                .build();
        UUID uuid = UUID.randomUUID();
        when(estrClient.createFact(estrFact)).thenReturn(EstrFact.builder()
                .id(uuid)
                .state("state0")
                .flow("flow0")
                .attributes(attributes)
                .build());
        when(estrClient.updateFactStatus(eq(uuid), anyString(), eq(EstrFact.builder().build())))
                .thenReturn(EstrFact.builder()
                        .id(uuid)
                        .build());

        StoreReview storeReview = storeReviewService.createStoreReview(StoreReview.builder()
                .storeNumber(192L)
                .assignee("user0")
                .refrigerantType("test")
                .sdm("NA")
                .reviewType("Health Review")
                .build(), userContext);

        assertThat(storeReview).isNotNull();
        assertThat(storeReview.getId()).isEqualTo("HR-192-11");
        assertThat(storeReview.getUuid()).isEqualTo(uuid);
        assertThat(storeReview.getStoreNumber()).isEqualTo(192L);
        assertThat(storeReview.getState()).isEqualTo("state0");
        assertThat(storeReview.getFlow()).isEqualTo("flow0");

        verify(storeReviewUserService).getReviewers(userContext);
        verify(estrClient).createFact(estrFact);
        verify(userAccountService, never()).getUser("user0");
    }

    @Test
    void testGetStoreReview() {
        UUID uuid = UUID.randomUUID();
        Instant now = Instant.now();
        when(storeReviewRepository.findById("SR-1")).thenReturn(Optional.of(StoreReviewEntity.builder()
                .id("SR-1")
                .uuid(uuid.toString())
                .storeNumber(876L)
                .assignee("user0")
                .assignedAt(now)
                .state("state0")
                .flow("flow0")
                .build()));

        when(userAccountService.getUser("user0")).thenReturn(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("McLast")
                .build());

        StoreReview storeReview = storeReviewService.getStoreReview("SR-1");

        assertThat(storeReview.getId()).isEqualTo("SR-1");
        assertThat(storeReview.getUuid()).isEqualTo(uuid);
        assertThat(storeReview.getStoreNumber()).isEqualTo(876L);
        assertThat(storeReview.getAssignee()).isEqualTo("user0");
        assertThat(storeReview.getAssigneeName()).isEqualTo("First McLast");
        assertThat(storeReview.getAssignedAt()).isEqualTo(now);
        assertThat(storeReview.getState()).isEqualTo("state0");
        assertThat(storeReview.getFlow()).isEqualTo("flow0");

        verify(storeReviewRepository).findById("SR-1");
        verify(userAccountService).getUser("user0");
    }

}
