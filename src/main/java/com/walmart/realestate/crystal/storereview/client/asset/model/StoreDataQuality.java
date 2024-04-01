package com.walmart.realestate.crystal.storereview.client.asset.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StoreDataQuality {

    private Long storeNumber;

    private Integer score;

}
