package com.walmart.realestate.crystal.storereview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.walmart.core.realestate.cerberus.bean.CerberusUserInformation;
import com.walmart.realestate.crystal.storereview.client.amg.model.AmgNote;
import com.walmart.realestate.crystal.storereview.client.estr.EstrClient;
import com.walmart.realestate.crystal.storereview.client.estr.model.*;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.command.AssignStoreReviewCommand;
import com.walmart.realestate.crystal.storereview.command.CancelStoreReviewCommand;
import com.walmart.realestate.crystal.storereview.command.CreateStoreReviewCommand;
import com.walmart.realestate.crystal.storereview.command.UpdateStoreReviewCommand;
import com.walmart.realestate.crystal.storereview.config.PropertiesConfig;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewEntity;
import com.walmart.realestate.crystal.storereview.entity.UserAccountEntity;
import com.walmart.realestate.crystal.storereview.model.*;
import com.walmart.realestate.crystal.storereview.properties.StoreReviewProperties;
import com.walmart.realestate.crystal.storereview.repository.StoreReviewRepository;
import com.walmart.realestate.crystal.storereview.repository.UserRepository;
import com.walmart.realestate.idn.model.Identifier;
import com.walmart.realestate.idn.model.IdentifiersContainer;
import com.walmart.realestate.idn.service.IdentifierOperationsService;
import com.walmart.realestate.soteria.model.User;
import com.walmart.realestate.soteria.model.UserContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@EnableRetry
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StoreReviewService.class, PropertiesConfig.class, StoreReviewProperties.class, RetryTemplate.class})
@AutoConfigureJson
@ActiveProfiles("test")
class StoreReviewServiceTest {

    @Autowired
    private StoreReviewService storeReviewService;

    @MockBean
    private StoreReviewUserService storeReviewUserService;

    @MockBean
    private AssetMaintenanceService assetMaintenanceService;

    @MockBean
    private IdentifierOperationsService identifierOperationsService;

    @MockBean
    private StoreAssetReviewOrchestrationService storeAssetReviewOrchestrationService;

    @MockBean
    private UserAccountService userAccountService;

    @MockBean
    private StoreReviewRepository storeReviewRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private StoreService storeService;

    @MockBean
    private EstrClient estrClient;

    @MockBean
    private HealthMetricsClient healthMetricsClient;

    @Test
    void testCreateStoreReview() {

        UserContext userContext = new UserContext(new CerberusUserInformation("user0", null, null, null, null, null), Set.of("reviewer"));
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

        when(storeReviewUserService.getReviewers(userContext)).thenReturn(List.of(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("Last")
                .build()));
        when(identifierOperationsService.generateIdentifier("HR-192")).thenReturn(identifiersContainer);
        when(assetMaintenanceService.getServiceModel(192L)).thenReturn(new AmgNote(192L, null, "sdm0", null));

        ObjectNode attributes = instance.objectNode()
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

        List<StoreAssetReview> storeAssetReviews = Arrays.asList(
                StoreAssetReview.builder().build(),
                StoreAssetReview.builder().build());

        when(storeAssetReviewOrchestrationService.createStoreAssetReviews(uuid, 192L, "HR-11"))
                .thenReturn(CompletableFuture.completedFuture(storeAssetReviews));

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
        verify(identifierOperationsService).generateIdentifier("HR-192");
        verify(estrClient).createFact(estrFact);
    }

    @Test
    void testCreateStoreReviewWithStartDate() {

        UserContext userContext = new UserContext(new CerberusUserInformation("user0", null, null, null, null, null), Set.of("reviewer"));
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

        when(storeReviewUserService.getReviewers(userContext)).thenReturn(List.of(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("Last")
                .build()));
        when(identifierOperationsService.generateIdentifier("HR-192")).thenReturn(identifiersContainer);
        when(assetMaintenanceService.getServiceModel(192L)).thenReturn(new AmgNote(192L, null, "sdm0", null));

        ObjectNode attributes = instance.objectNode()
                .put("@class", CreateStoreReviewCommand.class.getName())
                .put("storeReviewId", "HR-192-11")
                .put("storeNumber", 192L)
                .put("reviewType", "Health Review")
                .put("fmRegion", (String) null)
                .put("refrigerantType", "test")
                .put("sdm", "NA")
                .put("assignee", "user0")
                .put("startDate", "2021-08-23T09:30:00");
        EstrFact estrFact = EstrFact.builder()
                .type("crystalStoreReview")
                .attributes(attributes)
                .build();
        UUID uuid = UUID.randomUUID();
        when(estrClient.createFact(estrFact)).thenReturn(EstrFact.builder()
                .id(uuid)
                .state("scheduled")
                .flow("flow0")
                .attributes(attributes)
                .build());

        List<StoreAssetReview> storeAssetReviews = Arrays.asList(
                StoreAssetReview.builder().build(),
                StoreAssetReview.builder().build());

        when(storeAssetReviewOrchestrationService.createStoreAssetReviews(uuid, 192L, "HR-192-11"))
                .thenReturn(CompletableFuture.completedFuture(storeAssetReviews));

        StoreReview storeReview = storeReviewService.createStoreReview(StoreReview.builder()
                .storeNumber(192L)
                .assignee("user0")
                .refrigerantType("test")
                .sdm("NA")
                .reviewType("Health Review")
                .startDate(LocalDateTime.of(2021, 8, 23, 9, 30))
                .build(), userContext);

        assertThat(storeReview).isNotNull();
        assertThat(storeReview.getId()).isEqualTo("HR-192-11");
        assertThat(storeReview.getUuid()).isEqualTo(uuid);
        assertThat(storeReview.getStoreNumber()).isEqualTo(192L);
        assertThat(storeReview.getState()).isEqualTo("scheduled");
        assertThat(storeReview.getFlow()).isEqualTo("flow0");

        verify(storeReviewUserService).getReviewers(userContext);
        verify(identifierOperationsService).generateIdentifier("HR-192");
        verify(estrClient).createFact(estrFact);

        verify(storeAssetReviewOrchestrationService, never()).createStoreAssetReviews(uuid, 192L, "HR-192-11");
    }

