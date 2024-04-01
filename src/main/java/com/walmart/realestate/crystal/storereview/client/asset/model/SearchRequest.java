package com.walmart.realestate.crystal.storereview.client.asset.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SearchRequest {

    private AssetQuery assetQuery;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> assetFields;

    private Boolean dataQualityIssues;

}
