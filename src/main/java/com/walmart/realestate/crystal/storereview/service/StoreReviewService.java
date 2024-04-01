package com.walmart.realestate.crystal.storereview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.walmart.core.realestate.cerberus.exception.BadRequestException;
import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.asset.model.StoreReviewQuery;
import com.walmart.realestate.crystal.storereview.client.estr.EstrClient;
import com.walmart.realestate.crystal.storereview.client.estr.model.*;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.FMRealtyAlignment;
import com.walmart.realestate.crystal.storereview.command.CreateStoreReviewCommand;
import com.walmart.realestate.crystal.storereview.command.StoreReviewCommand;
import com.walmart.realestate.crystal.storereview.command.UpdateStoreReviewCommand;
import com.walmart.realestate.crystal.storereview.entity.StoreHealthScoreSnapshotEntity;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewEntity;
import com.walmart.realestate.crystal.storereview.entity.UserAccountEntity;
import com.walmart.realestate.crystal.storereview.model.*;
import com.walmart.realestate.crystal.storereview.repository.StoreReviewRepository;
import com.walmart.realestate.crystal.storereview.repository.projection.StoreReviewStoreNumberStateReviewType;
import com.walmart.realestate.crystal.storereview.util.IdUtil;
import com.walmart.realestate.idn.service.IdentifierOperationsService;
import com.walmart.realestate.soteria.model.User;
import com.walmart.realestate.soteria.model.UserContext;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class StoreReviewService {

    private static final String STORE_HEALTH_REVIEW_NAME = "crystalStoreReview";

    private static final String STORE_PREVENTIVE_MAINTENANCE_NAME = "crystalStorePreventiveMaintenance";
    public static final String HEALTH_REVIEW_PREFIX = "HR-";
    public static final String PREVENTIVE_MAINTENANCE_PREFIX = "PM-";
    public static final String HEALTH_REVIEW = "Health Review";
    public static final String PREVENTIVE_MAINTENANCE = "Preventive Maintenance";
    private final StoreAssetReviewOrchestrationService storeAssetReviewOrchestrationService;

    private final UserAccountService userAccountService;

    private final StoreReviewUserService storeReviewUserService;

    private final StoreReviewRepository storeReviewRepository;

    private final EstrClient estrClient;

    private final ObjectMapper objectMapper;

    private final IdentifierOperationsService identifierOperationsService;

    private final HealthMetricsClient healthMetricsClient;

    private final StoreService storeService;

    @Logger
    public StoreReview createStoreReview(StoreReview storeReview, UserContext userContext) {

        boolean isAssigneePresent = storeReviewUserService.getReviewers(userContext)
                .stream()
                .map(User::getId)
                .collect(Collectors.toList())
                .contains(storeReview.getAssignee());

        if (!isAssigneePresent) {
            throw new BadRequestException("Invalid assignee value");
        }

        String reviewPrefix = getReviewTypePrefix(storeReview);

        String storeReviewId = identifierOperationsService.generateIdentifier(reviewPrefix + storeReview.getStoreNumber())
                .getTop().getRepresentation();

        log.info("Generated id for store review {}", storeReviewId);

        String sdm = getSdmByStoreNumber(storeReview, storeReviewId);

        String reviewName = getReviewName(storeReview);

        if (storeReview.getRefrigerantType() == null) {
            storeReview.setRefrigerantType(Objects.requireNonNull(storeService.getStoreList(StoreReviewQuery.builder().storeNumber(storeReview.getStoreNumber().toString()).build(), PageRequest.of(0, 1)).getContent().stream().findFirst().get().getContent()).getRefrigerationTypes());
        }

        EstrFact storeReviewFact = estrClient.createFact(EstrFact.builder()
                .type(reviewName)
                .attributes(objectMapper.convertValue(CreateStoreReviewCommand.builder()
                                .storeReviewId(storeReviewId)
                                .storeNumber(storeReview.getStoreNumber())
                                .reviewType(storeReview.getReviewType())
                                .sdm(sdm)
                                .assignee(storeReview.getAssignee())
                                .startDate(storeReview.getStartDate())
                                .refrigerantType(storeReview.getRefrigerantType())
                                .build(),
                        ObjectNode.class))
                .build());
        log.info("Created store review fact uuid {}", storeReviewFact.getId());

        return buildStoreReview(storeReview, storeReviewId, storeReviewFact);
    }

    private String getSdmByStoreNumber(StoreReview storeReview, String storeReviewId) {
        FMRealtyAlignment fmRealtyAlignment = null;
        try {
            fmRealtyAlignment = healthMetricsClient.getFMRealtyAlignment(storeReview.getStoreNumber().toString());
        } catch (Exception ex) {
            log.error("Exception encountered while fetching fmRealtyAlignment for store reviewId {} with error {}", storeReviewId, ex.getMessage());
        }
        return Optional.ofNullable(fmRealtyAlignment)
                .map(FMRealtyAlignment::getSdm)
                .orElse("NA");
    }


    private String getReviewName(StoreReview storeReview) {
        String reviewName = null;
        if (HEALTH_REVIEW.equalsIgnoreCase(storeReview.getReviewType())) {
            reviewName = STORE_HEALTH_REVIEW_NAME;
        } else if (PREVENTIVE_MAINTENANCE.equalsIgnoreCase(storeReview.getReviewType())) {
            reviewName = STORE_PREVENTIVE_MAINTENANCE_NAME;
        }
        return reviewName;
    }

    private String getReviewTypePrefix(StoreReview storeReview) {

        if (storeReview.getReviewType().equalsIgnoreCase(HEALTH_REVIEW))
            return HEALTH_REVIEW_PREFIX;
        else if (storeReview.getReviewType().equalsIgnoreCase(PREVENTIVE_MAINTENANCE))
            return PREVENTIVE_MAINTENANCE_PREFIX;

        else throw new BadRequestException("Invalid review type");
    }

    @Logger
    public StoreReview updateStoreReview(String storeReviewId, StoreReview storeReview) {
        log.info("Update store review {}", storeReviewId);
        StoreReviewEntity storeReviewEntity = storeReviewRepository.findById(storeReviewId)
                .orElseThrow(EntityNotFoundException::new);

        EstrFact storeReviewFact = estrClient.updateFactStatus(
                IdUtil.uuid(storeReviewEntity.getUuid()),
                "update",
                EstrFact.builder()
                        .attributes(objectMapper.convertValue(UpdateStoreReviewCommand.builder()
                                        .storeReviewId(storeReviewId)
                                        .assignee(storeReview.getAssignee())
                                        .startDate(storeReview.getStartDate())
                                        .build(),
                                ObjectNode.class))
                        .build());
        log.info("Updated store review fact uuid {}", storeReviewFact.getId());
        return buildStoreReview(storeReview, storeReviewId, storeReviewFact);
    }

    @Logger
    @Retry(name = "store-review")
    public StoreReview getStoreReview(String storeReviewId) {
        StoreReviewEntity entity = storeReviewRepository.findById(storeReviewId)
                .orElseThrow(EntityNotFoundException::new);
        return fromStoreReviewEntity(entity);
    }

    @Logger
    public List<StoreReview> getStoreReviews(List<String> storeReviewIdList) {
        List<StoreReviewEntity> entities = storeReviewRepository.findByIdIn(storeReviewIdList);
        return entities.stream().map(this::fromStoreReviewEntity).collect(Collectors.toList());
    }

    public List<StoreReviewEntity> getActiveStoreReviews(Pageable pageable, Long days) {

        List<StoreReviewEntity> entities = storeReviewRepository.findAll(getStoreReviewSpecification(StoreReviewFilters.builder().state(Arrays.asList("inProgress", "waitingOnWorkOrder", "validation", "monitoring", "assigned", "assigning", "PM-Post-Review", "PM-In-Progress")).assignedDateFrom(LocalDate.now().minusDays(days).atStartOfDay(ZoneId.of("UTC")).toInstant()).build()), pageable).getContent();
        return entities;

    }

    @Logger
    public List<StoreReview> getStoreReviewsByStoreNumber(Long storeNumber) {
        List<StoreReviewEntity> entities = storeReviewRepository.findByStoreNumber(storeNumber);
        return entities.stream().map(this::fromStoreReviewEntity).collect(Collectors.toList());
    }

    @Logger
    public StoreReview getStoreReviewByAggregateId(String uuid) {
        StoreReviewEntity entity = storeReviewRepository.findByUuid(uuid);
        return fromStoreReviewEntity(entity);
    }

    @Logger
    public Page<StoreReview> getStoreReviews(Pageable pageable, StoreReviewFilters filters) {
        return fromStoreReviewEntities(storeReviewRepository.findAll(getStoreReviewSpecification(filters), pageable));
    }

    @Logger
    public StoreReview updateStoreReviewStatus(String storeReviewId, String action, StoreReviewCommand command) {
        log.info("Update store review status {} with action {}", storeReviewId, action);
        StoreReviewEntity storeReviewEntity = storeReviewRepository.findById(storeReviewId)
                .orElseThrow(EntityNotFoundException::new);

        EstrFact storeReviewFact = estrClient.updateFactStatus(
                IdUtil.uuid(storeReviewEntity.getUuid()),
                action,
                EstrFact.builder()
                        .attributes(objectMapper.convertValue(command, ObjectNode.class))
                        .build());
        log.info("Store review status {} with action {} updated", storeReviewId, action);

        return buildStoreReview(storeReviewEntity, storeReviewFact);
    }

    @Logger
    public StoreReview refreshStoreAssetReviews(String storeReviewId) {
        StoreReview storeReview = getStoreReview(storeReviewId);
        storeAssetReviewOrchestrationService.refreshStoreAssetReviews(storeReview.getUuid(), storeReview.getStoreNumber(), storeReviewId)
                .join();
        return storeReview;
    }

    @Logger
    public Workflow<StoreReview> getStoreReviewWorkflow(String storeReviewId) {
        StoreReview storeReview = getStoreReview(storeReviewId);
        EstrWorkflow estrWorkflow = estrClient.getWorkflow(storeReview.getUuid());
        return fromWorkflow(estrWorkflow, storeReview);
    }

    @Logger
    public Workflow<StoreReview> getStoreReviewWorkflow(String state, String flow) {
        EstrWorkflow estrWorkflow = estrClient.getWorkflow(STORE_HEALTH_REVIEW_NAME, state, flow);
        return fromWorkflow(estrWorkflow, null);
    }

    @Logger
    public Timeline<StoreReview> getStoreReviewTimeline(String storeReviewId) {
        StoreReview storeReview = getStoreReview(storeReviewId);
        EstrTimeline estrTimeline = estrClient.getTimeline(storeReview.getUuid());
        return fromTimeline(estrTimeline, storeReview);
    }

    @Logger
    @SneakyThrows
    public void processEvent(JsonNode event) {
        String uuid = event.get("aggregateId").textValue();
        String storeReviewId = event.get("defaultAttributes").get("storeReviewId").textValue();
        Long storeNumber = event.get("defaultAttributes").get("storeNumber").longValue();
        createStoreAssetReviews(IdUtil.uuid(uuid), storeReviewId, storeNumber);
    }

    private void getUserIdByName(Root<StoreReviewEntity> entity, String name, List<String> people, List<Predicate> predicates) {
        if (!CollectionUtils.isEmpty(people)) {
            predicates.add(entity.get(name).in(people));
        }
    }

    private void dateFilterHelper(Root<StoreReviewEntity> entity, String name, Instant from, Instant to, List<Predicate> predicates, CriteriaBuilder builder) {
        if (Objects.nonNull(from) && Objects.nonNull(to) && from.isBefore(to)) {
            predicates.add(builder.between(entity.get(name), from, to));
        } else if (Objects.nonNull(from)) {
            predicates.add(builder.greaterThan(entity.get(name), from));
        } else if (Objects.nonNull(to)) {
            predicates.add(builder.lessThan(entity.get(name), to));
        }
    }

    private Specification<StoreReviewEntity> getStoreReviewSpecification(StoreReviewFilters filters) {
        return (entity, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (Objects.nonNull(filters.getId())) {
                predicates.add(builder.like(entity.get("id"), "%" + filters.getId() + "%"));
            }

            if (!CollectionUtils.isEmpty(filters.getStoreNumber())) {
                predicates.add(entity.get("storeNumber").in(filters.getStoreNumber()));
            }

            getUserIdByName(entity, "assignee", filters.getAssignee(), predicates);
            getUserIdByName(entity, "createdBy", filters.getCreatedBy(), predicates);
            getUserIdByName(entity, "closedBy", filters.getClosedBy(), predicates);
            getUserIdByName(entity, "declinedBy", filters.getDeclinedBy(), predicates);

            if (!CollectionUtils.isEmpty(filters.getState())) {
                predicates.add(entity.get("state").in(filters.getState()));
            }

            Join<StoreHealthScoreSnapshotEntity, StoreReviewEntity> healthScore = entity.join("healthScore");
            if (Objects.nonNull(filters.getHealthScoreMin()) && Objects.nonNull(filters.getHealthScoreMax()) && filters.getHealthScoreMin() <= filters.getHealthScoreMax()) {
                predicates.add(builder.between(healthScore.get("value"), filters.getHealthScoreMin(), filters.getHealthScoreMax()));
            } else if (Objects.nonNull(filters.getHealthScoreMin())) {
                predicates.add(builder.greaterThan(healthScore.get("value"), filters.getHealthScoreMin()));
            } else if (Objects.nonNull(filters.getHealthScoreMax())) {
                predicates.add(builder.lessThan(healthScore.get("value"), filters.getHealthScoreMax()));
            }

            dateFilterHelper(entity, "assignedAt", filters.getAssignedDateFrom(), filters.getAssignedDateTo(), predicates, builder);
            dateFilterHelper(entity, "createdAt", filters.getCreatedFrom(), filters.getCreatedTo(), predicates, builder);

            if (Objects.nonNull(filters.getStartDateFrom()) && Objects.nonNull(filters.getStartDateTo()) && filters.getStartDateFrom().isBefore(filters.getStartDateTo())) {
                predicates.add(builder.between(entity.get("startDate"), LocalDateTime.ofInstant(filters.getStartDateFrom(), ZoneOffset.UTC), LocalDateTime.ofInstant(filters.getStartDateTo(), ZoneOffset.UTC)));
            } else if (Objects.nonNull(filters.getStartDateFrom())) {
                predicates.add(builder.greaterThan(entity.get("startDate"), LocalDateTime.ofInstant(filters.getStartDateFrom(), ZoneOffset.UTC)));
            } else if (Objects.nonNull(filters.getStartDateTo())) {
                predicates.add(builder.lessThan(entity.get("startDate"), LocalDateTime.ofInstant(filters.getStartDateTo(), ZoneOffset.UTC)));
            }

            dateFilterHelper(entity, "closedAt", filters.getClosedFrom(), filters.getClosedTo(), predicates, builder);
            dateFilterHelper(entity, "declinedAt", filters.getDeclinedFrom(), filters.getDeclinedTo(), predicates, builder);

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }


    private void createStoreAssetReviews(UUID uuid, String storeReviewId, Long storeNumber) {
        storeAssetReviewOrchestrationService.createStoreAssetReviews(uuid, storeNumber, storeReviewId);
    }

    private StoreReview buildStoreReview(StoreReviewEntity entity, EstrFact fact) {
        return StoreReview.builder()
                .id(entity.getId())
                .uuid(fact.getId())
                .storeNumber(entity.getStoreNumber())
                .state(fact.getState())
                .flow(fact.getFlow())
                .build();
    }

    private StoreReview buildStoreReview(StoreReview storeReview, String storeReviewId, EstrFact fact) {
        return StoreReview.builder()
                .id(storeReviewId)
                .uuid(fact.getId())
                .storeNumber(storeReview.getStoreNumber())
                .state(fact.getState())
                .reviewType(storeReview.getReviewType())
                .flow(fact.getFlow())
                .build();
    }

    private StoreReview fromStoreReviewEntity(StoreReviewEntity entity) {
        return StoreReview.builder()
                .id(entity.getId())
                .uuid(IdUtil.uuid(entity.getUuid()))
                .storeNumber(entity.getStoreNumber())
                .assignee(entity.getAssignee())
                .assigneeName(getUserName(entity.getAssignee()))
                .assigneeUser(buildUserAccount(entity.getAssigneeUser()))
                .assignedAt(entity.getAssignedAt())
                .startDate(entity.getStartDate())
                .state(entity.getState())
                .flow(entity.getFlow())
                .startedAt(entity.getStartedAt())
                .lastStartedAt(entity.getLastStartedAt())
                .timeInMonitoringDays(entity.getTimeInMonitoringDays())
                .monitoringStartedAt(entity.getMonitoringStartedAt())
                .closedBy(entity.getClosedBy())
                .closedByName(getUserName(entity.getClosedBy()))
                .closedByUser(buildUserAccount(entity.getClosedByUser()))
                .closedAt(entity.getClosedAt())
                .stateAtClosure(entity.getStateAtClosure())
                .commentsAtClosure(entity.getCommentsAtClosure())
                .validationStartedAt(entity.getValidationStartedAt())
                .declinedBy(entity.getDeclinedBy())
                .declinedByName(getUserName(entity.getDeclinedBy()))
                .declinedByUser(buildUserAccount(entity.getDeclinedByUser()))
                .declinedAt(entity.getDeclinedAt())
                .reasonForDeclining(entity.getReasonForDeclining())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .createdByName(getUserName(entity.getCreatedBy()))
                .createdByUser(buildUserAccount(entity.getCreatedByUser()))
                .healthScore(buildStoreHealthScoreSnapshot(entity.getHealthScore()))
                .lastModifiedBy(entity.getLastModifiedBy())
                .lastModifiedByUser(buildUserAccount(entity.getLastModifiedByUser()))
                .lastModifiedAt(entity.getLastModifiedAt())
                .postPreventiveMaintenanceStartedAt(entity.getPostPreventiveMaintenanceStartedAt())
                .preventiveMaintenanceStartedAt(entity.getPreventiveMaintenanceStartedAt())
                .sdm(entity.getSdm())
                .reviewType(entity.getReviewType())
                .refrigerantType(entity.getRefrigerantType())
                .build();
    }

    private UserAccount buildUserAccount(UserAccountEntity userAccountEntity) {
        return Optional.ofNullable(userAccountEntity).map(
                        userAccountEntity1 ->
                                UserAccount.builder()
                                        .userId(userAccountEntity.getUserId())
                                        .firstName(userAccountEntity.getFirstName())
                                        .lastName(userAccountEntity.getLastName())
                                        .fullName(userAccountEntity.getFullName())
                                        .email(userAccountEntity.getEmail())
                                        .memberships(userAccountEntity.getMemberships())
                                        .build())
                .orElse(null);
    }

    private StoreHealthScoreSnapshot buildStoreHealthScoreSnapshot(StoreHealthScoreSnapshotEntity storeHealthScoreSnapshotEntity) {
        return Objects.nonNull(storeHealthScoreSnapshotEntity) ? StoreHealthScoreSnapshot.builder()
                .storeNumber(storeHealthScoreSnapshotEntity.getStoreNumber())
                .rowId(storeHealthScoreSnapshotEntity.getRowId())
                .value(storeHealthScoreSnapshotEntity.getValue())
                .runTime(storeHealthScoreSnapshotEntity.getRunTime())
                .build() : null;
    }

    private String getUserName(String userId) {
        return Optional.ofNullable(userId)
                .map(id -> {
                    try {
                        return userAccountService.getUser(id);
                    } catch (NoSuchElementException e) {
                        return null;
                    }
                })
                .map(User::getFullName)
                .orElse(null);
    }

    private Page<StoreReview> fromStoreReviewEntities(Page<StoreReviewEntity> storeReviews) {
        return storeReviews.map(this::fromStoreReviewEntity);
    }

    private Workflow<StoreReview> fromWorkflow(EstrWorkflow estrWorkflow, StoreReview storeReview) {
        String state = Objects.nonNull(estrWorkflow.getState()) ? estrWorkflow.getState().getState() : null;
        String flow = Objects.nonNull(estrWorkflow.getState()) ? estrWorkflow.getState().getFlow() : null;
        return Workflow.<StoreReview>builder()
                .entity(storeReview)
                .state(state)
                .flow(flow)
                .transitions(fromWorkflowTransitions(estrWorkflow.getNextActions()))
                .build();
    }

    private Transition fromWorkflowTransition(EstrTransition estrTransition) {
        return Transition.builder()
                .action(estrTransition.getAction())
                .event(estrTransition.getEvent())
                .command(estrTransition.getCommand())
                .commandModel(estrTransition.getCommandModel())
                .eventAttributes(estrTransition.getEventAttributes())
                .build();
    }

    private List<Transition> fromWorkflowTransitions(List<EstrTransition> transitions) {
        return Optional.ofNullable(transitions)
                .orElseGet(Collections::emptyList).stream()
                .map(this::fromWorkflowTransition)
                .collect(Collectors.toList());
    }

    private Timeline<StoreReview> fromTimeline(EstrTimeline estrTimeline, StoreReview storeReview) {
        return Timeline.<StoreReview>builder()
                .entity(storeReview)
                .events(fromTimelineEvents(estrTimeline.getEvents()))
                .build();
    }

    private List<TimelineEvent> fromTimelineEvents(List<EstrTimelineEvent> events) {
        return events.stream()
                .map(this::fromTimelineEvent)
                .collect(Collectors.toList());
    }

    private TimelineEvent fromTimelineEvent(EstrTimelineEvent estrTimelineEvent) {
        return TimelineEvent.builder()
                .eventName(estrTimelineEvent.getEventName().replaceAll(".*\\.", ""))
                .author(estrTimelineEvent.getAuthor())
                .authorName(getUserName(estrTimelineEvent.getAuthor()))
                .timestamp(estrTimelineEvent.getTimestamp())
                .version(estrTimelineEvent.getVersion())
                .state(estrTimelineEvent.getState().getState())
                .flow(estrTimelineEvent.getState().getFlow())
                .attributeChanges(fromTimelineAttributeChanges(estrTimelineEvent.getAttributeChanges()))
                .build();
    }

    private List<AttributeChange<JsonNode>> fromTimelineAttributeChanges(List<EstrChange<JsonNode>> attributeChanges) {
        return attributeChanges.stream()
                .map(this::fromTimelineAttributeChange)
                .collect(Collectors.toList());
    }

    private AttributeChange<JsonNode> fromTimelineAttributeChange(EstrChange<JsonNode> estrChange) {
        if ("assignee".equals(estrChange.getAttribute())) {
            return AssigneeChange.<JsonNode>builder()
                    .attribute(estrChange.getAttribute())
                    .left(estrChange.getLeft())
                    .leftName(getUserName(estrChange.getLeft().textValue()))
                    .right(estrChange.getRight())
                    .rightName(getUserName(estrChange.getRight().textValue()))
                    .build();
        } else {
            return AttributeChange.<JsonNode>builder()
                    .attribute(estrChange.getAttribute())
                    .left(estrChange.getLeft())
                    .right(estrChange.getRight())
                    .build();
        }
    }

    public Long getOpenReviewsCount(Long days) {
        return storeReviewRepository.countByStateInAndAssignedAtAfter(Arrays.asList("inProgress", "waitingOnWorkOrder", "validation", "monitoring", "assigned", "assigning", "PM-Post-Review", "PM-In-Progress"), LocalDate.now().minusDays(days).atStartOfDay(ZoneId.of("UTC")).toInstant());
    }


    public List<Long> getActiveStoresForPmReadyToStartExclusion() {
        Instant sixtyDaysAgo = Instant.now().minus(Duration.ofDays(60));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMonth = LocalDateTime.now().plusMonths(1);
        Set<String> excludedStates = new HashSet<>(Arrays.asList("failed", "cancelled", "declined"));

        List<StoreReviewStoreNumberStateReviewType> entities = storeReviewRepository.findStoreNumbersByAssignedAtGreaterThanOrStartDateBetween(
                sixtyDaysAgo,
                now,
                nextMonth
        );

        return entities.stream()
                .filter(filter -> filter.getReviewType().equalsIgnoreCase("Preventive Maintenance"))
                .filter(filter -> !excludedStates.contains(filter.getState()))
                .map(StoreReviewStoreNumberStateReviewType::getStoreNumber)
                .distinct()
                .collect(Collectors.toList());
    }

}
