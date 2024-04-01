package com.walmart.realestate.crystal.storereview.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.core.realestate.cerberus.bean.CerberusUserInformation;
import com.walmart.core.realestate.cerberus.bean.PingFedDetails;
import com.walmart.core.realestate.cerberus.service.CerberusTokenService;
import com.walmart.realestate.crystal.storereview.client.storeinfo.model.StoreDetail;
import com.walmart.realestate.crystal.storereview.config.TestSecurityConfig;
import com.walmart.realestate.crystal.storereview.rbac.UserRepositoryAdapter;
import com.walmart.realestate.crystal.storereview.service.StoreService;
import com.walmart.realestate.idn.repository.IdentifierRepository;
import com.walmart.realestate.soteria.model.Role;
import com.walmart.realestate.soteria.service.SoteriaRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.walmart.core.realestate.cerberus.constants.CerberusConstants.CERBERUS_AUTH_HEADER_NAME;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {StoreController.class, TestSecurityConfig.class})
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, MongoDataAutoConfiguration.class, KafkaAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
class StoreControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StoreService storeService;

    @MockBean
    private CerberusTokenService cerberusTokenService;

    @MockBean
    private IdentifierRepository identifierRepository;

    @MockBean
    private UserRepositoryAdapter userRepositoryAdapter;

    @MockBean
    private SoteriaRoleService soteriaRoleService;

    private CerberusUserInformation cerberusUserInformation;

    private String baseUri;

    private HttpHeaders headers;

    private StoreDetail storeDetail;

    @BeforeEach
    void setup() throws JsonProcessingException {
        PingFedDetails details = new PingFedDetails();
        details.setAccessToken("token0");
        details.setExpiry(Date.from(Instant.now().plusSeconds(60L)));
        cerberusUserInformation = new CerberusUserInformation();
        cerberusUserInformation.setPingFedDetails(details);

        baseUri = "http://localhost:" + port;
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(AUTHORIZATION, "Bearer token0");
        headers.set(CERBERUS_AUTH_HEADER_NAME, "string0");
        headers.set("x-tenant", "US");

        String storeDetailJson = "{\n" +
                "  \"businessUnit\": {\n" +
                "    \"banner\": {\n" +
                "      \"description\": \"Test Type\"\n" +
                "    },\n" +
                "    \"number\": 1\n" +
                "  },\n" +
                "  \"facilityDetails\": [\n" +
                "    {\n" +
                "      \"location\": {\n" +
                "        \"locationTimeZone\": {\n" +
                "          \"standard\": \"CST\",\n" +
                "          \"dstTimeZone\": {\n" +
                "            \"dstOffset\": \"3600\",\n" +
                "            \"rawOffset\": -21600,\n" +
                "            \"timeZoneId\": \"America/Los_Angeles\",\n" +
                "            \"timeZoneCode\": \"CDT\",\n" +
                "            \"name\": \"Central Daylight Time\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        storeDetail = objectMapper.readValue(storeDetailJson, StoreDetail.class);
    }

    @Test
    void testGetStoreInfo() {

        when(cerberusTokenService.parseCerberusToken("string0")).thenReturn(cerberusUserInformation);
        when(storeService.getStoreInfo(1L)).thenReturn(storeDetail);

        List<Role> roles = Collections.singletonList(Role.builder().name("reviewer").build());
        when(soteriaRoleService.getRolesByUser(cerberusUserInformation)).thenReturn(roles);

        String uri = baseUri + "/store-info?storeNumber=1";
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<StoreDetail> response = restTemplate.exchange(uri, HttpMethod.GET, entity, StoreDetail.class);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBusinessUnit().getBanner().getStoreType()).isEqualTo("Test Type");
        assertThat(response.getBody().getFacilityDetails().get(0).getLocation()
                .getLocationTimeZone().getDstTimeZone().getTimeZoneId()).isEqualTo("America/Los_Angeles");
        assertThat(response.getBody().getFacilityDetails().get(0).getLocation()
                .getLocationTimeZone().getStandard()).isEqualTo("CST");

        verify(cerberusTokenService).parseCerberusToken("string0");
        verify(storeService).getStoreInfo(1L);
        verify(soteriaRoleService).getRolesByUser(cerberusUserInformation);

    }

}
