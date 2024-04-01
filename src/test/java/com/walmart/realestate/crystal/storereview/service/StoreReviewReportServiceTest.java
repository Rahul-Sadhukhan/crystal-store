package com.walmart.realestate.crystal.storereview.service;

import com.walmart.core.realestate.cerberus.bean.CerberusUserInformation;
import com.walmart.realestate.crystal.settingchangelog.model.Insight;
import com.walmart.realestate.crystal.settingchangelog.model.SettingChangeLog;
import com.walmart.realestate.crystal.settingchangelog.service.InsightService;
import com.walmart.realestate.crystal.settingchangelog.service.SettingChangeLogService;
import com.walmart.realestate.crystal.storereview.client.amg.model.AmgNote;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationSensor;
import com.walmart.realestate.crystal.storereview.client.storeinfo.model.StoreDetail;
import com.walmart.realestate.crystal.storereview.client.storeinfo.model.facilitydetails.*;
import com.walmart.realestate.crystal.storereview.config.TestAsyncConfig;
import com.walmart.realestate.crystal.storereview.model.StoreAssetReview;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.model.StoreReviewAssetHealthScore;
import com.walmart.realestate.crystal.storereview.model.StoreReviewStoreHealthScore;
import com.walmart.realestate.crystal.storereview.model.report.*;
import com.walmart.realestate.crystal.storereview.properties.StoreReviewProperties;
import com.walmart.realestate.crystal.storereview.report.MockWorksheetReporter;
import com.walmart.realestate.crystal.storereview.report.WorkbookReporterFactory;
import com.walmart.realestate.crystal.storereview.report.settingscalculator.SettingsCalculator;
import com.walmart.realestate.soteria.model.User;
import com.walmart.realestate.soteria.model.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.sleuth.autoconfig.brave.BraveAutoConfiguration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.walmart.realestate.crystal.storereview.service.StoreReviewReportService.Constants.TELEMETRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StoreReviewReportService.class, StoreReviewProperties.class, TestAsyncConfig.class, ThreadPoolTaskExecutor.class, TaskExecutorBuilder.class})
@ImportAutoConfiguration(BraveAutoConfiguration.class)
@ActiveProfiles("test")
class StoreReviewReportServiceTest {

    @Autowired
    private StoreReviewReportService storeReviewReportService;

    @MockBean
    private StoreReviewService storeReviewService;

    @MockBean
    private AssetService assetService;

    @MockBean
    private StoreAssetReviewService storeAssetReviewService;

    @MockBean
    private StoreReviewAssetService storeReviewAssetService;

    @MockBean
    private StoreService storeService;

    @MockBean
    private AssetMaintenanceService assetMaintenanceService;

    @MockBean
    private StoreReviewHealthScoreService storeReviewHealthScoreService;

    @MockBean
    private SettingChangeLogService settingChangeLogService;

    @MockBean
    private InsightService insightService;

    @MockBean
    private UserAccountService userAccountService;

    @MockBean
    private WorkbookReporterFactory workbookReporterFactory;

    @MockBean
    private StoreAssetService storeAssetService;

    @MockBean
    private SettingsCalculator settingsCalculator;

    private Instant timestamp;

    @BeforeEach
    void setup() {
        timestamp = Instant.now();
        storeReviewReportService.setTimeSupplier(() -> timestamp);
    }

    @Test
    void testGetStoreReviewReportData() {
        CerberusUserInformation userInformation = new CerberusUserInformation();
        userInformation.setUserName("currentUser");
        UserContext userContext = new UserContext(userInformation, Collections.emptySet());

        Instant reviewStartedAt = LocalDateTime.of(2021, 8, 10, 9, 15).toInstant(ZoneOffset.UTC);
        Instant monitoringStartedAt = LocalDateTime.of(2021, 8, 20, 14, 45).toInstant(ZoneOffset.UTC);
        when(storeReviewService.getStoreReview("SR-42")).thenReturn(StoreReview.builder()
                .id("SR-42")
                .storeNumber(240L)
                .assignee("user0")
                .assigneeName("User Name")
                .startedAt(reviewStartedAt)
                .monitoringStartedAt(monitoringStartedAt)
                .build());

        LocationTimeZone locationTimeZone = LocationTimeZone.builder()
                .dstTimeZone(DstTimeZone.builder()
                        .timeZoneId("America/Chicago")
                        .build())
                .build();
        LocationAddress locationAddress = LocationAddress.builder()
                .addressLine1("Address line 1")
                .city("City")
                .state("ZZ")
                .postalCode("99999-0000")
                .build();
        when(storeService.getStoreInfo(240L)).thenReturn(StoreDetail.builder()
                .facilityDetail(FacilityDetail.builder()
                        .location(Location.builder()
                                .locationTimeZone(locationTimeZone)
                                .locationAddress(locationAddress)
                                .build())
                        .build())
                .build());

        when(assetMaintenanceService.getServiceModel(240L)).thenReturn(AmgNote.builder()
                .header("SDM")
                .value("Service Model")
                .build());

        when(storeReviewHealthScoreService.getStoreReviewStoreHealthScore("SR-42")).thenReturn(Optional.of(StoreReviewStoreHealthScore.builder()
                .storeReviewId("SR-42")
                .storeNumber(240L)
                .reviewStartTimestamp(reviewStartedAt)
                .healthScoreStart(40.29)
                .timestampStart(reviewStartedAt.minusSeconds(300))
                .reviewEndTimestamp(monitoringStartedAt)
                .healthScoreEnd(56.43)
                .timestampEnd(monitoringStartedAt.minusSeconds(900))
                .build()));

        StoreReviewReport storeReviewReport = storeReviewReportService.getStoreReviewReportData("SR-42", userContext);

        assertThat(storeReviewReport).isNotNull();

        StoreReviewDetailsReport storeReviewDetailsReport = storeReviewReport.getStoreReviewDetails();
        assertThat(storeReviewDetailsReport).isNotNull();
        assertThat(storeReviewDetailsReport.getStoreReviewId()).isEqualTo("SR-42");
        assertThat(storeReviewDetailsReport.getReviewer()).isEqualTo("user0");
        assertThat(storeReviewDetailsReport.getReviewerName()).isEqualTo("User Name");
        assertThat(storeReviewDetailsReport.getStoreReviewStartDate()).isEqualTo(LocalDate.of(2021, 8, 10));
        assertThat(storeReviewDetailsReport.getStoreReviewEndDate()).isEqualTo(LocalDate.of(2021, 8, 20));

        StoreDetailsReport storeDetailsReport = storeReviewReport.getStoreDetails();
        assertThat(storeDetailsReport).isNotNull();
        assertThat(storeDetailsReport.getStoreNumber()).isEqualTo(240L);
        assertThat(storeDetailsReport.getStoreAddress()).isEqualTo(locationAddress);
        assertThat(storeDetailsReport.getStoreHealthStart()).isNotNull();
        assertThat(storeDetailsReport.getStoreHealthStart().getScore()).isEqualTo(40.29);
        assertThat(storeDetailsReport.getStoreHealthStart().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(reviewStartedAt.minusSeconds(300), ZoneId.of("America/Chicago")));
        assertThat(storeDetailsReport.getStoreHealthEnd()).isNotNull();
        assertThat(storeDetailsReport.getStoreHealthEnd().getScore()).isEqualTo(56.43);
        assertThat(storeDetailsReport.getStoreHealthEnd().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(monitoringStartedAt.minusSeconds(900), ZoneId.of("America/Chicago")));

        assertThat(storeReviewReport.getTimestamp()).isEqualTo(timestamp);
        assertThat(storeReviewReport.getTraceId()).isNotNull();
        assertThat(storeReviewReport.getUser()).isEqualTo("currentUser");
        assertThat(storeReviewReport.getUserName()).isEqualTo(null);

        verify(storeReviewService).getStoreReview("SR-42");
        verify(storeService).getStoreInfo(240L);
        verify(storeAssetService).getStoreHealthScore(240L, timestamp);
        verify(storeReviewHealthScoreService).getStoreReviewStoreHealthScore("SR-42");
        verify(settingChangeLogService).getSettingsLogByReferenceId("SR-42");
        verify(insightService).getInsightsByReferenceId("SR-42");
        verify(storeAssetReviewService).getStoreAssetReviews("SR-42");
        verify(storeReviewAssetService).getAssetsForStore(240L);
        verify(storeAssetService).getAssetHealthScore(240L, timestamp);
        verify(storeReviewHealthScoreService).getStoreReviewAssetHealthScores("SR-42");
        verify(userAccountService).findUser("currentUser");
    }