    @Test
    void testUpdateStoreReview() {
        UUID uuid = UUID.randomUUID();
        when(storeReviewRepository.findById("SR-1")).thenReturn(Optional.of(StoreReviewEntity.builder()
                .id("SR-1")
                .uuid(uuid.toString())
                .storeNumber(876L)
                .assignee("user0")
                .state("state0")
                .flow("flow0")
                .build()));

        ObjectNode attributes = instance.objectNode()
                .put("@class", UpdateStoreReviewCommand.class.getName())
                .put("storeReviewId", "SR-1")
                .put("assignee", "user1")
                .put("startDate", (String) null);
        EstrFact estrFact = EstrFact.builder()
                .attributes(attributes)
                .build();
        when(estrClient.updateFactStatus(uuid, "update", estrFact)).thenReturn(EstrFact.builder()
                .id(uuid)
                .state("state1")
                .flow("flow1")
                .build());

        StoreReview storeReviewUpdate = StoreReview.builder()
                .id("SR-1")
                .storeNumber(876L)
                .assignee("user1")
                .build();

        StoreReview storeReview = storeReviewService.updateStoreReview("SR-1", storeReviewUpdate);

        assertThat(storeReview).isNotNull();
        assertThat(storeReview.getId()).isEqualTo("SR-1");
        assertThat(storeReview.getUuid()).isEqualTo(uuid);
        assertThat(storeReview.getStoreNumber()).isEqualTo(876L);
        assertThat(storeReview.getState()).isEqualTo("state1");
        assertThat(storeReview.getFlow()).isEqualTo("flow1");

        verify(storeReviewRepository).findById("SR-1");
        verify(estrClient).updateFactStatus(uuid, "update", estrFact);
    }

