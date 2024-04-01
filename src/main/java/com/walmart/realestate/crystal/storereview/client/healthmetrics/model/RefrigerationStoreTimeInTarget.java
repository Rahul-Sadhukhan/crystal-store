package com.walmart.realestate.crystal.storereview.client.healthmetrics.model;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

@EqualsAndHashCode
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefrigerationStoreTimeInTarget implements Serializable {

    private String rowId;

    private Long storeNumber;

    private Double timeInTarget;

    private Instant startTime;

    private Instant endTime;

    private Instant runTime;

    private LocalDate runDate;

}
