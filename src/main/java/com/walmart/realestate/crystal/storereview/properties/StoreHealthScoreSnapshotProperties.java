package com.walmart.realestate.crystal.storereview.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "store-health-score-snapshot")
public class StoreHealthScoreSnapshotProperties {

    private String scheduleValue;

    private String initialDelayValue;

    private Integer partitionSize;

}
