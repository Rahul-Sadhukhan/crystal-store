package com.walmart.realestate.crystal.storereview.config;

import com.walmart.realestate.crystal.storereview.model.TenantDatabaseProperties;
import com.walmart.realestate.crystal.storereview.properties.TenantProperties;
import com.walmart.realestate.crystal.storereview.tenant.MultiTenantDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
@Profile("!test")
@EntityScan("com.walmart.realestate.crystal.storereview.entity")
public class MultiTenantConfiguration {

    private final TenantProperties tenantProperties;

    @Bean
    @ConfigurationProperties()
    public DataSource dataSource() {

        Map<Object, Object> resolvedDataSources = new HashMap<>();
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        Iterator<Map.Entry<String, TenantDatabaseProperties>> itr = tenantProperties.getTenants().entrySet().iterator();
        while(itr.hasNext())
        {
            Map.Entry<String, TenantDatabaseProperties> entry = itr.next();
            dataSourceBuilder.driverClassName(entry.getValue().getAzure().getDriverClassName());
            dataSourceBuilder.username(entry.getValue().getAzure().getUsername());
            dataSourceBuilder.password(entry.getValue().getAzure().getPassword());
            dataSourceBuilder.url(entry.getValue().getAzure().getUrl());
            resolvedDataSources.put(entry.getKey(), dataSourceBuilder.build());
        }

        AbstractRoutingDataSource dataSource = new MultiTenantDataSource();
        dataSource.setDefaultTargetDataSource(resolvedDataSources.get(tenantProperties.getDefaultTenant()));
        dataSource.setTargetDataSources(resolvedDataSources);

        dataSource.afterPropertiesSet();
        return dataSource;
    }

}
