package com.walmart.realestate.crystal.storereview.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreHealthScoreSnapshot {

    private Long storeNumber;

    private String rowId;

    private Double value;

    private Instant runTime;

}
