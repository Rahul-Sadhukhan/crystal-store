package com.walmart.realestate.crystal.storereview.client.healthmetrics.model;

import lombok.*;

import java.io.Serializable;
import java.sql.Date;
import java.time.Instant;

@EqualsAndHashCode
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefrigerationTimeInTargetSummary implements Serializable {

    private Instant runTime;

    private Instant startTime;

    private Instant endTime;

    private Date runDate;

}
