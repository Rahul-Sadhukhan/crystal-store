package com.walmart.realestate.crystal.storereview.model;

import lombok.*;

import java.time.Instant;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreReviewAssetHealthScore {

    private String storeReviewId;

    @Deprecated
    private Long assetId;

    private String assetMappingId;

    private Instant preReviewTimestamp;

    private Double preReviewScore;

    private Instant preReviewScoreTimestamp;

    private Instant reviewStartTimestamp;

    private Double healthScoreStart;

    private Instant timestampStart;

    private Instant reviewEndTimestamp;

    private Double healthScoreEnd;

    private Instant timestampEnd;

    private Double targetTemperatureEnd;

    private Double lowCutInTemperatureEnd;

    private Double lowCutOutTemperatureEnd;

    private Double averageTemperatureEnd;

    private Instant postReviewTimestamp;

    private Double postReviewScore;

    private Instant postReviewScoreTimestamp;

    private Instant postMaintenanceTimestamp;

    private Double postMaintenanceScore;

    private Instant postMaintenanceScoreTimestamp;

    private Instant postPreventiveMaintenanceTimestamp;

    private Double postPreventiveMaintenanceScore;

    private Instant postPreventiveMaintenanceScoreTimestamp;

    public String getAssetId() {
        return assetMappingId;
    }

}
