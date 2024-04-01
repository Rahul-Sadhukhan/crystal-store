package com.walmart.realestate.crystal.storereview.report.settingscalculator;

import com.walmart.realestate.crystal.storereview.model.StoreReviewAssetHealthScore;
import com.walmart.realestate.crystal.storereview.model.report.SettingChangeLogReport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.walmart.realestate.crystal.storereview.service.StoreReviewReportService.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SettingsCalculator.class)
class SettingsCalculatorTest {

    @Autowired
    private SettingsCalculator settingsCalculator;

    private static final ZoneId chicagoTimeZone = ZoneId.of("America/Chicago");

    @Test
    void testCalculateWithEmptyLogsEmptyTelemetry() {
        Instant timestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45).toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(Collections.emptyList(), context);

        assertThat(calculated).isEmpty();
    }

    @Test
    void testCalculateWithEmptyLogsTargetTelemetry() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .targetTemperatureEnd(-10.0)
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(Collections.emptyList(), context);

        LocalDateTime locationTimestamp = timestampEnd.atZone(chicagoTimeZone).toLocalDateTime();
        List<SettingChangeLogReport> expected = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(locationTimestamp)
                        .newValue("-8.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(locationTimestamp)
                        .newValue("-12.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(locationTimestamp)
                        .oldValue("-10.0")
                        .newValue("-10.0")
                        .source(TELEMETRY)
                        .build());

        assertThat(calculated).isEqualTo(expected);
    }

    @Test
    void testCalculateWithEmptyLogsSwitchesTelemetry() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .lowCutInTemperatureEnd(8.0)
                .lowCutOutTemperatureEnd(4.0)
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(Collections.emptyList(), context);

        LocalDateTime locationTimestamp = timestampEnd.atZone(chicagoTimeZone).toLocalDateTime();
        List<SettingChangeLogReport> expected = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(locationTimestamp)
                        .newValue("8.0")
                        .source(TELEMETRY)
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(locationTimestamp)
                        .newValue("4.0")
                        .source(TELEMETRY)
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(locationTimestamp)
                        .oldValue("6.0")
                        .newValue("6.0")
                        .build());

        assertThat(calculated).isEqualTo(expected);
    }

    @Test
    void testCalculateWithEmptyLogsCutInTelemetry() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .lowCutInTemperatureEnd(8.0)
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(Collections.emptyList(), context);

        assertThat(calculated).isEmpty();
    }

    @Test
    void testCalculateWithEmptyLogsCutOutTelemetry() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .lowCutOutTemperatureEnd(8.0)
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(Collections.emptyList(), context);

        assertThat(calculated).isEmpty();
    }

    @Test
    void testCalculateWithEmptyLogsTargetCutInTelemetry() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .targetTemperatureEnd(-10.0)
                .lowCutInTemperatureEnd(-3.4)
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(Collections.emptyList(), context);

        LocalDateTime locationTimestamp = timestampEnd.atZone(chicagoTimeZone).toLocalDateTime();
        List<SettingChangeLogReport> expected = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(locationTimestamp)
                        .newValue("-8.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(locationTimestamp)
                        .newValue("-12.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(locationTimestamp)
                        .oldValue("-10.0")
                        .newValue("-10.0")
                        .source(TELEMETRY)
                        .build());

        assertThat(calculated).isEqualTo(expected);
    }

    @Test
    void testCalculateWithEmptyLogsTargetCutOutTelemetry() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .targetTemperatureEnd(-10.0)
                .lowCutOutTemperatureEnd(-13.5)
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(Collections.emptyList(), context);

        LocalDateTime locationTimestamp = timestampEnd.atZone(chicagoTimeZone).toLocalDateTime();
        List<SettingChangeLogReport> expected = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(locationTimestamp)
                        .newValue("-8.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(locationTimestamp)
                        .newValue("-12.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(locationTimestamp)
                        .oldValue("-10.0")
                        .newValue("-10.0")
                        .source(TELEMETRY)
                        .build());

        assertThat(calculated).isEqualTo(expected);
    }

    @Test
    void testCalculateWithEmptyLogsTargetSwitchesTelemetry() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .targetTemperatureEnd(-12.0)
                .lowCutInTemperatureEnd(-10.0)
                .lowCutOutTemperatureEnd(-13.5)
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(Collections.emptyList(), context);

        LocalDateTime locationTimestamp = timestampEnd.atZone(chicagoTimeZone).toLocalDateTime();
        List<SettingChangeLogReport> expected = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(locationTimestamp)
                        .newValue("-10.0")
                        .source(TELEMETRY)
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(locationTimestamp)
                        .newValue("-13.5")
                        .source(TELEMETRY)
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(locationTimestamp)
                        .oldValue("-12.0")
                        .newValue("-12.0")
                        .source(TELEMETRY)
                        .build());

        assertThat(calculated).isEqualTo(expected);
    }

    @Test
    void testCalculateWithTargetLogs() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .targetTemperatureEnd(-10.0)
                .lowCutInTemperatureEnd(8.0)
                .lowCutOutTemperatureEnd(-13.5)
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> settingChanges = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(localTimestampEnd.minusSeconds(600))
                        .oldValue("4.0")
                        .newValue("-8.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(localTimestampEnd.minusSeconds(1200))
                        .oldValue("-18.0")
                        .newValue("4.0")
                        .build());

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(settingChanges, context);

        LocalDateTime locationTimestamp = timestampEnd.atZone(chicagoTimeZone).toLocalDateTime();
        List<SettingChangeLogReport> expected = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(locationTimestamp)
                        .newValue("-6.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(locationTimestamp)
                        .newValue("-10.0")
                        .build());

        assertThat(calculated).isEqualTo(expected);
    }

    @Test
    void testCalculateWithTargetLogsEmptyTelemetry() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> settingChanges = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(localTimestampEnd.minusSeconds(600))
                        .oldValue("4.0")
                        .newValue("-8.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(localTimestampEnd.minusSeconds(1200))
                        .oldValue("-18.0")
                        .newValue("4.0")
                        .build());

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(settingChanges, context);

        LocalDateTime locationTimestamp = timestampEnd.atZone(chicagoTimeZone).toLocalDateTime();
        List<SettingChangeLogReport> expected = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(locationTimestamp)
                        .newValue("-6.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(locationTimestamp)
                        .newValue("-10.0")
                        .build());

        assertThat(calculated).isEqualTo(expected);
    }

    @Test
    void testCalculateWithSwitchesLogs() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .targetTemperatureEnd(-10.0)
                .lowCutInTemperatureEnd(8.0)
                .lowCutOutTemperatureEnd(-13.5)
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> settingChanges = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(localTimestampEnd.minusSeconds(600))
                        .oldValue("4.0")
                        .newValue("-8.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(localTimestampEnd.minusSeconds(1200))
                        .oldValue("-18.0")
                        .newValue("4.0")
                        .build());

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(settingChanges, context);

        LocalDateTime locationTimestamp = timestampEnd.atZone(chicagoTimeZone).toLocalDateTime();
        List<SettingChangeLogReport> expected = Collections.singletonList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(locationTimestamp)
                        .oldValue("2.0")
                        .newValue("-10.0")
                        .build());

        assertThat(calculated).isEqualTo(expected);
    }

    @Test
    void testCalculateWithSwitchesLogsEmptyTelemetry() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> settingChanges = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(localTimestampEnd.minusSeconds(600))
                        .oldValue("4.0")
                        .newValue("-8.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(localTimestampEnd.minusSeconds(1200))
                        .oldValue("-18.0")
                        .newValue("4.0")
                        .build());

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(settingChanges, context);

        LocalDateTime locationTimestamp = timestampEnd.atZone(chicagoTimeZone).toLocalDateTime();
        List<SettingChangeLogReport> expected = Collections.singletonList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(locationTimestamp)
                        .oldValue("2.0")
                        .newValue("-10.0")
                        .build());

        assertThat(calculated).isEqualTo(expected);
    }

    @Test
    void testCalculateWithSwitchesLogEmptyTelemetry() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> settingChanges = Collections.singletonList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(localTimestampEnd.minusSeconds(600))
                        .oldValue("4.0")
                        .newValue("-8.0")
                        .build());

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(settingChanges, context);

        LocalDateTime locationTimestamp = timestampEnd.atZone(chicagoTimeZone).toLocalDateTime();
        List<SettingChangeLogReport> expected = Collections.singletonList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(locationTimestamp)
                        .oldValue("2.0")
                        .newValue("-10.0")
                        .build());

        assertThat(calculated).isEqualTo(expected);
    }

    @Test
    void testCalculateWithSwitchesLogsParseExceptionEmptyTelemetry() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> settingChanges = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(localTimestampEnd.minusSeconds(600))
                        .oldValue("4.0")
                        .newValue("-8.x")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(localTimestampEnd.minusSeconds(1200))
                        .oldValue("-18.0")
                        .newValue("4.0")
                        .build());

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(settingChanges, context);

        assertThat(calculated).isEmpty();
    }

    @Test
    void testCalculateWithTargetSwitchesLogs() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> settingChanges = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(localTimestampEnd.minusSeconds(600))
                        .oldValue("4.0")
                        .newValue("-8.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(localTimestampEnd.minusSeconds(1200))
                        .oldValue("-18.0")
                        .newValue("4.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(localTimestampEnd.minusSeconds(1800))
                        .oldValue("2.0")
                        .newValue("-10.0")
                        .build());

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(settingChanges, context);

        assertThat(calculated).isEmpty();
    }

    @Test
    void testCalculateWithCutInLogsCutOutTelemetry() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .targetTemperatureEnd(-10.0)
                .lowCutInTemperatureEnd(8.0)
                .lowCutOutTemperatureEnd(-13.5)
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> settingChanges = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(localTimestampEnd.minusSeconds(600))
                        .oldValue("4.0")
                        .newValue("-8.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(localTimestampEnd.minusSeconds(1200))
                        .oldValue("-18.0")
                        .newValue("4.0")
                        .build());

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(settingChanges, context);

        LocalDateTime locationTimestamp = timestampEnd.atZone(chicagoTimeZone).toLocalDateTime();
        List<SettingChangeLogReport> expected = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(locationTimestamp)
                        .newValue("-13.5")
                        .source(TELEMETRY)
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(locationTimestamp)
                        .oldValue("2.0")
                        .newValue("-10.0")
                        .build());

        assertThat(calculated).isEqualTo(expected);
    }

    @Test
    void testCalculateWithCutInLogsEmptyTelemetry() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .targetTemperatureEnd(-10.0)
                .lowCutInTemperatureEnd(8.0)
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> settingChanges = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(localTimestampEnd.minusSeconds(600))
                        .oldValue("4.0")
                        .newValue("-8.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(localTimestampEnd.minusSeconds(1200))
                        .newValue("4.0")
                        .oldValue("-18.0")
                        .build());

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(settingChanges, context);

        LocalDateTime locationTimestamp = timestampEnd.atZone(chicagoTimeZone).toLocalDateTime();
        List<SettingChangeLogReport> expected = Collections.singletonList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(locationTimestamp)
                        .oldValue("2.0")
                        .newValue("-10.0")
                        .build());

        assertThat(calculated).isEqualTo(expected);
    }

    @Test
    void testCalculateWithCutOutLogsCutInTelemetry() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .targetTemperatureEnd(-10.0)
                .lowCutInTemperatureEnd(8.0)
                .lowCutOutTemperatureEnd(-13.5)
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> settingChanges = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(localTimestampEnd.minusSeconds(600))
                        .oldValue("4.0")
                        .newValue("-8.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(localTimestampEnd.minusSeconds(1200))
                        .oldValue("-18.0")
                        .newValue("4.0")
                        .build());

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(settingChanges, context);

        LocalDateTime locationTimestamp = timestampEnd.atZone(chicagoTimeZone).toLocalDateTime();
        List<SettingChangeLogReport> expected = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(locationTimestamp)
                        .newValue("8.0")
                        .source(TELEMETRY)
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(locationTimestamp)
                        .oldValue("6.0")
                        .newValue("6.0")
                        .build());

        assertThat(calculated).isEqualTo(expected);
    }

    @Test
    void testCalculateWithCutOutLogsEmptyTelemetry() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .targetTemperatureEnd(-10.0)
                .lowCutOutTemperatureEnd(-13.5)
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> settingChanges = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(localTimestampEnd.minusSeconds(600))
                        .oldValue("4.0")
                        .newValue("-8.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(localTimestampEnd.minusSeconds(1200))
                        .newValue("4.0")
                        .oldValue("-18.0")
                        .build());

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(settingChanges, context);

        assertThat(calculated).isEmpty();
    }

    @Test
    void testCalculateWithTargetCutInLogs() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> settingChanges = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(localTimestampEnd.minusSeconds(600))
                        .oldValue("4.0")
                        .newValue("-8.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(localTimestampEnd.minusSeconds(1200))
                        .oldValue("-18.0")
                        .newValue("4.0")
                        .build());

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(settingChanges, context);

        LocalDateTime locationTimestamp = timestampEnd.atZone(chicagoTimeZone).toLocalDateTime();
        List<SettingChangeLogReport> expected = Collections.singletonList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(locationTimestamp)
                        .newValue("-10.0")
                        .build());

        assertThat(calculated).isEqualTo(expected);
    }

    @Test
    void testCalculateWithTargetCutOutLogs() {
        LocalDateTime localTimestampEnd = LocalDateTime.of(2021, 8, 20, 14, 45);
        Instant timestampEnd = localTimestampEnd.toInstant(ZoneOffset.UTC);

        StoreReviewAssetHealthScore healthScore = StoreReviewAssetHealthScore.builder()
                .timestampEnd(timestampEnd)
                .build();
        SettingsCalculatorContext context = new SettingsCalculatorContext(healthScore, chicagoTimeZone);

        List<SettingChangeLogReport> settingChanges = Arrays.asList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_TARGET)
                        .createdAt(localTimestampEnd.minusSeconds(600))
                        .oldValue("4.0")
                        .newValue("-8.0")
                        .build(),
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_OUT)
                        .createdAt(localTimestampEnd.minusSeconds(1200))
                        .oldValue("-18.0")
                        .newValue("4.0")
                        .build());

        List<SettingChangeLogReport> calculated = settingsCalculator.calculate(settingChanges, context);

        LocalDateTime locationTimestamp = timestampEnd.atZone(chicagoTimeZone).toLocalDateTime();
        List<SettingChangeLogReport> expected = Collections.singletonList(
                SettingChangeLogReport.builder()
                        .setting(CASE_TEMPERATURE_CUT_IN)
                        .createdAt(locationTimestamp)
                        .newValue("-6.0")
                        .build());

        assertThat(calculated).isEqualTo(expected);
    }

}
