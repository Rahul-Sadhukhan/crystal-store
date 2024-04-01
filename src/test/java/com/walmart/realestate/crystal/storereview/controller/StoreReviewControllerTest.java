package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.core.realestate.cerberus.bean.CerberusUserInformation;
import com.walmart.core.realestate.cerberus.bean.PingFedDetails;
import com.walmart.core.realestate.cerberus.service.CerberusTokenService;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.controller.hypermedia.StoreReviewAssembler;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.repository.*;
import com.walmart.realestate.crystal.storereview.service.StoreReviewService;
import com.walmart.realestate.crystal.storereview.service.StoreReviewSuspendService;
import com.walmart.realestate.soteria.model.Role;
import com.walmart.realestate.soteria.model.UserContext;
import com.walmart.realestate.soteria.service.SoteriaRoleService;
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
import org.springframework.hateoas.EntityModel;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.walmart.core.realestate.cerberus.constants.CerberusConstants.CERBERUS_AUTH_HEADER_NAME;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
class StoreReviewControllerTest {

    @LocalServerPort
    private int port;

    @MockBean
    private StoreReviewService storeReviewService;

    @MockBean
    private StoreReviewAssembler storeReviewAssembler;

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

    private StoreReview storeReview;

    private HttpHeaders headers;

    @BeforeEach
    void setup() {
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

        storeReview = StoreReview.builder()
                .assignee("test")
                .storeNumber(111L)
                .build();
    }

    @Test
    void createStoreReviewTestPass() {

        when(cerberusTokenService.parseCerberusToken("string0")).thenReturn(cerberusUserInformation);
        when(storeReviewService.createStoreReview(any(StoreReview.class), any(UserContext.class))).thenReturn(storeReview);
        when(storeReviewAssembler.toModel(any(StoreReview.class))).thenReturn(EntityModel.of(storeReview));

        List<Role> roles = Collections.singletonList(Role.builder().name("manager").build());
        when(soteriaRoleService.getRolesByUser(cerberusUserInformation)).thenReturn(roles);

        String uri = baseUri + "/store-reviews";
        HttpEntity<StoreReview> entity = new HttpEntity<>(storeReview, headers);

        ResponseEntity<EntityModel<StoreReview>> response = restTemplate.exchange(uri, HttpMethod.POST, entity,
                new ParameterizedTypeReference<EntityModel<StoreReview>>() {
                });

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotNull();
        assertThat(response.getBody().getContent().getStoreNumber()).isEqualTo(111L);

        verify(cerberusTokenService).parseCerberusToken("string0");
        verify(storeReviewService).createStoreReview(any(StoreReview.class), any(UserContext.class));
        verify(storeReviewAssembler).toModel(any(StoreReview.class));
        verify(soteriaRoleService).getRolesByUser(cerberusUserInformation);
    }

    @Test
    void createStoreReviewTestFail() {

        when(cerberusTokenService.parseCerberusToken("string0")).thenReturn(cerberusUserInformation);

        List<Role> roles = Collections.singletonList(Role.builder().name("default-role").build());
        when(soteriaRoleService.getRolesByUser(cerberusUserInformation)).thenReturn(roles);

        String uri = baseUri + "/store-reviews";
        HttpEntity<StoreReview> entity = new HttpEntity<>(storeReview, headers);

        Throwable thrown = catchThrowable(() -> restTemplate.exchange(uri, HttpMethod.POST, entity,
                new ParameterizedTypeReference<EntityModel<StoreReview>>() {
                }));

        assertThat(thrown).isInstanceOf(HttpClientErrorException.class);

        verify(cerberusTokenService).parseCerberusToken("string0");
        verify(soteriaRoleService).getRolesByUser(cerberusUserInformation);
    }

    @Test
    void getStoreReviewTestPass() {

        when(cerberusTokenService.parseCerberusToken("string0")).thenReturn(cerberusUserInformation);
        when(storeReviewService.getStoreReview("SR-1")).thenReturn(storeReview);
        when(storeReviewAssembler.toModel(any(StoreReview.class))).thenReturn(EntityModel.of(storeReview));

        List<Role> roles = Collections.singletonList(Role.builder().name("reviewer").build());
        when(soteriaRoleService.getRolesByUser(cerberusUserInformation)).thenReturn(roles);

        String uri = baseUri + "/store-reviews/SR-1";
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<EntityModel<StoreReview>> response = restTemplate.exchange(uri, HttpMethod.GET, entity,
                new ParameterizedTypeReference<EntityModel<StoreReview>>() {
                });

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotNull();
        assertThat(response.getBody().getContent().getStoreNumber()).isEqualTo(111L);

        verify(cerberusTokenService).parseCerberusToken("string0");
        verify(storeReviewService).getStoreReview("SR-1");
        verify(storeReviewAssembler).toModel(any(StoreReview.class));
        verify(soteriaRoleService).getRolesByUser(cerberusUserInformation);
    }

}
