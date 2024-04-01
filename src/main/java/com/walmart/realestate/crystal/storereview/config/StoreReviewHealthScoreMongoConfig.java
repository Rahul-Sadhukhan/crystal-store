package com.walmart.realestate.crystal.storereview.config;

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
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableMongoRepositories(basePackages = "com.walmart.realestate.crystal.storereview.repository", mongoTemplateRef = "multiTenantMongoTemplate")
public class StoreReviewHealthScoreMongoConfig {

    private final TenantProperties tenantProperties;

    @Primary
    @Bean
    @ConfigurationProperties("spring.data.mongodb")
    public MongoProperties mongoProperties() {
        return new MongoProperties();
    }

    @Autowired
    public StoreReviewHealthScoreMongoConfig(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    @Bean(name = "multiTenantMongoTemplate")
    @Primary
    public MongoTemplate mongoTemplate() {
        MongoProperties properties = mongoProperties();
        SimpleMongoClientDatabaseFactory simpleMongoClientDatabaseFactory = mongoDbFactoryStoreReviewHealthScore();
        MongoConverter converter = getDefaultMongoConverter(simpleMongoClientDatabaseFactory, properties);
        return new MongoTemplate(simpleMongoClientDatabaseFactory, converter);
    }

    @Bean
    @Primary
    public SimpleMongoClientDatabaseFactory mongoDbFactoryStoreReviewHealthScore() {
        final String defaultTenant = tenantProperties.getDefaultTenant();
        final TenantDatabaseProperties defaultTenantConfig = tenantProperties.getTenants().get(defaultTenant);
        final String defaultDbName = defaultTenantConfig.getStoreReview().getDatabase();
        final String defaultConnectionString = defaultTenantConfig.getStoreReview().getUrl();
        final MongoClient defaultClient = MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(defaultConnectionString)).build());

        final Map<String, SimpleMongoClientDatabaseFactory> tenantDbFactoryMap = new HashMap<>();

        tenantProperties.getTenants().forEach((tenantId, tenantConfig) -> {
            if (tenantConfig.getStoreReview() != null) {
                final ConnectionString connectionString = new ConnectionString(tenantConfig.getStoreReview().getUrl());
                final MongoClient client = MongoClients.create(MongoClientSettings.builder()
                        .applyConnectionString(connectionString).uuidRepresentation(UuidRepresentation.STANDARD).build());
                final SimpleMongoClientDatabaseFactory dbFactory = new SimpleMongoClientDatabaseFactory(client, tenantConfig.getStoreReview().getDatabase());
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
                    throw new UnsupportedOperationException("tenantId not supported for store review health score");
                }
                return tenantDbFactory.getMongoDatabase();
            }
        };
    }

    public static MongoConverter getDefaultMongoConverter(SimpleMongoClientDatabaseFactory factory, MongoProperties properties) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
        MongoCustomConversions conversions = new MongoCustomConversions(Collections.emptyList());

        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        if (Objects.nonNull(properties.isAutoIndexCreation())) {
            mappingContext.setAutoIndexCreation(properties.isAutoIndexCreation());
        }
        mappingContext.afterPropertiesSet();

        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mappingContext);
        converter.setCustomConversions(conversions);
        converter.setCodecRegistryProvider(factory);
        converter.afterPropertiesSet();

        return converter;
    }

}
