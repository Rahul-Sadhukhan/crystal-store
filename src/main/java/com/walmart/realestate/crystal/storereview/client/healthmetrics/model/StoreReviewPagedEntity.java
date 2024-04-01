package com.walmart.realestate.crystal.storereview.client.healthmetrics.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreReviewPagedEntity {

    private Integer storeNumber;

    private Double healthScore;

    private Instant latestRunTime;

    private String storeType;

    private Integer fmRegion;

    private String fmSubRegion;

    private Date completionDate;

    private String emsData;

    private String refrigerationTypes;

    private Integer rackCount;

    private Integer caseCount;
}
