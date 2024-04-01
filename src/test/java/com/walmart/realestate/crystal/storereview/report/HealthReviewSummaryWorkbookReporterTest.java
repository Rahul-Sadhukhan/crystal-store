package com.walmart.realestate.crystal.storereview.report;

import com.walmart.realestate.crystal.metadata.properties.MetadataProperties;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationSensor;
import com.walmart.realestate.crystal.storereview.config.PropertiesConfig;
import com.walmart.realestate.crystal.storereview.model.report.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = {PropertiesConfig.class, MetadataProperties.class,WorkbookUtil.class})
@ActiveProfiles("test")
public class HealthReviewSummaryWorkbookReporterTest {

    private HealthReviewSummaryWorkbookReporter healthReviewSummaryWorkbookReporter;

    @Autowired
    private WorkbookUtil workbookUtil;

    @Autowired
    private MetadataProperties metadataProperties;

    @Test
    void testGetWorkbook() {
        StoreReviewReport storeReviewReport = StoreReviewReport.builder()
                .storeReviewDetails(StoreReviewDetailsReport.builder()
                        .storeReviewStartDate(LocalDate.of(2021, 10, 10))
                        .storeReviewEndDate(LocalDate.of(2021, 10, 15))
                        .build())
                .storeDetails(StoreDetailsReport.builder()
                        .storeNumber(661L)
                        .build())
                .assetReviewSummary(Collections.singletonList(AssetReviewSummaryReport.builder()
                        .assetType("Rack")
                        .reviewedAssets(1)
                        .settingChangeLogs(1L)
                        .insights(1L)
                        .workOrders(1L)
                        .build()))
                .assets(Arrays.asList(
                        AssetReviewDetailsReport.builder()
                                .asset(RefrigerationSensor.builder()
                                        .type("Rack")
                                        .build())
                                .hasSettingChangeLogs(true)
                                .settingChangeLogs(Collections.singletonList(SettingChangeLogReport.builder()
                                        .setting("refrigeration-controller-settings:saturated-suction-temperature")
                                        .createdAt(LocalDateTime.of(2021, 10, 12, 11, 30))
                                        .build()))
                                .settingChangeLogsCalculated(Collections.emptyList())
                                .hasInsights(true)
                                .insights(Collections.singletonList(InsightReport.builder()
                                        .createdAt(LocalDateTime.of(2021, 10, 12, 12, 15))
                                        .build()))
                                .build(),
                        AssetReviewDetailsReport.builder()
                                .asset(RefrigerationSensor.builder()
                                        .type("Rack")
                                        .build())
                                .hasSettingChangeLogs(false)
                                .settingChangeLogs(Collections.emptyList())
                                .settingChangeLogsCalculated(Collections.emptyList())
                                .hasInsights(false)
                                .insights(Collections.emptyList())
                                .build(),
                        AssetReviewDetailsReport.builder()
                                .asset(RefrigerationSensor.builder()
                                        .type("Case")
                                        .build())
                                .assetHealthEnd(HealthReport.builder()
                                        .score(33.23)
                                        .build())
                                .hasSettingChangeLogs(true)
                                .settingChangeLogs(Arrays.asList(
                                        SettingChangeLogReport.builder()
                                                .setting("refrigeration-controller-settings:case-defrost-amount")
                                                .createdAt(LocalDateTime.of(2021, 10, 12, 11, 30))
                                                .build(),
                                        SettingChangeLogReport.builder()
                                                .setting("refrigeration-controller-settings:case-temperature-cut-in")
                                                .createdAt(LocalDateTime.of(2021, 10, 12, 11, 40))
                                                .build()))
                                .settingChangeLogsCalculated(Collections.emptyList())
                                .hasInsights(false)
                                .insights(Collections.emptyList())
                                .build(),
                        AssetReviewDetailsReport.builder()
                                .asset(RefrigerationSensor.builder()
                                        .type("Case")
                                        .build())
                                .assetHealthEnd(HealthReport.builder()
                                        .score(33.23)
                                        .build())
                                .hasSettingChangeLogs(false)
                                .settingChangeLogs(Collections.emptyList())
                                .settingChangeLogsCalculated(Collections.emptyList())
                                .hasInsights(false)
                                .insights(Collections.emptyList())
                                .build()))
                .timestamp(Instant.now())
                .build();

        healthReviewSummaryWorkbookReporter = new HealthReviewSummaryWorkbookReporter(metadataProperties, storeReviewReport,workbookUtil);

        byte[] workbook = healthReviewSummaryWorkbookReporter.getWorkbook();

        assertThat(workbook).isNotEmpty();
    }

    @Test
    void testGetTemplatePath() {
        StoreReviewReport storeReviewReport = StoreReviewReport.builder()
                .build();

        healthReviewSummaryWorkbookReporter = new HealthReviewSummaryWorkbookReporter(metadataProperties, storeReviewReport,workbookUtil);

        String templatePath = healthReviewSummaryWorkbookReporter.getTemplatePath();

        assertThat(templatePath).isEqualTo("report/templates/store-review-report/Crystal-HR-Template.xlsx");
    }

}
