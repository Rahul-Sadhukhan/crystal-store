package com.walmart.realestate.crystal.storereview.model;

import lombok.*;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Relation(itemRelation = "assetReview", collectionRelation = "assetReviews")
public class StoreAssetReview {

    private String id;

    private UUID uuid;

    private String storeReviewId;

    private Long storeNumber;

    private Long assetId;

    private String assetMappingId;

    private String assetType;

    private String reviewedBy;

    private Instant reviewedAt;

    private String workOrderId;

    private String state;

    private String flow;

    private String createdBy;

    private Instant createdAt;

    private String lastModifiedBy;

    private Instant lastModifiedAt;

}