    @Test
    void testUpdateStoreReviewWithStartDate() {
        UUID uuid = UUID.randomUUID();
        when(storeReviewRepository.findById("SR-1")).thenReturn(Optional.of(StoreReviewEntity.builder()
                .id("SR-1")
                .uuid(uuid.toString())
                .storeNumber(876L)
                .assignee("user0")
                .state("scheduled")
                .flow("flow0")
                .build()));

        ObjectNode attributes = instance.objectNode()
                .put("@class", UpdateStoreReviewCommand.class.getName())
                .put("storeReviewId", "SR-1")
                .put("assignee", "user1")
                .put("startDate", "2021-08-23T09:30:00");
        EstrFact estrFact = EstrFact.builder()
                .attributes(attributes)
                .build();
        when(estrClient.updateFactStatus(uuid, "update", estrFact)).thenReturn(EstrFact.builder()
                .id(uuid)
                .state("scheduled")
                .flow("flow1")
                .build());

        StoreReview storeReviewUpdate = StoreReview.builder()
                .id("SR-1")
                .storeNumber(876L)
                .assignee("user1")
                .startDate(LocalDateTime.of(2021, 8, 23, 9, 30))
                .build();

        StoreReview storeReview = storeReviewService.updateStoreReview("SR-1", storeReviewUpdate);

        assertThat(storeReview).isNotNull();
        assertThat(storeReview.getId()).isEqualTo("SR-1");
        assertThat(storeReview.getUuid()).isEqualTo(uuid);
        assertThat(storeReview.getStoreNumber()).isEqualTo(876L);
        assertThat(storeReview.getState()).isEqualTo("scheduled");
        assertThat(storeReview.getFlow()).isEqualTo("flow1");

        verify(storeReviewRepository).findById("SR-1");
        verify(estrClient).updateFactStatus(uuid, "update", estrFact);
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

    @Test
    void testGetStoreReviewUnknownUser() {
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

        when(userAccountService.getUser("user0")).thenThrow(NoSuchElementException.class);

        StoreReview storeReview = storeReviewService.getStoreReview("SR-1");

        assertThat(storeReview.getId()).isEqualTo("SR-1");
        assertThat(storeReview.getUuid()).isEqualTo(uuid);
        assertThat(storeReview.getStoreNumber()).isEqualTo(876L);
        assertThat(storeReview.getAssignee()).isEqualTo("user0");
        assertThat(storeReview.getAssigneeName()).isNull();
        assertThat(storeReview.getAssignedAt()).isEqualTo(now);
        assertThat(storeReview.getState()).isEqualTo("state0");
        assertThat(storeReview.getFlow()).isEqualTo("flow0");

        verify(storeReviewRepository).findById("SR-1");
        verify(userAccountService).getUser("user0");
    }

    @Test
    void testGetStoreReviewWithAssetReviews() {
        UUID uuid = UUID.randomUUID();
        when(storeReviewRepository.findById("SR-1")).thenReturn(Optional.of(StoreReviewEntity.builder()
                .id("SR-1")
                .uuid(uuid.toString())
                .storeNumber(876L)
                .assignee("user0")
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
        assertThat(storeReview.getState()).isEqualTo("state0");
        assertThat(storeReview.getFlow()).isEqualTo("flow0");

        verify(storeReviewRepository).findById("SR-1");
        verify(userAccountService).getUser("user0");
    }

    @Test
    void testGetStoreReviewByAggregateId() {
        UUID uuid = UUID.randomUUID();
        Instant now = Instant.now();
        when(storeReviewRepository.findByUuid(uuid.toString())).thenReturn(StoreReviewEntity.builder()
                .id("SR-1")
                .uuid(uuid.toString())
                .storeNumber(876L)
                .assignee("user0")
                .assignedAt(now)
                .state("state0")
                .flow("flow0")
                .build());

        when(userAccountService.getUser("user0")).thenReturn(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("McLast")
                .build());

        StoreReview storeReview = storeReviewService.getStoreReviewByAggregateId(uuid.toString());

        assertThat(storeReview.getId()).isEqualTo("SR-1");
        assertThat(storeReview.getUuid()).isEqualTo(uuid);
        assertThat(storeReview.getStoreNumber()).isEqualTo(876L);
        assertThat(storeReview.getAssignee()).isEqualTo("user0");
        assertThat(storeReview.getAssigneeName()).isEqualTo("First McLast");
        assertThat(storeReview.getAssignedAt()).isEqualTo(now);
        assertThat(storeReview.getState()).isEqualTo("state0");
        assertThat(storeReview.getFlow()).isEqualTo("flow0");

        verify(storeReviewRepository).findByUuid(uuid.toString());
        verify(userAccountService).getUser("user0");
    }

    @Test
    void testGetStoreReviews() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();
        Instant now = Instant.now();
        Page<StoreReviewEntity> testStoreReviewEntities = new PageImpl<>(Arrays.asList(
                StoreReviewEntity.builder()
                        .id("SR-1")
                        .uuid(uuid1.toString())
                        .storeNumber(8762L)
                        .assignee("user0")
                        .state("state0")
                        .flow("flow0")
                        .build(),
                StoreReviewEntity.builder()
                        .id("SR-2")
                        .uuid(uuid2.toString())
                        .storeNumber(8763L)
                        .assignee("user0")
                        .assignedAt(now.minusSeconds(600))
                        .state("state0")
                        .flow("flow0")
                        .build(),
                StoreReviewEntity.builder()
                        .id("SR-3")
                        .uuid(uuid3.toString())
                        .storeNumber(8764L)
                        .assignee("user0")
                        .assignedAt(now)
                        .state("state0")
                        .flow("flow0")
                        .build()));
        when(storeReviewRepository.findAll(ArgumentMatchers.<Specification<StoreReviewEntity>>any(), eq(Pageable.unpaged()))).thenReturn(testStoreReviewEntities);

        when(userAccountService.getUser("user0")).thenReturn(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("McLast")
                .build());

        StoreReviewFilters filters = StoreReviewFilters.builder().build();

        Page<StoreReview> storeReviewsPage = storeReviewService.getStoreReviews(Pageable.unpaged(), filters);

        assertThat(storeReviewsPage).hasSize(3);

        List<StoreReview> storeReviews = storeReviewsPage.toList();

        StoreReview storeReview1 = storeReviews.get(0);
        assertThat(storeReview1.getId()).isEqualTo("SR-1");
        assertThat(storeReview1.getUuid()).isEqualTo(uuid1);
        assertThat(storeReview1.getStoreNumber()).isEqualTo(8762L);
        assertThat(storeReview1.getAssignee()).isEqualTo("user0");
        assertThat(storeReview1.getAssignedAt()).isNull();
        assertThat(storeReview1.getState()).isEqualTo("state0");
        assertThat(storeReview1.getFlow()).isEqualTo("flow0");

        StoreReview storeReview2 = storeReviews.get(1);
        assertThat(storeReview2.getId()).isEqualTo("SR-2");
        assertThat(storeReview2.getUuid()).isEqualTo(uuid2);
        assertThat(storeReview2.getStoreNumber()).isEqualTo(8763L);
        assertThat(storeReview2.getAssignee()).isEqualTo("user0");
        assertThat(storeReview2.getAssignedAt()).isEqualTo(now.minusSeconds(600));
        assertThat(storeReview2.getState()).isEqualTo("state0");
        assertThat(storeReview2.getFlow()).isEqualTo("flow0");

        StoreReview storeReview3 = storeReviews.get(2);
        assertThat(storeReview3.getId()).isEqualTo("SR-3");
        assertThat(storeReview3.getUuid()).isEqualTo(uuid3);
        assertThat(storeReview3.getStoreNumber()).isEqualTo(8764L);
        assertThat(storeReview3.getAssignee()).isEqualTo("user0");
        assertThat(storeReview3.getAssignedAt()).isEqualTo(now);
        assertThat(storeReview3.getState()).isEqualTo("state0");
        assertThat(storeReview3.getFlow()).isEqualTo("flow0");

        verify(storeReviewRepository).findAll(ArgumentMatchers.<Specification<StoreReviewEntity>>any(), eq(Pageable.unpaged()));
        verify(userAccountService, times(3)).getUser("user0");
    }


    @Test
    void testGetStoreReviewsForStoreReviewIds() {

        List<StoreReviewEntity> storeReviewEntities = Arrays.asList(
                StoreReviewEntity.builder()
                        .id("SR-1")
                        .storeNumber(8764L)
                        .assignee("user0")
                        .state("state0")
                        .build(),
                StoreReviewEntity.builder()
                        .id("SR-2")
                        .storeNumber(8764L)
                        .assignee("user0")
                        .state("state0")
                        .build(),
                StoreReviewEntity.builder()
                        .id("SR-3")
                        .storeNumber(8764L)
                        .assignee("user0")
                        .state("state0")
                        .build());

        when(storeReviewRepository.findByIdIn(anyList())).thenReturn(storeReviewEntities);
        List<StoreReview> storeReviews = storeReviewService.getStoreReviews(Arrays.asList("SR-1", "SR-2", "SR-3"));

        assertThat(storeReviews).hasSize(3);


        StoreReview storeReview1 = storeReviews.get(0);
        assertThat(storeReview1.getId()).isEqualTo("SR-1");
        assertThat(storeReview1.getStoreNumber()).isEqualTo(8764L);
        assertThat(storeReview1.getState()).isEqualTo("state0");

        StoreReview storeReview2 = storeReviews.get(1);
        assertThat(storeReview2.getId()).isEqualTo("SR-2");
        assertThat(storeReview2.getStoreNumber()).isEqualTo(8764L);
        assertThat(storeReview2.getState()).isEqualTo("state0");

        StoreReview storeReview3 = storeReviews.get(2);
        assertThat(storeReview3.getId()).isEqualTo("SR-3");
        assertThat(storeReview3.getStoreNumber()).isEqualTo(8764L);
        assertThat(storeReview3.getAssignee()).isEqualTo("user0");

        verify(storeReviewRepository).findByIdIn(Arrays.asList("SR-1", "SR-2", "SR-3"));

    }

    @Test
    void testGetStoreReviewsAssignee() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        Page<StoreReviewEntity> testStoreReviewEntities = new PageImpl<>(Arrays.asList(
                StoreReviewEntity.builder()
                        .id("SR-1")
                        .uuid(uuid1.toString())
                        .storeNumber(8762L)
                        .assignee("user0")
                        .state("state0")
                        .flow("flow0")
                        .build(),
                StoreReviewEntity.builder()
                        .id("SR-2")
                        .uuid(uuid2.toString())
                        .storeNumber(8763L)
                        .assignee("user0")
                        .state("state0")
                        .flow("flow0")
                        .build()));
        when(storeReviewRepository.findAll(ArgumentMatchers.<Specification<StoreReviewEntity>>any(), eq(Pageable.unpaged()))).thenReturn(testStoreReviewEntities);

        when(userAccountService.getUser("user0")).thenReturn(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("McLast")
                .build());

        StoreReviewFilters filters = StoreReviewFilters.builder().build();

        Page<StoreReview> storeReviewsPage = storeReviewService.getStoreReviews(Pageable.unpaged(), filters);

        assertThat(storeReviewsPage).hasSize(2);

        List<StoreReview> storeReviews = storeReviewsPage.toList();

        StoreReview storeReview1 = storeReviews.get(0);
        assertThat(storeReview1.getId()).isEqualTo("SR-1");
        assertThat(storeReview1.getUuid()).isEqualTo(uuid1);
        assertThat(storeReview1.getStoreNumber()).isEqualTo(8762L);
        assertThat(storeReview1.getAssignee()).isEqualTo("user0");
        assertThat(storeReview1.getState()).isEqualTo("state0");
        assertThat(storeReview1.getFlow()).isEqualTo("flow0");

        StoreReview storeReview2 = storeReviews.get(1);
        assertThat(storeReview2.getId()).isEqualTo("SR-2");
        assertThat(storeReview2.getUuid()).isEqualTo(uuid2);
        assertThat(storeReview2.getStoreNumber()).isEqualTo(8763L);
        assertThat(storeReview2.getAssignee()).isEqualTo("user0");
        assertThat(storeReview2.getState()).isEqualTo("state0");
        assertThat(storeReview2.getFlow()).isEqualTo("flow0");

        verify(storeReviewRepository).findAll(ArgumentMatchers.<Specification<StoreReviewEntity>>any(), eq(Pageable.unpaged()));
        verify(userAccountService, times(2)).getUser("user0");
    }

    @Test
    void testGetStoreReviewsCreatedBy() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        Page<StoreReviewEntity> testStoreReviewEntities = new PageImpl<>(Arrays.asList(
                StoreReviewEntity.builder()
                        .id("SR-1")
                        .uuid(uuid1.toString())
                        .storeNumber(8762L)
                        .assignee("user0")
                        .state("state0")
                        .flow("flow0")
                        .build(),
                StoreReviewEntity.builder()
                        .id("SR-2")
                        .uuid(uuid2.toString())
                        .storeNumber(8763L)
                        .assignee("user0")
                        .state("state0")
                        .flow("flow0")
                        .build()));
        when(storeReviewRepository.findAll(ArgumentMatchers.<Specification<StoreReviewEntity>>any(), eq(Pageable.unpaged()))).thenReturn(testStoreReviewEntities);

        when(userAccountService.getUser("user0")).thenReturn(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("McLast")
                .build());

        StoreReviewFilters filters = StoreReviewFilters.builder()
                .createdBy(List.of("user0"))
                .build();

        when(userRepository.findTopByFullName("user0")).thenReturn(UserAccountEntity.builder()
                .firstName("user0")
                .userId("user0")
                .build());

        Page<StoreReview> storeReviewsPage = storeReviewService.getStoreReviews(Pageable.unpaged(), filters);

        assertThat(storeReviewsPage).hasSize(2);

        List<StoreReview> storeReviews = storeReviewsPage.toList();

        StoreReview storeReview1 = storeReviews.get(0);
        assertThat(storeReview1.getId()).isEqualTo("SR-1");
        assertThat(storeReview1.getUuid()).isEqualTo(uuid1);
        assertThat(storeReview1.getStoreNumber()).isEqualTo(8762L);
        assertThat(storeReview1.getAssignee()).isEqualTo("user0");
        assertThat(storeReview1.getState()).isEqualTo("state0");
        assertThat(storeReview1.getFlow()).isEqualTo("flow0");

        StoreReview storeReview2 = storeReviews.get(1);
        assertThat(storeReview2.getId()).isEqualTo("SR-2");
        assertThat(storeReview2.getUuid()).isEqualTo(uuid2);
        assertThat(storeReview2.getStoreNumber()).isEqualTo(8763L);
        assertThat(storeReview2.getAssignee()).isEqualTo("user0");
        assertThat(storeReview2.getState()).isEqualTo("state0");
        assertThat(storeReview2.getFlow()).isEqualTo("flow0");

        verify(storeReviewRepository).findAll(ArgumentMatchers.<Specification<StoreReviewEntity>>any(), eq(Pageable.unpaged()));
        verify(userAccountService, times(2)).getUser("user0");
    }

