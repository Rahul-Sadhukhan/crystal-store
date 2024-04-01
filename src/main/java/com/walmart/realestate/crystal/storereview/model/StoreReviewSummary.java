package com.walmart.realestate.crystal.storereview.model;

import lombok.*;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Relation(itemRelation = "storeReviewSummary", collectionRelation = "storeReviewSummaryList")
public class StoreReviewSummary {

    private Long storeNumber;

    private String storeReviewId;

    private Instant reviewStartDate;

    private Double preReviewHealthScore;

    private Instant reviewEndDate;

    private Double postReviewHealthScore;

    private String status;

    private String assignee;

    private String assigneeName;

}
