package com.walmart.realestate.crystal.settingchangelog.service;

import com.walmart.core.realestate.cerberus.bean.CerberusUserInformation;
import com.walmart.realestate.crystal.settingchangelog.entity.SettingChangeLogEntity;
import com.walmart.realestate.crystal.settingchangelog.repository.SettingChangeLogRepository;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.service.StoreReviewService;
import com.walmart.realestate.soteria.model.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityNotFoundException;
import javax.validation.ValidationException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SettingChangeLogDeleteService.class})
@ActiveProfiles("test")
class SettingChangeLogDeleteServiceTest {

    @Autowired
    private SettingChangeLogDeleteService settingChangeLogDeleteService;

    @MockBean
    private SettingChangeLogRepository settingChangeLogRepository;

    @MockBean
    private StoreReviewService storeReviewService;

    private UserContext userContext;

    @BeforeEach
    void setup() {
        CerberusUserInformation cerberusUserInformation = new CerberusUserInformation();
        cerberusUserInformation.setUserName("user0");
        userContext = new UserContext(cerberusUserInformation, new HashSet<>());
    }

    @Test
    void testDeleteSettingChangeLog() {
        when(settingChangeLogRepository.findById("123")).thenReturn(Optional.of(SettingChangeLogEntity.builder()
                .id("123")
                .referenceId("SR-1")
                .createdAt(Instant.parse("2022-06-15T00:00:00Z"))
                .build()));

        when(storeReviewService.getStoreReview("SR-1")).thenReturn(StoreReview.builder()
                .id("SR-1")
                .state("inProgress")
                .assignee("user0")
                .lastStartedAt(Instant.parse("2022-06-01T00:00:00Z"))
                .build());

        settingChangeLogDeleteService.deleteSettingChangeLog("123", "SR-1", "inProgress", userContext);

        verify(settingChangeLogRepository).findById("123");
        verify(storeReviewService).getStoreReview("SR-1");
        verify(settingChangeLogRepository).deleteById("123");
    }

    @Test
    void testDeleteSettingChangeLogInvalidId() {
        when(settingChangeLogRepository.findById("123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> settingChangeLogDeleteService.deleteSettingChangeLog("123", "SR-1", "inProgress", userContext))
                .isInstanceOf(EntityNotFoundException.class);

        verify(settingChangeLogRepository).findById("123");
        verify(storeReviewService, never()).getStoreReview("SR-1");
        verify(settingChangeLogRepository, never()).deleteById("123");
    }

    @Test
    void testDeleteSettingChangeLogInvalidState() {
        when(settingChangeLogRepository.findById("123")).thenReturn(Optional.of(SettingChangeLogEntity.builder()
                .id("123")
                .referenceId("SR-1")
                .createdAt(Instant.parse("2022-06-15T00:00:00Z"))
                .build()));

        assertThatThrownBy(() -> settingChangeLogDeleteService.deleteSettingChangeLog("123", "SR-1", "invalid", userContext))
                .isInstanceOf(ValidationException.class);

        verify(settingChangeLogRepository, never()).findById("123");
        verify(storeReviewService, never()).getStoreReview("SR-1");
        verify(settingChangeLogRepository, never()).deleteById("123");
    }

    @Test
    void testDeleteSettingChangeLogInvalidStateInEntity() {
        when(settingChangeLogRepository.findById("123")).thenReturn(Optional.of(SettingChangeLogEntity.builder()
                .id("123")
                .referenceId("SR-1")
                .createdAt(Instant.parse("2022-06-15T00:00:00Z"))
                .build()));

        when(storeReviewService.getStoreReview("SR-1")).thenReturn(StoreReview.builder()
                .id("SR-1")
                .state("invalid")
                .assignee("user0")
                .lastStartedAt(Instant.parse("2022-06-01T00:00:00Z"))
                .build());

        assertThatThrownBy(() -> settingChangeLogDeleteService.deleteSettingChangeLog("123", "SR-1", "inProgress", userContext))
                .isInstanceOf(ValidationException.class);

        verify(settingChangeLogRepository).findById("123");
        verify(storeReviewService).getStoreReview("SR-1");
        verify(settingChangeLogRepository, never()).deleteById("123");
    }

    @Test
    void testDeleteSettingChangeLogInvalidReferenceId() {
        when(settingChangeLogRepository.findById("123")).thenReturn(Optional.of(SettingChangeLogEntity.builder()
                .id("123")
                .referenceId("SR-2")
                .createdAt(Instant.parse("2022-06-15T00:00:00Z"))
                .build()));

        when(storeReviewService.getStoreReview("SR-1")).thenReturn(StoreReview.builder()
                .id("SR-1")
                .state("inProgress")
                .assignee("user0")
                .lastStartedAt(Instant.parse("2022-06-01T00:00:00Z"))
                .build());

        assertThatThrownBy(() -> settingChangeLogDeleteService.deleteSettingChangeLog("123", "SR-1", "inProgress", userContext))
                .isInstanceOf(ValidationException.class);

        verify(settingChangeLogRepository).findById("123");
        verify(storeReviewService).getStoreReview("SR-1");
        verify(settingChangeLogRepository, never()).deleteById("123");
    }

    @Test
    void testDeleteSettingChangeLogInvalidAssignee() {
        when(settingChangeLogRepository.findById("123")).thenReturn(Optional.of(SettingChangeLogEntity.builder()
                .id("123")
                .referenceId("SR-1")
                .createdAt(Instant.parse("2022-06-15T00:00:00Z"))
                .build()));

        when(storeReviewService.getStoreReview("SR-1")).thenReturn(StoreReview.builder()
                .id("SR-1")
                .state("inProgress")
                .assignee("user1")
                .lastStartedAt(Instant.parse("2022-06-15T00:00:01Z"))
                .build());

        assertThatThrownBy(() -> settingChangeLogDeleteService.deleteSettingChangeLog("123", "SR-1", "inProgress", userContext))
                .isInstanceOf(ValidationException.class);

        verify(settingChangeLogRepository).findById("123");
        verify(storeReviewService).getStoreReview("SR-1");
        verify(settingChangeLogRepository, never()).deleteById("123");
    }

    @Test
    void testDeleteSettingChangeLogInvalidTime() {
        when(settingChangeLogRepository.findById("123")).thenReturn(Optional.of(SettingChangeLogEntity.builder()
                .id("123")
                .referenceId("SR-1")
                .createdAt(Instant.parse("2022-06-15T00:00:00Z"))
                .build()));

        when(storeReviewService.getStoreReview("SR-1")).thenReturn(StoreReview.builder()
                .id("SR-1")
                .state("inProgress")
                .assignee("user0")
                .lastStartedAt(Instant.parse("2022-06-15T00:00:01Z"))
                .build());

        assertThatThrownBy(() -> settingChangeLogDeleteService.deleteSettingChangeLog("123", "SR-1", "inProgress", userContext))
                .isInstanceOf(ValidationException.class);

        verify(settingChangeLogRepository).findById("123");
        verify(storeReviewService).getStoreReview("SR-1");
        verify(settingChangeLogRepository, never()).deleteById("123");
    }

}
