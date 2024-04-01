package com.walmart.realestate.crystal.storereview.model;

import com.walmart.realestate.crystal.storereview.client.asset.model.Asset;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationAssetTimeInTarget;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationRackMetric;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationSensor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AggregatedStoreAssetReview {

    private Asset asset;

    private StoreAssetReview storeAssetReview;

    private RefrigerationAssetTimeInTarget refrigerationAssetTimeInTarget;

    private RefrigerationRackMetric refrigerationRackMetric;

    private RefrigerationSensor refrigerationSensor;

}
