package com.walmart.realestate.crystal.metadata.controller;

import com.walmart.core.realestate.cerberus.bean.CerberusUserInformation;
import com.walmart.core.realestate.cerberus.bean.PingFedDetails;
import com.walmart.core.realestate.cerberus.service.CerberusTokenService;
import com.walmart.realestate.crystal.metadata.controller.hypermedia.MetadataTypeAssembler;
import com.walmart.realestate.crystal.metadata.model.MetadataType;
import com.walmart.realestate.crystal.metadata.repository.MetadataTypeRepository;
import com.walmart.realestate.crystal.metadata.service.MetadataTypeService;
import com.walmart.realestate.crystal.settingchangelog.config.TestSecurityConfigSCL;
import com.walmart.realestate.crystal.storereview.rbac.UserRepositoryAdapter;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
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
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {
        MetadataTypeController.class, TestSecurityConfigSCL.class, MetadataTypeAssembler.class
})
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, MongoDataAutoConfiguration.class, KafkaAutoConfiguration.class})
class MetadataTypeControllerTest {

    @LocalServerPort
    private int port;

    @MockBean
    private MetadataTypeService metadataTypeService;

    @MockBean
    private MetadataTypeRepository metadataTypeRepository;

    @MockBean
    private IdentifierRepository identifierRepository;

    @MockBean
    private CerberusTokenService cerberusTokenService;

    @MockBean
    private UserRepositoryAdapter userRepositoryAdapter;

    @MockBean
    private SoteriaRoleService soteriaRoleService;

    @Autowired
    private RestTemplate restTemplate;

    private CerberusUserInformation cerberusUserInformation;

    private String baseUri;

    private HttpHeaders headers;

    private MetadataType metadataType;

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

        metadataType = MetadataType.builder()
                .name("settings")
                .id("settings")
                .build();
    }

    @Test
    void getMetadataTypesTest() {
        when(cerberusTokenService.parseCerberusToken("string0")).thenReturn(cerberusUserInformation);
        when(metadataTypeService.getMetadataTypes()).thenReturn(Collections.singletonList(metadataType));

        List<Role> roles = Collections.singletonList(Role.builder().name("manager").build());
        when(soteriaRoleService.getRolesByUser(cerberusUserInformation)).thenReturn(roles);

        String uri = baseUri + "/metadata-types";
        HttpEntity<MetadataType> entity = new HttpEntity<>(metadataType, headers);

        ResponseEntity<CollectionModel<EntityModel<MetadataType>>> response = restTemplate.exchange(uri, HttpMethod.GET, entity,
                new ParameterizedTypeReference<CollectionModel<EntityModel<MetadataType>>>() {
                });

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);

        List<EntityModel<MetadataType>> metadataTypes = new ArrayList<>(response.getBody().getContent());

        assertThat(metadataTypes.get(0)).isNotNull();
        assertThat(metadataTypes.get(0).getContent()).isNotNull();
        assertThat(metadataTypes.get(0).getContent().getId()).isEqualTo("settings");

        verify(metadataTypeService).getMetadataTypes();
        verify(soteriaRoleService).getRolesByUser(cerberusUserInformation);
    }

    @Test
    void createMetadataTypeTest() {
        when(cerberusTokenService.parseCerberusToken("string0")).thenReturn(cerberusUserInformation);
        when(metadataTypeService.createMetadataType(metadataType)).thenReturn(metadataType);

        List<Role> roles = Collections.singletonList(Role.builder().name("manager").build());
        when(soteriaRoleService.getRolesByUser(cerberusUserInformation)).thenReturn(roles);

        String uri = baseUri + "/metadata-types";
        HttpEntity<MetadataType> entity = new HttpEntity<>(metadataType, headers);

        ResponseEntity<EntityModel<MetadataType>> response = restTemplate.exchange(uri, HttpMethod.POST, entity,
                new ParameterizedTypeReference<EntityModel<MetadataType>>() {
                });

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotNull();
        assertThat(response.getBody().getContent().getId()).isEqualTo("settings");

        verify(metadataTypeService).createMetadataType(metadataType);
        verify(soteriaRoleService).getRolesByUser(cerberusUserInformation);
    }

}
