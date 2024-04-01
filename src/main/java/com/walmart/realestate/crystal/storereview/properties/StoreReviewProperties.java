package com.walmart.realestate.crystal.storereview.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

@Setter
@Getter
@ConfigurationProperties(prefix = "store-review")
public class StoreReviewProperties {

    private List<String> assetTypes;

    private String reviewerRole;

    @NestedConfigurationProperty
    private StoreReviewReportProperties report;

}
