package com.walmart.realestate.crystal.storereview.service;

import com.walmart.core.realestate.cerberus.bean.UserContextInformation;
import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.settingchangelog.model.Insight;
import com.walmart.realestate.crystal.settingchangelog.model.SettingChangeLog;
import com.walmart.realestate.crystal.settingchangelog.service.InsightService;
import com.walmart.realestate.crystal.settingchangelog.service.SettingChangeLogService;
import com.walmart.realestate.crystal.storereview.client.asset.model.Asset;
import com.walmart.realestate.crystal.storereview.client.asset.model.AssetTypeAware;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationAssetTimeInTarget;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationSensor;
import com.walmart.realestate.crystal.storereview.client.storeinfo.model.facilitydetails.Location;
import com.walmart.realestate.crystal.storereview.model.StoreAssetReview;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.model.StoreReviewAssetHealthScore;
import com.walmart.realestate.crystal.storereview.model.report.*;
import com.walmart.realestate.crystal.storereview.properties.StoreReviewProperties;
import com.walmart.realestate.crystal.storereview.report.WorkbookReporter;
import com.walmart.realestate.crystal.storereview.report.WorkbookReporterFactory;
import com.walmart.realestate.crystal.storereview.report.settingscalculator.SettingsCalculator;
import com.walmart.realestate.crystal.storereview.report.settingscalculator.SettingsCalculatorContext;
import com.walmart.realestate.soteria.model.User;
import com.walmart.realestate.soteria.model.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreReviewReportService implements InitializingBean {

    private final StoreReviewService storeReviewService;

    private final StoreAssetReviewService storeAssetReviewService;

    private final StoreReviewAssetService storeReviewAssetService;

    private final StoreService storeService;

    private final AssetService assetService;

    private final StoreReviewHealthScoreService storeReviewHealthScoreService;

    private final SettingChangeLogService settingChangeLogService;

    private final InsightService insightService;

    private final UserAccountService userAccountService;

    private final Tracer tracer;

    private final StoreReviewProperties storeReviewProperties;

    private final WorkbookReporterFactory workbookReporterFactory;

    private final StoreAssetService storeAssetService;

    private final SettingsCalculator settingsCalculator;

    private Comparator<AssetTypeAware> assetTypeComparator;

    private Comparator<AssetReviewDetailsReport> assetReviewDetailsReportComparator;

    private final TaskExecutor noUserContextSecondaryTaskExecutor;

    @Setter
    private Supplier<Instant> timeSupplier;

    @Logger
    @Override
    public void afterPropertiesSet() {
        List<String> assetTypes = storeReviewProperties.getAssetTypes().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        assetTypeComparator = Comparator.comparing(asset -> assetTypes.indexOf(asset.getAssetType().toLowerCase()));
        assetReviewDetailsReportComparator = Comparator.comparing((AssetReviewDetailsReport asset) -> assetTypes.indexOf(asset.getAssetType().toLowerCase()))
                .thenComparing(Comparator.naturalOrder());
        timeSupplier = Instant::now;
    }

    @Logger
    public StoreReviewReport getStoreReviewReportData(String storeReviewId, UserContext userContext) {
        return getStoreReviewReportData(storeReviewId, userContext, true);
    }

    private StoreReviewReport getStoreReviewReportData(String storeReviewId, UserContext userContext, boolean includeAllAssets) {
        log.info("Get store review report data {} with all assets {}", storeReviewId, includeAllAssets);
        StoreReview storeReview = storeReviewService.getStoreReview(storeReviewId);

        Map<String, List<SettingChangeLog>> settingChangeLogsByAsset = getSettingChangeLogsByAsset(storeReviewId);
        Map<String, List<Insight>> insightsByAsset = getInsightsByAsset(storeReviewId);
        List<StoreAssetReview> storeAssetReviews = getAssetsReviews(storeReviewId);
        Map<Long, Asset> equipmentIdAssetMap = assetService.getAssetsForStore(storeReview.getStoreNumber())
                .stream().collect(Collectors.toMap(Asset::getDwEquipmentId, Function.identity(), (a, b) -> a));

        Map<String, StoreAssetReview> assetsWithWorkOrder = storeAssetReviews.stream()
                .filter(review -> Objects.nonNull(review.getWorkOrderId()))
                .collect(Collectors.toMap(StoreAssetReview::getAssetMappingId, Function.identity()));
        Set<String> markedAsReviewedAssets = storeAssetReviews.stream()
                .filter(review -> "completed".equalsIgnoreCase(review.getState()))
                .map(StoreAssetReview::getAssetMappingId)
                .collect(Collectors.toSet());

        Predicate<String> filter = assetId -> settingChangeLogsByAsset.containsKey(assetId)
                || insightsByAsset.containsKey(assetId)
                || markedAsReviewedAssets.contains(assetId);

        List<RefrigerationSensor> assets = getAllAssets(storeReview);
        List<String> reviewedAssetIds = assets.stream()
                .map(RefrigerationSensor::getId)
                .filter(filter)
                .collect(Collectors.toList());

        Location location = storeService.getStoreInfo(storeReview.getStoreNumber()).getFacilityDetails().get(0).getLocation();
        ZoneId storeTimeZone = ZoneId.of(location.getLocationTimeZone().getDstTimeZone().getTimeZoneId());
        Optional<String> username = Optional.ofNullable(userContext)
                .map(UserContextInformation::getUsername);

        Instant now = timeSupplier.get();


        storeReviewHealthScoreService.updateHealthScoresAtStatuses(storeReview.getId(), storeTimeZone, false);
        CompletableFuture<StoreReviewDetailsReport> storeReviewDetailsReportFuture =
                CompletableFuture.supplyAsync(() -> getStoreReviewDetailsReport(storeReview, storeTimeZone), noUserContextSecondaryTaskExecutor);

        CompletableFuture<StoreDetailsReport> storeDetailsReportFuture =
                CompletableFuture.supplyAsync(() -> getStoreDetailsReport(storeReview, location, storeTimeZone, now), noUserContextSecondaryTaskExecutor);

        CompletableFuture<List<AssetReviewSummaryReport>> assetReviewSummaryReportListFuture =
                CompletableFuture.supplyAsync(() -> getAssetTypeDetailsReport(assets, reviewedAssetIds, settingChangeLogsByAsset, insightsByAsset, assetsWithWorkOrder), noUserContextSecondaryTaskExecutor);

        CompletableFuture<List<AssetReviewDetailsReport>> assetReviewDetailsReportsFuture =
                CompletableFuture.supplyAsync(() -> getAssetReportItems(equipmentIdAssetMap, storeReview, assets, reviewedAssetIds, includeAllAssets, settingChangeLogsByAsset, insightsByAsset, assetsWithWorkOrder, storeTimeZone, now), noUserContextSecondaryTaskExecutor);

        return CompletableFuture.allOf(storeReviewDetailsReportFuture, storeDetailsReportFuture,
                assetReviewSummaryReportListFuture, assetReviewDetailsReportsFuture).thenApply(ignore -> {
            StoreReviewDetailsReport storeReviewDetailsReport = storeReviewDetailsReportFuture.join();
            StoreDetailsReport storeDetailsReport = storeDetailsReportFuture.join();
            List<AssetReviewSummaryReport> assetReviewSummaryReportList = assetReviewSummaryReportListFuture.join();
            List<AssetReviewDetailsReport> assetReviewDetailsReports = assetReviewDetailsReportsFuture.join();

            return StoreReviewReport.builder()
                    .storeReviewDetails(storeReviewDetailsReport)
                    .storeDetails(storeDetailsReport)
                    .assetReviewSummary(assetReviewSummaryReportList)
                    .assets(assetReviewDetailsReports)
                    .user(username.orElse(null))
                    .userName(username.map(this::getUserName).orElse(null))
                    .traceId(getTraceId())
                    .timestamp(now)
                    .currentYear(Year.now())
                    .build();
        }).join();
    }

    @Logger
    public StoreReviewReportSpreadsheetContainer getStoreReviewReportSpreadsheet(String storeReviewId) {
        log.info("Get store review report spreadsheet {}", storeReviewId);
        StoreReviewReport storeReviewReport = getStoreReviewReportData(storeReviewId, null);
        List<StoreReviewReportSpreadsheetAssetItem> items = getStoreReviewReportSpreadsheetAssetItems(storeReviewReport);
        String serviceModel = storeReviewReport.getStoreReviewDetails().getServiceModel();
        Long storeNumber = storeReviewReport.getStoreDetails().getStoreNumber();
        return StoreReviewReportSpreadsheetContainer.builder()
                .items(items)
                .reportName(serviceModel + " DOC Refrigeration Health Review - Store " + storeNumber)
                .build();
    }

    @Logger
    public StoreReviewReportWorkbookContainer getStoreReviewReportWorkbook(String storeReviewId, UserContext userContext) {
        log.info("Get store review report workbook {}", storeReviewId);
        StoreReviewReport storeReviewReport = getStoreReviewReportData(storeReviewId, userContext);
        Long storeNumber = storeReviewReport.getStoreDetails().getStoreNumber();

        WorkbookReporter workbookReporter = workbookReporterFactory.getWorkbookReporter(storeReviewReport);
        byte[] workbook = workbookReporter.getWorkbook();

        return StoreReviewReportWorkbookContainer.builder()
                .body(workbook)
                .reportName("Store " + storeNumber + workbookReporter.getReportName())
                .build();
    }

    private List<StoreReviewReportSpreadsheetAssetItem> getStoreReviewReportSpreadsheetAssetItems(StoreReviewReport storeReviewReport) {

        return storeReviewReport.getAssets().stream()
                .map(assetReviewDetailsReport -> {
                    StoreReviewDetailsReport storeReviewDetails = storeReviewReport.getStoreReviewDetails();
                    return StoreReviewReportSpreadsheetAssetItem.builder()
                            .today(storeReviewDetails.getStoreReviewStartDate())
                            .initials(storeReviewDetails.getReviewerName())
                            .storeNumber(storeReviewReport.getStoreDetails().getStoreNumber())
                            .caseName(assetReviewDetailsReport.getAsset().getAssetName())
                            .workOrder("0")
                            .reviewType("Health Review")
                            .serviceModel(storeReviewDetails.getServiceModel())
                            .remote(assetReviewDetailsReport.getHasSettingChangeLogs() ? "YES" : "N/A")
                            .isReviewed(assetReviewDetailsReport.getIsReviewed() ? "YES" : "NO")
                            .preReviewDate(getDateFromHealthReport(storeReviewReport.getStoreDetails().getStoreHealthPreReview()))
                            .preReviewScore(Optional.ofNullable(assetReviewDetailsReport.getAssetHealthPreReview()).map(HealthReport::getScore).orElse(null))
                            .postReviewDate(getDateFromHealthReport(storeReviewReport.getStoreDetails().getStoreHealthPostReview()))
                            .postReviewScore(Optional.ofNullable(assetReviewDetailsReport.getAssetHealthPostReview()).map(HealthReport::getScore).orElse(null))
                            .postMaintenanceDate(getDateFromHealthReport(storeReviewReport.getStoreDetails().getStoreHealthPostMaintenance()))
                            .postMaintenanceScore(Optional.ofNullable(assetReviewDetailsReport.getAssetHealthPostMaintenance()).map(HealthReport::getScore).orElse(null))
                            .today(getDateFromHealthReport(storeReviewReport.getStoreDetails().getStoreHealthReportDownload()))
                            .todayHealthScore(Optional.ofNullable(assetReviewDetailsReport.getAssetHealthReportDownload()).map(HealthReport::getScore).orElse(null))
                            .assetTag(assetReviewDetailsReport.getMdmAsset().getQrCode())
                            .tagId(assetReviewDetailsReport.getMdmAsset().getTagId())
                            .modelNo(assetReviewDetailsReport.getMdmAsset().getModelNumber())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private LocalDate getDateFromHealthReport(HealthReport healthReport) {
        if (healthReport == null) {
            return null;
        }
        return Optional.ofNullable(healthReport.getTimestamp()).map(LocalDateTime::toLocalDate).orElse(null);
    }

    private Map<String, List<SettingChangeLog>> getSettingChangeLogsByAsset(String storeReviewId) {
        List<SettingChangeLog> settingChangeLogs = settingChangeLogService.getSettingsLogByReferenceId(storeReviewId);
        return settingChangeLogs.stream()
                .collect(Collectors.groupingBy(SettingChangeLog::
                        getAssetMappingId));
    }

    private Map<String, List<Insight>> getInsightsByAsset(String storeReviewId) {
        List<Insight> insights = insightService.getInsightsByReferenceId(storeReviewId);
        return insights.stream()
                .collect(Collectors.groupingBy(Insight::getAssetMappingId));
    }

    private List<StoreAssetReview> getAssetsReviews(String storeReviewId) {
        return storeAssetReviewService.getStoreAssetReviews(storeReviewId);

    }

    private List<RefrigerationSensor> getAllAssets(StoreReview storeReview) {
        return storeReviewAssetService.getAssetsForStore(storeReview.getStoreNumber());
    }

    private StoreReviewDetailsReport getStoreReviewDetailsReport(StoreReview storeReview, ZoneId storeTimeZone) {

        return StoreReviewDetailsReport.builder()
                .storeReviewId(storeReview.getId())
                .reviewType(storeReview.getReviewType())
                .reviewer(storeReview.getAssignee())
                .reviewerName(storeReview.getAssigneeName())
                .storeReviewStartDate(storeReview.getStartedAt().atZone(storeTimeZone).toLocalDate())
                .storeReviewEndDate(storeReview.getMonitoringStartedAt().atZone(storeTimeZone).toLocalDate())
                .storeReviewPostPreventiveMaintenanceDate(storeReview.getPostPreventiveMaintenanceStartedAt() != null ? storeReview.getPostPreventiveMaintenanceStartedAt().atZone(storeTimeZone).toLocalDate() : null)
                .serviceModel(storeReview.getSdm())
                .build();
    }

    private StoreDetailsReport getStoreDetailsReport(StoreReview storeReview, Location storeLocation, ZoneId storeTimeZone, Instant now) {
        StoreDetailsReport.StoreDetailsReportBuilder builder = StoreDetailsReport.builder()
                .storeNumber(storeReview.getStoreNumber())
                .storeAddress(storeLocation.getLocationAddress().get(0))
                .storeTimeZone(storeLocation.getLocationTimeZone());
        storeReviewHealthScoreService.getStoreReviewStoreHealthScore(storeReview.getId())
                .ifPresent(storeHealthScore -> {
                    buildHealthScore(builder::storeHealthPreReview, storeHealthScore.getPreReviewScore(), storeHealthScore.getPreReviewScoreTimestamp(), storeTimeZone);
                    buildHealthScore(builder::storeHealthStart, storeHealthScore.getHealthScoreStart(), storeHealthScore.getTimestampStart(), storeTimeZone);
                    buildHealthScore(builder::storeHealthEnd, storeHealthScore.getHealthScoreEnd(), storeHealthScore.getTimestampEnd(), storeTimeZone);
                    buildHealthScore(builder::storeHealthPostReview, storeHealthScore.getPostReviewScore(), storeHealthScore.getPostReviewScoreTimestamp(), storeTimeZone);
                    buildHealthScore(builder::storeHealthPostMaintenance, storeHealthScore.getPostMaintenanceScore(), storeHealthScore.getPostMaintenanceScoreTimestamp(), storeTimeZone);
                    buildHealthScore(builder::storeHealthPostPreventiveMaintenance, storeHealthScore.getPostPreventiveMaintenanceScore(), storeHealthScore.getPostPreventiveMaintenanceScoreTimestamp(), storeTimeZone);
                    Optional.ofNullable(storeAssetService.getStoreHealthScore(storeReview.getStoreNumber(), now))
                            .ifPresent(refrigerationStoreTimeInTarget -> buildHealthScore(builder::storeHealthReportDownload, refrigerationStoreTimeInTarget.getTimeInTarget(), refrigerationStoreTimeInTarget.getRunTime(), storeTimeZone));
                });

        return builder.build();
    }

    private void buildHealthScore(Consumer<HealthReport> builder, Double score, Instant timestamp, ZoneId storeTimeZone) {
        if (Objects.nonNull(score) && Objects.nonNull(timestamp)) {
            builder.accept(HealthReport.builder()
                    .score(score)
                    .timestamp(timestamp.atZone(storeTimeZone).toLocalDateTime())
                    .build());
        }
    }

    private List<AssetReviewSummaryReport> getAssetTypeDetailsReport(List<RefrigerationSensor> assets,
                                                                     List<String> filteredAssetIds,
                                                                     Map<String, List<SettingChangeLog>> settingChangeLogsByAsset,
                                                                     Map<String, List<Insight>> insightsByAsset,
                                                                     Map<String, StoreAssetReview> assetsWithWorkOrder) {
        Map<String, List<RefrigerationSensor>> assetsByType = assets.stream()
                .filter(asset -> filteredAssetIds.contains(asset.getId()))
                .collect(Collectors.groupingBy(RefrigerationSensor::getType));

        Map<String, Long> settingChangeLogsByAssetType = getAssetTypeCount(assetsByType, settingChangeLogsByAsset);
        Map<String, Long> insightsByAssetType = getAssetTypeCount(assetsByType, insightsByAsset);
        Map<String, Long> workOrdersByAssetType = getAssetTypeCount(assetsByType, assetsWithWorkOrder);

        return assetsByType.entrySet().stream()
                .map(entry -> AssetReviewSummaryReport.builder()
                        .assetType(entry.getKey())
                        .reviewedAssets(entry.getValue().size())
                        .settingChangeLogs(settingChangeLogsByAssetType.get(entry.getKey()))
                        .insights(insightsByAssetType.get(entry.getKey()))
                        .workOrders(workOrdersByAssetType.get(entry.getKey()))
                        .build())
                .sorted(assetTypeComparator)
                .collect(Collectors.toList());
    }

    private List<AssetReviewDetailsReport> getAssetReportItems(Map<Long, Asset> equipmentIdAssetMap,
                                                               StoreReview storeReview,
                                                               List<RefrigerationSensor> assets,
                                                               List<String> reviewedAssetIds,
                                                               boolean includeAllAssets,
                                                               Map<String, List<SettingChangeLog>> settingChangeLogsByAsset,
                                                               Map<String, List<Insight>> insightsByAsset,
                                                               Map<String, StoreAssetReview> assetsWithWorkOrder,
                                                               ZoneId storeTimeZone,
                                                               Instant now) {
        Map<String, StoreReviewAssetHealthScore> assetHealthScoreMap = storeReviewHealthScoreService.getStoreReviewAssetHealthScores(storeReview.getId()).stream()
                .collect(Collectors.toMap(StoreReviewAssetHealthScore::getAssetMappingId, Function.identity()));

        Map<String, RefrigerationAssetTimeInTarget> assetHealthScoresDownloadTimestampMap = storeAssetService
                .getAssetHealthScore(storeReview.getStoreNumber(), now).stream()
                .collect(Collectors.toMap(RefrigerationAssetTimeInTarget::getAssetMappingId, Function.identity()));

        List<AssetReviewDetailsReport> assetReviewDetailsReports = assets.stream()
                .filter(asset -> includeAllAssets || reviewedAssetIds.contains(asset.getId()))
                .map(getAssetReport(equipmentIdAssetMap, storeReview.getRefrigerantType(), settingChangeLogsByAsset, insightsByAsset, assetsWithWorkOrder, assetHealthScoreMap, storeTimeZone, assetHealthScoresDownloadTimestampMap))
                .collect(Collectors.toList());

        try {
            // Natural comparator of asset occasionally gives exception when tagId has bad data quality
            assetReviewDetailsReports.sort(assetReviewDetailsReportComparator);
        } catch (RuntimeException ignored) {
        }

        AtomicInteger integer = new AtomicInteger();
        assetReviewDetailsReports.forEach(assetReviewDetailsReport -> {
            assetReviewDetailsReport.setIndex(integer.incrementAndGet());
            assetReviewDetailsReport.setIsReviewed(reviewedAssetIds.contains(assetReviewDetailsReport.getAsset().getId()));
        });

        return assetReviewDetailsReports;
    }

    private Function<RefrigerationSensor, AssetReviewDetailsReport> getAssetReport(Map<Long, Asset> equipmentIdAssetMap,
                                                                                   String refrigerantType,
                                                                                   Map<String, List<SettingChangeLog>> settingChangeLogsByAsset,
                                                                                   Map<String, List<Insight>> insightsByAsset,
                                                                                   Map<String, StoreAssetReview> assetsWithWorkOrder,
                                                                                   Map<String, StoreReviewAssetHealthScore> assetHealthScoreMap,
                                                                                   ZoneId storeTimeZone,
                                                                                   Map<String, RefrigerationAssetTimeInTarget> assetHealthScoresDownloadTimestampMap) {
        return asset -> {
            List<SettingChangeLogReport> settingChangeLogsReport = getSettingChangeLogsReport(settingChangeLogsByAsset, asset, storeTimeZone);
            List<InsightReport> insightsReport = getInsightsReport(insightsByAsset, asset, storeTimeZone);

            AssetReviewDetailsReport.AssetReviewDetailsReportBuilder builder = AssetReviewDetailsReport.builder()
                    .asset(asset)
                    .mdmAsset(equipmentIdAssetMap.getOrDefault(asset.getEquipmentId(), Asset.builder().build()))
                    .workOrderId(Optional.ofNullable(assetsWithWorkOrder.get(asset.getId())).map(StoreAssetReview::getWorkOrderId).orElse(null))
                    .hasSettingChangeLogs(!settingChangeLogsReport.isEmpty())
                    .settingChangeLogs(settingChangeLogsReport)
                    .hasInsights(!insightsReport.isEmpty())
                    .refrigerantType(refrigerantType)
                    .insights(insightsReport);

            StoreReviewAssetHealthScore assetHealthScore = assetHealthScoreMap.get(asset.getId());
            if (Objects.nonNull(assetHealthScore)) {
                buildHealthScore(builder::assetHealthPreReview, assetHealthScore.getPreReviewScore(), assetHealthScore.getPreReviewScoreTimestamp(), storeTimeZone);
                buildHealthScore(builder::assetHealthStart, assetHealthScore.getHealthScoreStart(), assetHealthScore.getTimestampStart(), storeTimeZone);
                buildHealthScore(builder::assetHealthEnd, assetHealthScore.getHealthScoreEnd(), assetHealthScore.getTimestampEnd(), storeTimeZone);

                builder.targetTemperatureEnd(assetHealthScore.getTargetTemperatureEnd());
                builder.lowCutInTemperatureEnd(assetHealthScore.getLowCutInTemperatureEnd());
                builder.lowCutOutTemperatureEnd(assetHealthScore.getLowCutOutTemperatureEnd());
                builder.averageTemperatureEnd(assetHealthScore.getAverageTemperatureEnd());

                buildHealthScore(builder::assetHealthPostReview, assetHealthScore.getPostReviewScore(), assetHealthScore.getPostReviewScoreTimestamp(), storeTimeZone);
                buildHealthScore(builder::assetHealthPostMaintenance, assetHealthScore.getPostMaintenanceScore(), assetHealthScore.getPostMaintenanceScoreTimestamp(), storeTimeZone);
                buildHealthScore(builder::assetHealthPostPreventiveMaintenance, assetHealthScore.getPostPreventiveMaintenanceScore(), assetHealthScore.getPostPreventiveMaintenanceScoreTimestamp(), storeTimeZone);
                buildHealthScore(builder::assetHealthPostPreventiveMaintenanceImprovement, getScoreImprovement(assetHealthScore), assetHealthScore.getPostPreventiveMaintenanceScoreTimestamp(), storeTimeZone);

                Optional.ofNullable(assetHealthScoresDownloadTimestampMap.get(assetHealthScore.getAssetMappingId()))
                        .ifPresent(refrigerationAssetTimeInTarget -> buildHealthScore(builder::assetHealthReportDownload, refrigerationAssetTimeInTarget.getTimeInTarget(), refrigerationAssetTimeInTarget.getRunTime(), storeTimeZone));
            }

            StoreReviewAssetHealthScore healthScore = Optional.ofNullable(assetHealthScore)
                    .filter(score -> Objects.nonNull(score.getTimestampEnd()))
                    .orElseGet(() -> StoreReviewAssetHealthScore.builder()
                            .timestampEnd(settingChangeLogsReport.stream()
                                    .map(SettingChangeLogReport::getCreatedAt)
                                    .max(Comparator.naturalOrder())
                                    .map(localDateTime -> localDateTime.atZone(storeTimeZone).toInstant())
                                    .orElseGet(timeSupplier))
                            .build());
            SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, storeTimeZone);
            List<SettingChangeLogReport> calculated = settingsCalculator.calculate(settingChangeLogsReport, context);
            builder.settingChangeLogsCalculated(calculated);

            return builder.build();
        };
    }

    private Double getScoreImprovement(StoreReviewAssetHealthScore assetHealthScore) {
        return assetHealthScore.getPostPreventiveMaintenanceScore() != null && assetHealthScore.getHealthScoreEnd() != null ? assetHealthScore.getPostPreventiveMaintenanceScore() - assetHealthScore.getHealthScoreEnd() : null;
    }

    private List<SettingChangeLogReport> getSettingChangeLogsReport(Map<String, List<SettingChangeLog>> settingChangeLogsByAsset, RefrigerationSensor asset, ZoneId storeTimeZone) {
        return Optional.ofNullable(settingChangeLogsByAsset.get(asset.getId()))
                .orElseGet(Collections::emptyList).stream()
                .map(settingChangeLog -> SettingChangeLogReport.builder()
                        .id(settingChangeLog.getId())
                        .referenceId(settingChangeLog.getReferenceId())
                        .assetMappingId(settingChangeLog.getAssetMappingId())
                        .storeNumber(settingChangeLog.getStoreNumber())
                        .setting(settingChangeLog.getSetting())
                        .settingValue(settingChangeLog.getSettingValue())
                        .oldValue(settingChangeLog.getOldValue())
                        .newValue(settingChangeLog.getNewValue())
                        .unit(settingChangeLog.getUnit())
                        .notes(settingChangeLog.getNotes())
                        .reason(settingChangeLog.getReason())
                        .source(settingChangeLog.getSource())
                        .createdAt(settingChangeLog.getCreatedAt().atZone(storeTimeZone).toLocalDateTime())
                        .build())
                .sorted(Comparator.comparing(SettingChangeLogReport::getCreatedAt))
                .collect(Collectors.toList());
    }

    private List<InsightReport> getInsightsReport(Map<String, List<Insight>> insightsByAsset, RefrigerationSensor asset, ZoneId storeTimeZone) {
        return Optional.ofNullable(insightsByAsset.get(asset.getId()))
                .orElseGet(Collections::emptyList).stream()
                .map(insight -> InsightReport.builder()
                        .id(insight.getId())
                        .referenceId(insight.getReferenceId())
                        .assetMappingId(insight.getAssetMappingId())
                        .storeNumber(insight.getStoreNumber())
                        .recommendations(insight.getRecommendations())
                        .recommendationValues(insight.getRecommendationValues())
                        .recommendationNotes(insight.getRecommendationNotes())
                        .probableCauses(insight.getProbableCauses())
                        .probableCauseValues(insight.getProbableCauseValues())
                        .probableCauseNotes(insight.getProbableCauseNotes())
                        .observation(insight.getObservation())
                        .observationValue(insight.getObservationValue())
                        .observationNotes(insight.getObservationNotes())
                        .createdAt(insight.getCreatedAt().atZone(storeTimeZone).toLocalDateTime())
                        .build())
                .sorted(Comparator.comparing(InsightReport::getCreatedAt))
                .collect(Collectors.toList());
    }

    private Map<String, Long> getAssetTypeCount(Map<String, List<RefrigerationSensor>> assetsByType, Map<String, ?> assetIdsMap) {
        return assetsByType.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(RefrigerationSensor::getId)
                                .filter(assetIdsMap::containsKey)
                                .count()));
    }

    private String getUserName(String userId) {
        return Optional.ofNullable(userAccountService.findUser(userId))
                .map(User::getFullName)
                .orElse(null);
    }

    private String getTraceId() {
        return Optional.ofNullable(tracer.currentSpan())
                .orElse(tracer.nextSpan())
                .context()
                .traceId();
    }

    @Logger
    public static Optional<SettingChangeLogReport> findLatestSettingChangeLog(Stream<SettingChangeLogReport> stream, String setting) {
        return stream.filter(log -> setting.equals(log.getSetting()))
                .max(Comparator.comparing(SettingChangeLogReport::getCreatedAt));
    }

    public interface Constants {
        String TELEMETRY = "telemetry";

        String SATURATED_SUCTION_TEMPERATURE = "refrigeration-controller-settings:saturated-suction-temperature";
        String SUCTION_PRESSURE_TARGET = "refrigeration-controller-settings:suction-pressure-target";
        String SUCTION_PRESSURE_CUT_IN = "refrigeration-controller-settings:suction-pressure-cut-in";
        String SUCTION_PRESSURE_CUT_OUT = "refrigeration-controller-settings:suction-pressure-cut-out";
        String SUCTION_FLOAT = "refrigeration-controller-settings:suction-float";

        String CASE_TEMPERATURE_TARGET = "refrigeration-controller-settings:case-temperature-target";
        String CASE_TEMPERATURE_CUT_IN = "refrigeration-controller-settings:case-temperature-cut-in";
        String CASE_TEMPERATURE_CUT_OUT = "refrigeration-controller-settings:case-temperature-cut-out";

        String CASE_CYCLING = "observations:cases-cycling-circuit";

        List<String> DEFROST_SETTING_CHANGES = Arrays.asList(
                "refrigeration-controller-settings:case-defrost-amount",
                "refrigeration-controller-settings:case-defrost-length",
                "refrigeration-controller-settings:case-defrost-termination-temp",
                "refrigeration-controller-settings:case-defrost-type"
        );
        List<String> TEMPERATURE_SETTING_CHANGES = Arrays.asList(
                CASE_TEMPERATURE_TARGET,
                CASE_TEMPERATURE_CUT_IN,
                CASE_TEMPERATURE_CUT_OUT
        );
    }

}
