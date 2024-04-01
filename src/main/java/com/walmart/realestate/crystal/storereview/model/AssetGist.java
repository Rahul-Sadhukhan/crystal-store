package com.walmart.realestate.crystal.storereview.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmart.realestate.crystal.storereview.client.asset.model.DataQualityIssue;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetGist {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long storeNumber;

    private String qrCode;

    private String tagId;

    private String brand;

    private String modelNumber;

    private String serialNumber;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean active;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String assetType;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<DataQualityIssue> dataQualityIssues;

}
