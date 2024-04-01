package com.walmart.realestate.crystal.settingchangelog.service;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.metadata.model.MetadataItem;
import com.walmart.realestate.crystal.metadata.service.MetadataItemService;
import com.walmart.realestate.crystal.settingchangelog.entity.InsightEntity;
import com.walmart.realestate.crystal.settingchangelog.model.Insight;
import com.walmart.realestate.crystal.settingchangelog.repository.InsightRepository;
import com.walmart.realestate.crystal.storereview.service.StoreAssetReviewService;
import com.walmart.realestate.crystal.storereview.service.UserAccountService;
import com.walmart.realestate.soteria.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class InsightService {

    private final InsightRepository insightRepository;

    private final MetadataItemService metadataItemService;

    private final UserAccountService userAccountService;

    private final StoreAssetReviewService storeAssetReviewService;

    @Logger
    public Insight createInsight(Insight insight) {


        var insightEntity = insightRepository.save(buildInsightEntity(insight));

//        storeAssetReviewService.updateStoreAssetReviewStatus(insight.getReferenceId(), insight.getAssetMappingId(), "complete", null);

        return buildInsightObject(insightEntity);
    }

    @Logger
    public List<Insight> createInsights(List<Insight> insightList) {

        Set<String> recommendationSet = insightList.stream()
                .map(Insight::getRecommendations)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        Set<String> probableCausesSet = insightList.stream()
                .map(Insight::getProbableCauses)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        Set<String> observationList = insightList.stream()
                .map(Insight::getObservation)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<String> listCombined = Stream.of(recommendationSet, probableCausesSet, observationList)
                .flatMap(Collection::stream).collect(Collectors.toList());


        Map<String, MetadataItem> settingToMetadata = metadataItemService.getMetadataItems(listCombined)
                .stream()
                .collect(Collectors.toMap(MetadataItem::getId, Function.identity()));

        List<InsightEntity> insightEntityList = insightRepository
                .saveAll(insightList.stream()
                        .map(a -> buildInsightEntity(a, a.getRecommendations() != null ? a.getRecommendations().stream()
                                .map(settingToMetadata::get)
                                .collect(Collectors.toList()) : new ArrayList<>(), a.getProbableCauses() != null ? a.getProbableCauses().stream()
                                .map(settingToMetadata::get)
                                .collect(Collectors.toList()) : new ArrayList<>(), settingToMetadata.get(a.getObservation())))
                        .collect(Collectors.toList()));
//        storeAssetReviewService.updateStoreAssetReviewStatus(insightList.stream().findFirst().get().getReferenceId(), insightList.stream().map(Insight::getAssetMappingId).collect(Collectors.toList()), "complete");
        return insightEntityList.stream()
                .map(this::buildInsightObject)
                .collect(Collectors.toList());
    }

    @Logger
    public Insight getInsight(String id) {

        Optional<InsightEntity> entity = insightRepository.findById(id);

        return entity.map(this::buildInsightObject).orElse(null);
    }

    @Logger
    @PostFilter("hasPolicy(filterObject, 'viewStoreReview')")
    public List<Insight> getInsights(Long storeNumber, String assetId) {

        List<InsightEntity> entities;

        if (Objects.nonNull(storeNumber) && Objects.nonNull(assetId)) {
            entities = insightRepository.findByStoreNumberAndAssetMappingId(storeNumber, assetId);
        } else if (Objects.nonNull(storeNumber)) {
            entities = insightRepository.findByStoreNumber(storeNumber);
        } else if (Objects.nonNull(assetId)) {
            entities = insightRepository.findByAssetMappingId(assetId);
        } else {
            entities = insightRepository.findAll();
        }
        return fromInsightEntities(entities);
    }

    @Logger
    public List<Insight> getInsightsByReferenceId(String storeReviewNumber) {
        return fromInsightEntities(insightRepository.findByReferenceId(storeReviewNumber));
    }

    private InsightEntity buildInsightEntity(Insight insight) {

        List<MetadataItem> metadataItemRecommendations = metadataItemService.getMetadataItems(insight.getRecommendations());
        List<MetadataItem> metadataItemProbableCauses = metadataItemService.getMetadataItems(insight.getProbableCauses());
        MetadataItem metadataItemObservation = metadataItemService.getMetadataItem(insight.getObservation());

        return buildInsightEntityHelper(insight, metadataItemRecommendations, metadataItemProbableCauses, metadataItemObservation);
    }

    private InsightEntity buildInsightEntity(Insight insight, List<MetadataItem> metadataItemRecommendations, List<MetadataItem> metadataItemProbableCauses, MetadataItem metadataItemObservation) {

        return buildInsightEntityHelper(insight, metadataItemRecommendations, metadataItemProbableCauses, metadataItemObservation);
    }

    private InsightEntity buildInsightEntityHelper(Insight insight, List<MetadataItem> metadataItemRecommendations, List<MetadataItem> metadataItemProbableCauses, MetadataItem metadataItemObservation) {
        return InsightEntity.builder()
                .referenceId(insight.getReferenceId())
                .assetMappingId(insight.getAssetMappingId())
                .storeNumber(insight.getStoreNumber())
                .recommendations(insight.getRecommendations())
                .recommendationValues(metadataItemRecommendations.stream()
                        .map(MetadataItem::getDefaultValue)
                        .collect(Collectors.toList()))
                .recommendationNotes(insight.getRecommendationNotes())
                .probableCauses(insight.getProbableCauses())
                .probableCauseValues(metadataItemProbableCauses.stream()
                        .map(MetadataItem::getDefaultValue)
                        .collect(Collectors.toList()))
                .probableCauseNotes(insight.getProbableCauseNotes())
                .observation(insight.getObservation())
                .observationValue(metadataItemObservation.getDefaultValue())
                .observationNotes(insight.getObservationNotes())
                .build();
    }

    private Insight buildInsightObject(InsightEntity insightEntity) {
        return Insight.builder()
                .id(insightEntity.getId())
                .referenceId(insightEntity.getReferenceId())
                .assetMappingId(insightEntity.getAssetMappingId())
                .storeNumber(insightEntity.getStoreNumber())
                .recommendations(insightEntity.getRecommendations())
                .recommendationValues(insightEntity.getRecommendationValues())
                .recommendationNotes(insightEntity.getRecommendationNotes())
                .probableCauses(insightEntity.getProbableCauses())
                .probableCauseValues(insightEntity.getProbableCauseValues())
                .probableCauseNotes(insightEntity.getProbableCauseNotes())
                .observation(insightEntity.getObservation())
                .observationValue(insightEntity.getObservationValue())
                .observationNotes(insightEntity.getObservationNotes())
                .source(insightEntity.getSource())
                .createdBy(insightEntity.getCreatedBy())
                .createdByName(getUserName(insightEntity.getCreatedBy()))
                .createdAt(insightEntity.getCreatedAt())
                .build();
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

    private List<Insight> fromInsightEntities(List<InsightEntity> entities) {
        return Optional.ofNullable(entities)
                .orElseGet(Collections::emptyList).stream()
                .map(this::buildInsightObject)
                .collect(Collectors.toList());
    }

}
