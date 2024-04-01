package com.walmart.realestate.crystal.storereview.properties;

import com.walmart.realestate.crystal.storereview.model.TenantDatabaseProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Setter
@Getter
@ConfigurationProperties(prefix = "tenant-config")
public class TenantProperties {

    private String defaultTenant;

    private Map<String, TenantDatabaseProperties> tenants;
}
