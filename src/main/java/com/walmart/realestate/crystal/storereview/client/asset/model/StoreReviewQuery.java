package com.walmart.realestate.crystal.storereview.client.asset.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoreReviewQuery {

    private String healthScore;

    private String storeNumber;

    private String storeType;

    private String fmRegion;

    private String fmSubRegion;

    private String refrigerationTypes;

    private String emsData;
}