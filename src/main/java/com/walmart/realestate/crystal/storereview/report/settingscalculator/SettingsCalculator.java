package com.walmart.realestate.crystal.storereview.report.settingscalculator;

import com.walmart.realestate.crystal.storereview.model.report.SettingChangeLogReport;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.walmart.realestate.crystal.storereview.service.StoreReviewReportService.Constants.*;
import static com.walmart.realestate.crystal.storereview.service.StoreReviewReportService.findLatestSettingChangeLog;

@Component
public class SettingsCalculator {

    private static final SettingsCalculatorStrategy NO_OP_STRATEGY = Collections::emptyList;

    public List<SettingChangeLogReport> calculate(List<SettingChangeLogReport> settingChangeLogs, SettingsCalculatorContext context) {
        return findStrategy(settingChangeLogs, context).get();
    }

    private SettingsCalculatorStrategy findStrategy(List<SettingChangeLogReport> settingChangeLogs, SettingsCalculatorContext context) {
        Optional<SettingChangeLogReport> targetTemperatureOptional = findLatestSettingChangeLog(settingChangeLogs.stream(), CASE_TEMPERATURE_TARGET);
        Optional<SettingChangeLogReport> cutInTemperatureOptional = findLatestSettingChangeLog(settingChangeLogs.stream(), CASE_TEMPERATURE_CUT_IN);
        Optional<SettingChangeLogReport> cutOutTemperatureOptional = findLatestSettingChangeLog(settingChangeLogs.stream(), CASE_TEMPERATURE_CUT_OUT);

        targetTemperatureOptional.ifPresent(context::setTargetTemperature); // 4 points
        cutInTemperatureOptional.ifPresent(context::setCutInTemperature); // 2 points
        cutOutTemperatureOptional.ifPresent(context::setCutOutTemperature); // 1 point

        if (targetTemperatureOptional.isPresent() && cutInTemperatureOptional.isPresent() && cutOutTemperatureOptional.isPresent()) { // 7
            return NO_OP_STRATEGY;
        } else if (targetTemperatureOptional.isPresent() && !cutInTemperatureOptional.isPresent() && !cutOutTemperatureOptional.isPresent()) { // 4
            return new TemperatureSwitchesCalculatorStrategy(context);
        } else if (!targetTemperatureOptional.isPresent() && cutInTemperatureOptional.isPresent() && cutOutTemperatureOptional.isPresent()) { // 3
            return new TemperatureTargetCalculatorStrategy(context);
        } else if (targetTemperatureOptional.isPresent()) { // 5 || 6
            return new TemperatureSwitchCalculatorStrategy(context);
        } else if (cutInTemperatureOptional.isPresent() || cutOutTemperatureOptional.isPresent()) { // 1 || 2
            return new TemperatureTargetAndSwitchCalculatorStrategy(context);
        } else { // 0
            return new TemperatureTargetAndSwitchesCalculatorStrategy(context);
        }
    }

}
