package com.walmart.realestate.crystal.storereview.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "service-registry")
public class ServiceRegistryProperties {

    private String consumerId;

    private String privateKey;

    private String privateKeyVersion;

}
