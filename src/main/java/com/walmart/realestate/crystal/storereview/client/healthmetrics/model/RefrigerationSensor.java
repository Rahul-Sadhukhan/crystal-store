package com.walmart.realestate.crystal.storereview.client.healthmetrics.model;

import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefrigerationSensor {

    private String id;

    private String countryCode;

    private String storeNumber;

    private String vendorType;

    private Long rackIndex;

    private String rackLabel;

    private String modType;

    private Long modIndex;

    private String modLabel;

    private Long sensorIndex;

    private String sensorLabel;

    private String sensorUnits;

    private String required;

    private String category;

    private String assetName;

    private String type;

    private String rackName;

    private String rackCallLetter;

    private String circuitName;

    private String circuitCallNumber;

    private String caseName;

    private String caseType;

    private String caseContents;

    private String assetType;

    private String caseCallLetter;

    private String compressorName;

    private String sensorDescription;

    private Long equipmentId;

    private String hvacName;

    private String hvacType;

    private String hvacLocation;

    private String caseTemp;

    private String rackTemp;

    private String rackSatTemp;

    private String tagId;

    private String qrCode;

}
