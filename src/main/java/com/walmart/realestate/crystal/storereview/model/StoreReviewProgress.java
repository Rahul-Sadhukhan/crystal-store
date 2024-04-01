package com.walmart.realestate.crystal.storereview.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreReviewProgress {

    private String storeReviewId;

    private Integer total;

    private Integer completed;

}
