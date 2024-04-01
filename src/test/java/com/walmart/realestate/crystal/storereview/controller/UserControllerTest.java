package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.core.realestate.cerberus.bean.CerberusUserInformation;
import com.walmart.core.realestate.cerberus.bean.PingFedDetails;
import com.walmart.core.realestate.cerberus.service.CerberusTokenService;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.entity.UserAccountEntity;
import com.walmart.realestate.crystal.storereview.entity.UserMembershipEntity;
import com.walmart.realestate.crystal.storereview.repository.*;
import com.walmart.realestate.crystal.storereview.service.StoreReviewSuspendService;
import com.walmart.realestate.soteria.model.User;
import com.walmart.realestate.soteria.service.SoteriaRoleService;
import com.walmart.realestate.soteria.service.SoteriaUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.walmart.core.realestate.cerberus.constants.CerberusConstants.CERBERUS_AUTH_HEADER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
class UserControllerTest {

    @LocalServerPort
    private int port;

    @MockBean
    private CerberusTokenService cerberusTokenService;

    @MockBean
    private StoreAssetReviewRepository storeAssetReviewRepository;

    @MockBean
    private StoreHealthScoreSnapshotRepository storeHealthScoreSnapshotRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private StoreReviewRepository storeReviewRepository;

    @MockBean
    private StoreReviewTallyRepository storeReviewTallyRepository;

    @MockBean
    private SoteriaRoleService soteriaRoleService;

    @MockBean
    private SoteriaUserService soteriaUserService;

    @MockBean
    private HealthMetricsClient healthMetricsClient;

    @MockBean
    private StoreReviewSuspendService storeReviewSuspendService;

    @MockBean
    private StoreReviewSuspendRepository storeReviewSuspendRepository;

    @MockBean
    private AuditorAware<String> auditorAware;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private RestTemplate restTemplate;

    private CerberusUserInformation cerberusUserInformation;

    private String baseUri;

    private HttpHeaders headers;

    private UserAccountEntity userAccountEntity;

    @BeforeEach
    public void setup() {
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

        UserMembershipEntity userMembershipEntity =
                new UserMembershipEntity("associates", "associates");
        userAccountEntity = UserAccountEntity.builder()
                .userId("123")
                .firstName("test")
                .membership(userMembershipEntity)
                .build();
    }

    @Test
    void
    getUsersByTypeTest() {

        when(cerberusTokenService.parseCerberusToken("string0")).thenReturn(cerberusUserInformation);

        /* We need to create new ArrayList<>() because of some mutations done by SecurityFilter <-- PostFilter */
        List<User> users = new ArrayList<>(Collections.singletonList(User.builder().firstName("test").build()));
        when(soteriaUserService.getUsersByRole(cerberusUserInformation, "reviewer")).thenReturn(users);

        String uri = baseUri + "/users?role=reviewer";
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<CollectionModel<EntityModel<User>>> response = restTemplate.exchange(uri, HttpMethod.GET, entity,
                new ParameterizedTypeReference<CollectionModel<EntityModel<User>>>() {
                });

        assertThat(response.getBody()).isNotNull();
        response.getBody().getContent().forEach(item -> {
            assertThat(item.getContent()).isNotNull();
            assertThat(item.getContent().getFirstName()).isEqualTo("test");
        });

        verify(cerberusTokenService).parseCerberusToken("string0");
        verify(soteriaUserService).getUsersByRole(cerberusUserInformation, "reviewer");
    }

}
