package com.walmart.realestate.crystal.storereview.model;

import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreReviewTally {

    private String user;

    private String state;

    private long totalCount;

    private long assigneeCount;

    private long createdByCount;

    public StoreReviewTally(String state, long totalCount) {
        this.state = state;
        this.totalCount = totalCount;
    }

    public StoreReviewTally(String state, long totalCount, long assigneeCount, long createdByCount) {
        this.state = state;
        this.totalCount = totalCount;
        this.assigneeCount = assigneeCount;
        this.createdByCount = createdByCount;
    }

}
