package com.walmart.realestate.crystal.settingchangelog.service;

import com.walmart.realestate.crystal.metadata.model.MetadataItem;
import com.walmart.realestate.crystal.metadata.service.MetadataItemService;
import com.walmart.realestate.crystal.settingchangelog.entity.SettingChangeLogEntity;
import com.walmart.realestate.crystal.settingchangelog.model.SettingChangeLog;
import com.walmart.realestate.crystal.settingchangelog.repository.SettingChangeLogRepository;
import com.walmart.realestate.crystal.storereview.config.TestAsyncConfig;
import com.walmart.realestate.crystal.storereview.repository.StoreAssetReviewRepository;
import com.walmart.realestate.crystal.storereview.service.StoreAssetReviewService;
import com.walmart.realestate.crystal.storereview.service.UserAccountService;
import com.walmart.realestate.soteria.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SettingChangeLogService.class, TestAsyncConfig.class})
@ActiveProfiles("test")
class SettingChangeLogServiceTest {

    @Autowired
    private SettingChangeLogService settingChangeLogService;

    @MockBean
    private SettingChangeLogRepository settingChangeLogRepository;

    @MockBean
    private UserAccountService userAccountService;

    @MockBean
    private MetadataItemService metadataItemService;

    @MockBean
    private StoreAssetReviewService storeAssetReviewService;

    @MockBean
    private StoreAssetReviewRepository storeAssetReviewRepository;

    private SettingChangeLog settingChangeLog;

    private List<SettingChangeLog> settingChangeLogList;

    private SettingChangeLogEntity settingChangeLogEntity;

    private List<SettingChangeLogEntity> settingChangeLogEntityList;

    private MetadataItem metadataItem;

    @BeforeEach
    void setUp() {

        settingChangeLog = SettingChangeLog.builder()
                .referenceId("reference1")
                .assetMappingId("1")
                .storeNumber(1L)
                .setting("dummy")
                .oldValue("19")
                .newValue("20")
                .unit("unit")
                .notes("notes is optional")
                .reason("reason is optional")
                .source("source0")
                .createdAt(Instant.ofEpochSecond(1623178393))
                .build();

        settingChangeLogList = Arrays.asList(settingChangeLog,
                SettingChangeLog.builder()
                        .referenceId("reference2")
                        .assetMappingId("2")
                        .storeNumber(2L)
                        .setting("dummy")
                        .oldValue("19")
                        .newValue("20")
                        .unit("unit")
                        .notes("notes is optional")
                        .reason("reason is optional")
                        .source("source0")
                        .createdAt(Instant.ofEpochSecond(1623178393))
                        .build());

        settingChangeLogEntity = SettingChangeLogEntity.builder()
                .id("123")
                .referenceId("reference1")
                .assetMappingId("1")
                .storeNumber(1L)
                .setting("dummy")
                .settingValue("dummyValue")
                .oldValue("19")
                .newValue("20")
                .unit("unit")
                .notes("notes is optional")
                .reason("reason is optional")
                .source("source0")
                .createdBy("user0")
                .createdAt(Instant.ofEpochSecond(1623178393))
                .build();

        settingChangeLogEntityList = Arrays.asList(settingChangeLogEntity,
                SettingChangeLogEntity.builder()
                        .id("124")
                        .referenceId("reference2")
                        .assetMappingId("2")
                        .storeNumber(2L)
                        .setting("dummy")
                        .settingValue("dummyValue")
                        .oldValue("19")
                        .newValue("20")
                        .unit("unit")
                        .notes("notes is optional")
                        .reason("reason is optional")
                        .source("source0")
                        .createdBy("user0")
                        .createdAt(Instant.ofEpochSecond(1623178393))
                        .build());

        metadataItem = MetadataItem.builder()
                .id("dummy")
                .defaultValue("dummyvalue")
                .unit("unit")
                .build();

    }