    @Test
    void testGetStoreReviewsStates() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();
        Instant now = Instant.now();
        Page<StoreReviewEntity> testStoreReviewEntities = new PageImpl<>(Arrays.asList(
                StoreReviewEntity.builder()
                        .id("SR-1")
                        .uuid(uuid1.toString())
                        .storeNumber(8762L)
                        .assignee("user0")
                        .state("state0")
                        .flow("flow0")
                        .build(),
                StoreReviewEntity.builder()
                        .id("SR-2")
                        .uuid(uuid2.toString())
                        .storeNumber(8763L)
                        .assignee("user0")
                        .assignedAt(now.minusSeconds(600))
                        .state("state0")
                        .flow("flow0")
                        .build(),
                StoreReviewEntity.builder()
                        .id("SR-3")
                        .uuid(uuid3.toString())
                        .storeNumber(8764L)
                        .assignee("user0")
                        .assignedAt(now)
                        .state("state0")
                        .flow("flow0")
                        .build()));
        when(storeReviewRepository.findAll(ArgumentMatchers.<Specification<StoreReviewEntity>>any(), eq(Pageable.unpaged()))).thenReturn(testStoreReviewEntities);

        when(userAccountService.getUser("user0")).thenReturn(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("McLast")
                .build());

        StoreReviewFilters filters = StoreReviewFilters.builder()
                .state(List.of("state0", "state1"))
                .build();

