package com.walmart.realestate.crystal.storereview.report.settingscalculator;

import com.walmart.realestate.crystal.storereview.model.report.SettingChangeLogReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.walmart.realestate.crystal.storereview.service.StoreReviewReportService.Constants.TELEMETRY;

class TemperatureTargetAndSwitchesCalculatorStrategy extends AbstractSettingsCalculatorStrategy {

    public TemperatureTargetAndSwitchesCalculatorStrategy(SettingsCalculatorContext context) {
        super(context);
    }

    @Override
    protected List<SettingChangeLogReport> calculateSettingsInternal() {
        Optional<Double> targetTemperatureValue = Optional.ofNullable(context.getAssetHealthScore().getTargetTemperatureEnd());
        Optional<Double> cutInTemperatureValue = Optional.ofNullable(context.getAssetHealthScore().getLowCutInTemperatureEnd());
        Optional<Double> cutOutTemperatureValue = Optional.ofNullable(context.getAssetHealthScore().getLowCutOutTemperatureEnd());

        List<SettingChangeLogReport> calculated = new ArrayList<>();
        if (cutInTemperatureValue.isPresent() && cutOutTemperatureValue.isPresent()) {
            calculated.add(buildCutInTemperature(cutInTemperatureValue.get(), TELEMETRY));
            calculated.add(buildCutOutTemperature(cutOutTemperatureValue.get(), TELEMETRY));

            SettingChangeLogReport targetTemperature;
            if (targetTemperatureValue.isPresent()) {
                targetTemperature = buildTargetTemperature(targetTemperatureValue.get(), targetTemperatureValue.get(), TELEMETRY);
            } else {
                targetTemperature = buildTargetTemperature(cutInTemperatureValue.get() - 2, cutInTemperatureValue.get() - 2);
            }
            calculated.add(targetTemperature);
        } else if (targetTemperatureValue.isPresent()) {
            calculated.add(buildCutInTemperature(targetTemperatureValue.get() + 2));
            calculated.add(buildCutOutTemperature(targetTemperatureValue.get() - 2));
            calculated.add(buildTargetTemperature(targetTemperatureValue.get(), targetTemperatureValue.get(), TELEMETRY));
        }

        return calculated;
    }

}
