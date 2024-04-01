package com.walmart.realestate.crystal.storereview.report.settingscalculator;

import com.walmart.realestate.crystal.storereview.model.report.SettingChangeLogReport;

import java.util.Arrays;
import java.util.List;

class TemperatureSwitchesCalculatorStrategy extends AbstractSettingsCalculatorStrategy {

    public TemperatureSwitchesCalculatorStrategy(SettingsCalculatorContext context) {
        super(context);
    }

    @Override
    protected List<SettingChangeLogReport> calculateSettingsInternal() {
        SettingChangeLogReport targetTemperature = context.getTargetTemperature();
        double targetNew = Double.parseDouble(targetTemperature.getNewValue());

        SettingChangeLogReport cutInTemperature = buildCutInTemperature(targetNew + 2);
        SettingChangeLogReport cutOutTemperature = buildCutOutTemperature(targetNew - 2);

        return Arrays.asList(cutInTemperature, cutOutTemperature);
    }

}
