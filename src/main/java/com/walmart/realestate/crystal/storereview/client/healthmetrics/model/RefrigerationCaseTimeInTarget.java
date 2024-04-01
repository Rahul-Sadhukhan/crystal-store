package com.walmart.realestate.crystal.storereview.client.healthmetrics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RefrigerationCaseTimeInTarget extends RefrigerationRackTimeInTarget {

    private String caseId;

    private String circuitName;

    private String caseName;

    private String storeNumberWithCountryCode;

    private Long moduleIndex;

    private Long sensorIndex;

    private String sensorLabel;

    private Double targetTemperature;

    private Double lowCutInTemperature;

    private Double lowCutOutTemperature;

    private Double averageTemperature;

    private String temperatureSensorId;

}
