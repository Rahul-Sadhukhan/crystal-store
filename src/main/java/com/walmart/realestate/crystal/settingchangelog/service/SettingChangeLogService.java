package com.walmart.realestate.crystal.settingchangelog.service;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.metadata.exception.MetadataItemInvalidException;
import com.walmart.realestate.crystal.metadata.model.MetadataItem;
import com.walmart.realestate.crystal.metadata.service.MetadataItemService;
import com.walmart.realestate.crystal.settingchangelog.entity.SettingChangeLogEntity;
import com.walmart.realestate.crystal.settingchangelog.model.SettingChangeLog;
import com.walmart.realestate.crystal.settingchangelog.repository.SettingChangeLogRepository;
import com.walmart.realestate.crystal.storereview.service.StoreAssetReviewService;
import com.walmart.realestate.crystal.storereview.service.UserAccountService;
import com.walmart.realestate.soteria.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettingChangeLogService {

    private final SettingChangeLogRepository settingChangeLogRepository;

    private final MetadataItemService metadataItemService;

    private final UserAccountService userAccountService;

    private final StoreAssetReviewService storeAssetReviewService;

    @Logger
    public SettingChangeLog createSettingChangeLog(SettingChangeLog settingChangeLog) {

        var settingChangeLogEntity = settingChangeLogRepository.save(buildSettingChangeLogEntity(settingChangeLog));
//        storeAssetReviewService.updateStoreAssetReviewStatus(settingChangeLog.getReferenceId(), settingChangeLog.getAssetMappingId(), "complete", null);
        return buildSettingChangeObject(settingChangeLogEntity);
    }

    @Logger
    public List<SettingChangeLog> createSettingChangeLogs(List<SettingChangeLog> settingChangeLogList) {

        Map<String, MetadataItem> settingToMetadata = metadataItemService.getMetadataItems(new ArrayList<>(settingChangeLogList.stream()
                        .collect(Collectors.groupingBy(SettingChangeLog::getSetting))
                        .keySet()))
                .stream()
                .collect(Collectors.toMap(MetadataItem::getId, Function.identity()));

        Map<String, List<SettingChangeLog>> settingToSettingChangeLog = settingChangeLogList.stream().collect(Collectors.groupingBy(SettingChangeLog::getSetting));

        List<SettingChangeLogEntity> settingChangeLogEntityList = new ArrayList<>();

        settingToSettingChangeLog.forEach((k, v) -> settingChangeLogEntityList.addAll(settingChangeLogRepository.saveAll(v.stream()
                .map(setting -> buildSettingChangeLogEntity(setting, settingToMetadata.get(setting.getSetting())))
                .collect(Collectors.toList()))));
//        storeAssetReviewService.updateStoreAssetReviewStatus(settingChangeLogList.stream().findFirst().get().getReferenceId(), settingChangeLogEntityList.stream().map(SettingChangeLogEntity::getAssetMappingId).collect(Collectors.toList()), "complete");

        return settingChangeLogEntityList.stream()
                .map(this::buildSettingChangeObject)
                .collect(Collectors.toList());
    }

    @Logger
    public SettingChangeLog getSettingChangeLog(String id) {

        Optional<SettingChangeLogEntity> entity = settingChangeLogRepository.findById(id);

        return entity.map(this::buildSettingChangeObject).orElse(null);
    }

    @Logger
    @PostFilter("hasPolicy(filterObject, 'viewStoreReview')")
    public List<SettingChangeLog> getSettingChangeLogs(Long storeNumber, String assetId) {

        List<SettingChangeLogEntity> entities;

        if (Objects.nonNull(storeNumber) && Objects.nonNull(assetId)) {
            entities = settingChangeLogRepository.findByStoreNumberAndAssetMappingId(storeNumber, assetId);
        } else if (Objects.nonNull(storeNumber)) {
            entities = settingChangeLogRepository.findByStoreNumber(storeNumber);
        } else if (Objects.nonNull(assetId)) {
            entities = settingChangeLogRepository.findByAssetMappingId(assetId);
        } else {
            entities = settingChangeLogRepository.findAll();
        }
        return fromSettingChangeLogEntities(entities);
    }

    @Logger
    public List<SettingChangeLog> getSettingsLogByReferenceId(String storeReviewId) {
        return fromSettingChangeLogEntities(settingChangeLogRepository.findByReferenceId(storeReviewId));
    }

    private SettingChangeLogEntity buildSettingChangeLogEntity(SettingChangeLog settingChangeLog) {

        MetadataItem metadataItemSetting = metadataItemService.getMetadataItem(settingChangeLog.getSetting());

        return buildSettingChangeLogEntityHelper(settingChangeLog, metadataItemSetting);
    }

    private SettingChangeLogEntity buildSettingChangeLogEntity(SettingChangeLog settingChangeLog, MetadataItem metadataItem) {

        return buildSettingChangeLogEntityHelper(settingChangeLog, metadataItem);
    }

    private SettingChangeLogEntity buildSettingChangeLogEntityHelper(SettingChangeLog settingChangeLog, MetadataItem metadataItemSetting) {
        if (Objects.nonNull(metadataItemSetting.getUnit()) &&
                settingChangeLog.getUnit().equals(metadataItemSetting.getUnit())) {
            return SettingChangeLogEntity.builder()
                    .referenceId(settingChangeLog.getReferenceId())
                    .assetMappingId(settingChangeLog.getAssetMappingId())
                    .storeNumber(settingChangeLog.getStoreNumber())
                    .setting(settingChangeLog.getSetting())
                    .settingValue(metadataItemSetting.getDefaultValue())
                    .oldValue(settingChangeLog.getOldValue())
                    .newValue(settingChangeLog.getNewValue())
                    .unit(settingChangeLog.getUnit())
                    .notes(settingChangeLog.getNotes())
                    .reason(settingChangeLog.getReason())
                    .build();
        } else throw new MetadataItemInvalidException("Invalid unit!");
    }

    private SettingChangeLog buildSettingChangeObject(SettingChangeLogEntity settingChangeLogEntity) {
        return SettingChangeLog.builder()
                .id(settingChangeLogEntity.getId())
                .referenceId(settingChangeLogEntity.getReferenceId())
                .assetMappingId(settingChangeLogEntity.getAssetMappingId())
                .storeNumber(settingChangeLogEntity.getStoreNumber())
                .setting(settingChangeLogEntity.getSetting())
                .settingValue(settingChangeLogEntity.getSettingValue())
                .oldValue(settingChangeLogEntity.getOldValue())
                .newValue(settingChangeLogEntity.getNewValue())
                .unit(settingChangeLogEntity.getUnit())
                .notes(settingChangeLogEntity.getNotes())
                .reason(settingChangeLogEntity.getReason())
                .source(settingChangeLogEntity.getSource())
                .createdBy(settingChangeLogEntity.getCreatedBy())
                .createdByName(getUserName(settingChangeLogEntity.getCreatedBy()))
                .createdAt(settingChangeLogEntity.getCreatedAt())
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

    private List<SettingChangeLog> fromSettingChangeLogEntities(List<SettingChangeLogEntity> entities) {
        return Optional.ofNullable(entities)
                .orElseGet(Collections::emptyList).stream()
                .map(this::buildSettingChangeObject)
                .collect(Collectors.toList());
    }

}