    @Test
    void testGetStoreReviewReportDataWithAssets() {
        CerberusUserInformation userInformation = new CerberusUserInformation();
        userInformation.setUserName("currentUser");
        UserContext userContext = new UserContext(userInformation, Collections.emptySet());

        Instant reviewStartedAt = LocalDateTime.of(2021, 8, 10, 9, 15).toInstant(ZoneOffset.UTC);
        Instant monitoringStartedAt = LocalDateTime.of(2021, 8, 20, 14, 45).toInstant(ZoneOffset.UTC);
        when(storeReviewService.getStoreReview("SR-42")).thenReturn(StoreReview.builder()
                .id("SR-42")
                .storeNumber(240L)
                .assignee("user0")
                .assigneeName("User Name")
                .startedAt(reviewStartedAt)
                .monitoringStartedAt(monitoringStartedAt)
                .build());

        LocationTimeZone locationTimeZone = LocationTimeZone.builder()
                .dstTimeZone(DstTimeZone.builder()
                        .timeZoneId("America/Chicago")
                        .build())
                .build();
        LocationAddress locationAddress = LocationAddress.builder()
                .addressLine1("Address line 1")
                .city("City")
                .state("ZZ")
                .postalCode("99999-0000")
                .build();
        when(storeService.getStoreInfo(240L)).thenReturn(StoreDetail.builder()
                .facilityDetail(FacilityDetail.builder()
                        .location(Location.builder()
                                .locationTimeZone(locationTimeZone)
                                .locationAddress(locationAddress)
                                .build())
                        .build())
                .build());

        when(assetMaintenanceService.getServiceModel(240L)).thenReturn(AmgNote.builder()
                .header("SDM")
                .value("Service Model")
                .build());

        when(storeReviewHealthScoreService.getStoreReviewStoreHealthScore("SR-42")).thenReturn(Optional.of(StoreReviewStoreHealthScore.builder()
                .storeReviewId("SR-42")
                .storeNumber(240L)
                .reviewStartTimestamp(reviewStartedAt)
                .healthScoreStart(40.29)
                .timestampStart(reviewStartedAt.minusSeconds(300))
                .reviewEndTimestamp(monitoringStartedAt)
                .healthScoreEnd(56.43)
                .timestampEnd(monitoringStartedAt.minusSeconds(900))
                .build()));

        RefrigerationSensor asset = RefrigerationSensor.builder()
                .id("8647")
                .type("Rack")
                .build();
        when(storeReviewAssetService.getAssetsForStore(240L)).thenReturn(Collections.singletonList(asset));

        when(storeReviewHealthScoreService.getStoreReviewAssetHealthScores("SR-42")).thenReturn(Collections.singletonList(StoreReviewAssetHealthScore.builder()
                .storeReviewId("SR-42")
                .assetMappingId("8647")
                .reviewStartTimestamp(reviewStartedAt)
                .healthScoreStart(80.41)
                .timestampStart(reviewStartedAt.minusSeconds(600))
                .reviewEndTimestamp(monitoringStartedAt)
                .healthScoreEnd(91.28)
                .timestampEnd(monitoringStartedAt.minusSeconds(1200))
                .build()));

        when(storeAssetReviewService.getStoreAssetReviews("SR-42")).thenReturn(Collections.singletonList(
                StoreAssetReview.builder()
                        .assetMappingId("8647")
                        .workOrderId("WO00123L")
                        .build()));

        List<SettingChangeLog> settingChangeLogs = Arrays.asList(
                SettingChangeLog.builder()
                        .assetMappingId("8647")
                        .createdAt(ZonedDateTime.of(LocalDateTime.of(2021, 9, 1, 10, 30), ZoneOffset.UTC).toInstant())
                        .build(),
                SettingChangeLog.builder()
                        .assetMappingId("8647")
                        .createdAt(ZonedDateTime.of(LocalDateTime.of(2021, 9, 1, 10, 45), ZoneOffset.UTC).toInstant())
                        .build());
        when(settingChangeLogService.getSettingsLogByReferenceId("SR-42")).thenReturn(settingChangeLogs);

        List<Insight> insights = Arrays.asList(
                Insight.builder()
                        .assetMappingId("8647")
                        .createdAt(LocalDateTime.of(2021, 9, 1, 10, 40).toInstant(ZoneOffset.UTC))
                        .build(),
                Insight.builder()
                        .assetMappingId("8647")
                        .createdAt(LocalDateTime.of(2021, 9, 1, 9, 30).toInstant(ZoneOffset.UTC))
                        .build());
        when(insightService.getInsightsByReferenceId("SR-42")).thenReturn(insights);

        User user = User.builder()
                .firstName("Current")
                .lastName("User")
                .build();
        when(userAccountService.findUser("currentUser")).thenReturn(user);

        StoreReviewReport storeReviewReport = storeReviewReportService.getStoreReviewReportData("SR-42", userContext);

        assertThat(storeReviewReport).isNotNull();

        StoreReviewDetailsReport storeReviewDetailsReport = storeReviewReport.getStoreReviewDetails();
        assertThat(storeReviewDetailsReport).isNotNull();
        assertThat(storeReviewDetailsReport.getStoreReviewId()).isEqualTo("SR-42");
        assertThat(storeReviewDetailsReport.getReviewer()).isEqualTo("user0");
        assertThat(storeReviewDetailsReport.getReviewerName()).isEqualTo("User Name");
        assertThat(storeReviewDetailsReport.getStoreReviewStartDate()).isEqualTo(LocalDate.of(2021, 8, 10));
        assertThat(storeReviewDetailsReport.getStoreReviewEndDate()).isEqualTo(LocalDate.of(2021, 8, 20));

        StoreDetailsReport storeDetailsReport = storeReviewReport.getStoreDetails();
        assertThat(storeDetailsReport).isNotNull();
        assertThat(storeDetailsReport.getStoreNumber()).isEqualTo(240L);
        assertThat(storeDetailsReport.getStoreAddress()).isEqualTo(locationAddress);
        assertThat(storeDetailsReport.getStoreHealthStart()).isNotNull();
        assertThat(storeDetailsReport.getStoreHealthStart().getScore()).isEqualTo(40.29);
        assertThat(storeDetailsReport.getStoreHealthStart().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(reviewStartedAt.minusSeconds(300), ZoneId.of("America/Chicago")));
        assertThat(storeDetailsReport.getStoreHealthEnd()).isNotNull();
        assertThat(storeDetailsReport.getStoreHealthEnd().getScore()).isEqualTo(56.43);
        assertThat(storeDetailsReport.getStoreHealthEnd().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(monitoringStartedAt.minusSeconds(900), ZoneId.of("America/Chicago")));

        List<AssetReviewSummaryReport> summary = storeReviewReport.getAssetReviewSummary();
        assertThat(summary).hasSize(1);
        assertThat(summary.get(0)).isNotNull();
        assertThat(summary.get(0).getAssetType()).isEqualTo("Rack");
        assertThat(summary.get(0).getReviewedAssets()).isEqualTo(1);
        assertThat(summary.get(0).getSettingChangeLogs()).isEqualTo(1);
        assertThat(summary.get(0).getInsights()).isEqualTo(1);
        assertThat(summary.get(0).getWorkOrders()).isEqualTo(1);

        List<AssetReviewDetailsReport> assets = storeReviewReport.getAssets();
        assertThat(assets).hasSize(1);
        assertThat(assets.get(0)).isNotNull();
        assertThat(assets.get(0).getIndex()).isEqualTo(1);
        assertThat(assets.get(0).getAsset()).isEqualTo(asset);
        assertThat(assets.get(0).getWorkOrderId()).isEqualTo("WO00123L");
        assertThat(assets.get(0).getAssetHealthStart()).isNotNull();
        assertThat(assets.get(0).getAssetHealthStart().getScore()).isEqualTo(80.41);
        assertThat(assets.get(0).getAssetHealthStart().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(reviewStartedAt.minusSeconds(600), ZoneId.of("America/Chicago")));
        assertThat(assets.get(0).getAssetHealthEnd()).isNotNull();
        assertThat(assets.get(0).getAssetHealthEnd().getScore()).isEqualTo(91.28);
        assertThat(assets.get(0).getAssetHealthEnd().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(monitoringStartedAt.minusSeconds(1200), ZoneId.of("America/Chicago")));

        assertThat(assets.get(0).getHasSettingChangeLogs()).isTrue();
        assertThat(assets.get(0).getSettingChangeLogs()).hasSize(2);
        assertThat(assets.get(0).getSettingChangeLogs().get(0).getAssetMappingId()).isEqualTo(settingChangeLogs.get(0).getAssetMappingId());
        assertThat(assets.get(0).getSettingChangeLogs().get(0).getCreatedAt()).isEqualTo(settingChangeLogs.get(0).getCreatedAt().atZone(ZoneId.of("America/Chicago")).toLocalDateTime());
        assertThat(assets.get(0).getSettingChangeLogs().get(1).getAssetMappingId()).isEqualTo(settingChangeLogs.get(1).getAssetMappingId());
        assertThat(assets.get(0).getSettingChangeLogs().get(1).getCreatedAt()).isEqualTo(settingChangeLogs.get(1).getCreatedAt().atZone(ZoneId.of("America/Chicago")).toLocalDateTime());

        assertThat(assets.get(0).getSettingChangeLogsCalculated()).isEmpty();

        assertThat(assets.get(0).getHasInsights()).isTrue();
        assertThat(assets.get(0).getInsights()).hasSize(2);
        assertThat(assets.get(0).getInsights().get(0).getAssetMappingId()).isEqualTo(insights.get(1).getAssetMappingId());
        assertThat(assets.get(0).getInsights().get(0).getCreatedAt()).isEqualTo(LocalDateTime.ofInstant(insights.get(1).getCreatedAt(), ZoneId.of("America/Chicago")));
        assertThat(assets.get(0).getInsights().get(1).getAssetMappingId()).isEqualTo(insights.get(0).getAssetMappingId());
        assertThat(assets.get(0).getInsights().get(1).getCreatedAt()).isEqualTo(LocalDateTime.ofInstant(insights.get(0).getCreatedAt(), ZoneId.of("America/Chicago")));

        assertThat(assets.get(0).getIsReviewed()).isTrue();

        assertThat(storeReviewReport.getTimestamp()).isNotNull();
        assertThat(storeReviewReport.getTraceId()).isNotNull();
        assertThat(storeReviewReport.getUser()).isEqualTo("currentUser");
        assertThat(storeReviewReport.getUserName()).isEqualTo(user.getFullName());

        verify(storeReviewService).getStoreReview("SR-42");
        verify(storeService).getStoreInfo(240L);
        verify(storeReviewHealthScoreService).getStoreReviewStoreHealthScore("SR-42");
        verify(settingChangeLogService).getSettingsLogByReferenceId("SR-42");
        verify(settingsCalculator).calculate(anyList(), any());
        verify(insightService).getInsightsByReferenceId("SR-42");
        verify(storeAssetReviewService).getStoreAssetReviews("SR-42");
        verify(storeReviewAssetService).getAssetsForStore(240L);
        verify(storeReviewHealthScoreService).getStoreReviewAssetHealthScores("SR-42");
        verify(userAccountService).findUser("currentUser");
    }

