package com.walmart.realestate.crystal.storereview.util;

import com.walmart.realestate.crystal.storereview.config.PropertiesConfig;
import com.walmart.realestate.crystal.storereview.config.ServiceRegistryConfig;
import com.walmart.realestate.crystal.storereview.config.TestServiceRegistryConfig;
import com.walmart.realestate.crystal.storereview.properties.ServiceProviderProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {ServiceRegistryUtil.class, PropertiesConfig.class, TestServiceRegistryConfig.class})
@ActiveProfiles("test")
class ServiceRegistryUtilTest {

    @Autowired
    private ServiceRegistryUtil serviceRegistryUtil;

    @Test
    void testGenerateRoutingHeaders() {
        ServiceProviderProperties properties = ServiceProviderProperties.builder()
                .name("ASSET_MDM_APP")
                .environment("stage")
                .build();

        Map<String, String> headers = serviceRegistryUtil.generateRoutingHeaders(properties);

        assertThat(headers).isNotNull();
        assertThat(headers).containsOnlyKeys("wm_svc.name", "wm_svc.env", "wm_consumer.id", "wm_consumer.intimestamp", "wm_sec.key_version", "wm_sec.auth_signature", "x-tenant");
        assertThat(headers).containsEntry("wm_svc.name", "ASSET_MDM_APP");
        assertThat(headers).containsEntry("wm_svc.env", "stage");
        assertThat(headers).containsEntry("x-tenant", "US");
    }

}
