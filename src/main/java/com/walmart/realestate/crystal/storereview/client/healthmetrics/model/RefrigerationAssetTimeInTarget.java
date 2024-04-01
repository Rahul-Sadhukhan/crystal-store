package com.walmart.realestate.crystal.storereview.client.healthmetrics.model;

import java.time.Instant;

public interface RefrigerationAssetTimeInTarget {

    void setAssetMappingId(String assetMappingId);

    String getAssetMappingId();

    Long getEquipmentId();

    Double getTimeInTarget();

    Instant getRunTime();

    default Double getTargetTemperature() {
        return null;
    }

    default Double getLowCutInTemperature() {
        return null;
    }

    default Double getLowCutOutTemperature() {
        return null;
    }

    default Double getAverageTemperature() {
        return null;
    }

}
