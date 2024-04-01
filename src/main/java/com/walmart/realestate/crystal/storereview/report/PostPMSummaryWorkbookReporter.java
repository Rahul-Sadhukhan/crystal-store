package com.walmart.realestate.crystal.storereview.report;

import com.walmart.realestate.crystal.metadata.properties.MetadataProperties;
import com.walmart.realestate.crystal.storereview.model.report.*;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;

import java.util.*;
import java.util.stream.Collectors;

import static com.walmart.realestate.crystal.storereview.report.WorkbookManifest.Type.*;
import static com.walmart.realestate.crystal.storereview.report.WorkbookUtil.Constants.*;
import static com.walmart.realestate.crystal.storereview.report.WorkbookUtil.*;
import static com.walmart.realestate.crystal.storereview.service.StoreReviewReportService.Constants.*;
import static java.lang.Boolean.TRUE;

@Slf4j
public class PostPMSummaryWorkbookReporter implements WorkbookReporter {

    private final MetadataProperties metadataProperties;

    private final StoreReviewReport storeReviewReport;

    private static Map<String, List<String>> assetTypesMap;

    private static final Map<String, WorkbookManifest<AssetReviewDetailsReport>> assetDetailManifests = new HashMap<>();

    private static final Map<String, WorkbookManifest<StoreReviewReport>> reviewManifests = new HashMap<>();

    private static final Map<String, Integer> rackCellPositions = new HashMap<>();

    private static final Map<String, Integer> caseCellPositions = new HashMap<>();

    private final WorkbookUtil workbookUtil;

    @Getter
    private final String reportName = " - DOC Post-PM Report";

    public PostPMSummaryWorkbookReporter(MetadataProperties metadataProperties, StoreReviewReport storeReviewReport, WorkbookUtil workbookUtil) {
        this.metadataProperties = metadataProperties;
        this.storeReviewReport = storeReviewReport;
        this.workbookUtil = workbookUtil;
        setupAssetTypes();
    }

    @Override
    public String getTemplatePath() {
        return "report/templates/store-review-report/Crystal-POST-PM-Template.xlsx";
    }

