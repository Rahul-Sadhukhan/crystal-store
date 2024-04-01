package com.walmart.realestate.crystal.storereview.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@EqualsAndHashCode
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "StoreAssetReview")
public class StoreAssetReviewEntity {

    @Id
    private String id;

    private String uuid;

    private String storeReviewId;

    private Long storeNumber;

    private Long assetId;

    private String assetType;

    private String assetMappingId;

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
