package com.walmart.realestate.crystal.storereview.report.settingscalculator;

import com.walmart.realestate.crystal.storereview.model.StoreReviewAssetHealthScore;
import com.walmart.realestate.crystal.storereview.model.report.SettingChangeLogReport;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter(value = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
public class SettingsCalculatorContext {

    private final StoreReviewAssetHealthScore assetHealthScore;

    private final LocalDateTime timestamp;

    private SettingChangeLogReport targetTemperature;

    private SettingChangeLogReport cutInTemperature;

    private SettingChangeLogReport cutOutTemperature;

    public SettingsCalculatorContext(StoreReviewAssetHealthScore assetHealthScore, ZoneId storeTimeZone) {
        this.assetHealthScore = assetHealthScore;
        this.timestamp = assetHealthScore.getTimestampEnd()
                .atZone(storeTimeZone)
                .toLocalDateTime();
    }

}
