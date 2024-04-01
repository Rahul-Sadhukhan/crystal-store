package com.walmart.realestate.crystal.storereview.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "store-review.retry")
public class StoreReviewRetryProperties {

    private Long initialInterval;

    private Double multiplier;

    private Long maxInterval;

    private Integer maxAttempts;

}
