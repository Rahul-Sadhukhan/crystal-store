package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationSensor;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.walmart.realestate.crystal.storereview.util.BeanUtils.copyNonNullProperties;
import static java.util.function.Predicate.not;

@RequiredArgsConstructor
@Service
public class StoreReviewAssetService {

    private final HealthMetricsClient healthMetricsClient;

    @Logger
    public List<RefrigerationSensor> getAssetsForStore(Long storeNumber) {
        var rackAssets = healthMetricsClient.getRackAssets(storeNumber)
                .getContent().stream()
                .map(EntityModel::getContent)
                .filter(Objects::nonNull)
                .filter(item -> Objects.nonNull(item.getRackCallLetter()))
                .peek(item -> item.setAssetName(item.getId()))
                .peek(item -> item.setType("rack"))
                .peek(item -> item.setStoreNumber(String.valueOf(storeNumber)))
                .collect(Collectors.groupingBy(RefrigerationSensor::getRackCallLetter, LinkedHashMap::new, Collectors.toList()))
                .values().stream()
                .filter(not(List::isEmpty))
                .map(LinkedList::new)
                .map(assetGroup -> {
                    RefrigerationSensor rackAsset = assetGroup.pop();
                    while (!assetGroup.isEmpty())
                        copyNonNullProperties(assetGroup.pop(), rackAsset);
                    return rackAsset;
                });

        var caseAssets = healthMetricsClient.getCaseAssets(storeNumber)
                .getContent().stream()
                .map(EntityModel::getContent)
                .filter(Objects::nonNull)
                .filter(item -> Objects.nonNull(item.getCaseName()))
                .peek(item -> item.setAssetName(item.getCaseName() + "-" + storeNumber))
                .peek(item -> item.setType("case"))
                .sorted(Comparator.comparing(RefrigerationSensor::getAssetName));

        return Stream.concat(rackAssets, caseAssets)
                .collect(Collectors.toList());
    }

}