    @Test
    void testGetStoreReviewReportDataWithAssetsPartialHealthScore() {
        CerberusUserInformation userInformation = new CerberusUserInformation();
        userInformation.setUserName("currentUser");
        UserContext userContext = new UserContext(userInformation, Collections.emptySet());

        Instant reviewStartedAt = LocalDateTime.of(2021, 8, 10, 9, 15).toInstant(ZoneOffset.UTC);
        Instant monitoringStartedAt = LocalDateTime.of(2021, 8, 20, 14, 45).toInstant(ZoneOffset.UTC);
        when(storeReviewService.getStoreReview("SR-42")).thenReturn(StoreReview.builder()
                .id("SR-42")
                .storeNumber(240L)
                .assignee("user0")
                .assigneeName("User Name")
                .startedAt(reviewStartedAt)
                .monitoringStartedAt(monitoringStartedAt)
                .build());

        LocationTimeZone locationTimeZone = LocationTimeZone.builder()
                .dstTimeZone(DstTimeZone.builder()
                        .timeZoneId("America/Chicago")
                        .build())
                .build();
        LocationAddress locationAddress = LocationAddress.builder()
                .addressLine1("Address line 1")
                .city("City")
                .state("ZZ")
                .postalCode("99999-0000")
                .build();
        when(storeService.getStoreInfo(240L)).thenReturn(StoreDetail.builder()
                .facilityDetail(FacilityDetail.builder()
                        .location(Location.builder()
                                .locationTimeZone(locationTimeZone)
                                .locationAddress(locationAddress)
                                .build())
                        .build())
                .build());

        when(assetMaintenanceService.getServiceModel(240L)).thenReturn(AmgNote.builder()
                .header("SDM")
                .value("Service Model")
                .build());

        when(storeReviewHealthScoreService.getStoreReviewStoreHealthScore("SR-42")).thenReturn(Optional.of(StoreReviewStoreHealthScore.builder()
                .storeReviewId("SR-42")
                .storeNumber(240L)
                .reviewStartTimestamp(reviewStartedAt)
                .healthScoreStart(40.29)
                .timestampStart(reviewStartedAt.minusSeconds(300))
                .build()));

        RefrigerationSensor asset = RefrigerationSensor.builder()
                .id("8647")
                .type("Rack")
                .assetName("B-240")
                .build();
        when(storeReviewAssetService.getAssetsForStore(240L)).thenReturn(Collections.singletonList(asset));

        when(storeReviewHealthScoreService.getStoreReviewAssetHealthScores("SR-42")).thenReturn(Collections.singletonList(StoreReviewAssetHealthScore.builder()
                .storeReviewId("SR-42")
                .assetMappingId("8647")
                .reviewStartTimestamp(reviewStartedAt)
                .healthScoreStart(80.41)
                .timestampStart(reviewStartedAt.minusSeconds(600))
                .build()));

        when(storeAssetReviewService.getStoreAssetReviews("SR-42")).thenReturn(Collections.singletonList(
                StoreAssetReview.builder()
                        .assetMappingId("8647")
                        .workOrderId("WO00123L")
                        .build()));

        List<SettingChangeLog> settingChangeLogs = Arrays.asList(
                SettingChangeLog.builder()
                        .assetMappingId("8647")
                        .createdAt(ZonedDateTime.of(LocalDateTime.of(2021, 9, 1, 10, 30), ZoneOffset.UTC).toInstant())
                        .build(),
                SettingChangeLog.builder()
                        .assetMappingId("8647")
                        .createdAt(ZonedDateTime.of(LocalDateTime.of(2021, 9, 1, 10, 45), ZoneOffset.UTC).toInstant())
                        .build());
        when(settingChangeLogService.getSettingsLogByReferenceId("SR-42")).thenReturn(settingChangeLogs);

        List<Insight> insights = Arrays.asList(
                Insight.builder()
                        .assetMappingId("8647")
                        .createdAt(LocalDateTime.of(2021, 9, 1, 10, 40).toInstant(ZoneOffset.UTC))
                        .build(),
                Insight.builder()
                        .assetMappingId("8647")
                        .createdAt(LocalDateTime.of(2021, 9, 1, 9, 30).toInstant(ZoneOffset.UTC))
                        .build());
        when(insightService.getInsightsByReferenceId("SR-42")).thenReturn(insights);

        User user = User.builder()
                .firstName("Current")
                .lastName("User")
                .build();
        when(userAccountService.findUser("currentUser")).thenReturn(user);

        StoreReviewReport storeReviewReport = storeReviewReportService.getStoreReviewReportData("SR-42", userContext);

        assertThat(storeReviewReport).isNotNull();

        StoreReviewDetailsReport storeReviewDetailsReport = storeReviewReport.getStoreReviewDetails();
        assertThat(storeReviewDetailsReport).isNotNull();
        assertThat(storeReviewDetailsReport.getStoreReviewId()).isEqualTo("SR-42");
        assertThat(storeReviewDetailsReport.getReviewer()).isEqualTo("user0");
        assertThat(storeReviewDetailsReport.getReviewerName()).isEqualTo("User Name");
        assertThat(storeReviewDetailsReport.getStoreReviewStartDate()).isEqualTo(LocalDate.of(2021, 8, 10));
        assertThat(storeReviewDetailsReport.getStoreReviewEndDate()).isEqualTo(LocalDate.of(2021, 8, 20));

        StoreDetailsReport storeDetailsReport = storeReviewReport.getStoreDetails();
        assertThat(storeDetailsReport).isNotNull();
        assertThat(storeDetailsReport.getStoreNumber()).isEqualTo(240L);
        assertThat(storeDetailsReport.getStoreAddress()).isEqualTo(locationAddress);
        assertThat(storeDetailsReport.getStoreHealthStart()).isNotNull();
        assertThat(storeDetailsReport.getStoreHealthStart().getScore()).isEqualTo(40.29);
        assertThat(storeDetailsReport.getStoreHealthStart().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(reviewStartedAt.minusSeconds(300), ZoneId.of("America/Chicago")));
        assertThat(storeDetailsReport.getStoreHealthEnd()).isNull();

        List<AssetReviewSummaryReport> summary = storeReviewReport.getAssetReviewSummary();
        assertThat(summary).hasSize(1);
        assertThat(summary.get(0)).isNotNull();
        assertThat(summary.get(0).getAssetType()).isEqualTo("Rack");
        assertThat(summary.get(0).getReviewedAssets()).isEqualTo(1);
        assertThat(summary.get(0).getSettingChangeLogs()).isEqualTo(1);
        assertThat(summary.get(0).getInsights()).isEqualTo(1);
        assertThat(summary.get(0).getWorkOrders()).isEqualTo(1);

        List<AssetReviewDetailsReport> assets = storeReviewReport.getAssets();
        assertThat(assets).hasSize(1);
        assertThat(assets.get(0)).isNotNull();
        assertThat(assets.get(0).getIndex()).isEqualTo(1);
        assertThat(assets.get(0).getAsset()).isEqualTo(asset);
        assertThat(assets.get(0).getWorkOrderId()).isEqualTo("WO00123L");
        assertThat(assets.get(0).getAssetHealthStart()).isNotNull();
        assertThat(assets.get(0).getAssetHealthStart().getScore()).isEqualTo(80.41);
        assertThat(assets.get(0).getAssetHealthStart().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(reviewStartedAt.minusSeconds(600), ZoneId.of("America/Chicago")));
        assertThat(assets.get(0).getAssetHealthEnd()).isNull();

        assertThat(assets.get(0).getHasSettingChangeLogs()).isTrue();
        assertThat(assets.get(0).getSettingChangeLogs()).hasSize(2);
        assertThat(assets.get(0).getSettingChangeLogs().get(0).getAssetMappingId()).isEqualTo(settingChangeLogs.get(0).getAssetMappingId());
        assertThat(assets.get(0).getSettingChangeLogs().get(0).getCreatedAt()).isEqualTo(settingChangeLogs.get(0).getCreatedAt().atZone(ZoneId.of("America/Chicago")).toLocalDateTime());
        assertThat(assets.get(0).getSettingChangeLogs().get(1).getAssetMappingId()).isEqualTo(settingChangeLogs.get(1).getAssetMappingId());
        assertThat(assets.get(0).getSettingChangeLogs().get(1).getCreatedAt()).isEqualTo(settingChangeLogs.get(1).getCreatedAt().atZone(ZoneId.of("America/Chicago")).toLocalDateTime());

        assertThat(assets.get(0).getSettingChangeLogsCalculated()).isEmpty();

        assertThat(assets.get(0).getHasInsights()).isTrue();
        assertThat(assets.get(0).getInsights()).hasSize(2);
        assertThat(assets.get(0).getInsights().get(0).getAssetMappingId()).isEqualTo(insights.get(1).getAssetMappingId());
        assertThat(assets.get(0).getInsights().get(0).getCreatedAt()).isEqualTo(LocalDateTime.ofInstant(insights.get(1).getCreatedAt(), ZoneId.of("America/Chicago")));
        assertThat(assets.get(0).getInsights().get(1).getAssetMappingId()).isEqualTo(insights.get(0).getAssetMappingId());
        assertThat(assets.get(0).getInsights().get(1).getCreatedAt()).isEqualTo(LocalDateTime.ofInstant(insights.get(0).getCreatedAt(), ZoneId.of("America/Chicago")));

        assertThat(assets.get(0).getIsReviewed()).isTrue();

        assertThat(storeReviewReport.getTimestamp()).isNotNull();
        assertThat(storeReviewReport.getTraceId()).isNotNull();
        assertThat(storeReviewReport.getUser()).isEqualTo("currentUser");
        assertThat(storeReviewReport.getUserName()).isEqualTo(user.getFullName());

        verify(storeReviewService).getStoreReview("SR-42");
        verify(storeService).getStoreInfo(240L);
        verify(storeReviewHealthScoreService).getStoreReviewStoreHealthScore("SR-42");
        verify(settingChangeLogService).getSettingsLogByReferenceId("SR-42");
        verify(settingsCalculator).calculate(anyList(), any());
        verify(insightService).getInsightsByReferenceId("SR-42");
        verify(storeAssetReviewService).getStoreAssetReviews("SR-42");
        verify(storeReviewAssetService).getAssetsForStore(240L);
        verify(storeReviewHealthScoreService).getStoreReviewAssetHealthScores("SR-42");
        verify(userAccountService).findUser("currentUser");
    }