    @Test
    void testCreateSettingChangeLog() {

        when(settingChangeLogRepository.save(Mockito.any(SettingChangeLogEntity.class))).thenReturn(settingChangeLogEntity);
        when(metadataItemService.getMetadataItem("dummy")).thenReturn(metadataItem);
        when(userAccountService.getUser("user0")).thenReturn(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("Last")
                .build());

        SettingChangeLog actual = settingChangeLogService.createSettingChangeLog(settingChangeLog);

        assertThat(actual.getId()).isEqualTo("123");
        assertThat(actual.getSettingValue()).isEqualTo("dummyValue");
        assertThat(actual.getCreatedByName()).isEqualTo("First Last");
        assertThat(actual.getNotes()).isEqualTo("notes is optional");

        verify(metadataItemService).getMetadataItem("dummy");
        verify(settingChangeLogRepository).save(Mockito.any(SettingChangeLogEntity.class));
        verify(userAccountService).getUser("user0");

    }

    @Test
    void testCreateSettingChangeLogs() {

        List<SettingChangeLogEntity> entityList = Arrays.asList(
                SettingChangeLogEntity.builder()
                        .referenceId("reference1")
                        .assetMappingId("1")
                        .storeNumber(1L)
                        .setting("dummy")
                        .settingValue("dummyvalue")
                        .oldValue("19")
                        .newValue("20")
                        .unit("unit")
                        .notes("notes is optional")
                        .reason("reason is optional")
                        .build(),
                SettingChangeLogEntity.builder()
                        .referenceId("reference2")
                        .assetMappingId("2")
                        .storeNumber(2L)
                        .setting("dummy")
                        .settingValue("dummyvalue")
                        .oldValue("19")
                        .newValue("20")
                        .unit("unit")
                        .notes("notes is optional")
                        .reason("reason is optional")
                        .build());

        when(settingChangeLogRepository.saveAll(entityList)).thenReturn(settingChangeLogEntityList);
        when(metadataItemService.getMetadataItem("dummy")).thenReturn(metadataItem);
        when(metadataItemService.getMetadataItems(Mockito.any())).thenReturn(Collections.singletonList(metadataItem));
        when(userAccountService.getUser("user0")).thenReturn(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("Last")
                .build());

        List<SettingChangeLog> actualList = settingChangeLogService.createSettingChangeLogs(settingChangeLogList);

        assertThat(actualList.size()).isEqualTo(2);
        assertThat(actualList.get(0).getId()).isEqualTo("123");
        assertThat(actualList.get(0).getSettingValue()).isEqualTo("dummyValue");
        assertThat(actualList.get(0).getCreatedByName()).isEqualTo("First Last");
        assertThat(actualList.get(0).getNotes()).isEqualTo("notes is optional");
        assertThat(actualList.get(1).getId()).isEqualTo("124");
        assertThat(actualList.get(1).getReferenceId()).isEqualTo("reference2");

        verify(metadataItemService).getMetadataItems(Mockito.any());
        verify(settingChangeLogRepository).saveAll(entityList);
        verify(userAccountService, times(2)).getUser("user0");

    }

    @Test
    void testGetSettingChangeLog() {

        when(settingChangeLogRepository.findById("123")).thenReturn(Optional.ofNullable(settingChangeLogEntity));
        when(userAccountService.getUser("user0")).thenReturn(User.builder()
                .id("user0")
                .firstName("First")
                .lastName("Last")
                .build());

        SettingChangeLog actual = settingChangeLogService.getSettingChangeLog("123");

        assertThat(actual.getId()).isEqualTo("123");
        assertThat(actual.getSettingValue()).isEqualTo("dummyValue");
        assertThat(actual.getCreatedByName()).isEqualTo("First Last");

        verify(settingChangeLogRepository).findById("123");
        verify(userAccountService).getUser("user0");

    }

    @Test
    void testGetSettingChangeLogs() {

        when(settingChangeLogRepository.findByStoreNumberAndAssetMappingId(1L, "1"))
                .thenReturn(Collections.singletonList(settingChangeLogEntity));

        List<SettingChangeLog> actual = settingChangeLogService.getSettingChangeLogs(1L, "1");

        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual.get(0).getId()).isEqualTo("123");
        assertThat(actual.get(0).getAssetMappingId()).isEqualTo("1");

        verify(settingChangeLogRepository).findByStoreNumberAndAssetMappingId(1L, "1");

    }

}
