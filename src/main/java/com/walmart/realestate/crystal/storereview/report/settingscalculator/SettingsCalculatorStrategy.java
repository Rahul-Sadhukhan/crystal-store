package com.walmart.realestate.crystal.storereview.report.settingscalculator;

import com.walmart.realestate.crystal.storereview.model.report.SettingChangeLogReport;

import java.util.List;
import java.util.function.Supplier;

@FunctionalInterface
interface SettingsCalculatorStrategy extends Supplier<List<SettingChangeLogReport>> {
}
