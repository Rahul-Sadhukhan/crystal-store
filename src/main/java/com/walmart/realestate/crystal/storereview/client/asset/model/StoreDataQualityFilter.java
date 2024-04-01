package com.walmart.realestate.crystal.storereview.client.asset.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class StoreDataQualityFilter {

    @JsonProperty("storenum")
    private List<Long> storeNumbers;

    @JsonProperty("assettype")
    private List<String> assetTypes;

    @JsonProperty("articlenum")
    private List<String> articleNumbers;

    @JsonProperty("tradeid")
    private List<String> tradeIds;

    private List<String> attributes;

}
