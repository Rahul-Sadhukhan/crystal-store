package com.walmart.realestate.crystal.storereview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.client.storeinfo.StoreClient;
import com.walmart.realestate.crystal.storereview.client.storeinfo.model.StoreDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StoreService.class, ObjectMapper.class})
@ActiveProfiles("test")
class StoreServiceTest {

    @Autowired
    private StoreService storeService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StoreClient storeClient;

    @MockBean
    private HealthMetricsClient healthMetricsClient;

    private StoreDetail storeDetail;

    private Long storeNumber;

    private String storePlan;

    @BeforeEach
    void setUp() throws IOException {
        File storeResponseJson = new ClassPathResource("data/storeinfo/store-response.json").getFile();
        storeDetail = objectMapper.readValue(storeResponseJson, StoreDetail.class);
        storeNumber = 1L;
        storePlan = "<html>Test plan for store 1 </html>";
    }

    @Test
    void testGetStoreInfo() {

        when(storeClient.getStoreInfo(storeNumber)).thenReturn(storeDetail);

        StoreDetail storeDetail = storeService.getStoreInfo(1L);

        assertThat(storeDetail.getBusinessUnit().getStoreNumber()).isEqualTo(1L);
        assertThat(storeDetail.getBusinessUnit().getBanner().getStoreType()).isEqualTo("WM Supercenter");
        assertThat(storeDetail.getFacilityDetails().get(0)
                .getLocation().getLocationTimeZone().getDstTimeZone().getTimeZoneId()).isEqualTo("America/Chicago");

        verify(storeClient).getStoreInfo(1L);

    }

    @Test
    void testgetStorePlan() {

        when(storeClient.getStorePlan(storeNumber)).thenReturn(storePlan);

        String response = storeService.getStorePlan(1L);

        assertThat(response).isEqualTo(storePlan);

        verify(storeClient).getStorePlan(1L);

    }

}
