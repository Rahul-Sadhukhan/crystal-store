package com.walmart.realestate.crystal.metadata.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.walmart.realestate.crystal.storereview.model.TenantDatabaseProperties;
import com.walmart.realestate.crystal.storereview.properties.TenantProperties;
import com.walmart.realestate.crystal.storereview.tenant.TenantContext;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableMongoRepositories(basePackages = "com.walmart.realestate.crystal.metadata.repository",
        mongoTemplateRef = "multiTenantMetadataTemplate")
public class MetadataMongoConfig {

    private final TenantProperties tenantProperties;

    @Autowired
    public MetadataMongoConfig(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    @Bean(name = "multiTenantMetadataTemplate")
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoDbFactoryMetadata());
    }

    @Bean
    public SimpleMongoClientDatabaseFactory mongoDbFactoryMetadata() {
        final String defaultTenant = tenantProperties.getDefaultTenant();
        final TenantDatabaseProperties defaultTenantConfig = tenantProperties.getTenants().get(defaultTenant);
        final String defaultDbName = defaultTenantConfig.getMetadata().getDatabase();
        final String defaultConnectionString = defaultTenantConfig.getMetadata().getUrl();
        final MongoClient defaultClient = MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(defaultConnectionString)).build());

        final Map<String, SimpleMongoClientDatabaseFactory> tenantDbFactoryMap = new HashMap<>();

        tenantProperties.getTenants().forEach((tenantId, tenantConfig) -> {
            if (tenantConfig.getMetadata() != null) {
                final ConnectionString connectionString = new ConnectionString(tenantConfig.getMetadata().getUrl());
                final MongoClient client = MongoClients.create(MongoClientSettings.builder()
                        .applyConnectionString(connectionString).uuidRepresentation(UuidRepresentation.STANDARD).build());
                final SimpleMongoClientDatabaseFactory dbFactory = new SimpleMongoClientDatabaseFactory(client, tenantConfig.getMetadata().getDatabase());
                tenantDbFactoryMap.put(tenantId, dbFactory);
            }
        });

        return new SimpleMongoClientDatabaseFactory(defaultClient, defaultDbName) {
            @Override
            public MongoDatabase getMongoDatabase() {
                final String tenantId = TenantContext.getCurrentTenant();
                if (tenantId == null) {
                    return super.getMongoDatabase();
                }
                final SimpleMongoClientDatabaseFactory tenantDbFactory = tenantDbFactoryMap.get(tenantId);
                if (tenantDbFactory == null) {
                    throw new UnsupportedOperationException("tenantId not supported for metadata");
                }
                return tenantDbFactory.getMongoDatabase();
            }
        };
    }

}
