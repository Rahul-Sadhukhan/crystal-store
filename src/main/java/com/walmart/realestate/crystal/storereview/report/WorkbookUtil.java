package com.walmart.realestate.crystal.storereview.report;

import com.walmart.realestate.crystal.metadata.properties.MetadataProperties;
import com.walmart.realestate.crystal.storereview.model.report.AssetReviewDetailsReport;
import com.walmart.realestate.crystal.storereview.model.report.InsightReport;
import com.walmart.realestate.crystal.storereview.model.report.SettingChangeLogReport;
import com.walmart.realestate.crystal.storereview.model.report.StoreReviewReport;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.walmart.realestate.crystal.storereview.report.WorkbookUtil.Constants.*;
import static com.walmart.realestate.crystal.storereview.service.StoreReviewReportService.findLatestSettingChangeLog;

@Component
public class WorkbookUtil {

    public static Function<AssetReviewDetailsReport, Object> joinInsights(Function<InsightReport, String> mapper) {
        return report -> report.getInsights().stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(DELIMITER));
    }

    public static Function<AssetReviewDetailsReport, Object> joinInsightsList(Function<InsightReport, List<String>> mapper) {
        return report -> report.getInsights().stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.joining(DELIMITER));
    }

    public static Function<AssetReviewDetailsReport, Object> findSettingChangeLog(String setting, Function<SettingChangeLogReport, String> mapper) {
        return report -> findLatestSettingChangeLog(Stream.concat(
                report.getSettingChangeLogs().stream(),
                report.getSettingChangeLogsCalculated().stream()), setting)
                .map(mapper)
                .orElse(null);
    }

    public static void addAssetDetailManifest(String field, WorkbookManifest.Type dataType, Function<AssetReviewDetailsReport, Object> extractor, Map<String, WorkbookManifest<AssetReviewDetailsReport>> assetDetailManifests) {
        assetDetailManifests.put(field, WorkbookManifest.<AssetReviewDetailsReport>builder()
                .field(field)
                .dataType(dataType)
                .extractor(extractor)
                .build());
    }

    public static void addReviewManifest(String field, WorkbookManifest.Type dataType, Function<StoreReviewReport, Object> extractor, Map<String, WorkbookManifest<StoreReviewReport>> reviewManifests) {
        reviewManifests.put(field, WorkbookManifest.<StoreReviewReport>builder()
                .field(field)
                .dataType(dataType)
                .extractor(extractor)
                .build());
    }

    public Sheet addDataToCellsAndRows(XSSFWorkbook workbook,
                                       StoreReviewReport storeReviewReport,
                                       MetadataProperties metadataProperties,
                                       Map<String, WorkbookManifest<StoreReviewReport>> reviewManifests,
                                       Map<String, WorkbookManifest<AssetReviewDetailsReport>> assetDetailManifests,
                                       Map<String, Integer> rackCellPositions,
                                       Map<String, Integer> caseCellPositions) {
        Sheet reportSheet = workbook.getSheetAt(0);

        Map<String, List<AssetReviewDetailsReport>> assetsByType = storeReviewReport.getAssets().stream()
                .collect(Collectors.groupingBy(report -> metadataProperties.getAssetTypes().getOrDefault(report.getAssetType(), "case")));

        List<AssetReviewDetailsReport> racks = assetsByType.getOrDefault(RACK, Collections.emptyList());
        List<AssetReviewDetailsReport> cases = assetsByType.getOrDefault(CASE, Collections.emptyList());

        addReviewDataToCell(reportSheet, STORE_NUMBER, 3, 4, reviewManifests, storeReviewReport);
        addReviewDataToCell(reportSheet, REVIEW_END_DATE, 3, 7, reviewManifests, storeReviewReport);
        addReviewDataToCell(reportSheet, TOTAL_RACKS, 4, 4, reviewManifests, storeReviewReport);
        addReviewDataToCell(reportSheet, TOTAL_CASES_REVIEWED, 5, 4, reviewManifests, storeReviewReport);
        addReviewDataToCell(reportSheet, PRE_REVIEW_STORE_HEALTH_SCORE, 4, 7, reviewManifests, storeReviewReport);
        addReviewDataToCell(reportSheet, SERVICE_MODEL, 3, 10, reviewManifests, storeReviewReport);
        addReviewDataToCell(reportSheet, REVIEW_TYPE, 4, 10, reviewManifests, storeReviewReport);
        addReviewDataToCell(reportSheet, REVIEWER, 5, 10, reviewManifests, storeReviewReport);

        addAssetDataToRows(reportSheet, racks, RACK_ROW_MAX, RACK_ROW_BEGIN, rackCellPositions, assetDetailManifests);
        addAssetDataToRows(reportSheet, cases, CASE_ROW_MAX, CASE_ROW_BEGIN, caseCellPositions, assetDetailManifests);
        return reportSheet;
    }

    public void applyValueToCell(Cell cell, Object value, WorkbookManifest.Type type) {
        if (Objects.nonNull(value) && Objects.nonNull(type)) {
            switch (type) {
                case BOOLEAN:
                    boolean booleanValue;
                    if (value instanceof Boolean) booleanValue = (Boolean) value;
                    else booleanValue = Boolean.parseBoolean(value.toString());
                    cell.setCellValue(booleanValue);
                    break;
                case STRING:
                    String stringValue;
                    if (value instanceof String) stringValue = (String) value;
                    else stringValue = value.toString();
                    cell.setCellValue(stringValue);
                    break;
                case NUMBER:
                    double doubleValue;
                    if (value instanceof Number) doubleValue = ((Number) value).doubleValue();
                    else doubleValue = Double.parseDouble(value.toString());
                    cell.setCellValue(doubleValue);
                    break;
                case DATE:
                    if (value instanceof LocalDateTime) {
                        cell.setCellValue((LocalDateTime) value);
                    } else if (value instanceof LocalDate) {
                        cell.setCellValue(((LocalDate) value).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                    } else {
                        cell.setCellValue(value.toString());
                    }
                    break;
            }
        }
    }

    public byte[] setDefaultProperties(XSSFWorkbook workbook, StoreReviewReport storeReviewReport) throws IOException {
        String generatedBy = StringUtils.hasText(storeReviewReport.getUserName())
                ? storeReviewReport.getUserName() + " (" + storeReviewReport.getUser() + ")"
                : storeReviewReport.getUser();
        workbook.getProperties().getCustomProperties().addProperty("Source", "Crystal v0.1");
        workbook.getProperties().getCustomProperties().addProperty("Generated by", generatedBy);
        workbook.getProperties().getCustomProperties().addProperty("Timestamp", storeReviewReport.getTimestamp().toString());
        workbook.getProperties().getCustomProperties().addProperty("Unique ID", storeReviewReport.getTraceId());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public void addAssetDataToRows(Sheet reportSheet, List<AssetReviewDetailsReport> assets, int max, int offset, Map<String, Integer> cellPositions, Map<String, WorkbookManifest<AssetReviewDetailsReport>> assetDetailManifests) {
        for (int i = 0; i < Math.min(assets.size(), max); i++) {
            AssetReviewDetailsReport report = assets.get(i);
            Row row = reportSheet.getRow(i + offset - 1);
            for (Map.Entry<String, Integer> cellPosition : cellPositions.entrySet()) {
                Cell cell = row.getCell(cellPosition.getValue() - 1);

                WorkbookManifest<AssetReviewDetailsReport> manifest = assetDetailManifests.get(cellPosition.getKey());
                Object value = manifest.getExtractor().apply(report);

                applyValueToCell(cell, value, manifest.getDataType());
            }
        }
    }

    public void addReviewDataToCell(Sheet reportSheet, String manifestField, int rowNumber, int columnNumber, Map<String, WorkbookManifest<StoreReviewReport>> reviewManifests, StoreReviewReport storeReviewReport) {
        Row row = reportSheet.getRow(rowNumber - 1);
        Cell cell = row.getCell(columnNumber - 1);

        WorkbookManifest<StoreReviewReport> manifest = reviewManifests.get(manifestField);

        Object value = manifest.getExtractor().apply(storeReviewReport);
        applyValueToCell(cell, value, manifest.getDataType());
    }

    interface Constants {
        String ASSET_NAME = "assetName";
        String WORK_ORDER_NUMBER = "workOrder";
        String HAS_SETTING_CHANGE_LOGS = "hasSettingChangeLogs";
        String SATURATED_SUCTION_TEMPERATURE_NEW = "saturatedSuctionTemperatureNew";
        String SUCTION_PRESSURE_TARGET_OLD = "suctionPressureTargetOld";
        String SUCTION_PRESSURE_TARGET_NEW = "suctionPressureTargetNew";
        String SUCTION_PRESSURE_CUT_IN_NEW = "suctionPressureCutInNew";
        String SUCTION_PRESSURE_CUT_OUT_NEW = "suctionPressureCutOutNew";
        String SUCTION_FLOAT_NEW = "suctionFloatNew";
        String AVERAGE_CASE_TEMPERATURE = "averageCaseTemperature";
        String HAS_TEMPERATURE_SETTING_CHANGE_LOGS = "hasTemperatureSettingChangeLogs";
        String HAS_DEFROST_SETTING_CHANGE_LOGS = "hasDefrostSettingChangeLogs";
        String HAS_CASE_CYCLING = "hasCaseCycling";
        String CASE_TEMPERATURE_TARGET_OLD = "caseTemperatureTargetOld";
        String CASE_TEMPERATURE_TARGET_NEW = "caseTemperatureTargetNew";
        String CASE_TEMPERATURE_CUT_IN_NEW = "caseTemperatureCutInNew";
        String CASE_TEMPERATURE_CUT_OUT_NEW = "caseTemperatureCutOutNew";
        String OBSERVATIONS = "observations";
        String OBSERVATION_NOTES = "observationNotes";
        String PROBABLE_CAUSES = "probableCauses";
        String PROBABLE_CAUSE_NOTES = "probableCauseNotes";
        String RECOMMENDATIONS = "recommendations";
        String RECOMMENDATION_NOTES = "recommendationNotes";
        String STORE_NUMBER = "storeNumber";
        String REVIEW_END_DATE = "reviewEndDate";
        String PRE_REVIEW_STORE_HEALTH_SCORE = "preReviewStoreHealthScore";
        String REVIEW_END_ASSET_HEALTH_SCORE = "reviewEndAssetStoreHealthScore";
        String REFRIGERANT_TYPE = "refrigerantType";
        String TOTAL_RACKS = "totalRacks";
        String TOTAL_CASES_REVIEWED = "totalCasesReviewed";
        String WORK_ORDER_COUNT = "workOrderCount";
        String SERVICE_MODEL = "serviceModel";
        String REVIEW_TYPE = "reviewType";
        String REVIEWER = "reviewer";
        String POST_PM_STORE_HEALTH_SCORE = "postPMStoreHealthScore";
        String POST_PM_REVIEW_DATE = "postPMReviewDate";
        String RACK = "rack";
        String CASE = "case";

        String POST_PM_ASSET_HEALTH_SCORE = "postPMAssetHealthScore";

        String POST_PM_ASSET_HEALTH_SCORE_IMPROVEMENT = "postPMAssetHealthScoreImprovement";
        String DELIMITER = ", ";

        int RACK_ROW_BEGIN = 9;
        int RACK_ROW_MAX = 10;
        int CASE_ROW_BEGIN = 21;
        int CASE_ROW_MAX = Integer.MAX_VALUE;
    }
}