    @Test
    void testGetStoreReviewReportDataWithAssetsWithCalculatedSettingsChangeLogs() {
        CerberusUserInformation userInformation = new CerberusUserInformation();
        userInformation.setUserName("currentUser");
        UserContext userContext = new UserContext(userInformation, Collections.emptySet());

        Instant reviewStartedAt = LocalDateTime.of(2021, 8, 10, 9, 15).toInstant(ZoneOffset.UTC);
        Instant monitoringStartedAt = LocalDateTime.of(2021, 8, 20, 14, 45).toInstant(ZoneOffset.UTC);
        when(storeReviewService.getStoreReview("SR-42")).thenReturn(StoreReview.builder()
                .id("SR-42")
                .storeNumber(240L)
                .assignee("user0")
                .assigneeName("User Name")
                .startedAt(reviewStartedAt)
                .monitoringStartedAt(monitoringStartedAt)
                .build());

        LocationTimeZone locationTimeZone = LocationTimeZone.builder()
                .dstTimeZone(DstTimeZone.builder()
                        .timeZoneId("America/Chicago")
                        .build())
                .build();
        LocationAddress locationAddress = LocationAddress.builder()
                .addressLine1("Address line 1")
                .city("City")
                .state("ZZ")
                .postalCode("99999-0000")
                .build();
        when(storeService.getStoreInfo(240L)).thenReturn(StoreDetail.builder()
                .facilityDetail(FacilityDetail.builder()
                        .location(Location.builder()
                                .locationTimeZone(locationTimeZone)
                                .locationAddress(locationAddress)
                                .build())
                        .build())
                .build());

        when(assetMaintenanceService.getServiceModel(240L)).thenReturn(AmgNote.builder()
                .header("SDM")
                .value("Service Model")
                .build());

        when(storeReviewHealthScoreService.getStoreReviewStoreHealthScore("SR-42")).thenReturn(Optional.of(StoreReviewStoreHealthScore.builder()
                .storeReviewId("SR-42")
                .storeNumber(240L)
                .reviewStartTimestamp(reviewStartedAt)
                .healthScoreStart(40.29)
                .timestampStart(reviewStartedAt.minusSeconds(300))
                .reviewEndTimestamp(monitoringStartedAt)
                .healthScoreEnd(56.43)
                .timestampEnd(monitoringStartedAt.minusSeconds(900))
                .build()));

        RefrigerationSensor asset = RefrigerationSensor.builder()
                .id("8647")
                .type("Rack")
                .build();
        when(storeReviewAssetService.getAssetsForStore(240L)).thenReturn(Collections.singletonList(asset));

        StoreReviewAssetHealthScore assetHealthScore = StoreReviewAssetHealthScore.builder()
                .storeReviewId("SR-42")
                .assetMappingId("8647")
                .reviewStartTimestamp(reviewStartedAt)
                .healthScoreStart(80.41)
                .timestampStart(reviewStartedAt.minusSeconds(600))
                .reviewEndTimestamp(monitoringStartedAt)
                .healthScoreEnd(91.28)
                .timestampEnd(monitoringStartedAt.minusSeconds(1200))
                .build();
        when(storeReviewHealthScoreService.getStoreReviewAssetHealthScores("SR-42")).thenReturn(Collections.singletonList(assetHealthScore));

        when(storeAssetReviewService.getStoreAssetReviews("SR-42")).thenReturn(Collections.singletonList(
                StoreAssetReview.builder()
                        .assetMappingId("8647")
                        .workOrderId("WO00123L")
                        .build()));

        List<SettingChangeLog> settingChangeLogs = Arrays.asList(
                SettingChangeLog.builder()
                        .assetMappingId("8647")
                        .createdAt(ZonedDateTime.of(LocalDateTime.of(2021, 9, 1, 10, 30), ZoneOffset.UTC).toInstant())
                        .build(),
                SettingChangeLog.builder()
                        .assetMappingId("8647")
                        .createdAt(ZonedDateTime.of(LocalDateTime.of(2021, 9, 1, 10, 45), ZoneOffset.UTC).toInstant())
                        .build());
        when(settingChangeLogService.getSettingsLogByReferenceId("SR-42")).thenReturn(settingChangeLogs);

        List<SettingChangeLogReport> calculatedSettings = Collections.singletonList(
                SettingChangeLogReport.builder()
                        .setting("setting")
                        .oldValue("100.0")
                        .newValue("-100.0")
                        .source(TELEMETRY)
                        .build());
        when(settingsCalculator.calculate(anyList(), any())).thenReturn(calculatedSettings);

        List<Insight> insights = Arrays.asList(
                Insight.builder()
                        .assetMappingId("8647")
                        .createdAt(LocalDateTime.of(2021, 9, 1, 10, 40).toInstant(ZoneOffset.UTC))
                        .build(),
                Insight.builder()
                        .assetMappingId("8647")
                        .createdAt(LocalDateTime.of(2021, 9, 1, 9, 30).toInstant(ZoneOffset.UTC))
                        .build());
        when(insightService.getInsightsByReferenceId("SR-42")).thenReturn(insights);

        User user = User.builder()
                .firstName("Current")
                .lastName("User")
                .build();
        when(userAccountService.findUser("currentUser")).thenReturn(user);

        StoreReviewReport storeReviewReport = storeReviewReportService.getStoreReviewReportData("SR-42", userContext);

        assertThat(storeReviewReport).isNotNull();

        StoreReviewDetailsReport storeReviewDetailsReport = storeReviewReport.getStoreReviewDetails();
        assertThat(storeReviewDetailsReport).isNotNull();
        assertThat(storeReviewDetailsReport.getStoreReviewId()).isEqualTo("SR-42");
        assertThat(storeReviewDetailsReport.getReviewer()).isEqualTo("user0");
        assertThat(storeReviewDetailsReport.getReviewerName()).isEqualTo("User Name");
        assertThat(storeReviewDetailsReport.getStoreReviewStartDate()).isEqualTo(LocalDate.of(2021, 8, 10));
        assertThat(storeReviewDetailsReport.getStoreReviewEndDate()).isEqualTo(LocalDate.of(2021, 8, 20));

        StoreDetailsReport storeDetailsReport = storeReviewReport.getStoreDetails();
        assertThat(storeDetailsReport).isNotNull();
        assertThat(storeDetailsReport.getStoreNumber()).isEqualTo(240L);
        assertThat(storeDetailsReport.getStoreAddress()).isEqualTo(locationAddress);
        assertThat(storeDetailsReport.getStoreHealthStart()).isNotNull();
        assertThat(storeDetailsReport.getStoreHealthStart().getScore()).isEqualTo(40.29);
        assertThat(storeDetailsReport.getStoreHealthStart().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(reviewStartedAt.minusSeconds(300), ZoneId.of("America/Chicago")));
        assertThat(storeDetailsReport.getStoreHealthEnd()).isNotNull();
        assertThat(storeDetailsReport.getStoreHealthEnd().getScore()).isEqualTo(56.43);
        assertThat(storeDetailsReport.getStoreHealthEnd().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(monitoringStartedAt.minusSeconds(900), ZoneId.of("America/Chicago")));

        List<AssetReviewSummaryReport> summary = storeReviewReport.getAssetReviewSummary();
        assertThat(summary).hasSize(1);
        assertThat(summary.get(0)).isNotNull();
        assertThat(summary.get(0).getAssetType()).isEqualTo("Rack");
        assertThat(summary.get(0).getReviewedAssets()).isEqualTo(1);
        assertThat(summary.get(0).getSettingChangeLogs()).isEqualTo(1);
        assertThat(summary.get(0).getInsights()).isEqualTo(1);
        assertThat(summary.get(0).getWorkOrders()).isEqualTo(1);

        List<AssetReviewDetailsReport> assets = storeReviewReport.getAssets();
        assertThat(assets).hasSize(1);
        assertThat(assets.get(0)).isNotNull();
        assertThat(assets.get(0).getIndex()).isEqualTo(1);
        assertThat(assets.get(0).getAsset()).isEqualTo(asset);
        assertThat(assets.get(0).getWorkOrderId()).isEqualTo("WO00123L");
        assertThat(assets.get(0).getAssetHealthStart()).isNotNull();
        assertThat(assets.get(0).getAssetHealthStart().getScore()).isEqualTo(80.41);
        assertThat(assets.get(0).getAssetHealthStart().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(reviewStartedAt.minusSeconds(600), ZoneId.of("America/Chicago")));
        assertThat(assets.get(0).getAssetHealthEnd()).isNotNull();
        assertThat(assets.get(0).getAssetHealthEnd().getScore()).isEqualTo(91.28);
        assertThat(assets.get(0).getAssetHealthEnd().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(monitoringStartedAt.minusSeconds(1200), ZoneId.of("America/Chicago")));

        assertThat(assets.get(0).getHasSettingChangeLogs()).isTrue();
        assertThat(assets.get(0).getSettingChangeLogs()).hasSize(2);
        assertThat(assets.get(0).getSettingChangeLogs().get(0).getAssetMappingId()).isEqualTo(settingChangeLogs.get(0).getAssetMappingId());
        assertThat(assets.get(0).getSettingChangeLogs().get(0).getCreatedAt()).isEqualTo(settingChangeLogs.get(0).getCreatedAt().atZone(ZoneId.of("America/Chicago")).toLocalDateTime());
        assertThat(assets.get(0).getSettingChangeLogs().get(1).getAssetMappingId()).isEqualTo(settingChangeLogs.get(1).getAssetMappingId());
        assertThat(assets.get(0).getSettingChangeLogs().get(1).getCreatedAt()).isEqualTo(settingChangeLogs.get(1).getCreatedAt().atZone(ZoneId.of("America/Chicago")).toLocalDateTime());

        assertThat(assets.get(0).getSettingChangeLogsCalculated()).isEqualTo(calculatedSettings);

        assertThat(assets.get(0).getHasInsights()).isTrue();
        assertThat(assets.get(0).getInsights()).hasSize(2);
        assertThat(assets.get(0).getInsights().get(0).getAssetMappingId()).isEqualTo(insights.get(1).getAssetMappingId());
        assertThat(assets.get(0).getInsights().get(0).getCreatedAt()).isEqualTo(LocalDateTime.ofInstant(insights.get(1).getCreatedAt(), ZoneId.of("America/Chicago")));
        assertThat(assets.get(0).getInsights().get(1).getAssetMappingId()).isEqualTo(insights.get(0).getAssetMappingId());
        assertThat(assets.get(0).getInsights().get(1).getCreatedAt()).isEqualTo(LocalDateTime.ofInstant(insights.get(0).getCreatedAt(), ZoneId.of("America/Chicago")));

        assertThat(assets.get(0).getIsReviewed()).isTrue();

        assertThat(storeReviewReport.getTimestamp()).isNotNull();
        assertThat(storeReviewReport.getTraceId()).isNotNull();
        assertThat(storeReviewReport.getUser()).isEqualTo("currentUser");
        assertThat(storeReviewReport.getUserName()).isEqualTo(user.getFullName());

        verify(storeReviewService).getStoreReview("SR-42");
        verify(storeService).getStoreInfo(240L);
        verify(storeReviewHealthScoreService).getStoreReviewStoreHealthScore("SR-42");
        verify(settingChangeLogService).getSettingsLogByReferenceId("SR-42");
        verify(settingsCalculator).calculate(anyList(), any());
        verify(insightService).getInsightsByReferenceId("SR-42");
        verify(storeAssetReviewService).getStoreAssetReviews("SR-42");
        verify(storeReviewAssetService).getAssetsForStore(240L);
        verify(storeReviewHealthScoreService).getStoreReviewAssetHealthScores("SR-42");
        verify(userAccountService).findUser("currentUser");
    }

    @Test
    void testGetStoreReviewReportDataWithAssetsAndNoChangesNorInsights() {
        CerberusUserInformation userInformation = new CerberusUserInformation();
        userInformation.setUserName("currentUser");
        UserContext userContext = new UserContext(userInformation, Collections.emptySet());

        Instant reviewStartedAt = LocalDateTime.of(2021, 8, 10, 9, 15).toInstant(ZoneOffset.UTC);
        Instant monitoringStartedAt = LocalDateTime.of(2021, 8, 20, 14, 45).toInstant(ZoneOffset.UTC);
        when(storeReviewService.getStoreReview("SR-42")).thenReturn(StoreReview.builder()
                .id("SR-42")
                .storeNumber(240L)
                .assignee("user0")
                .assigneeName("User Name")
                .startedAt(reviewStartedAt)
                .monitoringStartedAt(monitoringStartedAt)
                .build());

        LocationTimeZone locationTimeZone = LocationTimeZone.builder()
                .dstTimeZone(DstTimeZone.builder()
                        .timeZoneId("America/Chicago")
                        .build())
                .build();
        LocationAddress locationAddress = LocationAddress.builder()
                .addressLine1("Address line 1")
                .city("City")
                .state("ZZ")
                .postalCode("99999-0000")
                .build();
        when(storeService.getStoreInfo(240L)).thenReturn(StoreDetail.builder()
                .facilityDetail(FacilityDetail.builder()
                        .location(Location.builder()
                                .locationTimeZone(locationTimeZone)
                                .locationAddress(locationAddress)
                                .build())
                        .build())
                .build());

        when(assetMaintenanceService.getServiceModel(240L)).thenReturn(AmgNote.builder()
                .header("SDM")
                .value("Service Model")
                .build());

        when(storeReviewHealthScoreService.getStoreReviewStoreHealthScore("SR-42")).thenReturn(Optional.of(StoreReviewStoreHealthScore.builder()
                .storeReviewId("SR-42")
                .storeNumber(240L)
                .reviewStartTimestamp(reviewStartedAt)
                .healthScoreStart(40.29)
                .timestampStart(reviewStartedAt.minusSeconds(300))
                .reviewEndTimestamp(monitoringStartedAt)
                .healthScoreEnd(56.43)
                .timestampEnd(monitoringStartedAt.minusSeconds(900))
                .build()));

        RefrigerationSensor asset = RefrigerationSensor.builder()
                .id("8647")
                .type("Rack")
                .build();
        when(storeReviewAssetService.getAssetsForStore(240L)).thenReturn(Collections.singletonList(asset));

        when(storeReviewHealthScoreService.getStoreReviewAssetHealthScores("SR-42")).thenReturn(Collections.singletonList(
                StoreReviewAssetHealthScore.builder()
                        .storeReviewId("SR-42")
                        .assetMappingId("8647")
                        .reviewStartTimestamp(reviewStartedAt)
                        .healthScoreStart(80.41)
                        .timestampStart(reviewStartedAt.minusSeconds(600))
                        .reviewEndTimestamp(monitoringStartedAt)
                        .healthScoreEnd(91.28)
                        .timestampEnd(monitoringStartedAt.minusSeconds(1200))
                        .build()));

        when(storeAssetReviewService.getStoreAssetReviews("SR-42")).thenReturn(Collections.singletonList(
                StoreAssetReview.builder()
                        .assetMappingId("8647")
                        .workOrderId("WO00123L")
                        .build()));

        User user = User.builder()
                .firstName("Current")
                .lastName("User")
                .build();
        when(userAccountService.findUser("currentUser")).thenReturn(user);

        StoreReviewReport storeReviewReport = storeReviewReportService.getStoreReviewReportData("SR-42", userContext);

        assertThat(storeReviewReport).isNotNull();

        StoreReviewDetailsReport storeReviewDetailsReport = storeReviewReport.getStoreReviewDetails();
        assertThat(storeReviewDetailsReport).isNotNull();
        assertThat(storeReviewDetailsReport.getStoreReviewId()).isEqualTo("SR-42");
        assertThat(storeReviewDetailsReport.getReviewer()).isEqualTo("user0");
        assertThat(storeReviewDetailsReport.getReviewerName()).isEqualTo("User Name");
        assertThat(storeReviewDetailsReport.getStoreReviewStartDate()).isEqualTo(LocalDate.of(2021, 8, 10));
        assertThat(storeReviewDetailsReport.getStoreReviewEndDate()).isEqualTo(LocalDate.of(2021, 8, 20));

        StoreDetailsReport storeDetailsReport = storeReviewReport.getStoreDetails();
        assertThat(storeDetailsReport).isNotNull();
        assertThat(storeDetailsReport.getStoreNumber()).isEqualTo(240L);
        assertThat(storeDetailsReport.getStoreAddress()).isEqualTo(locationAddress);
        assertThat(storeDetailsReport.getStoreHealthStart()).isNotNull();
        assertThat(storeDetailsReport.getStoreHealthStart().getScore()).isEqualTo(40.29);
        assertThat(storeDetailsReport.getStoreHealthStart().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(reviewStartedAt.minusSeconds(300), ZoneId.of("America/Chicago")));
        assertThat(storeDetailsReport.getStoreHealthEnd()).isNotNull();
        assertThat(storeDetailsReport.getStoreHealthEnd().getScore()).isEqualTo(56.43);
        assertThat(storeDetailsReport.getStoreHealthEnd().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(monitoringStartedAt.minusSeconds(900), ZoneId.of("America/Chicago")));

        List<AssetReviewSummaryReport> summary = storeReviewReport.getAssetReviewSummary();
        assertThat(summary).isEmpty();

        List<AssetReviewDetailsReport> assets = storeReviewReport.getAssets();
        assertThat(assets).hasSize(1);
        assertThat(assets.get(0)).isNotNull();
        assertThat(assets.get(0).getIndex()).isEqualTo(1);
        assertThat(assets.get(0).getAsset()).isEqualTo(asset);
        assertThat(assets.get(0).getWorkOrderId()).isEqualTo("WO00123L");
        assertThat(assets.get(0).getAssetHealthStart()).isNotNull();
        assertThat(assets.get(0).getAssetHealthStart().getScore()).isEqualTo(80.41);
        assertThat(assets.get(0).getAssetHealthStart().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(reviewStartedAt.minusSeconds(600), ZoneId.of("America/Chicago")));
        assertThat(assets.get(0).getAssetHealthEnd()).isNotNull();
        assertThat(assets.get(0).getAssetHealthEnd().getScore()).isEqualTo(91.28);
        assertThat(assets.get(0).getAssetHealthEnd().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(monitoringStartedAt.minusSeconds(1200), ZoneId.of("America/Chicago")));

        assertThat(assets.get(0).getHasSettingChangeLogs()).isFalse();
        assertThat(assets.get(0).getSettingChangeLogs()).isEmpty();

        assertThat(assets.get(0).getHasInsights()).isFalse();
        assertThat(assets.get(0).getInsights()).isEmpty();

        assertThat(assets.get(0).getIsReviewed()).isFalse();

        assertThat(storeReviewReport.getTimestamp()).isNotNull();
        assertThat(storeReviewReport.getTraceId()).isNotNull();
        assertThat(storeReviewReport.getUser()).isEqualTo("currentUser");
        assertThat(storeReviewReport.getUserName()).isEqualTo(user.getFullName());

        verify(storeReviewService).getStoreReview("SR-42");
        verify(storeService).getStoreInfo(240L);
        verify(storeReviewHealthScoreService).getStoreReviewStoreHealthScore("SR-42");
        verify(settingChangeLogService).getSettingsLogByReferenceId("SR-42");
        verify(settingsCalculator).calculate(anyList(), any());
        verify(insightService).getInsightsByReferenceId("SR-42");
        verify(storeAssetReviewService).getStoreAssetReviews("SR-42");
        verify(storeReviewAssetService).getAssetsForStore(240L);
        verify(storeReviewHealthScoreService).getStoreReviewAssetHealthScores("SR-42");
        verify(userAccountService).findUser("currentUser");
    }

