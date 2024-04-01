package com.walmart.realestate.crystal.settingchangelog.controller;

import com.walmart.core.realestate.cerberus.bean.CerberusUserInformation;
import com.walmart.core.realestate.cerberus.bean.PingFedDetails;
import com.walmart.core.realestate.cerberus.service.CerberusTokenService;
import com.walmart.realestate.crystal.metadata.repository.MetadataItemRepository;
import com.walmart.realestate.crystal.settingchangelog.config.TestSecurityConfigSCL;
import com.walmart.realestate.crystal.settingchangelog.controller.hypermedia.SettingChangeLogAssembler;
import com.walmart.realestate.crystal.settingchangelog.model.SettingChangeLog;
import com.walmart.realestate.crystal.settingchangelog.repository.SettingChangeLogRepository;
import com.walmart.realestate.crystal.settingchangelog.service.SettingChangeLogService;
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
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.walmart.core.realestate.cerberus.constants.CerberusConstants.CERBERUS_AUTH_HEADER_NAME;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {SettingChangeLogController.class,
        TestSecurityConfigSCL.class, SettingChangeLogAssembler.class})
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, MongoDataAutoConfiguration.class, KafkaAutoConfiguration.class})
class SettingChangeLogControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private SettingChangeLogAssembler settingChangeLogAssembler;

    @MockBean
    private SettingChangeLogService settingChangeLogService;

    @MockBean
    private CerberusTokenService cerberusTokenService;

    @MockBean
    private SettingChangeLogRepository settingChangeLogRepository;

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

    private SettingChangeLog settingChangeLog;

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

        settingChangeLog = SettingChangeLog.builder()
                .referenceId("reference1")
                .assetMappingId("1")
                .storeNumber(1L)
                .setting("dummy")
                .oldValue("19")
                .newValue("20")
                .unit("unit")
                .notes("notes is optional")
                .reason("reason is optional")
                .createdAt(Instant.ofEpochSecond(1623178393).atZone(ZoneOffset.UTC).toInstant())
                .build();

    }

    @Test
    void testCreateSettingChangeLog() {

        when(cerberusTokenService.parseCerberusToken("string0")).thenReturn(cerberusUserInformation);
        when(settingChangeLogService.createSettingChangeLog(any(SettingChangeLog.class))).thenReturn(settingChangeLog);

        List<Role> roles = Collections.singletonList(Role.builder().name("manager").build());
        when(soteriaRoleService.getRolesByUser(cerberusUserInformation)).thenReturn(roles);

        String uri = baseUri + "/setting-change-logs";
        HttpEntity<SettingChangeLog> entity = new HttpEntity<>(settingChangeLog, headers);

        ResponseEntity<EntityModel<SettingChangeLog>> response = restTemplate.exchange(uri, HttpMethod.POST, entity,
                new ParameterizedTypeReference<>() {
                });

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotNull();
        assertThat(response.getBody().getContent().getAssetMappingId()).isEqualTo("1");
        assertThat(response.getBody().getContent().getSetting()).isEqualTo("dummy");

        verify(cerberusTokenService).parseCerberusToken("string0");
        verify(settingChangeLogService).createSettingChangeLog(any(SettingChangeLog.class));
        verify(soteriaRoleService).getRolesByUser(cerberusUserInformation);

    }

    @Test
    void testGetSettingChangeLog() {

        when(cerberusTokenService.parseCerberusToken("string0")).thenReturn(cerberusUserInformation);
        when(settingChangeLogService.getSettingChangeLog("123")).thenReturn(settingChangeLog);

        List<Role> roles = Collections.singletonList(Role.builder().name("manager").build());
        when(soteriaRoleService.getRolesByUser(cerberusUserInformation)).thenReturn(roles);

        String uri = baseUri + "/setting-change-logs/123";
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<EntityModel<SettingChangeLog>> response = restTemplate.exchange(uri, HttpMethod.GET, entity,
                new ParameterizedTypeReference<>() {
                });

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotNull();
        assertThat(response.getBody().getContent().getAssetMappingId()).isEqualTo("1");
        assertThat(response.getBody().getContent().getSetting()).isEqualTo("dummy");

        verify(cerberusTokenService).parseCerberusToken("string0");
        verify(settingChangeLogService).getSettingChangeLog("123");
        verify(soteriaRoleService).getRolesByUser(cerberusUserInformation);

    }

    @Test
    void testGetSettingChangeLogs() {

        when(cerberusTokenService.parseCerberusToken("string0")).thenReturn(cerberusUserInformation);
        when(settingChangeLogService.getSettingChangeLogs(1L, "1"))
                .thenReturn(Collections.singletonList(settingChangeLog));

        List<Role> roles = Collections.singletonList(Role.builder().name("manager").build());
        when(soteriaRoleService.getRolesByUser(cerberusUserInformation)).thenReturn(roles);

        String uri = baseUri + "/setting-change-logs?assetId=1&storeNumber=1";
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<CollectionModel<EntityModel<SettingChangeLog>>> response = restTemplate.exchange(uri, HttpMethod.GET, entity,
                new ParameterizedTypeReference<>() {
                });

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);

        verify(cerberusTokenService).parseCerberusToken("string0");
        verify(settingChangeLogService).getSettingChangeLogs(1L, "1");
        verify(soteriaRoleService).getRolesByUser(cerberusUserInformation);

    }

}
