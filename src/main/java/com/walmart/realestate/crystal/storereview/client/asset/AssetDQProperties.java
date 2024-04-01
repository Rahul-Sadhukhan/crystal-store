package com.walmart.realestate.crystal.storereview.client.asset;

import com.walmart.realestate.crystal.storereview.properties.ServiceProviderProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@Setter
@ConfigurationProperties(prefix = "asset-mdm-api")
public class AssetDQProperties {

    private String url;

    @NestedConfigurationProperty
    private ServiceProviderProperties serviceProvider;

}
