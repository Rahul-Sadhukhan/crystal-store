package com.walmart.realestate.crystal.storereview.model.report;

import com.walmart.realestate.crystal.storereview.client.asset.model.AssetTypeAware;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetReviewSummaryReport implements AssetTypeAware, Serializable {

    private String assetType;

    private Integer reviewedAssets;

    private Long settingChangeLogs;

    private Long insights;

    private Long workOrders;

}