    @Test
    void testGetStoreReviewReportDataWithAssetsAndNoChangesNorInsightsNorWorkOrder() {
        CerberusUserInformation userInformation = new CerberusUserInformation();
        userInformation.setUserName("currentUser");
        UserContext userContext = new UserContext(userInformation, Collections.emptySet());

        Instant reviewStartedAt = LocalDateTime.of(2021, 8, 10, 9, 15).toInstant(ZoneOffset.UTC);
        Instant monitoringStartedAt = LocalDateTime.of(2021, 8, 20, 14, 45).toInstant(ZoneOffset.UTC);
        when(storeReviewService.getStoreReview("SR-42")).thenReturn(StoreReview.builder()
                .id("SR-42")
                .storeNumber(240L)
                .assignee("user0")
                .assigneeName("User Name")
                .startedAt(reviewStartedAt)
                .monitoringStartedAt(monitoringStartedAt)
                .build());

        LocationTimeZone locationTimeZone = LocationTimeZone.builder()
                .dstTimeZone(DstTimeZone.builder()
                        .timeZoneId("America/Chicago")
                        .build())
                .build();
        LocationAddress locationAddress = LocationAddress.builder()
                .addressLine1("Address line 1")
                .city("City")
                .state("ZZ")
                .postalCode("99999-0000")
                .build();
        when(storeService.getStoreInfo(240L)).thenReturn(StoreDetail.builder()
                .facilityDetail(FacilityDetail.builder()
                        .location(Location.builder()
                                .locationTimeZone(locationTimeZone)
                                .locationAddress(locationAddress)
                                .build())
                        .build())
                .build());

        when(assetMaintenanceService.getServiceModel(240L)).thenReturn(AmgNote.builder()
                .header("SDM")
                .value("Service Model")
                .build());

        when(storeReviewHealthScoreService.getStoreReviewStoreHealthScore("SR-42")).thenReturn(Optional.of(StoreReviewStoreHealthScore.builder()
                .storeReviewId("SR-42")
                .storeNumber(240L)
                .reviewStartTimestamp(reviewStartedAt)
                .healthScoreStart(40.29)
                .timestampStart(reviewStartedAt.minusSeconds(300))
                .reviewEndTimestamp(monitoringStartedAt)
                .healthScoreEnd(56.43)
                .timestampEnd(monitoringStartedAt.minusSeconds(900))
                .build()));

        RefrigerationSensor asset = RefrigerationSensor.builder()
                .id("8647")
                .type("Rack")
                .build();
        when(storeReviewAssetService.getAssetsForStore(240L)).thenReturn(Collections.singletonList(asset));

        when(storeReviewHealthScoreService.getStoreReviewAssetHealthScores("SR-42")).thenReturn(Collections.singletonList(
                StoreReviewAssetHealthScore.builder()
                        .storeReviewId("SR-42")
                        .assetMappingId("8647")
                        .reviewStartTimestamp(reviewStartedAt)
                        .healthScoreStart(80.41)
                        .timestampStart(reviewStartedAt.minusSeconds(600))
                        .reviewEndTimestamp(monitoringStartedAt)
                        .healthScoreEnd(91.28)
                        .timestampEnd(monitoringStartedAt.minusSeconds(1200))
                        .build()));

        User user = User.builder()
                .firstName("Current")
                .lastName("User")
                .build();
        when(userAccountService.findUser("currentUser")).thenReturn(user);

        StoreReviewReport storeReviewReport = storeReviewReportService.getStoreReviewReportData("SR-42", userContext);

        assertThat(storeReviewReport).isNotNull();

        StoreReviewDetailsReport storeReviewDetailsReport = storeReviewReport.getStoreReviewDetails();
        assertThat(storeReviewDetailsReport).isNotNull();
        assertThat(storeReviewDetailsReport.getStoreReviewId()).isEqualTo("SR-42");
        assertThat(storeReviewDetailsReport.getReviewer()).isEqualTo("user0");
        assertThat(storeReviewDetailsReport.getReviewerName()).isEqualTo("User Name");
        assertThat(storeReviewDetailsReport.getStoreReviewStartDate()).isEqualTo(LocalDate.of(2021, 8, 10));
        assertThat(storeReviewDetailsReport.getStoreReviewEndDate()).isEqualTo(LocalDate.of(2021, 8, 20));

        StoreDetailsReport storeDetailsReport = storeReviewReport.getStoreDetails();
        assertThat(storeDetailsReport).isNotNull();
        assertThat(storeDetailsReport.getStoreNumber()).isEqualTo(240L);
        assertThat(storeDetailsReport.getStoreAddress()).isEqualTo(locationAddress);
        assertThat(storeDetailsReport.getStoreHealthStart()).isNotNull();
        assertThat(storeDetailsReport.getStoreHealthStart().getScore()).isEqualTo(40.29);
        assertThat(storeDetailsReport.getStoreHealthStart().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(reviewStartedAt.minusSeconds(300), ZoneId.of("America/Chicago")));
        assertThat(storeDetailsReport.getStoreHealthEnd()).isNotNull();
        assertThat(storeDetailsReport.getStoreHealthEnd().getScore()).isEqualTo(56.43);
        assertThat(storeDetailsReport.getStoreHealthEnd().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(monitoringStartedAt.minusSeconds(900), ZoneId.of("America/Chicago")));

        assertThat(storeReviewReport.getAssetReviewSummary()).isEmpty();

        List<AssetReviewDetailsReport> assets = storeReviewReport.getAssets();
        assertThat(assets).hasSize(1);
        assertThat(assets.get(0)).isNotNull();
        assertThat(assets.get(0).getIndex()).isEqualTo(1);
        assertThat(assets.get(0).getAsset()).isEqualTo(asset);
        assertThat(assets.get(0).getWorkOrderId()).isNull();
        assertThat(assets.get(0).getAssetHealthStart()).isNotNull();
        assertThat(assets.get(0).getAssetHealthStart().getScore()).isEqualTo(80.41);
        assertThat(assets.get(0).getAssetHealthStart().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(reviewStartedAt.minusSeconds(600), ZoneId.of("America/Chicago")));
        assertThat(assets.get(0).getAssetHealthEnd()).isNotNull();
        assertThat(assets.get(0).getAssetHealthEnd().getScore()).isEqualTo(91.28);
        assertThat(assets.get(0).getAssetHealthEnd().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(monitoringStartedAt.minusSeconds(1200), ZoneId.of("America/Chicago")));

        assertThat(assets.get(0).getHasSettingChangeLogs()).isFalse();
        assertThat(assets.get(0).getSettingChangeLogs()).isEmpty();

        assertThat(assets.get(0).getHasInsights()).isFalse();
        assertThat(assets.get(0).getInsights()).isEmpty();

        assertThat(assets.get(0).getIsReviewed()).isFalse();

        assertThat(storeReviewReport.getTimestamp()).isNotNull();
        assertThat(storeReviewReport.getTraceId()).isNotNull();
        assertThat(storeReviewReport.getUser()).isEqualTo("currentUser");
        assertThat(storeReviewReport.getUserName()).isEqualTo(user.getFullName());

        verify(storeReviewService).getStoreReview("SR-42");
        verify(storeService).getStoreInfo(240L);
        verify(storeReviewHealthScoreService).getStoreReviewStoreHealthScore("SR-42");
        verify(settingChangeLogService).getSettingsLogByReferenceId("SR-42");
        verify(settingsCalculator).calculate(anyList(), any());
        verify(insightService).getInsightsByReferenceId("SR-42");
        verify(storeAssetReviewService).getStoreAssetReviews("SR-42");
        verify(storeReviewAssetService).getAssetsForStore(240L);
        verify(storeReviewHealthScoreService).getStoreReviewAssetHealthScores("SR-42");
        verify(userAccountService).findUser("currentUser");
    }

