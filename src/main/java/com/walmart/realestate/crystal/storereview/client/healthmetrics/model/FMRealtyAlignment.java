package com.walmart.realestate.crystal.storereview.client.healthmetrics.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FMRealtyAlignment {

    private Long storeNumber;

    private Long fmRegion;

    private String fmSubRegion;

    private String sdm;


}