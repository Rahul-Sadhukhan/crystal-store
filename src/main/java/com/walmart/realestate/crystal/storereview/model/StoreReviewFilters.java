package com.walmart.realestate.crystal.storereview.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StoreReviewFilters {

    private String id;

    private List<Long> storeNumber;

    private List<String> assignee;

    private List<String> createdBy;

    private List<String> closedBy;

    private List<String> declinedBy;

    private List<String> state;

    private Double healthScoreMin;

    private Double healthScoreMax;

    private Instant assignedDateFrom;

    private Instant assignedDateTo;

    private Instant createdFrom;

    private Instant createdTo;

    private Instant startDateFrom;

    private Instant startDateTo;

    private Instant closedFrom;

    private Instant closedTo;

    private Instant declinedFrom;

    private Instant declinedTo;

}
