package com.walmart.realestate.crystal.storereview.client.healthmetrics.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@EqualsAndHashCode
@Builder
@Getter
public class RefrigerationRackMetric {

    private String uniqueRecordIdentifier;

    private Long storeNumber;

    private String vendorType;

    private Long rackIndex;

    private String rackName;

    private String rackCallLetter;

    private Double superHeatScore;

    private Double suctionPressureScore;

    private Double lowCutInSuctionPressure;

    private Double lowCutOutSuctionPressure;

    private Double targetSuctionPressure;

    private Long equipmentId;

    private Instant runTime;

    private LocalDate runDate;

}
