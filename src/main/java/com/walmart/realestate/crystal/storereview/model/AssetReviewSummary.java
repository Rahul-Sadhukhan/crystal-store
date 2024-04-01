package com.walmart.realestate.crystal.storereview.model;

import lombok.*;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Relation(itemRelation = "assetReviewSummary", collectionRelation = "assetReviewSummaries")
public class AssetReviewSummary {

    private String assetMappingId;

    private String storeReviewId;

    private Instant reviewStartDate;

    private Double preReviewHealthScore;

    private Instant reviewEndDate;

    private Double postReviewHealthScore;

    private String workOrder;

    private String status;

    public String getAssetId() {
        return assetMappingId;
    }

}
