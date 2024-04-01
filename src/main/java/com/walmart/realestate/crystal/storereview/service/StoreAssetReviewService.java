package com.walmart.realestate.crystal.storereview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.walmart.core.realestate.cerberus.exception.BadRequestException;
import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.asset.model.Asset;
import com.walmart.realestate.crystal.storereview.client.estr.EstrClient;
import com.walmart.realestate.crystal.storereview.client.estr.model.EstrFact;
import com.walmart.realestate.crystal.storereview.client.estr.model.EstrTransition;
import com.walmart.realestate.crystal.storereview.client.estr.model.EstrWorkflow;
import com.walmart.realestate.crystal.storereview.client.estr.model.StoreAssetReviewStateResponse;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationAssetTimeInTarget;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationRackMetric;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationRackTimeInTarget;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationSensor;
import com.walmart.realestate.crystal.storereview.command.CreateStoreAssetReviewCommand;
import com.walmart.realestate.crystal.storereview.command.StoreAssetReviewCommand;
import com.walmart.realestate.crystal.storereview.entity.StoreAssetReviewEntity;
import com.walmart.realestate.crystal.storereview.model.*;
import com.walmart.realestate.crystal.storereview.repository.StoreAssetReviewRepository;
import com.walmart.realestate.crystal.storereview.util.IdUtil;
import com.walmart.realestate.idn.model.Identifier;
import com.walmart.realestate.idn.service.IdentifierOperationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class StoreAssetReviewService {

    private static final String STORE_ASSET_REVIEW_NAME = "crystalStoreAssetReview";

    private final AssetService assetService;

    private final StoreReviewAssetService storeReviewAssetService;

    private final StoreAssetService storeAssetService;

    private final StoreAssetReviewRepository storeAssetReviewRepository;

    private final EstrClient estrClient;

    private final HealthMetricsClient healthMetricsClient;

    private final ObjectMapper objectMapper;

    private final TaskExecutor noUserContextPrimaryTaskExecutor;

    private final IdentifierOperationsService identifierOperationsService;

    @Logger
    public List<StoreAssetReview> createStoreAssetReviews(Long storeNumber, String storeReviewId) {
        List<RefrigerationSensor> storeAssets = storeReviewAssetService.getAssetsForStore(storeNumber);
        log.info("Create {} asset reviews for store review {} store number {}", storeAssets.size(), storeReviewId, storeNumber);

        if (storeAssets.isEmpty()) {
            log.warn("Create store review {} store number {} with no assets", storeReviewId, storeNumber);
        }

        List<String> existingAssetIds = storeAssetReviewRepository.findByStoreReviewId(storeReviewId).stream()
                .map(StoreAssetReviewEntity::getAssetMappingId)
                .collect(Collectors.toList());
        log.info("Existing asset reviews {} for store review {} store number {}", existingAssetIds.size(), storeReviewId, storeNumber);

        List<RefrigerationSensor> assets = storeAssets.stream()
                .filter(asset -> !existingAssetIds.contains(asset.getId()))
                .collect(Collectors.toList());

        if (assets.isEmpty()) {
            log.info("Asset reviews for all assets exists for store review {} store number {}", storeReviewId, storeNumber);
            return Collections.emptyList();
        }

        Map<Long, Long> assetIdMap = assetService.getAssetsForStore(storeNumber).stream()
                .filter(asset -> Objects.nonNull(asset.getDwEquipmentId()))
                .collect(Collectors.toMap(Asset::getDwEquipmentId, Asset::getId, (a, b) -> a));

        String assetReviewPrefix = getAssetReviewPrefix(storeReviewId);

        List<String> storeAssetReviewIds = identifierOperationsService.generateIdentifiers(storeReviewId + assetReviewPrefix, assets.size())
                .getIdentifiers().stream()
                .map(Identifier::getRepresentation)
                .collect(Collectors.toList());
        log.info("Generated asset review ids {}", storeAssetReviewIds);

        List<EstrFact> storeAssetReviewFacts = IntStream.range(0, assets.size())
                .mapToObj(i -> estrClient.createFact(EstrFact.builder()
                        .type(STORE_ASSET_REVIEW_NAME)
                        .attributes(objectMapper.convertValue(CreateStoreAssetReviewCommand.builder()
                                        .storeAssetReviewId(storeAssetReviewIds.get(i))
                                        .storeReviewId(storeReviewId)
                                        .storeNumber(storeNumber)
                                        .assetId(assetIdMap.getOrDefault(assets.get(i).getEquipmentId(), null))
                                        .assetMappingId(assets.get(i).getId())
                                        .assetType(assets.get(i).getType())
                                        .build(),
                                ObjectNode.class))
                        .build()))
                .collect(Collectors.toList());
        List<UUID> uuids = storeAssetReviewFacts.stream()
                .map(EstrFact::getId)
                .collect(Collectors.toList());
        log.info("Created asset review facts uuid {}", uuids);

        return buildStoreAssetReviews(storeAssetReviewIds, storeNumber, storeReviewId, assets, storeAssetReviewFacts, assetIdMap);
    }

    private String getAssetReviewPrefix(String storeReviewId) {
        if (storeReviewId.startsWith("PM"))
            return "-PAM";
        else if (storeReviewId.startsWith("HR"))
            return "-HAR";
        else if (storeReviewId.startsWith("SR"))
            return "-SAR";
        else throw new BadRequestException("Invalid store review!");
    }

    @Logger
    public StoreAssetReview getStoreAssetReview(String storeAssetReviewId) {
        return fromStoreAssetReviewEntity(storeAssetReviewRepository.getOne(storeAssetReviewId));
    }

    @Logger
    public StoreAssetReview getStoreAssetReview(String storeReviewId, String assetMappingId) {
        return fromStoreAssetReviewEntity(storeAssetReviewRepository.findByAssetMappingIdAndStoreReviewId(assetMappingId, storeReviewId));
    }

    @Logger
    @PostFilter("hasPolicy(filterObject,'viewStoreReview')")
    public List<StoreAssetReview> getStoreAssetReviews(String storeReviewId) {
        return fromStoreAssetReviewEntities(storeAssetReviewRepository.findByStoreReviewId(storeReviewId));
    }

    @Logger
    public List<StoreAssetReview> getStoreAssetReviews(Long assetId) {
        return fromStoreAssetReviewEntities(storeAssetReviewRepository.findByAssetId(assetId));
    }

    public void updateStoreAssetReviewStatus(String storeReviewId, String assetMappingId, String action, StoreAssetReviewCommand command) {
        if (storeReviewId != null) {
            StoreAssetReview storeAssetReview = getStoreAssetReview(storeReviewId, assetMappingId);
            updateStoreAssetReviewStatus(storeAssetReview.getId(), action, command);
        }
    }

    public void updateStoreAssetReviewStatus(String storeReviewId, List<String> assetMappingId, String action) {
        updateStoreAssetReviewsStatus(getStoreAssetReviews(storeReviewId, assetMappingId).stream().map(StoreAssetReview::getId).collect(Collectors.toList()), action);
    }

    @Logger
    public List<StoreAssetReview> getStoreAssetReviews(String storeReviewId, List<String> assetMappingIds) {
        return storeAssetReviewRepository.findByAssetMappingIdInAndStoreReviewId(assetMappingIds, storeReviewId).stream().map(this::fromStoreAssetReviewEntity).collect(Collectors.toList());
    }

    @Logger
    public StoreAssetReview updateStoreAssetReviewStatus(String storeAssetReviewId, String action, StoreAssetReviewCommand command) {
        log.info("Update asset review status {} with action {}", storeAssetReviewId, action);

        Optional<StoreAssetReviewEntity> storeAssetReviewEntity = storeAssetReviewRepository.findById(storeAssetReviewId);

        if (storeAssetReviewEntity.isPresent() && !storeAssetReviewEntity.get().getState().equals("completed")) {
            EstrFact storeAssetReviewFact = estrClient.updateFactStatus(
                    IdUtil.uuid(storeAssetReviewEntity.get().getUuid()),
                    action,
                    EstrFact.builder()
                            .attributes(objectMapper.convertValue(command, ObjectNode.class))
                            .build());
            log.info("Asset review status {} with action {} updated", storeAssetReviewId, action);

            return buildStoreAssetReview(storeAssetReviewEntity.get(), storeAssetReviewFact);
        }

        return StoreAssetReview.builder().build();
    }

    @Logger
    public List<StoreAssetReview> updateStoreAssetReviewsStatus(List<String> storeAssetReviewIdList, String action) {
        log.info("Update asset reviews status {} with action {}", storeAssetReviewIdList, action);

        if (storeAssetReviewIdList.isEmpty()) {
            return Collections.emptyList();
        }

        List<StoreAssetReviewEntity> storeAssetReviewEntityList = storeAssetReviewRepository.findAllById(storeAssetReviewIdList)
                .stream()
                .filter(assetReview -> !assetReview.getState()
                        .equals("completed"))
                .collect(Collectors.toList());

        List<EstrFact> storeAssetReviewFactList = estrClient.updateFactsStatus(action, UpdateStoreAssetReviewStatusEstrFact.builder()
                .idList(IdUtil.uuids(storeAssetReviewEntityList.stream().map(StoreAssetReviewEntity::getUuid).collect(Collectors.toList())))
                .build());
        log.info("Asset reviews status {} with action {} updated", storeAssetReviewIdList, action);

        return storeAssetReviewEntityList.stream()
                .map(storeAssetReviewEntity ->
                        buildStoreAssetReview(storeAssetReviewEntity,
                                storeAssetReviewFactList.stream().filter(storeAssetReviewFact ->
                                                String.valueOf(storeAssetReviewFact.getId()).equals(storeAssetReviewEntity.getUuid()))
                                        .findAny().orElse(EstrFact.builder().build())))
                .collect(Collectors.toList());
    }

    @Logger
    public List<AggregatedStoreAssetReview> getAggregatedStoreAssetReviews(Long storeNumber, String storeReviewId, List<String> fields) {
        CompletableFuture<List<StoreAssetReview>> storeAssetReviewListFuture =
                CompletableFuture.supplyAsync(() -> fromStoreAssetReviewEntities(storeAssetReviewRepository.findByStoreReviewId(storeReviewId)), noUserContextPrimaryTaskExecutor);

        CompletableFuture<List<RefrigerationAssetTimeInTarget>> refrigerationAssetTimeInTargetListFuture =
                CompletableFuture.supplyAsync(() -> storeAssetService.getAssetHealthScore(storeNumber), noUserContextPrimaryTaskExecutor);

        CompletableFuture<List<RefrigerationRackMetric>> refrigerationRackMetricListFuture =
                CompletableFuture.supplyAsync(() -> healthMetricsClient.getRefrigerationRackMetricByStore(storeNumber), noUserContextPrimaryTaskExecutor);

        CompletableFuture<List<RefrigerationSensor>> refrigerationSensorListFuture =
                CompletableFuture.supplyAsync(() -> storeReviewAssetService.getAssetsForStore(storeNumber), noUserContextPrimaryTaskExecutor);

        CompletableFuture<List<Asset>> assetListFuture =
                CompletableFuture.supplyAsync(() -> assetService.getAssetsForStore(storeNumber, fields), noUserContextPrimaryTaskExecutor);

        CompletableFuture<List<StoreAssetReviewStateResponse>> assetReviewStateListResponse = CompletableFuture.supplyAsync(() -> estrClient.getAssetReviews(storeReviewId), noUserContextPrimaryTaskExecutor);

        return CompletableFuture.allOf(storeAssetReviewListFuture, refrigerationAssetTimeInTargetListFuture,
                        refrigerationRackMetricListFuture, refrigerationSensorListFuture, assetListFuture, assetReviewStateListResponse)
                .thenApply(ignore -> {
                    List<StoreAssetReview> storeAssetReviewList = storeAssetReviewListFuture.join();
                    Map<Long, Asset> assetMap = assetListFuture.join().stream().collect(Collectors.toMap(Asset::getId, Function.identity(), (a, b) -> a));
                    Map<String, RefrigerationSensor> refrigerationSensorMap = refrigerationSensorListFuture.join().stream().collect(Collectors.toMap(RefrigerationSensor::getId, Function.identity(), (a, b) -> a));
                    Map<String, RefrigerationAssetTimeInTarget> refrigerationAssetTimeInTargetMap = refrigerationAssetTimeInTargetListFuture.join().stream().collect(Collectors.toMap(RefrigerationAssetTimeInTarget::getAssetMappingId, Function.identity(), (a, b) -> a));
                    Map<String, RefrigerationRackMetric> refrigerationRackMetricMap = refrigerationRackMetricListFuture.join().stream().collect(Collectors.toMap(refrigerationRackMetric -> String.join("-", refrigerationRackMetric.getRackCallLetter(), String.valueOf(refrigerationRackMetric.getStoreNumber())), Function.identity(), (a, b) -> a));
                    Map<String, String> assetReviewIdStateMap = assetReviewStateListResponse.join().stream().collect(Collectors.toMap(StoreAssetReviewStateResponse::getStoreAssetReviewId, StoreAssetReviewStateResponse::getStateId, (a, b) -> a));
                    return storeAssetReviewList.stream().map(storeAssetReview -> {
                                var aggregatedStoreAssetReview = new AggregatedStoreAssetReview();
                                aggregatedStoreAssetReview.setAsset(assetMap.getOrDefault(storeAssetReview.getAssetId(), Asset.builder().build()));
                                storeAssetReview.setState(assetReviewIdStateMap.getOrDefault(storeAssetReview.getId(), storeAssetReview.getState()));
                                aggregatedStoreAssetReview.setStoreAssetReview(storeAssetReview);
                                aggregatedStoreAssetReview.setRefrigerationSensor(refrigerationSensorMap.getOrDefault(storeAssetReview.getAssetMappingId(), RefrigerationSensor.builder().build()));
                                aggregatedStoreAssetReview.setRefrigerationAssetTimeInTarget(refrigerationAssetTimeInTargetMap.getOrDefault(storeAssetReview.getAssetMappingId(), RefrigerationRackTimeInTarget.builder().build()));
                                aggregatedStoreAssetReview.setRefrigerationRackMetric(refrigerationRackMetricMap.getOrDefault(String.join("-", aggregatedStoreAssetReview.getRefrigerationSensor().getRackCallLetter(), aggregatedStoreAssetReview.getRefrigerationSensor().getStoreNumber()), RefrigerationRackMetric.builder().build()));
                                return aggregatedStoreAssetReview;
                            }
                    ).collect(Collectors.toList());
                })
                .join();
    }

    @Logger
    public Workflow<StoreAssetReview> getStoreAssetReviewWorkflow(String storeAssetReviewId) {
        StoreAssetReview storeAssetReview = getStoreAssetReview(storeAssetReviewId);
        EstrWorkflow estrWorkflow = estrClient.getWorkflow(storeAssetReview.getUuid());
        return fromWorkflow(estrWorkflow, storeAssetReview);
    }

    @Logger
    public StoreReviewProgress getStoreReviewProgress(String storeReviewId) {
        int total = storeAssetReviewRepository.countByStoreReviewId(storeReviewId);
        int completed = storeAssetReviewRepository.countByStoreReviewIdAndState(storeReviewId, "completed");

        return StoreReviewProgress.builder()
                .storeReviewId(storeReviewId)
                .total(total)
                .completed(completed)
                .build();
    }

    private StoreAssetReview buildStoreAssetReview(StoreAssetReviewEntity entity, EstrFact fact) {
        return StoreAssetReview.builder()
                .id(entity.getId())
                .uuid(fact.getId())
                .storeReviewId(entity.getStoreReviewId())
                .storeNumber(entity.getStoreNumber())
                .assetId(entity.getAssetId())
                .assetMappingId(entity.getAssetMappingId())
                .workOrderId(entity.getWorkOrderId())
                .state(fact.getState())
                .flow(fact.getFlow())
                .build();
    }

    private StoreAssetReview buildStoreAssetReview(String storeAssetReviewId, Long storeNumber, String storeReviewId, RefrigerationSensor asset, EstrFact fact, Long assetId) {
        return StoreAssetReview.builder()
                .id(storeAssetReviewId)
                .uuid(fact.getId())
                .storeReviewId(storeReviewId)
                .storeNumber(storeNumber)
                .assetId(assetId)
                .assetMappingId(asset.getId())
                .state(fact.getState())
                .flow(fact.getFlow())
                .build();
    }

    private List<StoreAssetReview> buildStoreAssetReviews(List<String> storeAssetReviewIds, Long storeNumber, String storeReviewId, List<RefrigerationSensor> assets, List<EstrFact> facts, Map<Long, Long> assetIdMap) {
        return IntStream.range(0, facts.size())
                .mapToObj(i -> buildStoreAssetReview(storeAssetReviewIds.get(i), storeNumber, storeReviewId, assets.get(i), facts.get(i), assetIdMap.getOrDefault(assets.get(i).getEquipmentId(), null)))
                .collect(Collectors.toList());
    }

    private StoreAssetReview fromStoreAssetReviewEntity(StoreAssetReviewEntity entity) {
        return StoreAssetReview.builder()
                .id(entity.getId())
                .uuid(IdUtil.uuid(entity.getUuid()))
                .storeReviewId(entity.getStoreReviewId())
                .storeNumber(entity.getStoreNumber())
                .assetId(entity.getAssetId())
                .assetMappingId(entity.getAssetMappingId())
                .reviewedBy(entity.getReviewedBy())
                .reviewedAt(entity.getReviewedAt())
                .workOrderId(entity.getWorkOrderId())
                .state(entity.getState())
                .flow(entity.getFlow())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .lastModifiedAt(entity.getLastModifiedAt())
                .lastModifiedBy(entity.getLastModifiedBy())
                .build();
    }

    private List<StoreAssetReview> fromStoreAssetReviewEntities(List<StoreAssetReviewEntity> storeAssetReviews) {
        return Optional.ofNullable(storeAssetReviews)
                .orElseGet(Collections::emptyList).stream()
                .map(this::fromStoreAssetReviewEntity)
                .collect(Collectors.toList());
    }

    private Workflow<StoreAssetReview> fromWorkflow(EstrWorkflow estrWorkflow, StoreAssetReview storeAssetReview) {
        return Workflow.<StoreAssetReview>builder()
                .entity(storeAssetReview)
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

}