        Page<StoreReview> storeReviewsPage = storeReviewService.getStoreReviews(Pageable.unpaged(), filters);

        assertThat(storeReviewsPage).hasSize(3);

        List<StoreReview> storeReviews = storeReviewsPage.toList();

        StoreReview storeReview1 = storeReviews.get(0);
        assertThat(storeReview1.getId()).isEqualTo("SR-1");
        assertThat(storeReview1.getUuid()).isEqualTo(uuid1);
        assertThat(storeReview1.getStoreNumber()).isEqualTo(8762L);
        assertThat(storeReview1.getAssignee()).isEqualTo("user0");
        assertThat(storeReview1.getAssignedAt()).isNull();
        assertThat(storeReview1.getState()).isEqualTo("state0");
        assertThat(storeReview1.getFlow()).isEqualTo("flow0");

        StoreReview storeReview2 = storeReviews.get(1);
        assertThat(storeReview2.getId()).isEqualTo("SR-2");
        assertThat(storeReview2.getUuid()).isEqualTo(uuid2);
        assertThat(storeReview2.getStoreNumber()).isEqualTo(8763L);
        assertThat(storeReview2.getAssignee()).isEqualTo("user0");
        assertThat(storeReview2.getAssignedAt()).isEqualTo(now.minusSeconds(600));
        assertThat(storeReview2.getState()).isEqualTo("state0");
        assertThat(storeReview2.getFlow()).isEqualTo("flow0");

        StoreReview storeReview3 = storeReviews.get(2);
        assertThat(storeReview3.getId()).isEqualTo("SR-3");
        assertThat(storeReview3.getUuid()).isEqualTo(uuid3);
        assertThat(storeReview3.getStoreNumber()).isEqualTo(8764L);
        assertThat(storeReview3.getAssignee()).isEqualTo("user0");
        assertThat(storeReview3.getAssignedAt()).isEqualTo(now);
        assertThat(storeReview3.getState()).isEqualTo("state0");
        assertThat(storeReview3.getFlow()).isEqualTo("flow0");

