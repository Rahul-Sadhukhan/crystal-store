package com.walmart.realestate.crystal.storereview.report.settingscalculator;

import com.walmart.realestate.crystal.storereview.model.report.SettingChangeLogReport;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

class TemperatureSwitchCalculatorStrategy extends AbstractSettingsCalculatorStrategy {

    public TemperatureSwitchCalculatorStrategy(SettingsCalculatorContext context) {
        super(context);
    }

    @Override
    protected List<SettingChangeLogReport> calculateSettingsInternal() {
        Optional<SettingChangeLogReport> cutOutTemperatureOptional = Optional.ofNullable(context.getCutOutTemperature());

        SettingChangeLogReport targetTemperature = context.getTargetTemperature();
        double targetTemperatureNew = Double.parseDouble(targetTemperature.getNewValue());

        Function<Double, SettingChangeLogReport> setting;
        double value;
        if (cutOutTemperatureOptional.isPresent()) {
            setting = this::buildCutInTemperature;
            value = targetTemperatureNew + 2;
        } else {
            setting = this::buildCutOutTemperature;
            value = targetTemperatureNew - 2;
        }

        return Collections.singletonList(setting.apply(value));
    }

}
