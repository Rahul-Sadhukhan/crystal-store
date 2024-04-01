package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.core.realestate.cerberus.bean.CerberusUserInformation;
import com.walmart.core.realestate.cerberus.bean.PingFedDetails;
import com.walmart.core.realestate.cerberus.service.CerberusTokenService;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.model.StoreAssetReview;
import com.walmart.realestate.crystal.storereview.model.UpdateStoreAssetReviewStatus;
import com.walmart.realestate.crystal.storereview.repository.*;
import com.walmart.realestate.crystal.storereview.service.StoreAssetReviewService;
import com.walmart.realestate.crystal.storereview.service.StoreReviewSuspendService;
import com.walmart.realestate.soteria.model.Role;
import com.walmart.realestate.soteria.service.SoteriaRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.walmart.core.realestate.cerberus.constants.CerberusConstants.CERBERUS_AUTH_HEADER_NAME;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
class StoreAssetReviewControllerTest {

    @LocalServerPort
    private int port;

    @MockBean
    private StoreAssetReviewService storeAssetReviewService;

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

    private StoreAssetReview storeAssetReview;

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

        storeAssetReview = StoreAssetReview.builder()
                .id("SAR-1")
                .storeReviewId("SR-1")
                .build();
    }

    @Test
    void getStoreAssetReviewTestPass() {

        when(cerberusTokenService.parseCerberusToken("string0")).thenReturn(cerberusUserInformation);
        when(storeAssetReviewService.getStoreAssetReview("SAR-1")).thenReturn(storeAssetReview);

        List<Role> roles = Collections.singletonList(Role.builder().name("reviewer").build());
        when(soteriaRoleService.getRolesByUser(cerberusUserInformation)).thenReturn(roles);

        String uri = baseUri + "/store-asset-reviews/SAR-1";
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<EntityModel<StoreAssetReview>> response = restTemplate.exchange(uri, HttpMethod.GET, entity,
                new ParameterizedTypeReference<EntityModel<StoreAssetReview>>() {
                });

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotNull();
        assertThat(response.getBody().getContent().getStoreReviewId()).isEqualTo("SR-1");

        verify(cerberusTokenService).parseCerberusToken("string0");
        verify(storeAssetReviewService).getStoreAssetReview("SAR-1");
        verify(soteriaRoleService).getRolesByUser(cerberusUserInformation);
    }

    @Test()
    void updateAssetReviewStatusTestFail() {
        cerberusUserInformation.setRoles(Collections.singleton("unauthorized"));
        when(cerberusTokenService.parseCerberusToken("string0")).thenReturn(cerberusUserInformation);

        List<Role> roles = Collections.singletonList(Role.builder().name("reviewer").build());
        when(soteriaRoleService.getRolesByUser(cerberusUserInformation)).thenReturn(roles);

        String uri = baseUri + "/store-asset-reviews/SAR-1/status?action=test";
        HttpEntity<StoreAssetReview> entity = new HttpEntity<>(storeAssetReview, headers);

        Throwable thrown = catchThrowable(() -> restTemplate.exchange(uri, HttpMethod.POST, entity,
                new ParameterizedTypeReference<EntityModel<StoreAssetReview>>() {
                }));

        assertThat(thrown).isInstanceOf(HttpClientErrorException.class);

        verify(cerberusTokenService).parseCerberusToken("string0");
        verify(soteriaRoleService).getRolesByUser(cerberusUserInformation);
    }

    @Test
    void updateAssetReviewsStatus() {

        List<StoreAssetReview> storeAssetReviewList = List.of(StoreAssetReview.builder()
                        .id("SAR-1")
                        .storeReviewId("SR-1")
                        .build(),
                StoreAssetReview.builder()
                        .id("SAR-2")
                        .storeReviewId("SR-1")
                        .build(),
                StoreAssetReview.builder()
                        .id("SAR-3")
                        .storeReviewId("SR-1")
                        .build());

        List<Role> roles = Collections.singletonList(Role.builder().name("reviewer").build());

        when(cerberusTokenService.parseCerberusToken("string0")).thenReturn(cerberusUserInformation);
        when(storeAssetReviewService.updateStoreAssetReviewsStatus(anyList(), eq("complete"))).thenReturn(storeAssetReviewList);
        when(soteriaRoleService.getRolesByUser(cerberusUserInformation)).thenReturn(roles);

        UpdateStoreAssetReviewStatus updateStoreAssetReviewStatus = UpdateStoreAssetReviewStatus.builder()
                .storeAssetReviewIdList(List.of("SAR-1", "SAR-2", "SAR-3"))
                .build();

        String uri = baseUri + "/store-asset-reviews/bulk/updateStatus?action=complete";
        HttpEntity<UpdateStoreAssetReviewStatus> entity = new HttpEntity<>(updateStoreAssetReviewStatus, headers);

        ResponseEntity<CollectionModel<EntityModel<StoreAssetReview>>> response = restTemplate.exchange(uri, HttpMethod.POST, entity,
                new ParameterizedTypeReference<CollectionModel<EntityModel<StoreAssetReview>>>() {
                });

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(3);
        assertThat(response.getBody().getContent()).isNotNull();

        verify(cerberusTokenService).parseCerberusToken("string0");

        ArgumentCaptor<ArrayList<String>> listArgumentCaptor = ArgumentCaptor.forClass(ArrayList.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

        verify(storeAssetReviewService).updateStoreAssetReviewsStatus(listArgumentCaptor.capture(), stringArgumentCaptor.capture());
        verify(soteriaRoleService).getRolesByUser(cerberusUserInformation);
        assertThat(listArgumentCaptor.getValue()).isEqualTo(List.of("SAR-1", "SAR-2", "SAR-3"));
        assertThat(stringArgumentCaptor.getValue()).isEqualTo("complete");

    }

}