    @SneakyThrows
    @Override
    public byte[] getWorkbook() {
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ClassPathResource(getTemplatePath()).getInputStream())) {
            Sheet reportSheet = workbookUtil.addDataToCellsAndRows(workbook, storeReviewReport, metadataProperties, reviewManifests, assetDetailManifests, rackCellPositions, caseCellPositions);
            workbookUtil.addReviewDataToCell(reportSheet, POST_PM_STORE_HEALTH_SCORE, 6, 7, reviewManifests, storeReviewReport);
            workbookUtil.addReviewDataToCell(reportSheet, POST_PM_REVIEW_DATE, 6, 10, reviewManifests, storeReviewReport);
            return workbookUtil.setDefaultProperties(workbook, storeReviewReport);
        }
    }


    private void setupAssetTypes() {
        assetTypesMap = metadataProperties.getAssetTypes().entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
    }

    static {
        addReviewManifest(STORE_NUMBER, STRING, report -> report.getStoreDetails().getStoreNumber(), reviewManifests);
        addReviewManifest(REVIEW_END_DATE, DATE, report -> report.getStoreReviewDetails().getStoreReviewEndDate(), reviewManifests);
        addReviewManifest(PRE_REVIEW_STORE_HEALTH_SCORE, NUMBER, report -> Optional.ofNullable(report.getStoreDetails().getStoreHealthPreReview())
                .map(HealthReport::getScore)
                .orElse(null), reviewManifests);
        addReviewManifest(TOTAL_RACKS, NUMBER, report -> report.getAssets().stream()
                .filter(summary -> assetTypesMap.getOrDefault(RACK, Collections.emptyList()).contains(summary.getAssetType()))
                .count(), reviewManifests);
        addReviewManifest(TOTAL_CASES_REVIEWED, NUMBER, report -> report.getAssetReviewSummary().stream()
                .filter(summary -> assetTypesMap.getOrDefault(CASE, Collections.emptyList()).contains(summary.getAssetType()))
                .map(AssetReviewSummaryReport::getReviewedAssets)
                .reduce(Integer::sum)
                .orElse(0), reviewManifests);
        addReviewManifest(WORK_ORDER_COUNT, NUMBER, report -> report.getAssetReviewSummary().stream()
                .map(AssetReviewSummaryReport::getWorkOrders)
                .reduce(Long::sum)
                .orElse(0L), reviewManifests);
        addReviewManifest(SERVICE_MODEL, STRING, report -> report.getStoreReviewDetails().getServiceModel(), reviewManifests);
        addReviewManifest(REVIEW_TYPE, STRING, report -> report.getStoreReviewDetails().getReviewType(), reviewManifests);
        addReviewManifest(REVIEWER, STRING, report -> report.getStoreReviewDetails().getReviewerName(), reviewManifests);
        addReviewManifest(POST_PM_STORE_HEALTH_SCORE, STRING, report -> report.getStoreDetails().getStoreHealthPostPreventiveMaintenance().getScore(), reviewManifests);
        addReviewManifest(POST_PM_REVIEW_DATE, DATE, report -> report.getStoreReviewDetails().getStoreReviewPostPreventiveMaintenanceDate(), reviewManifests);

        addAssetDetailManifest(ASSET_NAME, STRING, report -> report.getAsset().getAssetName(), assetDetailManifests);
        addAssetDetailManifest(WORK_ORDER_NUMBER, STRING, AssetReviewDetailsReport::getWorkOrderId, assetDetailManifests);
        addAssetDetailManifest(REVIEW_END_ASSET_HEALTH_SCORE, NUMBER, report -> Optional.ofNullable(report.getAssetHealthEnd())
                .map(HealthReport::getScore)
                .orElse(null), assetDetailManifests);
        addAssetDetailManifest(POST_PM_ASSET_HEALTH_SCORE, NUMBER, report -> Optional.ofNullable(report.getAssetHealthPostPreventiveMaintenance())
                .map(HealthReport::getScore)
                .orElse(null), assetDetailManifests);
        addAssetDetailManifest(POST_PM_ASSET_HEALTH_SCORE_IMPROVEMENT, NUMBER, report -> Optional.ofNullable(report.getAssetHealthPostPreventiveMaintenanceImprovement())
                .map(HealthReport::getScore)
                .orElse(null), assetDetailManifests);
        addAssetDetailManifest(REFRIGERANT_TYPE, STRING, AssetReviewDetailsReport::getRefrigerantType, assetDetailManifests);

        addAssetDetailManifest(AVERAGE_CASE_TEMPERATURE, NUMBER, AssetReviewDetailsReport::getAverageTemperatureEnd, assetDetailManifests);
        addAssetDetailManifest(HAS_SETTING_CHANGE_LOGS, BOOLEAN, report -> TRUE.equals(report.getHasSettingChangeLogs()) ? true : null, assetDetailManifests);
        addAssetDetailManifest(HAS_TEMPERATURE_SETTING_CHANGE_LOGS, BOOLEAN, report -> TRUE.equals(report.getSettingChangeLogs().stream()
                .anyMatch(log -> TEMPERATURE_SETTING_CHANGES.contains(log.getSetting()))) ? true : null, assetDetailManifests);
        addAssetDetailManifest(HAS_DEFROST_SETTING_CHANGE_LOGS, BOOLEAN, report -> TRUE.equals(report.getSettingChangeLogs().stream()
                .anyMatch(log -> DEFROST_SETTING_CHANGES.contains(log.getSetting()))) ? true : null, assetDetailManifests);
        addAssetDetailManifest(HAS_CASE_CYCLING, BOOLEAN, report -> TRUE.equals(report.getInsights().stream()
                .anyMatch(insight -> CASE_CYCLING.equals(insight.getObservation()))) ? true : null, assetDetailManifests);

        addAssetDetailManifest(SATURATED_SUCTION_TEMPERATURE_NEW, STRING, findSettingChangeLog(SATURATED_SUCTION_TEMPERATURE, SettingChangeLogReport::getNewValue), assetDetailManifests);
        addAssetDetailManifest(SUCTION_PRESSURE_TARGET_OLD, STRING, findSettingChangeLog(SUCTION_PRESSURE_TARGET, SettingChangeLogReport::getOldValue), assetDetailManifests);
        addAssetDetailManifest(SUCTION_PRESSURE_TARGET_NEW, STRING, findSettingChangeLog(SUCTION_PRESSURE_TARGET, SettingChangeLogReport::getNewValue), assetDetailManifests);
        addAssetDetailManifest(SUCTION_PRESSURE_CUT_IN_NEW, STRING, findSettingChangeLog(SUCTION_PRESSURE_CUT_IN, SettingChangeLogReport::getNewValue), assetDetailManifests);
        addAssetDetailManifest(SUCTION_PRESSURE_CUT_OUT_NEW, STRING, findSettingChangeLog(SUCTION_PRESSURE_CUT_OUT, SettingChangeLogReport::getNewValue), assetDetailManifests);
        addAssetDetailManifest(SUCTION_FLOAT_NEW, STRING, findSettingChangeLog(SUCTION_FLOAT, SettingChangeLogReport::getNewValue), assetDetailManifests);
        addAssetDetailManifest(CASE_TEMPERATURE_TARGET_OLD, STRING, findSettingChangeLog(CASE_TEMPERATURE_TARGET, SettingChangeLogReport::getOldValue), assetDetailManifests);
        addAssetDetailManifest(CASE_TEMPERATURE_TARGET_NEW, STRING, findSettingChangeLog(CASE_TEMPERATURE_TARGET, SettingChangeLogReport::getNewValue), assetDetailManifests);
        addAssetDetailManifest(CASE_TEMPERATURE_CUT_IN_NEW, STRING, findSettingChangeLog(CASE_TEMPERATURE_CUT_IN, SettingChangeLogReport::getNewValue), assetDetailManifests);
        addAssetDetailManifest(CASE_TEMPERATURE_CUT_OUT_NEW, STRING, findSettingChangeLog(CASE_TEMPERATURE_CUT_OUT, SettingChangeLogReport::getNewValue), assetDetailManifests);

        addAssetDetailManifest(OBSERVATIONS, STRING, joinInsights(InsightReport::getObservationValue), assetDetailManifests);
        addAssetDetailManifest(OBSERVATION_NOTES, STRING, joinInsights(InsightReport::getObservationNotes), assetDetailManifests);
        addAssetDetailManifest(PROBABLE_CAUSES, STRING, joinInsightsList(InsightReport::getProbableCauseValues), assetDetailManifests);
        addAssetDetailManifest(PROBABLE_CAUSE_NOTES, STRING, joinInsights(InsightReport::getProbableCauseNotes), assetDetailManifests);
        addAssetDetailManifest(RECOMMENDATIONS, STRING, joinInsightsList(InsightReport::getRecommendationValues), assetDetailManifests);
        addAssetDetailManifest(RECOMMENDATION_NOTES, STRING, joinInsights(InsightReport::getRecommendationNotes), assetDetailManifests);

    }

    static {
        rackCellPositions.put(ASSET_NAME, 1);
        rackCellPositions.put(REVIEW_END_ASSET_HEALTH_SCORE, 2);
        rackCellPositions.put(REFRIGERANT_TYPE, 3);
        rackCellPositions.put(HAS_SETTING_CHANGE_LOGS, 4);
        rackCellPositions.put(SATURATED_SUCTION_TEMPERATURE_NEW, 5);
        rackCellPositions.put(SUCTION_PRESSURE_TARGET_OLD, 6);
        rackCellPositions.put(SUCTION_PRESSURE_TARGET_NEW, 7);
        rackCellPositions.put(SUCTION_PRESSURE_CUT_IN_NEW, 8);
        rackCellPositions.put(SUCTION_PRESSURE_CUT_OUT_NEW, 9);
        rackCellPositions.put(SUCTION_FLOAT_NEW, 10);
        rackCellPositions.put(WORK_ORDER_NUMBER, 11);
        rackCellPositions.put(OBSERVATIONS, 12);
        rackCellPositions.put(OBSERVATION_NOTES, 13);
        rackCellPositions.put(PROBABLE_CAUSES, 14);
        rackCellPositions.put(PROBABLE_CAUSE_NOTES, 15);
        rackCellPositions.put(RECOMMENDATIONS, 16);
        rackCellPositions.put(POST_PM_ASSET_HEALTH_SCORE, 22);
        rackCellPositions.put(POST_PM_ASSET_HEALTH_SCORE_IMPROVEMENT, 23);
    }

    static {
        caseCellPositions.put(ASSET_NAME, 1);
        caseCellPositions.put(REVIEW_END_ASSET_HEALTH_SCORE, 2);
        caseCellPositions.put(AVERAGE_CASE_TEMPERATURE, 3);
        caseCellPositions.put(HAS_TEMPERATURE_SETTING_CHANGE_LOGS, 4);
        caseCellPositions.put(HAS_DEFROST_SETTING_CHANGE_LOGS, 5);
        caseCellPositions.put(CASE_TEMPERATURE_TARGET_OLD, 6);
        caseCellPositions.put(CASE_TEMPERATURE_TARGET_NEW, 7);
        caseCellPositions.put(CASE_TEMPERATURE_CUT_IN_NEW, 8);
        caseCellPositions.put(CASE_TEMPERATURE_CUT_OUT_NEW, 9);
        caseCellPositions.put(HAS_CASE_CYCLING, 10);
        caseCellPositions.put(WORK_ORDER_NUMBER, 11);
        caseCellPositions.put(OBSERVATIONS, 12);
        caseCellPositions.put(OBSERVATION_NOTES, 13);
        caseCellPositions.put(PROBABLE_CAUSES, 14);
        caseCellPositions.put(PROBABLE_CAUSE_NOTES, 15);
        caseCellPositions.put(RECOMMENDATIONS, 16);
        caseCellPositions.put(RECOMMENDATION_NOTES, 17);
        caseCellPositions.put(POST_PM_ASSET_HEALTH_SCORE, 22);
        caseCellPositions.put(POST_PM_ASSET_HEALTH_SCORE_IMPROVEMENT, 23);
    }

}
