package com.walmart.realestate.crystal.storereview.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("StoreReviewAssetHealthScore")
public class StoreReviewAssetHealthScoreEntity {

    @Id
    @GeneratedValue
    private String id;

    @Indexed(useGeneratedName = true)
    private String storeReviewId;

    @Deprecated
    @Indexed(useGeneratedName = true)
    private Long assetId;

    @Indexed(useGeneratedName = true)
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

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedBy
    private String lastModifiedBy;

    @LastModifiedDate
    private Instant lastModifiedAt;

    @Version
    private Integer version;

}
