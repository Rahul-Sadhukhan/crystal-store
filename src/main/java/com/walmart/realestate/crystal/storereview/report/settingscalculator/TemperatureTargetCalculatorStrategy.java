package com.walmart.realestate.crystal.storereview.report.settingscalculator;

import com.walmart.realestate.crystal.storereview.model.report.SettingChangeLogReport;

import java.util.Collections;
import java.util.List;

class TemperatureTargetCalculatorStrategy extends AbstractSettingsCalculatorStrategy {

    public TemperatureTargetCalculatorStrategy(SettingsCalculatorContext settingsCalculatorContext) {
        super(settingsCalculatorContext);
    }

    @Override
    protected List<SettingChangeLogReport> calculateSettingsInternal() {
        SettingChangeLogReport cutInTemperature = context.getCutInTemperature();

        double cutInTemperatureNew = Double.parseDouble(cutInTemperature.getNewValue()) - 2;
        double cutInTemperatureOld = Double.parseDouble(cutInTemperature.getOldValue()) - 2;

        return Collections.singletonList(buildTargetTemperature(cutInTemperatureNew, cutInTemperatureOld));
    }

}
