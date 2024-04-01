package com.walmart.realestate.crystal.storereview.report.settingscalculator;

import com.walmart.realestate.crystal.storereview.model.report.SettingChangeLogReport;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

import static com.walmart.realestate.crystal.storereview.service.StoreReviewReportService.Constants.*;

@RequiredArgsConstructor
abstract class AbstractSettingsCalculatorStrategy implements SettingsCalculatorStrategy {

    protected final SettingsCalculatorContext context;

    protected abstract List<SettingChangeLogReport> calculateSettingsInternal();

    @Override
    public final List<SettingChangeLogReport> get() {
        try {
            return calculateSettingsInternal();
        } catch (NumberFormatException ignored) {}

        return Collections.emptyList();
    }

    protected final SettingChangeLogReport buildTargetTemperature(double newValue, double oldValue) {
        return buildTargetTemperature(newValue, oldValue, null);
    }

    protected final SettingChangeLogReport buildTargetTemperature(double newValue, double oldValue, String source) {
        return SettingChangeLogReport.builder()
                .setting(CASE_TEMPERATURE_TARGET)
                .newValue(Double.toString(newValue))
                .oldValue(Double.toString(oldValue))
                .source(source)
                .createdAt(context.getTimestamp())
                .build();
    }

    protected final SettingChangeLogReport buildCutInTemperature(double newValue) {
        return buildCutInTemperature(newValue, null);
    }

    protected final SettingChangeLogReport buildCutInTemperature(double newValue, String source) {
        return buildSwitchTemperature(CASE_TEMPERATURE_CUT_IN, newValue, source);
    }

    protected final SettingChangeLogReport buildCutOutTemperature(double newValue) {
        return buildCutOutTemperature(newValue, null);
    }

    protected final SettingChangeLogReport buildCutOutTemperature(double newValue, String source) {
        return buildSwitchTemperature(CASE_TEMPERATURE_CUT_OUT, newValue, source);
    }

    protected final SettingChangeLogReport buildSwitchTemperature(String setting, double newValue, String source) {
        return SettingChangeLogReport.builder()
                .setting(setting)
                .newValue(Double.toString(newValue))
                .source(source)
                .createdAt(context.getTimestamp())
                .build();
    }

}
