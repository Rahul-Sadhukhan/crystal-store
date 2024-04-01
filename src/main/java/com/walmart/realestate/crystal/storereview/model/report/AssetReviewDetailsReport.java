package com.walmart.realestate.crystal.storereview.model.report;

import com.walmart.realestate.crystal.storereview.client.asset.model.Asset;
import com.walmart.realestate.crystal.storereview.client.asset.model.AssetTypeAware;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationSensor;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetReviewDetailsReport implements AssetTypeAware, Comparable<AssetReviewDetailsReport>, Serializable {

    @Setter
    private Integer index;

    private RefrigerationSensor asset;

    private Asset mdmAsset;

    @Setter
    private Boolean isReviewed;

    private String workOrderId;

    private HealthReport assetHealthPreReview;

    private HealthReport assetHealthStart;

    private HealthReport assetHealthEnd;

    private Double targetTemperatureEnd;

    private Double lowCutInTemperatureEnd;

    private Double lowCutOutTemperatureEnd;

    private Double averageTemperatureEnd;

    private HealthReport assetHealthPostReview;

    private HealthReport assetHealthPostMaintenance;

    private HealthReport assetHealthPostPreventiveMaintenance;

    private HealthReport assetHealthPostPreventiveMaintenanceImprovement;

    private HealthReport assetHealthReportDownload;

    private Boolean hasSettingChangeLogs;

    private List<SettingChangeLogReport> settingChangeLogs;

    private List<SettingChangeLogReport> settingChangeLogsCalculated;

    private Boolean hasInsights;

    private List<InsightReport> insights;

    private String refrigerantType;

    @Override
    public String getAssetType() {
        return Optional.ofNullable(asset)
                .map(RefrigerationSensor::getType)
                .orElse(null);
    }

    @Override
    public int compareTo(AssetReviewDetailsReport that) {
        if (Objects.isNull(that)) return 0;
        if (Objects.isNull(this.asset) || Objects.isNull(that.asset) || Objects.isNull(this.asset.getAssetName()) || Objects.isNull(that.asset.getAssetName()))
            return 0;

        String thisAssetName = this.asset.getAssetName();
        String thatAssetName = that.asset.getAssetName();
        return thisAssetName.compareTo(thatAssetName);
    }

}