    @Test
    void testGetStoreReviewReportDataWithAssetsWithFilter() {
        Instant reviewStartedAt = LocalDateTime.of(2021, 8, 10, 9, 15).toInstant(ZoneOffset.UTC);
        Instant monitoringStartedAt = LocalDateTime.of(2021, 8, 20, 14, 45).toInstant(ZoneOffset.UTC);
        when(storeReviewService.getStoreReview("SR-42")).thenReturn(StoreReview.builder()
                .id("SR-42")
                .storeNumber(240L)
                .assignee("user0")
                .assigneeName("User Name")
                .startedAt(reviewStartedAt)
                .monitoringStartedAt(monitoringStartedAt)
                .build());

        LocationTimeZone locationTimeZone = LocationTimeZone.builder()
                .dstTimeZone(DstTimeZone.builder()
                        .timeZoneId("America/Chicago")
                        .build())
                .build();
        LocationAddress locationAddress = LocationAddress.builder()
                .addressLine1("Address line 1")
                .city("City")
                .state("ZZ")
                .postalCode("99999-0000")
                .build();
        when(storeService.getStoreInfo(240L)).thenReturn(StoreDetail.builder()
                .facilityDetail(FacilityDetail.builder()
                        .location(Location.builder()
                                .locationTimeZone(locationTimeZone)
                                .locationAddress(locationAddress)
                                .build())
                        .build())
                .build());

        when(assetMaintenanceService.getServiceModel(240L)).thenReturn(AmgNote.builder()
                .header("SDM")
                .value("Service Model")
                .build());

        when(storeReviewHealthScoreService.getStoreReviewStoreHealthScore("SR-42")).thenReturn(Optional.of(StoreReviewStoreHealthScore.builder()
                .storeReviewId("SR-42")
                .storeNumber(240L)
                .reviewStartTimestamp(reviewStartedAt)
                .healthScoreStart(40.29)
                .timestampStart(reviewStartedAt.minusSeconds(300))
                .reviewEndTimestamp(monitoringStartedAt)
                .healthScoreEnd(56.43)
                .timestampEnd(monitoringStartedAt.minusSeconds(900))
                .build()));

        RefrigerationSensor asset1 = RefrigerationSensor.builder()
                .id("8647")
                .type("Rack")
                .assetName("A-240")
                .build();
        RefrigerationSensor asset2 = RefrigerationSensor.builder()
                .id("8648")
                .type("Case")
                .assetName("B13d-240")
                .build();
        RefrigerationSensor asset3 = RefrigerationSensor.builder()
                .id("8649")
                .type("Case")
                .assetName("A16b-240")
                .build();
        RefrigerationSensor asset4 = RefrigerationSensor.builder()
                .id("8650")
                .type("Case")
                .assetName("A16a-240")
                .build();
        RefrigerationSensor asset5 = RefrigerationSensor.builder()
                .id("8651")
                .type("Case")
                .assetName("C14a-240")
                .build();
        RefrigerationSensor asset6 = RefrigerationSensor.builder()
                .id("8652")
                .type("Rack")
                .assetName("C-240")
                .build();
        RefrigerationSensor asset7 = RefrigerationSensor.builder()
                .id("8653")
                .type("Cooler")
                .assetName("F-240")
                .build();
        RefrigerationSensor asset8 = RefrigerationSensor.builder()
                .id("8654")
                .type("Cooler")
                .assetName("D-240")
                .build();
        RefrigerationSensor asset9 = RefrigerationSensor.builder()
                .id("8655")
                .type("Freezer")
                .assetName("C18-240")
                .build();
        RefrigerationSensor asset10 = RefrigerationSensor.builder()
                .id("8656")
                .type("Freezer")
                .assetName("B20-240")
                .build();
        when(storeReviewAssetService.getAssetsForStore(240L)).thenReturn(Arrays.asList(asset1, asset2, asset3, asset4, asset5, asset6, asset7, asset8, asset9, asset10));

        when(storeReviewHealthScoreService.getStoreReviewAssetHealthScores("SR-42")).thenReturn(Collections.singletonList(
                StoreReviewAssetHealthScore.builder()
                        .storeReviewId("SR-42")
                        .assetMappingId("8647")
                        .reviewStartTimestamp(reviewStartedAt)
                        .healthScoreStart(80.41)
                        .timestampStart(reviewStartedAt.minusSeconds(600))
                        .reviewEndTimestamp(monitoringStartedAt)
                        .healthScoreEnd(91.28)
                        .timestampEnd(monitoringStartedAt.minusSeconds(1200))
                        .build()));

        when(storeAssetReviewService.getStoreAssetReviews("SR-42")).thenReturn(List.of(
                StoreAssetReview.builder()
                        .assetMappingId("8647")
                        .workOrderId("WO00123L")
                        .build(),
                StoreAssetReview.builder()
                        .assetMappingId("8649")
                        .workOrderId("WO00123L")
                        .build(),
                StoreAssetReview.builder()
                        .assetMappingId("8650")
                        .state("created")
                        .build(),
                StoreAssetReview.builder()
                        .assetMappingId("8654")
                        .state("completed")
                        .build()));

        List<SettingChangeLog> settingChangeLogs = Arrays.asList(
                SettingChangeLog.builder()
                        .assetMappingId("8647")
                        .createdAt(ZonedDateTime.of(LocalDateTime.of(2021, 9, 1, 10, 30), ZoneOffset.UTC).toInstant())
                        .build(),
                SettingChangeLog.builder()
                        .assetMappingId("8647")
                        .createdAt(ZonedDateTime.of(LocalDateTime.of(2021, 9, 1, 10, 45), ZoneOffset.UTC).toInstant())
                        .build());
        when(settingChangeLogService.getSettingsLogByReferenceId("SR-42")).thenReturn(settingChangeLogs);

        List<Insight> insights = Arrays.asList(
                Insight.builder()
                        .assetMappingId("8647")
                        .createdAt(LocalDateTime.of(2021, 9, 1, 10, 40).toInstant(ZoneOffset.UTC))
                        .build(),
                Insight.builder()
                        .assetMappingId("8647")
                        .createdAt(LocalDateTime.of(2021, 9, 1, 9, 30).toInstant(ZoneOffset.UTC))
                        .build(),
                Insight.builder()
                        .assetMappingId("8648")
                        .createdAt(LocalDateTime.of(2021, 9, 1, 9, 45).toInstant(ZoneOffset.UTC))
                        .build(),
                Insight.builder()
                        .assetMappingId("8652")
                        .createdAt(LocalDateTime.of(2021, 9, 1, 10, 15).toInstant(ZoneOffset.UTC))
                        .build());
        when(insightService.getInsightsByReferenceId("SR-42")).thenReturn(insights);

        User user = User.builder()
                .firstName("Current")
                .lastName("User")
                .build();
        when(userAccountService.findUser("currentUser")).thenReturn(user);

        StoreReviewReport storeReviewReport = storeReviewReportService.getStoreReviewReportData("SR-42", null);

        assertThat(storeReviewReport).isNotNull();

        StoreReviewDetailsReport storeReviewDetailsReport = storeReviewReport.getStoreReviewDetails();
        assertThat(storeReviewDetailsReport).isNotNull();
        assertThat(storeReviewDetailsReport.getStoreReviewId()).isEqualTo("SR-42");
        assertThat(storeReviewDetailsReport.getReviewer()).isEqualTo("user0");
        assertThat(storeReviewDetailsReport.getReviewerName()).isEqualTo("User Name");
        assertThat(storeReviewDetailsReport.getStoreReviewStartDate()).isEqualTo(LocalDate.of(2021, 8, 10));
        assertThat(storeReviewDetailsReport.getStoreReviewEndDate()).isEqualTo(LocalDate.of(2021, 8, 20));

        StoreDetailsReport storeDetailsReport = storeReviewReport.getStoreDetails();
        assertThat(storeDetailsReport).isNotNull();
        assertThat(storeDetailsReport.getStoreNumber()).isEqualTo(240L);
        assertThat(storeDetailsReport.getStoreAddress()).isEqualTo(locationAddress);
        assertThat(storeDetailsReport.getStoreHealthStart()).isNotNull();
        assertThat(storeDetailsReport.getStoreHealthStart().getScore()).isEqualTo(40.29);
        assertThat(storeDetailsReport.getStoreHealthStart().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(reviewStartedAt.minusSeconds(300), ZoneId.of("America/Chicago")));
        assertThat(storeDetailsReport.getStoreHealthEnd()).isNotNull();
        assertThat(storeDetailsReport.getStoreHealthEnd().getScore()).isEqualTo(56.43);
        assertThat(storeDetailsReport.getStoreHealthEnd().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(monitoringStartedAt.minusSeconds(900), ZoneId.of("America/Chicago")));

        List<AssetReviewSummaryReport> summary = storeReviewReport.getAssetReviewSummary();
        assertThat(summary).hasSize(3);

        assertThat(summary.get(0)).isNotNull();
        assertThat(summary.get(0).getAssetType()).isEqualTo("Rack");
        assertThat(summary.get(0).getReviewedAssets()).isEqualTo(2);
        assertThat(summary.get(0).getSettingChangeLogs()).isEqualTo(1);
        assertThat(summary.get(0).getInsights()).isEqualTo(2);
        assertThat(summary.get(0).getWorkOrders()).isEqualTo(1);

        assertThat(summary.get(1)).isNotNull();
        assertThat(summary.get(1).getAssetType()).isEqualTo("Case");
        assertThat(summary.get(1).getReviewedAssets()).isEqualTo(1);
        assertThat(summary.get(1).getSettingChangeLogs()).isEqualTo(0);
        assertThat(summary.get(1).getInsights()).isEqualTo(1);
        assertThat(summary.get(1).getWorkOrders()).isEqualTo(0);

        assertThat(summary.get(2)).isNotNull();
        assertThat(summary.get(2).getAssetType()).isEqualTo("Cooler");
        assertThat(summary.get(2).getReviewedAssets()).isEqualTo(1);
        assertThat(summary.get(2).getSettingChangeLogs()).isEqualTo(0);
        assertThat(summary.get(2).getInsights()).isEqualTo(0);
        assertThat(summary.get(2).getWorkOrders()).isEqualTo(0);

        List<AssetReviewDetailsReport> assets = storeReviewReport.getAssets();
        assertThat(assets).hasSize(10);
        assertThat(assets.get(0)).isNotNull();
        assertThat(assets.get(0).getIndex()).isEqualTo(1);
        assertThat(assets.get(0).getAsset()).isEqualTo(asset1);
        assertThat(assets.get(0).getWorkOrderId()).isEqualTo("WO00123L");
        assertThat(assets.get(0).getAssetHealthStart()).isNotNull();
        assertThat(assets.get(0).getAssetHealthStart().getScore()).isEqualTo(80.41);
        assertThat(assets.get(0).getAssetHealthStart().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(reviewStartedAt.minusSeconds(600), ZoneId.of("America/Chicago")));
        assertThat(assets.get(0).getAssetHealthEnd()).isNotNull();
        assertThat(assets.get(0).getAssetHealthEnd().getScore()).isEqualTo(91.28);
        assertThat(assets.get(0).getAssetHealthEnd().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(monitoringStartedAt.minusSeconds(1200), ZoneId.of("America/Chicago")));

        assertThat(assets.get(0).getHasSettingChangeLogs()).isTrue();
        assertThat(assets.get(0).getSettingChangeLogs()).hasSize(2);
        assertThat(assets.get(0).getSettingChangeLogs().get(0).getAssetMappingId()).isEqualTo(settingChangeLogs.get(0).getAssetMappingId());
        assertThat(assets.get(0).getSettingChangeLogs().get(0).getCreatedAt()).isEqualTo(settingChangeLogs.get(0).getCreatedAt().atZone(ZoneId.of("America/Chicago")).toLocalDateTime());
        assertThat(assets.get(0).getSettingChangeLogs().get(1).getAssetMappingId()).isEqualTo(settingChangeLogs.get(1).getAssetMappingId());
        assertThat(assets.get(0).getSettingChangeLogs().get(1).getCreatedAt()).isEqualTo(settingChangeLogs.get(1).getCreatedAt().atZone(ZoneId.of("America/Chicago")).toLocalDateTime());

        assertThat(assets.get(0).getHasInsights()).isTrue();
        assertThat(assets.get(0).getInsights()).hasSize(2);
        assertThat(assets.get(0).getInsights().get(0).getAssetMappingId()).isEqualTo(insights.get(1).getAssetMappingId());
        assertThat(assets.get(0).getInsights().get(0).getCreatedAt()).isEqualTo(insights.get(1).getCreatedAt().atZone(ZoneId.of("America/Chicago")).toLocalDateTime());
        assertThat(assets.get(0).getInsights().get(1).getAssetMappingId()).isEqualTo(insights.get(0).getAssetMappingId());
        assertThat(assets.get(0).getInsights().get(1).getCreatedAt()).isEqualTo(insights.get(0).getCreatedAt().atZone(ZoneId.of("America/Chicago")).toLocalDateTime());

        assertThat(assets.get(0).getIsReviewed()).isTrue();

        assertThat(assets.get(1)).isNotNull();
        assertThat(assets.get(1).getIndex()).isEqualTo(2);
        assertThat(assets.get(1).getAsset()).isEqualTo(asset6);
        assertThat(assets.get(1).getHasSettingChangeLogs()).isFalse();
        assertThat(assets.get(1).getHasInsights()).isTrue();
        assertThat(assets.get(1).getIsReviewed()).isTrue();

        assertThat(assets.get(2)).isNotNull();
        assertThat(assets.get(2).getIndex()).isEqualTo(3);
        assertThat(assets.get(2).getAsset()).isEqualTo(asset4);
        assertThat(assets.get(2).getHasSettingChangeLogs()).isFalse();
        assertThat(assets.get(2).getHasInsights()).isFalse();
        assertThat(assets.get(2).getIsReviewed()).isFalse();

        assertThat(assets.get(3)).isNotNull();
        assertThat(assets.get(3).getIndex()).isEqualTo(4);
        assertThat(assets.get(3).getAsset()).isEqualTo(asset3);
        assertThat(assets.get(3).getHasSettingChangeLogs()).isFalse();
        assertThat(assets.get(3).getHasInsights()).isFalse();
        assertThat(assets.get(3).getIsReviewed()).isFalse();

        assertThat(assets.get(4)).isNotNull();
        assertThat(assets.get(4).getIndex()).isEqualTo(5);
        assertThat(assets.get(4).getAsset()).isEqualTo(asset2);
        assertThat(assets.get(4).getHasSettingChangeLogs()).isFalse();
        assertThat(assets.get(4).getHasInsights()).isTrue();
        assertThat(assets.get(4).getIsReviewed()).isTrue();

        assertThat(assets.get(5)).isNotNull();
        assertThat(assets.get(5).getIndex()).isEqualTo(6);
        assertThat(assets.get(5).getAsset()).isEqualTo(asset5);
        assertThat(assets.get(5).getHasSettingChangeLogs()).isFalse();
        assertThat(assets.get(5).getHasInsights()).isFalse();
        assertThat(assets.get(5).getIsReviewed()).isFalse();

        assertThat(assets.get(6)).isNotNull();
        assertThat(assets.get(6).getIndex()).isEqualTo(7);
        assertThat(assets.get(6).getAsset()).isEqualTo(asset10);
        assertThat(assets.get(6).getHasSettingChangeLogs()).isFalse();
        assertThat(assets.get(6).getHasInsights()).isFalse();
        assertThat(assets.get(6).getIsReviewed()).isFalse();

        assertThat(assets.get(7)).isNotNull();
        assertThat(assets.get(7).getIndex()).isEqualTo(8);
        assertThat(assets.get(7).getAsset()).isEqualTo(asset9);
        assertThat(assets.get(7).getHasSettingChangeLogs()).isFalse();
        assertThat(assets.get(7).getHasInsights()).isFalse();
        assertThat(assets.get(7).getIsReviewed()).isFalse();

        assertThat(assets.get(8)).isNotNull();
        assertThat(assets.get(8).getIndex()).isEqualTo(9);
        assertThat(assets.get(8).getAsset()).isEqualTo(asset8);
        assertThat(assets.get(8).getHasSettingChangeLogs()).isFalse();
        assertThat(assets.get(8).getHasInsights()).isFalse();
        assertThat(assets.get(8).getIsReviewed()).isTrue();

        assertThat(assets.get(9)).isNotNull();
        assertThat(assets.get(9).getIndex()).isEqualTo(10);
        assertThat(assets.get(9).getAsset()).isEqualTo(asset7);
        assertThat(assets.get(9).getHasSettingChangeLogs()).isFalse();
        assertThat(assets.get(9).getHasInsights()).isFalse();
        assertThat(assets.get(9).getIsReviewed()).isFalse();

        assertThat(storeReviewReport.getTimestamp()).isNotNull();
        assertThat(storeReviewReport.getTraceId()).isNotNull();
        assertThat(storeReviewReport.getUser()).isNull();
        assertThat(storeReviewReport.getUserName()).isNull();

        verify(storeReviewService).getStoreReview("SR-42");
        verify(storeService).getStoreInfo(240L);
        verify(storeReviewHealthScoreService).getStoreReviewStoreHealthScore("SR-42");
        verify(settingChangeLogService).getSettingsLogByReferenceId("SR-42");
        verify(settingsCalculator, times(10)).calculate(anyList(), any());
        verify(insightService).getInsightsByReferenceId("SR-42");
        verify(storeAssetReviewService).getStoreAssetReviews("SR-42");
        verify(storeReviewAssetService).getAssetsForStore(240L);
        verify(storeReviewHealthScoreService).getStoreReviewAssetHealthScores("SR-42");

        verify(userAccountService, never()).findUser(anyString());
    }

