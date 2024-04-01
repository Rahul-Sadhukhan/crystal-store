package com.walmart.realestate.crystal.storereview.service;

import com.walmart.core.realestate.cerberus.bean.CerberusUserInformation;
import com.walmart.core.realestate.cerberus.bean.PingFedDetails;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.repository.*;
import com.walmart.realestate.crystal.storereview.util.WithMockUser;
import com.walmart.realestate.soteria.model.User;
import com.walmart.realestate.soteria.model.UserContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
class StoreReviewUserServiceIntegrationTest {

    @Autowired
    private StoreReviewUserService storeReviewUserService;

    @MockBean
    private UserService userService;

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
    private HealthMetricsClient healthMetricsClient;

    @MockBean
    private StoreReviewSuspendService storeReviewSuspendService;

    @MockBean
    private StoreReviewSuspendRepository storeReviewSuspendRepository;

    @MockBean
    private AuditorAware<String> auditorAware;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @WithMockUser(username = "user0", roles = {"reviewer"})
    @Test
    void testGetUsersByRole() {

        when(userService.getUsersByRole(any(UserContext.class), eq("reviewer")))
                .thenReturn(new ArrayList<>(List.of(User.builder()
                                .id("user0")
                                .firstName("First")
                                .lastName("McLast")
                                .role("reviewer")
                                .build(),
                        User.builder()
                                .id("user1")
                                .firstName("First")
                                .lastName("McLast")
                                .role("reviewer")
                                .build(),
                        User.builder()
                                .id("user2")
                                .firstName("First")
                                .lastName("McLast")
                                .role("reviewer")
                                .build())));

        List<User> users = storeReviewUserService.getReviewers(new UserContext(new CerberusUserInformation("user0",
                "First McLast", "Manager", null, new PingFedDetails(), null), new HashSet<>(List.of("reviewer"))));

        assertThat(users).hasSize(1);

        User user = users.get(0);
        assertThat(user.getId()).isEqualTo("user0");
        assertThat(user.getFirstName()).isEqualTo("First");
        assertThat(user.getLastName()).isEqualTo("McLast");
        assertThat(user.getFullName()).isEqualTo("First McLast");
        assertThat(user.getRoles()).containsExactlyInAnyOrder("reviewer");

        verify(userService).getUsersByRole(any(UserContext.class), eq("reviewer"));

    }

}
