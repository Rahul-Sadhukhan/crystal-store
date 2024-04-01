package com.walmart.realestate.crystal.storereview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationAssetTimeInTarget;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationStoreTimeInTarget;
import com.walmart.realestate.crystal.storereview.client.storeinfo.model.facilitydetails.Location;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewAssetHealthScoreEntity;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewStoreHealthScoreEntity;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.model.StoreReviewAssetHealthScore;
import com.walmart.realestate.crystal.storereview.model.StoreReviewStoreHealthScore;
import com.walmart.realestate.crystal.storereview.repository.StoreReviewAssetHealthScoreRepository;
import com.walmart.realestate.crystal.storereview.repository.StoreReviewStoreHealthScoreRepository;
import com.walmart.realestate.crystal.storereview.service.healthscoredetailshandler.HealthScoreFetchHandler;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class StoreReviewHealthScoreService {

    public static final String STARTED_EVENT = "com.walmart.realestate.crystal.storereview.event.StoreReviewStartedEvent";
    public static final String MONITORING_EVENT = "com.walmart.realestate.crystal.storereview.event.StoreReviewMonitoringEvent";
    public static final String DETERIORATED_EVENT = "com.walmart.realestate.crystal.storereview.event.StoreReviewDeterioratedEvent";
    public static final String POST_REVIEW_EVENT = "com.walmart.realestate.crystal.storereview.event.StoreReviewPostReviewEvent";
    public static final String POST_MAINTENANCE_EVENT = "com.walmart.realestate.crystal.storereview.event.StoreReviewPostMaintenanceEvent";

    public static final String POST_PREVENTIVE_MAINTENANCE_EVENT = "com.walmart.realestate.crystal.storereview.event.StoreReviewPostPreventiveMaintenanceEvent";
    private static final List<String> subscribedEvents = Arrays.asList(STARTED_EVENT, MONITORING_EVENT, DETERIORATED_EVENT, POST_REVIEW_EVENT, POST_MAINTENANCE_EVENT,POST_PREVENTIVE_MAINTENANCE_EVENT);

    private final StoreReviewService storeReviewService;

    private final StoreAssetService storeAssetService;

    private final StoreService storeService;

    private final StoreReviewStoreHealthScoreRepository storeReviewStoreHealthScoreRepository;

    private final StoreReviewAssetHealthScoreRepository storeReviewAssetHealthScoreRepository;

    private final List<HealthScoreFetchHandler> healthScoreFetchHandlers;

    @Logger
    public Optional<StoreReviewStoreHealthScore> getStoreReviewStoreHealthScore(String storeReviewId) {
        return toStoreHealthScore(storeReviewStoreHealthScoreRepository.findByStoreReviewId(storeReviewId));
    }

    @Logger
    public List<StoreReviewAssetHealthScore> getStoreReviewAssetHealthScores(String storeReviewId) {
        return toAssetHealthScores(storeReviewAssetHealthScoreRepository.findByStoreReviewId(storeReviewId));
    }

    @Logger
    public List<StoreReviewAssetHealthScore> getStoreReviewAssetHealthScores(List<String> storeReviewIds) {
        return toAssetHealthScores(storeReviewAssetHealthScoreRepository.findByStoreReviewIdIn(storeReviewIds));
    }

    @Logger
    @SneakyThrows
    @Transactional
    public void processEvent(JsonNode eventPayload) {
        String eventName = eventPayload.get("eventName").textValue();
        if (!subscribedEvents.contains(eventName)) return;
        String uuid = eventPayload.get("aggregateId").textValue();
        Instant timestamp = Optional.ofNullable(eventPayload.get("timestamp").textValue())
                .map(Instant::parse)
                .orElseThrow(IllegalArgumentException::new);

        StoreReview storeReview = storeReviewService.getStoreReviewByAggregateId(uuid);
        Long storeNumber = storeReview.getStoreNumber();

        String storeReviewId = storeReview.getId();
        if (STARTED_EVENT.equals(eventName)) {
            createHealthScoresAtReviewStart(storeReviewId, storeNumber, timestamp);
        } else if (MONITORING_EVENT.equals(eventName)) {
            updateHealthScoresAtReviewEnd(storeReviewId, storeNumber, timestamp);
        } else if (DETERIORATED_EVENT.equals(eventName)) {
            clearHealthScoresAtReviewRestart(storeReviewId);
        } else if (POST_PREVENTIVE_MAINTENANCE_EVENT.equals(eventName)) {
            updateHealthScoreAtPostReviewEnd(storeReviewId, storeNumber, timestamp);
        } else {
            updateHealthScoresAtStatuses(storeReview);
        }
    }

    @Logger
    @Retry(name = "snapshot")
    public void updateHealthScoreAtPostReviewEnd(String storeReviewId, Long storeNumber, Instant timestamp) {
        RefrigerationStoreTimeInTarget storeHealthScore = storeAssetService.getStoreHealthScore(storeNumber, timestamp);
        List<RefrigerationAssetTimeInTarget> assetHealthScores = storeAssetService.getAssetHealthScore(storeNumber, timestamp);

        StoreReviewStoreHealthScoreEntity storeHealthScoreEntity = storeReviewStoreHealthScoreRepository.findByStoreReviewId(storeReviewId)
                .orElseThrow(NoSuchElementException::new);

        storeHealthScoreEntity.setPostPreventiveMaintenanceTimestamp(timestamp);
        storeHealthScoreEntity.setPostPreventiveMaintenanceScore(storeHealthScore.getTimeInTarget());
        storeHealthScoreEntity.setPostPreventiveMaintenanceScoreTimestamp(storeHealthScore.getRunTime());

        storeReviewStoreHealthScoreRepository.save(storeHealthScoreEntity);

        List<StoreReviewAssetHealthScoreEntity> assetHealthScoreEntities = storeReviewAssetHealthScoreRepository.findByStoreReviewId(storeReviewId);
        Map<String, StoreReviewAssetHealthScoreEntity> assetHealthScoreEntityMap = assetHealthScoreEntities.stream()
                .collect(Collectors.toMap(StoreReviewAssetHealthScoreEntity::getAssetMappingId, Function.identity()));

        assetHealthScores.forEach(assetHealthScore -> Optional.ofNullable(assetHealthScoreEntityMap.get(assetHealthScore.getAssetMappingId()))
                .ifPresent(assetHealthScoreEntity -> {
                    assetHealthScoreEntity.setPostPreventiveMaintenanceTimestamp(timestamp);
                    assetHealthScoreEntity.setPostPreventiveMaintenanceScore(assetHealthScore.getTimeInTarget());
                    assetHealthScoreEntity.setPostPreventiveMaintenanceScoreTimestamp(assetHealthScore.getRunTime());
                }));

        storeReviewAssetHealthScoreRepository.saveAll(assetHealthScoreEntities);
    }

    @Logger
    @Retry(name = "snapshot")
    public void createHealthScoresAtReviewStart(String storeReviewId, Long storeNumber, Instant timestamp) {
        RefrigerationStoreTimeInTarget storeHealthScore = storeAssetService.getStoreHealthScore(storeNumber, timestamp);
        List<RefrigerationAssetTimeInTarget> assetHealthScores = storeAssetService.getAssetHealthScore(storeNumber, timestamp);

        StoreReviewStoreHealthScoreEntity storeHealthScoreEntity = storeReviewStoreHealthScoreRepository.findByStoreReviewId(storeReviewId)
                .orElseGet(StoreReviewStoreHealthScoreEntity::new);

        storeHealthScoreEntity.setStoreReviewId(storeReviewId);
        storeHealthScoreEntity.setStoreNumber(storeHealthScore.getStoreNumber());
        storeHealthScoreEntity.setReviewStartTimestamp(timestamp);
        storeHealthScoreEntity.setHealthScoreStart(storeHealthScore.getTimeInTarget());
        storeHealthScoreEntity.setTimestampStart(storeHealthScore.getRunTime());

        storeReviewStoreHealthScoreRepository.save(storeHealthScoreEntity);

        Map<String, StoreReviewAssetHealthScoreEntity> assetHealthScoreEntityMap = storeReviewAssetHealthScoreRepository.findByStoreReviewId(storeReviewId).stream()
                .collect(Collectors.toMap(StoreReviewAssetHealthScoreEntity::getAssetMappingId, Function.identity()));

        List<StoreReviewAssetHealthScoreEntity> storeReviewAssetHealthScoreEntityEntities = assetHealthScores.stream()
                .map(assetHealthScore -> {
                    StoreReviewAssetHealthScoreEntity storeReviewAssetHealthScoreEntity = Optional.ofNullable(assetHealthScoreEntityMap.get(assetHealthScore.getAssetMappingId()))
                            .orElseGet(StoreReviewAssetHealthScoreEntity::new);
                    storeReviewAssetHealthScoreEntity.setStoreReviewId(storeReviewId);
                    storeReviewAssetHealthScoreEntity.setAssetMappingId(assetHealthScore.getAssetMappingId());
                    storeReviewAssetHealthScoreEntity.setReviewStartTimestamp(timestamp);
                    storeReviewAssetHealthScoreEntity.setHealthScoreStart(assetHealthScore.getTimeInTarget());
                    storeReviewAssetHealthScoreEntity.setTimestampStart(assetHealthScore.getRunTime());
                    return storeReviewAssetHealthScoreEntity;
                })
                .collect(Collectors.toList());

        storeReviewAssetHealthScoreRepository.saveAll(storeReviewAssetHealthScoreEntityEntities);
    }

    @Logger
    @Retry(name = "snapshot")
    public void updateHealthScoresAtReviewEnd(String storeReviewId, Long storeNumber, Instant timestamp) {
        RefrigerationStoreTimeInTarget storeHealthScore = storeAssetService.getStoreHealthScore(storeNumber, timestamp);
        List<RefrigerationAssetTimeInTarget> assetHealthScores = storeAssetService.getAssetHealthScore(storeNumber, timestamp);

        StoreReviewStoreHealthScoreEntity storeHealthScoreEntity = storeReviewStoreHealthScoreRepository.findByStoreReviewId(storeReviewId)
                .orElseThrow(NoSuchElementException::new);

        storeHealthScoreEntity.setReviewEndTimestamp(timestamp);
        storeHealthScoreEntity.setHealthScoreEnd(storeHealthScore.getTimeInTarget());
        storeHealthScoreEntity.setTimestampEnd(storeHealthScore.getRunTime());

        storeReviewStoreHealthScoreRepository.save(storeHealthScoreEntity);

        List<StoreReviewAssetHealthScoreEntity> assetHealthScoreEntities = storeReviewAssetHealthScoreRepository.findByStoreReviewId(storeReviewId);
        Map<String, StoreReviewAssetHealthScoreEntity> assetHealthScoreEntityMap = assetHealthScoreEntities.stream()
                .collect(Collectors.toMap(StoreReviewAssetHealthScoreEntity::getAssetMappingId, Function.identity()));

        assetHealthScores.forEach(assetHealthScore -> Optional.ofNullable(assetHealthScoreEntityMap.get(assetHealthScore.getAssetMappingId()))
                .ifPresent(assetHealthScoreEntity -> {
                    assetHealthScoreEntity.setReviewEndTimestamp(timestamp);
                    assetHealthScoreEntity.setHealthScoreEnd(assetHealthScore.getTimeInTarget());
                    assetHealthScoreEntity.setTimestampEnd(assetHealthScore.getRunTime());
                    assetHealthScoreEntity.setTargetTemperatureEnd(assetHealthScore.getTargetTemperature());
                    assetHealthScoreEntity.setLowCutInTemperatureEnd(assetHealthScore.getLowCutInTemperature());
                    assetHealthScoreEntity.setLowCutOutTemperatureEnd(assetHealthScore.getLowCutOutTemperature());
                    assetHealthScoreEntity.setAverageTemperatureEnd(assetHealthScore.getAverageTemperature());

                }));

        storeReviewAssetHealthScoreRepository.saveAll(assetHealthScoreEntities);
    }

    private void clearHealthScoresAtReviewRestart(String storeReviewId) {
        StoreReviewStoreHealthScoreEntity storeHealthScoreEntity = storeReviewStoreHealthScoreRepository.findByStoreReviewId(storeReviewId)
                .orElseThrow(NoSuchElementException::new);

        storeHealthScoreEntity.setReviewEndTimestamp(null);
        storeHealthScoreEntity.setHealthScoreEnd(null);
        storeHealthScoreEntity.setTimestampEnd(null);
        storeHealthScoreEntity.setPostPreventiveMaintenanceScoreTimestamp(null);
        storeHealthScoreEntity.setPostPreventiveMaintenanceScore(null);
        storeHealthScoreEntity.setPostPreventiveMaintenanceTimestamp(null);
        List<StoreReviewAssetHealthScoreEntity> assetHealthScoreEntities = storeReviewAssetHealthScoreRepository.findByStoreReviewId(storeReviewId);

        assetHealthScoreEntities.forEach(assetHealthScoreEntity -> {
            assetHealthScoreEntity.setReviewEndTimestamp(null);
            assetHealthScoreEntity.setHealthScoreEnd(null);
            assetHealthScoreEntity.setTimestampEnd(null);
            assetHealthScoreEntity.setTargetTemperatureEnd(null);
            assetHealthScoreEntity.setLowCutInTemperatureEnd(null);
            assetHealthScoreEntity.setLowCutOutTemperatureEnd(null);
            assetHealthScoreEntity.setAverageTemperatureEnd(null);
            assetHealthScoreEntity.setPostPreventiveMaintenanceScoreTimestamp(null);
            assetHealthScoreEntity.setPostPreventiveMaintenanceScore(null);
            assetHealthScoreEntity.setPostPreventiveMaintenanceTimestamp(null);
        });

        healthScoreFetchHandlers.stream()
                .sorted(Comparator.comparing(HealthScoreFetchHandler::getPriority))
                .forEach(handler -> handler.clear(storeHealthScoreEntity, assetHealthScoreEntities));

        storeReviewStoreHealthScoreRepository.save(storeHealthScoreEntity);
        storeReviewAssetHealthScoreRepository.saveAll(assetHealthScoreEntities);
    }

    private void updateHealthScoresAtStatuses(StoreReview storeReview) {
        Location location = storeService.getStoreInfo(storeReview.getStoreNumber()).getFacilityDetails().get(0).getLocation();
        ZoneId storeTimeZone = ZoneId.of(location.getLocationTimeZone().getDstTimeZone().getTimeZoneId());

        updateHealthScoresAtStatuses(storeReview, storeTimeZone, false);
    }

    public void updateHealthScoresAtStatuses(String storeReviewId, ZoneId storeTimeZone, boolean healthScoresOverride) {
        StoreReview storeReview = storeReviewService.getStoreReview(storeReviewId);
        updateHealthScoresAtStatuses(storeReview, storeTimeZone, healthScoresOverride);
    }

    private void updateHealthScoresAtStatuses(StoreReview storeReview, ZoneId storeTimeZone, boolean healthScoresOverride) {
        if (storeReview.getStartedAt() == null || storeReview.getMonitoringStartedAt() == null) {
            return;
        }

        Optional<StoreReviewStoreHealthScoreEntity> storeHealthScoreEntity = storeReviewStoreHealthScoreRepository.findByStoreReviewId(storeReview.getId());
        List<StoreReviewAssetHealthScoreEntity> assetHealthScoreEntities = storeReviewAssetHealthScoreRepository.findByStoreReviewId(storeReview.getId());

        updateHealthScoresAtStatuses(storeReview, storeTimeZone, storeHealthScoreEntity.orElseThrow(NoSuchElementException::new), assetHealthScoreEntities, healthScoresOverride);
    }

    @Logger
    public void updateHealthScoresAtStatuses(StoreReview storeReview, ZoneId storeTimeZone, StoreReviewStoreHealthScoreEntity storeHealthScoreEntity, List<StoreReviewAssetHealthScoreEntity> assetHealthScoreEntities, boolean healthScoresOverride) {
        healthScoreFetchHandlers.stream()
                .sorted(Comparator.comparing(HealthScoreFetchHandler::getPriority))
                .forEach(handler -> handler.populate(storeReview, storeTimeZone, storeHealthScoreEntity, assetHealthScoreEntities, healthScoresOverride));

        storeReviewStoreHealthScoreRepository.save(storeHealthScoreEntity);
        storeReviewAssetHealthScoreRepository.saveAll(assetHealthScoreEntities);
    }

    private Optional<StoreReviewStoreHealthScore> toStoreHealthScore(Optional<StoreReviewStoreHealthScoreEntity> storeHealthScoreEntity) {
        return storeHealthScoreEntity.map(entity -> StoreReviewStoreHealthScore.builder()
                .storeReviewId(entity.getStoreReviewId())
                .storeNumber(entity.getStoreNumber())
                .preReviewTimestamp(entity.getPreReviewTimestamp())
                .preReviewScore(entity.getPreReviewScore())
                .preReviewScoreTimestamp(entity.getPreReviewScoreTimestamp())
                .reviewStartTimestamp(entity.getReviewStartTimestamp())
                .healthScoreStart(entity.getHealthScoreStart())
                .timestampStart(entity.getTimestampStart())
                .reviewEndTimestamp(entity.getReviewEndTimestamp())
                .healthScoreEnd(entity.getHealthScoreEnd())
                .timestampEnd(entity.getTimestampEnd())
                .postReviewTimestamp(entity.getPostReviewTimestamp())
                .postReviewScore(entity.getPostReviewScore())
                .postReviewScoreTimestamp(entity.getPostReviewScoreTimestamp())
                .postMaintenanceTimestamp(entity.getPostMaintenanceTimestamp())
                .postMaintenanceScore(entity.getPostMaintenanceScore())
                .postMaintenanceScoreTimestamp(entity.getPostMaintenanceScoreTimestamp())
                .postPreventiveMaintenanceScore(entity.getPostPreventiveMaintenanceScore())
                .postPreventiveMaintenanceScoreTimestamp(entity.getPostPreventiveMaintenanceScoreTimestamp())
                .postPreventiveMaintenanceTimestamp(entity.getPostPreventiveMaintenanceTimestamp())
                .build());
    }

    private List<StoreReviewAssetHealthScore> toAssetHealthScores(List<StoreReviewAssetHealthScoreEntity> assetHealthScoreEntities) {
        return assetHealthScoreEntities.stream()
                .map(this::toAssetHealthScore)
                .collect(Collectors.toList());
    }

    private StoreReviewAssetHealthScore toAssetHealthScore(StoreReviewAssetHealthScoreEntity entity) {
        return StoreReviewAssetHealthScore.builder()
                .storeReviewId(entity.getStoreReviewId())
                .assetMappingId(entity.getAssetMappingId())
                .preReviewTimestamp(entity.getPreReviewTimestamp())
                .preReviewScore(entity.getPreReviewScore())
                .preReviewScoreTimestamp(entity.getPreReviewScoreTimestamp())
                .reviewStartTimestamp(entity.getReviewStartTimestamp())
                .healthScoreStart(entity.getHealthScoreStart())
                .timestampStart(entity.getTimestampStart())
                .reviewEndTimestamp(entity.getReviewEndTimestamp())
                .healthScoreEnd(entity.getHealthScoreEnd())
                .timestampEnd(entity.getTimestampEnd())
                .targetTemperatureEnd(entity.getTargetTemperatureEnd())
                .lowCutInTemperatureEnd(entity.getLowCutInTemperatureEnd())
                .lowCutOutTemperatureEnd(entity.getLowCutOutTemperatureEnd())
                .averageTemperatureEnd(entity.getAverageTemperatureEnd())
                .postReviewTimestamp(entity.getPostReviewTimestamp())
                .postReviewScore(entity.getPostReviewScore())
                .postReviewScoreTimestamp(entity.getPostReviewScoreTimestamp())
                .postMaintenanceTimestamp(entity.getPostMaintenanceTimestamp())
                .postMaintenanceScore(entity.getPostMaintenanceScore())
                .postMaintenanceScoreTimestamp(entity.getPostMaintenanceScoreTimestamp())
                .postPreventiveMaintenanceScore(entity.getPostPreventiveMaintenanceScore())
                .postPreventiveMaintenanceScoreTimestamp(entity.getPostPreventiveMaintenanceScoreTimestamp())
                .postPreventiveMaintenanceTimestamp(entity.getPostPreventiveMaintenanceTimestamp())
                .build();
    }

}