    @Test
    void testGetStoreReviewReportSpreadsheetWithAssets() {
        Instant reviewStartedAt = LocalDateTime.of(2021, 8, 10, 9, 15).toInstant(ZoneOffset.UTC);
        Instant monitoringStartedAt = LocalDateTime.of(2021, 8, 20, 14, 45).toInstant(ZoneOffset.UTC);
        Instant validationStartedAt = LocalDateTime.of(2021, 8, 24, 15, 15).toInstant(ZoneOffset.UTC);
        when(storeReviewService.getStoreReview("SR-42")).thenReturn(StoreReview.builder()
                .id("SR-42")
                .storeNumber(240L)
                .assignee("user0")
                .assigneeName("User Name")
                .startedAt(reviewStartedAt)
                .monitoringStartedAt(monitoringStartedAt)
                .build());

        LocationTimeZone locationTimeZone = LocationTimeZone.builder()
                .dstTimeZone(DstTimeZone.builder()
                        .timeZoneId("America/Chicago")
                        .build())
                .build();
        LocationAddress locationAddress = LocationAddress.builder()
                .addressLine1("Address line 1")
                .city("City")
                .state("ZZ")
                .postalCode("99999-0000")
                .build();
        when(storeService.getStoreInfo(240L)).thenReturn(StoreDetail.builder()
                .facilityDetail(FacilityDetail.builder()
                        .location(Location.builder()
                                .locationTimeZone(locationTimeZone)
                                .locationAddress(locationAddress)
                                .build())
                        .build())
                .build());

        when(assetMaintenanceService.getServiceModel(240L)).thenReturn(AmgNote.builder()
                .header("SDM")
                .value("Service Model")
                .build());

        when(storeReviewHealthScoreService.getStoreReviewStoreHealthScore("SR-42")).thenReturn(Optional.of(StoreReviewStoreHealthScore.builder()
                .storeReviewId("SR-42")
                .storeNumber(240L)
                .preReviewTimestamp(reviewStartedAt.minusSeconds(600))
                .preReviewScore(90.00)
                .preReviewScoreTimestamp(reviewStartedAt.minusSeconds(600))
                .reviewStartTimestamp(reviewStartedAt)
                .healthScoreStart(40.29)
                .timestampStart(reviewStartedAt.minusSeconds(300))
                .reviewEndTimestamp(monitoringStartedAt)
                .healthScoreEnd(56.43)
                .timestampEnd(monitoringStartedAt.minusSeconds(900))
                .postReviewTimestamp(monitoringStartedAt.plus(3, ChronoUnit.DAYS))
                .postReviewScore(91.01)
                .postReviewScoreTimestamp(monitoringStartedAt.plus(3, ChronoUnit.DAYS))
                .postMaintenanceTimestamp(validationStartedAt.plusSeconds(300))
                .postMaintenanceScore(91.28)
                .postMaintenanceScoreTimestamp(validationStartedAt.plusSeconds(300))
                .build()));

        RefrigerationSensor asset1 = RefrigerationSensor.builder()
                .id("8647")
                .type("Rack")
                .assetName("RACK B-240")
                .build();
        RefrigerationSensor asset2 = RefrigerationSensor.builder()
                .id("8648")
                .type("Rack")
                .assetName("RACK A-240")
                .build();
        when(storeReviewAssetService.getAssetsForStore(240L)).thenReturn(Arrays.asList(asset1, asset2));

        when(storeReviewHealthScoreService.getStoreReviewAssetHealthScores("SR-42")).thenReturn(Collections.singletonList(
                StoreReviewAssetHealthScore.builder()
                        .storeReviewId("SR-42")
                        .assetMappingId("8647")
                        .preReviewTimestamp(reviewStartedAt.minusSeconds(600))
                        .preReviewScore(90.00)
                        .preReviewScoreTimestamp(reviewStartedAt.minusSeconds(600))
                        .reviewStartTimestamp(reviewStartedAt)
                        .healthScoreStart(40.29)
                        .timestampStart(reviewStartedAt.minusSeconds(300))
                        .reviewEndTimestamp(monitoringStartedAt)
                        .healthScoreEnd(56.43)
                        .timestampEnd(monitoringStartedAt.minusSeconds(900))
                        .postReviewTimestamp(monitoringStartedAt.plus(3, ChronoUnit.DAYS))
                        .postReviewScore(91.01)
                        .postReviewScoreTimestamp(monitoringStartedAt.plus(3, ChronoUnit.DAYS))
                        .postMaintenanceTimestamp(validationStartedAt.plusSeconds(300))
                        .postMaintenanceScore(91.28)
                        .postMaintenanceScoreTimestamp(validationStartedAt.plusSeconds(300))
                        .build()));

        when(storeAssetReviewService.getStoreAssetReviews("SR-42")).thenReturn(Collections.singletonList(
                StoreAssetReview.builder()
                        .assetMappingId("8647")
                        .workOrderId("WO00123L")
                        .build()));

        List<SettingChangeLog> settingChangeLogs = Arrays.asList(
                SettingChangeLog.builder()
                        .assetMappingId("8647")
                        .createdAt(ZonedDateTime.of(LocalDateTime.of(2021, 9, 1, 10, 30), ZoneOffset.UTC).toInstant())
                        .build(),
                SettingChangeLog.builder()
                        .assetMappingId("8647")
                        .createdAt(ZonedDateTime.of(LocalDateTime.of(2021, 9, 1, 10, 45), ZoneOffset.UTC).toInstant())
                        .build());
        when(settingChangeLogService.getSettingsLogByReferenceId("SR-42")).thenReturn(settingChangeLogs);

        List<Insight> insights = Arrays.asList(
                Insight.builder()
                        .assetMappingId("8647")
                        .createdAt(LocalDateTime.of(2021, 9, 1, 10, 40).toInstant(ZoneOffset.UTC))
                        .build(),
                Insight.builder()
                        .assetMappingId("8647")
                        .createdAt(LocalDateTime.of(2021, 9, 1, 9, 30).toInstant(ZoneOffset.UTC))
                        .build());
        when(insightService.getInsightsByReferenceId("SR-42")).thenReturn(insights);

        User user = User.builder()
                .firstName("Current")
                .lastName("User")
                .build();
        when(userAccountService.findUser("currentUser")).thenReturn(user);

        StoreReviewReportSpreadsheetContainer storeReviewReportSpreadsheetContainer = storeReviewReportService.getStoreReviewReportSpreadsheet("SR-42");

        assertThat(storeReviewReportSpreadsheetContainer).isNotNull();

        List<StoreReviewReportSpreadsheetAssetItem> items = storeReviewReportSpreadsheetContainer.getItems();
        assertThat(items).hasSize(2);

        StoreReviewReportSpreadsheetAssetItem item1 = items.get(0);
        assertThat(item1).isNotNull();
        assertThat(item1.getInitials()).isEqualTo("User Name");
        assertThat(item1.getStoreNumber()).isEqualTo(240L);
        assertThat(item1.getCaseName()).isEqualTo("RACK A-240");
        assertThat(item1.getWorkOrder()).isEqualTo("0");
        assertThat(item1.getReviewType()).isEqualTo("Health Review");
        assertThat(item1.getRemote()).isEqualTo("N/A");

        StoreReviewReportSpreadsheetAssetItem item2 = items.get(1);
        assertThat(item2).isNotNull();
        assertThat(item2.getInitials()).isEqualTo("User Name");
        assertThat(item2.getStoreNumber()).isEqualTo(240L);
        assertThat(item2.getCaseName()).isEqualTo("RACK B-240");
        assertThat(item2.getWorkOrder()).isEqualTo("0");
        assertThat(item2.getReviewType()).isEqualTo("Health Review");
        assertThat(item2.getRemote()).isEqualTo("YES");
        assertThat(item2.getPostReviewScore()).isEqualTo(91.01);
        assertThat(item2.getPreReviewScore()).isEqualTo(90.00);

        verify(storeReviewService).getStoreReview("SR-42");
        verify(storeService).getStoreInfo(240L);
        verify(storeReviewHealthScoreService).getStoreReviewStoreHealthScore("SR-42");
        verify(settingChangeLogService).getSettingsLogByReferenceId("SR-42");
        verify(settingsCalculator, times(2)).calculate(anyList(), any());
        verify(insightService).getInsightsByReferenceId("SR-42");
        verify(storeAssetReviewService).getStoreAssetReviews("SR-42");
        verify(storeReviewAssetService).getAssetsForStore(240L);
        verify(storeReviewHealthScoreService).getStoreReviewAssetHealthScores("SR-42");

        verify(userAccountService, never()).findUser(anyString());
    }

