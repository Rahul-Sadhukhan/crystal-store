package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.asset.model.Asset;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.*;
import de.qaware.tools.collectioncacheableforspring.CollectionCacheable;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.task.TaskExecutor;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class StoreAssetService {

    private final StoreReviewAssetService storeReviewAssetService;

    private final AssetService assetService;

    private final HealthMetricsClient healthMetricsClient;

    private final TaskExecutor noUserContextSecondaryTaskExecutor;

    @Logger
    @CollectionCacheable("storeHealthScores")
    public Map<Long, RefrigerationStoreTimeInTarget> getStoreHealthScore(List<Long> storeNumbers) {
        return healthMetricsClient.getStoreHealthScores(storeNumbers)
                .getContent().stream()
                .map(EntityModel::getContent)
                .collect(StoreHealthScoreCollector.collector());
    }

    @Logger
    @Cacheable("storeHealthScoresPerpetual")
    public RefrigerationStoreTimeInTarget getStoreHealthScore(Long storeNumber, Instant timestamp) {
        return healthMetricsClient.getStoreHealthScore(storeNumber, timestamp, timestamp.atZone(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ISO_DATE))
                .getContent();
    }

    @Logger
    @Cacheable("assetHealthScores")
    public List<RefrigerationAssetTimeInTarget> getAssetHealthScore(Long storeNumber) {
        CompletableFuture<List<RefrigerationRackTimeInTarget>> rackHealthScoreFuture =
                CompletableFuture.supplyAsync(() -> getRackHealthScore(storeNumber), noUserContextSecondaryTaskExecutor);
        CompletableFuture<List<RefrigerationCaseTimeInTarget>> caseHealthScoreFuture =
                CompletableFuture.supplyAsync(() -> getCaseHealthScore(storeNumber), noUserContextSecondaryTaskExecutor);
        return (List<RefrigerationAssetTimeInTarget>) getRefrigerationAssetHealthScores(storeNumber, rackHealthScoreFuture, caseHealthScoreFuture);
    }

    @Logger
    @Cacheable("assetHealthScoresPerpetual")
    public List<RefrigerationAssetTimeInTarget> getAssetHealthScore(Long storeNumber, Instant timestamp) {
        CompletableFuture<List<RefrigerationRackTimeInTarget>> rackHealthScoreFuture =
                CompletableFuture.supplyAsync(() -> getRackHealthScore(storeNumber, timestamp), noUserContextSecondaryTaskExecutor);
        CompletableFuture<List<RefrigerationCaseTimeInTarget>> caseHealthScoreFuture =
                CompletableFuture.supplyAsync(() -> getCaseHealthScore(storeNumber, timestamp), noUserContextSecondaryTaskExecutor);
        return (List<RefrigerationAssetTimeInTarget>) getRefrigerationAssetHealthScores(storeNumber, rackHealthScoreFuture, caseHealthScoreFuture);
    }

    private List<? extends RefrigerationAssetTimeInTarget> getRefrigerationAssetHealthScores(Long storeNumber,
                                                                                             CompletableFuture<List<RefrigerationRackTimeInTarget>> rackHealthScoreFuture,
                                                                                             CompletableFuture<List<RefrigerationCaseTimeInTarget>> caseHealthScoreFuture) {

        Map<Long, Asset> assetsMap = assetService.getAssetsForStore(storeNumber, null).stream()
                .collect(Collectors.toMap(
                        Asset::getDwEquipmentId,
                        Function.identity(),
                        (a, b) -> b));

        List<RefrigerationSensor> refrigerationAssets = storeReviewAssetService.getAssetsForStore(storeNumber).stream().map(refrigerationSensor -> {
                    if (assetsMap.containsKey(refrigerationSensor.getEquipmentId())) {
                        var asset = assetsMap.get(refrigerationSensor.getEquipmentId());
                        refrigerationSensor.setTagId(asset.getTagId());
                        refrigerationSensor.setQrCode(asset.getQrCode());
                    }
                    return refrigerationSensor;
                })
                .collect(Collectors.toList());


        return CompletableFuture.allOf(rackHealthScoreFuture, caseHealthScoreFuture)
                .thenApply(ignored -> {
                    Map<String, RefrigerationRackTimeInTarget> refrigerationRackTimeInTargetMap = rackHealthScoreFuture.join().stream().collect(Collectors.toMap(refrigerationRack -> String.join("-", refrigerationRack.getRackCallLetter(), String.valueOf(refrigerationRack.getStoreNumber())), Function.identity(), (a, b) -> b));
                    Map<String, RefrigerationCaseTimeInTarget> refrigerationCaseTimeInTargetMap = caseHealthScoreFuture.join().stream().collect(Collectors.toMap(RefrigerationCaseTimeInTarget::getTemperatureSensorId, Function.identity(), (a, b) -> b));

                    return refrigerationAssets.stream().map(refrigerationSensor -> {
                        if (refrigerationSensor.getType().equals("rack")) {

                            var refrigerationRackTimeInTarget = refrigerationRackTimeInTargetMap.getOrDefault(refrigerationSensor.getId(), RefrigerationRackTimeInTarget.builder().build());

                            refrigerationRackTimeInTarget.setQrCode(refrigerationSensor.getQrCode());
                            refrigerationRackTimeInTarget.setTagId(refrigerationSensor.getTagId());
                            refrigerationRackTimeInTarget.setAssetMappingId(refrigerationSensor.getId());
                            refrigerationRackTimeInTarget.setRackId(String.join("-", refrigerationRackTimeInTarget.getRackCallLetter(), String.valueOf(refrigerationRackTimeInTarget.getStoreNumber())));
                            return refrigerationRackTimeInTarget;

                        } else {

                            var refrigerationCaseTimeInTarget = refrigerationCaseTimeInTargetMap.getOrDefault(refrigerationSensor.getId(), RefrigerationCaseTimeInTarget.builder().build());

                            refrigerationCaseTimeInTarget.setQrCode(refrigerationSensor.getQrCode());
                            refrigerationCaseTimeInTarget.setTagId(refrigerationSensor.getTagId());
                            refrigerationCaseTimeInTarget.setAssetMappingId(refrigerationSensor.getId());
                            refrigerationCaseTimeInTarget.setCaseId(String.join("-", refrigerationCaseTimeInTarget.getCaseName(), String.valueOf(refrigerationCaseTimeInTarget.getStoreNumber())));
                            refrigerationCaseTimeInTarget.setSensorLabel(refrigerationSensor.getSensorLabel());
                            refrigerationCaseTimeInTarget.setRackCallLetter(refrigerationSensor.getRackCallLetter());
                            return refrigerationCaseTimeInTarget;

                        }
                    }).collect(Collectors.toList());
                })
                .join();
    }

    private List<RefrigerationRackTimeInTarget> getRackHealthScore(Long storeNumber) {
        return healthMetricsClient.getRackHealthScore(storeNumber)
                .getContent().stream()
                .map(EntityModel::getContent)
                .collect(Collectors.toList());
    }

    private List<RefrigerationRackTimeInTarget> getRackHealthScore(Long storeNumber, Instant timestamp) {
        return healthMetricsClient.getRackHealthScore(storeNumber, timestamp, timestamp.atZone(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ISO_DATE))
                .getContent().stream()
                .map(EntityModel::getContent)
                .collect(Collectors.toList());
    }

    private List<RefrigerationCaseTimeInTarget> getCaseHealthScore(Long storeNumber) {
        return healthMetricsClient.getCaseHealthScore(storeNumber)
                .getContent().stream()
                .map(EntityModel::getContent)
                .collect(Collectors.toList());
    }

    private List<RefrigerationCaseTimeInTarget> getCaseHealthScore(Long storeNumber, Instant timestamp) {
        return healthMetricsClient.getCaseHealthScore(storeNumber, timestamp, timestamp.atZone(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ISO_DATE))
                .getContent().stream()
                .map(EntityModel::getContent)
                .collect(Collectors.toList());
    }

    @Logger
    public List<RefrigerationAssetTimeInTarget> getAssetHealthScoreByStoreNumberDate(Long storeNumber, LocalDate requestDate) {
        if (requestDate == null) {
            return getAssetHealthScore(storeNumber);
        } else {
            return getAssetHealthScore(storeNumber, requestDate.plusDays(1).atStartOfDay().minusSeconds(1).toInstant(ZoneOffset.UTC));
        }
    }

    private static class StoreHealthScoreCollector implements Collector<RefrigerationStoreTimeInTarget, Map<Long, RefrigerationStoreTimeInTarget>, Map<Long, RefrigerationStoreTimeInTarget>> {

        private final Set<Long> storeNumbers = new HashSet<>();

        private static StoreHealthScoreCollector collector() {
            return new StoreHealthScoreCollector();
        }

        @Override
        public Supplier<Map<Long, RefrigerationStoreTimeInTarget>> supplier() {
            return TreeMap::new;
        }

        @Override
        public BiConsumer<Map<Long, RefrigerationStoreTimeInTarget>, RefrigerationStoreTimeInTarget> accumulator() {
            return (map, item) -> {
                if (Objects.isNull(item.getStoreNumber()) || storeNumbers.contains(item.getStoreNumber())) return;

                map.put(item.getStoreNumber(), item);
                storeNumbers.add(item.getStoreNumber());
            };
        }

        @Override
        public BinaryOperator<Map<Long, RefrigerationStoreTimeInTarget>> combiner() {
            return (a, b) -> {
                a.putAll(b);
                return a;
            };
        }

        @Override
        public Function<Map<Long, RefrigerationStoreTimeInTarget>, Map<Long, RefrigerationStoreTimeInTarget>> finisher() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Collector.Characteristics> characteristics() {
            return Collections.singleton(Characteristics.IDENTITY_FINISH);
        }

    }

}
