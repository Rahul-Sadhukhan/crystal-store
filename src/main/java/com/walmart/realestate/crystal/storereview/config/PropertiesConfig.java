package com.walmart.realestate.crystal.storereview.config;

import com.walmart.realestate.crystal.metadata.properties.MetadataProperties;
import com.walmart.realestate.crystal.storereview.client.amg.AmgProperties;
import com.walmart.realestate.crystal.storereview.client.asset.AssetDQProperties;
import com.walmart.realestate.crystal.storereview.client.asset.AssetProperties;
import com.walmart.realestate.crystal.storereview.properties.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({WebProperties.class, StoreReviewProperties.class, StoreReviewRetryProperties.class,
        StoreHealthScoreSnapshotProperties.class, ServiceRegistryProperties.class, AssetProperties.class, AssetDQProperties.class,
        MetadataProperties.class, AmgProperties.class, TenantProperties.class})
public class PropertiesConfig {
}
