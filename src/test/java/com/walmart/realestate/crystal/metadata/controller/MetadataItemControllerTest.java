package com.walmart.realestate.crystal.metadata.controller;

import com.walmart.core.realestate.cerberus.bean.CerberusUserInformation;
import com.walmart.core.realestate.cerberus.bean.PingFedDetails;
import com.walmart.core.realestate.cerberus.service.CerberusTokenService;
import com.walmart.realestate.crystal.metadata.controller.hypermedia.MetadataItemAssembler;
import com.walmart.realestate.crystal.metadata.model.LocalizedValue;
import com.walmart.realestate.crystal.metadata.model.MetadataItem;
import com.walmart.realestate.crystal.metadata.repository.MetadataItemRepository;
import com.walmart.realestate.crystal.metadata.service.MetadataItemService;
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
import java.util.*;

import static com.walmart.core.realestate.cerberus.constants.CerberusConstants.CERBERUS_AUTH_HEADER_NAME;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {
        MetadataItemController.class, MetadataItemAssembler.class, TestSecurityConfigSCL.class
})
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, MongoDataAutoConfiguration.class, KafkaAutoConfiguration.class})
class MetadataItemControllerTest {

    @LocalServerPort
    private int port;

    @MockBean
    private MetadataItemService metadataItemService;

    @MockBean
    private MetadataTypeService metadataTypeService;

    @MockBean
    private CerberusTokenService cerberusTokenService;

    @MockBean
    private MetadataItemRepository metadataItemRepository;

    @MockBean
    private IdentifierRepository identifierRepository;

    @MockBean
    private UserRepositoryAdapter userRepositoryAdapter;

    @MockBean
    private SoteriaRoleService soteriaRoleService;

    @Autowired
    private RestTemplate restTemplate;

    private CerberusUserInformation cerberusUserInformation;

    private String baseUri;

    private HttpHeaders headers;

    private MetadataItem metadataItemOne;

    private MetadataItem metadataItemTwo;

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
        metadataItemOne = MetadataItem.builder()
                .id("test")
                .defaultValue("test")
                .assetTypes(Arrays.asList("type0", "type1"))
                .index(1)
                .isEnabled(true)
                .values(Arrays.asList(LocalizedValue
                                .builder()
                                .value("testcanada")
                                .locale(Locale.CANADA)
                                .build(),
                        LocalizedValue.builder()
                                .value("testfrench")
                                .locale(Locale.FRANCE)
                                .build()))
                .metadataType("settings")
                .maxValue("10")
                .minValue("20")
                .unit("psi")
                .build();

        metadataItemTwo = MetadataItem.builder()
                .id("suction-pressure")
                .defaultValue("suction-pressure test")
                .assetTypes(Arrays.asList("type0", "type1"))
                .index(2)
                .isEnabled(true)
                .values(Arrays.asList(LocalizedValue
                                .builder()
                                .value("suction-pressure test")
                                .locale(Locale.CANADA)
                                .build(),
                        LocalizedValue.builder()
                                .value("uction-pressure test")
                                .locale(Locale.FRANCE)
                                .build()))
                .metadataType("settings")
                .maxValue("10")
                .minValue("20")
                .unit("psi")
                .build();

    }

    @Test
    void createMetadataItemTest() {
        cerberusUserInformation.setRoles(Collections.singleton("associates"));
        when(cerberusTokenService.parseCerberusToken("string0")).thenReturn(cerberusUserInformation);
        when(metadataItemService.createMetadataItem(metadataItemOne)).thenReturn(metadataItemOne);

        List<Role> roles = Collections.singletonList(Role.builder().name("manager").build());
        when(soteriaRoleService.getRolesByUser(cerberusUserInformation)).thenReturn(roles);

        String uri = baseUri + "/metadata-items";
        HttpEntity<MetadataItem> entity = new HttpEntity<>(metadataItemOne, headers);

        ResponseEntity<EntityModel<MetadataItem>> response = restTemplate.exchange(uri, HttpMethod.POST, entity,
                new ParameterizedTypeReference<>() {
                });

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotNull();
        assertThat(response.getBody().getContent().getId()).isEqualTo("test");
        assertThat(response.getBody().getContent().getValues().size()).isEqualTo(2);

        verify(metadataItemService).createMetadataItem(metadataItemOne);
        verify(soteriaRoleService).getRolesByUser(cerberusUserInformation);
    }

    @Test
    void getMetadataItemTest() {
        cerberusUserInformation.setRoles(Collections.singleton("associates"));
        when(cerberusTokenService.parseCerberusToken("string0")).thenReturn(cerberusUserInformation);
        when(metadataItemService.getMetadataItems(null, null, false))
                .thenReturn(Arrays.asList(metadataItemOne, metadataItemTwo));

        List<Role> roles = Collections.singletonList(Role.builder().name("reviewer").build());
        when(soteriaRoleService.getRolesByUser(cerberusUserInformation)).thenReturn(roles);

        String uri = baseUri + "/metadata-items";
        HttpEntity<MetadataItem> entity = new HttpEntity<>(headers);

        ResponseEntity<CollectionModel<MetadataItem>> response = restTemplate.exchange(uri, HttpMethod.GET, entity,
                new ParameterizedTypeReference<>() {
                });

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2);

        verify(metadataItemService).getMetadataItems(null, null, false);
        verify(soteriaRoleService).getRolesByUser(cerberusUserInformation);
    }

}
