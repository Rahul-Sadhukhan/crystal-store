package com.walmart.realestate.crystal.storereview.client.healthmetrics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

@EqualsAndHashCode
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RefrigerationRackTimeInTarget implements RefrigerationAssetTimeInTarget, Serializable {

    private String rackId;

    private String rowId;

    private String assetMappingId;

    private Long storeNumber;

    private String vendorType;

    private String rackName;

    private Long rackIndex;

    private String rackCallLetter;

    private Double timeInTarget;

    private Instant startTime;

    private Instant endTime;

    private String qrCode;

    private String tagId;

    private Instant runTime;

    private LocalDate runDate;

    private Long equipmentId;

}
