package com.walmart.realestate.crystal.storereview.report.settingscalculator;

import com.walmart.realestate.crystal.storereview.model.report.SettingChangeLogReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.walmart.realestate.crystal.storereview.service.StoreReviewReportService.Constants.TELEMETRY;

class TemperatureTargetAndSwitchCalculatorStrategy extends AbstractSettingsCalculatorStrategy {

    public TemperatureTargetAndSwitchCalculatorStrategy(SettingsCalculatorContext context) {
        super(context);
    }

    @Override
    protected List<SettingChangeLogReport> calculateSettingsInternal() {
        Optional<Double> cutInTemperatureValue = Optional.ofNullable(context.getAssetHealthScore().getLowCutInTemperatureEnd());
        Optional<Double> cutOutTemperatureValue = Optional.ofNullable(context.getAssetHealthScore().getLowCutOutTemperatureEnd());

        Optional<SettingChangeLogReport> cutInTemperatureOptional = Optional.ofNullable(context.getCutInTemperature());
        Optional<SettingChangeLogReport> cutOutTemperatureOptional = Optional.ofNullable(context.getCutOutTemperature());

        List<SettingChangeLogReport> calculated = new ArrayList<>();

        if (cutInTemperatureOptional.isPresent() && cutOutTemperatureValue.isPresent()) {
            calculated.add(buildCutOutTemperature(cutOutTemperatureValue.get(), TELEMETRY));
        } else if (cutOutTemperatureOptional.isPresent() && cutInTemperatureValue.isPresent()) {
            calculated.add(buildCutInTemperature(cutInTemperatureValue.get(), TELEMETRY));
        }

        double cutInTemperatureNew, cutInTemperatureOld;
        if (cutInTemperatureOptional.isPresent()) {
            cutInTemperatureNew = Double.parseDouble(cutInTemperatureOptional.get().getNewValue());
            cutInTemperatureOld = Double.parseDouble(cutInTemperatureOptional.get().getOldValue());
        } else if (cutInTemperatureValue.isPresent()) {
            cutInTemperatureNew = cutInTemperatureOld = cutInTemperatureValue.get();
        } else return calculated;

        calculated.add(buildTargetTemperature(cutInTemperatureNew - 2, cutInTemperatureOld - 2));

        return calculated;
    }

}