        verify(storeReviewRepository).findAll(ArgumentMatchers.<Specification<StoreReviewEntity>>any(), eq(Pageable.unpaged()));
        verify(userAccountService, times(3)).getUser("user0");
    }

    @Test
    void testGetStoreReviewsAssigneeAndState() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        Page<StoreReviewEntity> testStoreReviewEntities = new PageImpl<>(Arrays.asList(
                StoreReviewEntity.builder()
                        .id("SR-1")
                        .uuid(uuid1.toString())
                        .storeNumber(8762L)
                        .assignee("user0")
                        .state("state0")
                        .flow("flow0")
                        .build(),
                StoreReviewEntity.builder()
                        .id("SR-2")
                        .uuid(uuid2.toString())
                        .storeNumber(8763L)
                        .assignee("user0")
                        .state("state0")
                        .flow("flow0")
                        .build()));
        when(storeReviewRepository.findAll(ArgumentMatchers.<Specification<StoreReviewEntity>>any(), eq(Pageable.unpaged()))).thenReturn(testStoreReviewEntities);

        StoreReviewFilters filters = StoreReviewFilters.builder()
                .assignee(List.of("user0"))
                .state(List.of("state0", "state1"))
                .build();

        Page<StoreReview> storeReviewsPage = storeReviewService.getStoreReviews(Pageable.unpaged(), filters);

        assertThat(storeReviewsPage).hasSize(2);

        List<StoreReview> storeReviews = storeReviewsPage.toList();

        StoreReview storeReview1 = storeReviews.get(0);
        assertThat(storeReview1.getId()).isEqualTo("SR-1");
        assertThat(storeReview1.getUuid()).isEqualTo(uuid1);
        assertThat(storeReview1.getStoreNumber()).isEqualTo(8762L);
        assertThat(storeReview1.getAssignee()).isEqualTo("user0");
        assertThat(storeReview1.getState()).isEqualTo("state0");
        assertThat(storeReview1.getFlow()).isEqualTo("flow0");

        StoreReview storeReview2 = storeReviews.get(1);
        assertThat(storeReview2.getId()).isEqualTo("SR-2");
        assertThat(storeReview2.getUuid()).isEqualTo(uuid2);
        assertThat(storeReview2.getStoreNumber()).isEqualTo(8763L);
        assertThat(storeReview2.getAssignee()).isEqualTo("user0");
        assertThat(storeReview2.getState()).isEqualTo("state0");
        assertThat(storeReview2.getFlow()).isEqualTo("flow0");

        verify(storeReviewRepository).findAll(ArgumentMatchers.<Specification<StoreReviewEntity>>any(), eq(Pageable.unpaged()));
        verify(userAccountService, times(2)).getUser("user0");
    }

    @Test
    void testUpdateStoreReviewStatusAssign() {
        UUID uuid = UUID.randomUUID();
        when(storeReviewRepository.findById("SR-1")).thenReturn(Optional.of(StoreReviewEntity.builder()
                .id("SR-1")
                .uuid(uuid.toString())
                .storeNumber(876L)
                .assignee("user0")
                .state("state0")
                .flow("flow0")
                .build()));

        ObjectNode attributes = instance.objectNode()
                .put("@class", AssignStoreReviewCommand.class.getName())
                .put("assignee", "user1")
                .put("declinedBy", (String) null)
                .put("reasonForDeclining", (String) null);
        EstrFact estrFact = EstrFact.builder()
                .attributes(attributes)
                .build();
        when(estrClient.updateFactStatus(uuid, "assign", estrFact)).thenReturn(EstrFact.builder()
                .id(uuid)
                .state("state1")
                .flow("flow1")
                .build());

        AssignStoreReviewCommand assignCommand = AssignStoreReviewCommand.builder()
                .assignee("user1")
                .build();

        StoreReview storeReview = storeReviewService.updateStoreReviewStatus("SR-1", "assign", assignCommand);

        assertThat(storeReview).isNotNull();
        assertThat(storeReview.getId()).isEqualTo("SR-1");
        assertThat(storeReview.getUuid()).isEqualTo(uuid);
        assertThat(storeReview.getStoreNumber()).isEqualTo(876L);
        assertThat(storeReview.getState()).isEqualTo("state1");
        assertThat(storeReview.getFlow()).isEqualTo("flow1");

        verify(storeReviewRepository).findById("SR-1");
        verify(estrClient).updateFactStatus(uuid, "assign", estrFact);

        verify(storeAssetReviewOrchestrationService, never()).createStoreAssetReviews(uuid, 876L, "SR-1");
    }

    @Test
    void testUpdateStoreReviewStatusCancel() {
        UUID uuid = UUID.randomUUID();
        when(storeReviewRepository.findById("SR-1")).thenReturn(Optional.of(StoreReviewEntity.builder()
                .id("SR-1")
                .uuid(uuid.toString())
                .storeNumber(876L)
                .assignee("user0")
                .state("state0")
                .flow("flow0")
                .build()));

        ObjectNode attributes = instance.objectNode()
                .put("@class", CancelStoreReviewCommand.class.getName())
                .put("cancelledBy", "anonymousUser")
                .put("reasonForCancellation", "Duplicate");
        EstrFact estrFact = EstrFact.builder()
                .attributes(attributes)
                .build();
        when(estrClient.updateFactStatus(uuid, "cancel", estrFact)).thenReturn(EstrFact.builder()
                .id(uuid)
                .state("state1")
                .flow("flow1")
                .build());

        CancelStoreReviewCommand cancelCommand = CancelStoreReviewCommand.builder()
                .reasonForCancellation("Duplicate")
                .build();

        StoreReview storeReview = storeReviewService.updateStoreReviewStatus("SR-1", "cancel", cancelCommand);

        assertThat(storeReview).isNotNull();
        assertThat(storeReview.getId()).isEqualTo("SR-1");
        assertThat(storeReview.getUuid()).isEqualTo(uuid);
        assertThat(storeReview.getStoreNumber()).isEqualTo(876L);
        assertThat(storeReview.getState()).isEqualTo("state1");
        assertThat(storeReview.getFlow()).isEqualTo("flow1");

        verify(storeReviewRepository).findById("SR-1");
        verify(estrClient).updateFactStatus(uuid, "cancel", estrFact);

        verify(storeAssetReviewOrchestrationService, never()).createStoreAssetReviews(uuid, 876L, "SR-1");
    }

    @Test
    void testRefreshStoreAssetReviews() {
        UUID uuid = UUID.randomUUID();
        when(storeReviewRepository.findById("SR-11")).thenReturn(Optional.of(StoreReviewEntity.builder()
                .id("SR-11")
                .uuid(uuid.toString())
                .storeNumber(2352L)
                .assignee("user0")
                .state("state0")
                .flow("flow0")
                .build()));

        List<StoreAssetReview> storeAssetReviews = Arrays.asList(
                StoreAssetReview.builder().build(),
                StoreAssetReview.builder().build());
        when(storeAssetReviewOrchestrationService.refreshStoreAssetReviews(uuid, 2352L, "SR-11"))
                .thenReturn(CompletableFuture.completedFuture(storeAssetReviews));

        StoreReview storeReview = storeReviewService.refreshStoreAssetReviews("SR-11");

        assertThat(storeReview).isNotNull();
        assertThat(storeReview.getId()).isEqualTo("SR-11");
        assertThat(storeReview.getUuid()).isEqualTo(uuid);
        assertThat(storeReview.getStoreNumber()).isEqualTo(2352L);
        assertThat(storeReview.getState()).isEqualTo("state0");
        assertThat(storeReview.getFlow()).isEqualTo("flow0");

        verify(storeReviewRepository).findById("SR-11");
        verify(storeAssetReviewOrchestrationService).refreshStoreAssetReviews(uuid, 2352L, "SR-11");
    }

    @Test
    void testGetStoreReviewWorkflow() {
        UUID uuid = UUID.randomUUID();
        when(storeReviewRepository.findById("SR-1")).thenReturn(Optional.of(StoreReviewEntity.builder()
                .id("SR-1")
                .uuid(uuid.toString())
                .storeNumber(2352L)
                .assignee("user0")
                .state("state0")
                .flow("flow0")
                .build()));

        when(userAccountService.getUser("user0")).thenReturn(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("McLast")
                .build());

        EstrWorkflow estrWorkflow = EstrWorkflow.builder()
                .state(EstrState.builder()
                        .state("state0")
                        .flow("flow0")
                        .build())
                .nextAction(EstrTransition.builder()
                        .action("action0")
                        .build())
                .nextAction(EstrTransition.builder()
                        .action("action1")
                        .build())
                .build();
        when(estrClient.getWorkflow(uuid)).thenReturn(estrWorkflow);

        Workflow<StoreReview> workflow = storeReviewService.getStoreReviewWorkflow("SR-1");

        assertThat(workflow).isNotNull();
        assertThat(workflow.getEntity()).isNotNull();
        assertThat(workflow.getEntity().getId()).isEqualTo("SR-1");
        assertThat(workflow.getEntity().getState()).isEqualTo("state0");
        assertThat(workflow.getEntity().getFlow()).isEqualTo("flow0");

        assertThat(workflow.getState()).isEqualTo("state0");
        assertThat(workflow.getFlow()).isEqualTo("flow0");

        assertThat(workflow.getTransitions()).hasSize(2);
        assertThat(workflow.getTransitions().get(0).getAction()).isEqualTo("action0");
        assertThat(workflow.getTransitions().get(1).getAction()).isEqualTo("action1");

        verify(storeReviewRepository).findById("SR-1");
        verify(userAccountService).getUser("user0");
        verify(estrClient).getWorkflow(uuid);
    }

    @Test
    void testGetStoreReviewWorkflowByStateAndFlow() {
        EstrWorkflow estrWorkflow = EstrWorkflow.builder()
                .state(EstrState.builder()
                        .state("state0")
                        .flow("flow0")
                        .build())
                .nextAction(EstrTransition.builder()
                        .action("action0")
                        .build())
                .nextAction(EstrTransition.builder()
                        .action("action1")
                        .build())
                .build();
        when(estrClient.getWorkflow("crystalStoreReview", "state0", "flow0")).thenReturn(estrWorkflow);

        Workflow<StoreReview> workflow = storeReviewService.getStoreReviewWorkflow("state0", "flow0");

        assertThat(workflow).isNotNull();
        assertThat(workflow.getEntity()).isNull();

        assertThat(workflow.getState()).isEqualTo("state0");
        assertThat(workflow.getFlow()).isEqualTo("flow0");

        assertThat(workflow.getTransitions()).hasSize(2);
        assertThat(workflow.getTransitions().get(0).getAction()).isEqualTo("action0");
        assertThat(workflow.getTransitions().get(1).getAction()).isEqualTo("action1");

        verify(estrClient).getWorkflow("crystalStoreReview", "state0", "flow0");
    }

    @Test
    void testGetStoreReviewTimeline() {
        UUID uuid = UUID.randomUUID();
        Instant timestamp1 = Instant.now();
        Instant timestamp2 = Instant.now().plusSeconds(2);
        when(storeReviewRepository.findById("SR-1")).thenReturn(Optional.of(StoreReviewEntity.builder()
                .id("SR-1")
                .uuid(uuid.toString())
                .storeNumber(2352L)
                .assignee("user2")
                .state("state1")
                .flow("flow0")
                .build()));

        when(userAccountService.getUser("user0")).thenReturn(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("McLast")
                .build());

        when(userAccountService.getUser("user1")).thenReturn(User.builder()
                .id("user1")
                .firstName("Second")
                .lastName("McLast")
                .build());

        when(userAccountService.getUser("user2")).thenReturn(User.builder()
                .id("user2")
                .firstName("Third")
                .lastName("McLast")
                .build());

        List<EstrTimelineEvent> events = Arrays.asList(
                EstrTimelineEvent.builder()
                        .eventName("event.FirstEvent")
                        .timestamp(timestamp1)
                        .version(1)
                        .author("user0")
                        .state(EstrState.builder()
                                .state("state0")
                                .flow("flow0")
                                .build())
                        .attributeChanges(Arrays.asList(
                                EstrChange.<JsonNode>builder()
                                        .attribute("storeReviewId")
                                        .left(instance.nullNode())
                                        .right(instance.textNode("SR-1"))
                                        .build(),
                                EstrChange.<JsonNode>builder()
                                        .attribute("storeNumber")
                                        .left(instance.nullNode())
                                        .right(instance.numberNode(100))
                                        .build()))
                        .build(),
                EstrTimelineEvent.builder()
                        .eventName("event.SecondEvent")
                        .timestamp(timestamp2)
                        .version(3)
                        .author("user1")
                        .state(EstrState.builder()
                                .state("state1")
                                .flow("flow0")
                                .build())
                        .attributeChanges(Collections.singletonList(
                                EstrChange.<JsonNode>builder()
                                        .attribute("assignee")
                                        .left(instance.nullNode())
                                        .right(instance.textNode("user2"))
                                        .build()))
                        .build());
        ObjectNode attributes = instance.objectNode()
                .put("storeReviewId", "SR-1")
                .put("storeNumber", 100);
        EstrTimeline estrTimeline = EstrTimeline.builder()
                .id(uuid)
                .latestTimestamp(timestamp2)
                .latestVersion(3)
                .events(events)
                .attributes(attributes)
                .build();
        when(estrClient.getTimeline(uuid)).thenReturn(estrTimeline);

        Timeline<StoreReview> timeline = storeReviewService.getStoreReviewTimeline("SR-1");

        assertThat(timeline).isNotNull();
        assertThat(timeline.getEntity()).isNotNull();
        assertThat(timeline.getEntity().getId()).isEqualTo("SR-1");
        assertThat(timeline.getEntity().getAssignee()).isEqualTo("user2");
        assertThat(timeline.getEntity().getAssigneeName()).isEqualTo("Third McLast");
        assertThat(timeline.getEntity().getState()).isEqualTo("state1");
        assertThat(timeline.getEntity().getFlow()).isEqualTo("flow0");

        assertThat(timeline.getEvents()).hasSize(2);

        TimelineEvent event1 = timeline.getEvents().get(0);
        assertThat(event1).isNotNull();
        assertThat(event1.getEventName()).isEqualTo("FirstEvent");
        assertThat(event1.getTimestamp()).isEqualTo(timestamp1);
        assertThat(event1.getState()).isEqualTo("state0");
        assertThat(event1.getFlow()).isEqualTo("flow0");
        assertThat(event1.getAuthor()).isEqualTo("user0");
        assertThat(event1.getAuthorName()).isEqualTo("First McLast");

        assertThat(event1.getAttributeChanges()).hasSize(2);
        assertThat(event1.getAttributeChanges()).containsExactly(
                AttributeChange.<JsonNode>builder()
                        .attribute("storeReviewId")
                        .left(instance.nullNode())
                        .right(instance.textNode("SR-1"))
                        .build(),
                AttributeChange.<JsonNode>builder()
                        .attribute("storeNumber")
                        .left(instance.nullNode())
                        .right(instance.numberNode(100))
                        .build());

        TimelineEvent event2 = timeline.getEvents().get(1);
        assertThat(event2).isNotNull();
        assertThat(event2.getEventName()).isEqualTo("SecondEvent");
        assertThat(event2.getTimestamp()).isEqualTo(timestamp2);
        assertThat(event2.getState()).isEqualTo("state1");
        assertThat(event2.getFlow()).isEqualTo("flow0");
        assertThat(event2.getAuthor()).isEqualTo("user1");
        assertThat(event2.getAuthorName()).isEqualTo("Second McLast");

        assertThat(event2.getAttributeChanges()).hasSize(1);
        assertThat(event2.getAttributeChanges()).containsExactly(
                AssigneeChange.<JsonNode>builder()
                        .attribute("assignee")
                        .left(instance.nullNode())
                        .leftName(null)
                        .right(instance.textNode("user2"))
                        .rightName("Third McLast")
                        .build());

        verify(storeReviewRepository).findById("SR-1");
        verify(userAccountService).getUser("user0");
        verify(userAccountService).getUser("user1");
        verify(userAccountService, times(2)).getUser("user2");
        verify(estrClient).getTimeline(uuid);
    }

    @Test
    void testGetStoreReviewTimelineLeftValues() {
        UUID uuid = UUID.randomUUID();
        Instant timestamp1 = Instant.now();
        Instant timestamp2 = Instant.now().plusSeconds(2);
        when(storeReviewRepository.findById("SR-1")).thenReturn(Optional.of(StoreReviewEntity.builder()
                .id("SR-1")
                .uuid(uuid.toString())
                .storeNumber(2352L)
                .assignee("user1")
                .state("state1")
                .flow("flow0")
                .build()));

        when(userAccountService.getUser("user0")).thenReturn(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("McLast")
                .build());

        when(userAccountService.getUser("user1")).thenReturn(User.builder()
                .id("user1")
                .firstName("Second")
                .lastName("McLast")
                .build());

        when(userAccountService.getUser("user2")).thenReturn(User.builder()
                .id("user2")
                .firstName("Third")
                .lastName("McLast")
                .build());

        List<EstrTimelineEvent> events = Arrays.asList(
                EstrTimelineEvent.builder()
                        .eventName("event.ThirdEvent")
                        .timestamp(timestamp1)
                        .version(3)
                        .author("user0")
                        .state(EstrState.builder()
                                .state("state0")
                                .flow("flow0")
                                .build())
                        .attributeChanges(Collections.singletonList(
                                EstrChange.<JsonNode>builder()
                                        .attribute("timeInMonitoringDays")
                                        .left(instance.numberNode(3))
                                        .right(instance.numberNode(4))
                                        .build()))
                        .build(),
                EstrTimelineEvent.builder()
                        .eventName("event.FourthEvent")
                        .timestamp(timestamp2)
                        .version(5)
                        .author("user1")
                        .state(EstrState.builder()
                                .state("state1")
                                .flow("flow0")
                                .build())
                        .attributeChanges(Collections.singletonList(
                                EstrChange.<JsonNode>builder()
                                        .attribute("assignee")
                                        .left(instance.textNode("user2"))
                                        .right(instance.textNode("user0"))
                                        .build()))
                        .build());
        ObjectNode attributes = instance.objectNode()
                .put("storeReviewId", "SR-1")
                .put("storeNumber", 100);
        EstrTimeline estrTimeline = EstrTimeline.builder()
                .id(uuid)
                .latestTimestamp(timestamp2)
                .latestVersion(5)
                .events(events)
                .attributes(attributes)
                .build();
        when(estrClient.getTimeline(uuid)).thenReturn(estrTimeline);

        Timeline<StoreReview> timeline = storeReviewService.getStoreReviewTimeline("SR-1");

        assertThat(timeline).isNotNull();
        assertThat(timeline.getEntity()).isNotNull();
        assertThat(timeline.getEntity().getId()).isEqualTo("SR-1");
        assertThat(timeline.getEntity().getAssignee()).isEqualTo("user1");
        assertThat(timeline.getEntity().getAssigneeName()).isEqualTo("Second McLast");
        assertThat(timeline.getEntity().getState()).isEqualTo("state1");
        assertThat(timeline.getEntity().getFlow()).isEqualTo("flow0");

        assertThat(timeline.getEvents()).hasSize(2);

        TimelineEvent event1 = timeline.getEvents().get(0);
        assertThat(event1).isNotNull();
        assertThat(event1.getEventName()).isEqualTo("ThirdEvent");
        assertThat(event1.getTimestamp()).isEqualTo(timestamp1);
        assertThat(event1.getState()).isEqualTo("state0");
        assertThat(event1.getFlow()).isEqualTo("flow0");
        assertThat(event1.getAuthor()).isEqualTo("user0");
        assertThat(event1.getAuthorName()).isEqualTo("First McLast");

        assertThat(event1.getAttributeChanges()).hasSize(1);
        assertThat(event1.getAttributeChanges()).containsExactly(
                AttributeChange.<JsonNode>builder()
                        .attribute("timeInMonitoringDays")
                        .left(instance.numberNode(3))
                        .right(instance.numberNode(4))
                        .build());

        TimelineEvent event2 = timeline.getEvents().get(1);
        assertThat(event2).isNotNull();
        assertThat(event2.getEventName()).isEqualTo("FourthEvent");
        assertThat(event2.getTimestamp()).isEqualTo(timestamp2);
        assertThat(event2.getState()).isEqualTo("state1");
        assertThat(event2.getFlow()).isEqualTo("flow0");
        assertThat(event2.getAuthor()).isEqualTo("user1");
        assertThat(event2.getAuthorName()).isEqualTo("Second McLast");

        assertThat(event2.getAttributeChanges()).hasSize(1);
        assertThat(event2.getAttributeChanges()).containsExactly(
                AssigneeChange.<JsonNode>builder()
                        .attribute("assignee")
                        .left(instance.textNode("user2"))
                        .leftName("Third McLast")
                        .right(instance.textNode("user0"))
                        .rightName("First McLast")
                        .build());

        verify(storeReviewRepository).findById("SR-1");
        verify(userAccountService, times(2)).getUser("user0");
        verify(userAccountService, times(2)).getUser("user1");
        verify(userAccountService).getUser("user2");
        verify(estrClient).getTimeline(uuid);
    }

}