    @Test
    void testGetStoreReviewReportWorkbookWithAssets() {
        CerberusUserInformation userInformation = new CerberusUserInformation();
        userInformation.setUserName("currentUser");
        UserContext userContext = new UserContext(userInformation, Collections.emptySet());

        Instant reviewStartedAt = LocalDateTime.of(2021, 8, 10, 9, 15).toInstant(ZoneOffset.UTC);
        Instant monitoringStartedAt = LocalDateTime.of(2021, 8, 20, 14, 45).toInstant(ZoneOffset.UTC);
        when(storeReviewService.getStoreReview("SR-42")).thenReturn(StoreReview.builder()
                .id("SR-42")
                .storeNumber(240L)
                .assignee("user0")
                .assigneeName("User Name")
                .startedAt(reviewStartedAt)
                .monitoringStartedAt(monitoringStartedAt)
                .build());

        LocationTimeZone locationTimeZone = LocationTimeZone.builder()
                .dstTimeZone(DstTimeZone.builder()
                        .timeZoneId("America/Chicago")
                        .build())
                .build();
        LocationAddress locationAddress = LocationAddress.builder()
                .addressLine1("Address line 1")
                .city("City")
                .state("ZZ")
                .postalCode("99999-0000")
                .build();
        when(storeService.getStoreInfo(240L)).thenReturn(StoreDetail.builder()
                .facilityDetail(FacilityDetail.builder()
                        .location(Location.builder()
                                .locationTimeZone(locationTimeZone)
                                .locationAddress(locationAddress)
                                .build())
                        .build())
                .build());

        when(assetMaintenanceService.getServiceModel(240L)).thenReturn(AmgNote.builder()
                .header("SDM")
                .value("Service Model")
                .build());

        when(storeReviewHealthScoreService.getStoreReviewStoreHealthScore("SR-42")).thenReturn(Optional.of(StoreReviewStoreHealthScore.builder()
                .storeReviewId("SR-42")
                .storeNumber(240L)
                .reviewStartTimestamp(reviewStartedAt)
                .healthScoreStart(40.29)
                .timestampStart(reviewStartedAt.minusSeconds(300))
                .reviewEndTimestamp(monitoringStartedAt)
                .healthScoreEnd(56.43)
                .timestampEnd(monitoringStartedAt.minusSeconds(900))
                .build()));

        RefrigerationSensor asset1 = RefrigerationSensor.builder()
                .id("RACK B-240")
                .type("Rack")
                .build();
        RefrigerationSensor asset2 = RefrigerationSensor.builder()
                .id("RACK A-240")
                .type("Rack")
                .build();
        when(storeReviewAssetService.getAssetsForStore(240L)).thenReturn(Arrays.asList(asset1, asset2));

        when(storeReviewHealthScoreService.getStoreReviewAssetHealthScores("SR-42")).thenReturn(Collections.singletonList(
                StoreReviewAssetHealthScore.builder()
                        .storeReviewId("SR-42")
                        .assetMappingId("8647")
                        .reviewStartTimestamp(reviewStartedAt)
                        .healthScoreStart(80.41)
                        .timestampStart(reviewStartedAt.minusSeconds(600))
                        .reviewEndTimestamp(monitoringStartedAt)
                        .healthScoreEnd(91.28)
                        .timestampEnd(monitoringStartedAt.minusSeconds(1200))
                        .build()));

        when(storeAssetReviewService.getStoreAssetReviews("SR-42")).thenReturn(Collections.singletonList(
                StoreAssetReview.builder()
                        .assetMappingId("8647")
                        .workOrderId("WO00123L")
                        .build()));

        List<SettingChangeLog> settingChangeLogs = Arrays.asList(
                SettingChangeLog.builder()
                        .assetMappingId("8647")
                        .createdAt(ZonedDateTime.of(LocalDateTime.of(2021, 9, 1, 10, 30), ZoneOffset.UTC).toInstant())
                        .build(),
                SettingChangeLog.builder()
                        .assetMappingId("8647")
                        .createdAt(ZonedDateTime.of(LocalDateTime.of(2021, 9, 1, 10, 45), ZoneOffset.UTC).toInstant())
                        .build());
        when(settingChangeLogService.getSettingsLogByReferenceId("SR-42")).thenReturn(settingChangeLogs);

        List<Insight> insights = Arrays.asList(
                Insight.builder()
                        .assetMappingId("8647")
                        .createdAt(LocalDateTime.of(2021, 9, 1, 10, 40).toInstant(ZoneOffset.UTC))
                        .build(),
                Insight.builder()
                        .assetMappingId("8647")
                        .createdAt(LocalDateTime.of(2021, 9, 1, 9, 30).toInstant(ZoneOffset.UTC))
                        .build());
        when(insightService.getInsightsByReferenceId("SR-42")).thenReturn(insights);

        User user = User.builder()
                .firstName("Current")
                .lastName("User")
                .build();
        when(userAccountService.findUser("currentUser")).thenReturn(user);

        when(workbookReporterFactory.getWorkbookReporter(any(StoreReviewReport.class))).thenReturn(new MockWorksheetReporter());

        StoreReviewReportWorkbookContainer storeReviewReportWorkbookContainer = storeReviewReportService.getStoreReviewReportWorkbook("SR-42", userContext);

        assertThat(storeReviewReportWorkbookContainer).isNotNull();
        assertThat(storeReviewReportWorkbookContainer.getBody()).isEqualTo(new byte[]{21});
        verify(storeReviewService).getStoreReview("SR-42");
        verify(storeService).getStoreInfo(240L);
        verify(storeReviewHealthScoreService).getStoreReviewStoreHealthScore("SR-42");
        verify(settingChangeLogService).getSettingsLogByReferenceId("SR-42");
        verify(settingsCalculator, times(2)).calculate(anyList(), any());
        verify(insightService).getInsightsByReferenceId("SR-42");
        verify(storeAssetReviewService).getStoreAssetReviews("SR-42");
        verify(storeReviewAssetService).getAssetsForStore(240L);
        verify(storeReviewHealthScoreService).getStoreReviewAssetHealthScores("SR-42");
        verify(userAccountService).findUser("currentUser");

        ArgumentCaptor<StoreReviewReport> reportCaptor = ArgumentCaptor.forClass(StoreReviewReport.class);
        verify(workbookReporterFactory).getWorkbookReporter(reportCaptor.capture());

        StoreReviewReport storeReviewReport = reportCaptor.getValue();

        assertThat(storeReviewReport).isNotNull();

        StoreReviewDetailsReport storeReviewDetailsReport = storeReviewReport.getStoreReviewDetails();
        assertThat(storeReviewDetailsReport).isNotNull();
        assertThat(storeReviewDetailsReport.getStoreReviewId()).isEqualTo("SR-42");
        assertThat(storeReviewDetailsReport.getReviewer()).isEqualTo("user0");
        assertThat(storeReviewDetailsReport.getReviewerName()).isEqualTo("User Name");
        assertThat(storeReviewDetailsReport.getStoreReviewStartDate()).isEqualTo(LocalDate.of(2021, 8, 10));
        assertThat(storeReviewDetailsReport.getStoreReviewEndDate()).isEqualTo(LocalDate.of(2021, 8, 20));

        StoreDetailsReport storeDetailsReport = storeReviewReport.getStoreDetails();
        assertThat(storeDetailsReport).isNotNull();
        assertThat(storeDetailsReport.getStoreNumber()).isEqualTo(240L);
        assertThat(storeDetailsReport.getStoreAddress()).isEqualTo(locationAddress);
        assertThat(storeDetailsReport.getStoreHealthStart()).isNotNull();
        assertThat(storeDetailsReport.getStoreHealthStart().getScore()).isEqualTo(40.29);
        assertThat(storeDetailsReport.getStoreHealthStart().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(reviewStartedAt.minusSeconds(300), ZoneId.of("America/Chicago")));
        assertThat(storeDetailsReport.getStoreHealthEnd()).isNotNull();
        assertThat(storeDetailsReport.getStoreHealthEnd().getScore()).isEqualTo(56.43);
        assertThat(storeDetailsReport.getStoreHealthEnd().getTimestamp()).isEqualTo(LocalDateTime.ofInstant(monitoringStartedAt.minusSeconds(900), ZoneId.of("America/Chicago")));
    }

}
