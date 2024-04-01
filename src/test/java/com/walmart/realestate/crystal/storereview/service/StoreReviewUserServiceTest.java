package com.walmart.realestate.crystal.storereview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.core.realestate.cerberus.bean.CerberusUserInformation;
import com.walmart.realestate.crystal.storereview.config.PropertiesConfig;
import com.walmart.realestate.crystal.storereview.properties.StoreReviewProperties;
import com.walmart.realestate.soteria.model.User;
import com.walmart.realestate.soteria.model.UserContext;
import com.walmart.realestate.soteria.properties.SoteriaProperties;
import com.walmart.realestate.soteria.service.SoteriaPolicyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StoreReviewUserService.class, PropertiesConfig.class,
        SoteriaProperties.class, SoteriaPolicyService.class, ObjectMapper.class})
@ActiveProfiles("test")
public class StoreReviewUserServiceTest {

    @Autowired
    private StoreReviewUserService storeReviewUserService;

    @MockBean
    private UserService userService;

    @MockBean
    private StoreReviewProperties storeReviewProperties;

    @Test
    void testGetUsersByRole() {

        when(storeReviewProperties.getReviewerRole()).thenReturn("reviewer");
        when(userService.getUsersByRole(any(UserContext.class), eq("reviewer")))
                .thenReturn(Collections.singletonList(User.builder()
                        .id("user0")
                        .firstName("First")
                        .lastName("McLast")
                        .role("reviewer")
                        .role("manager")
                        .build()));

        List<User> users = storeReviewUserService.getReviewers(new UserContext(new CerberusUserInformation(), Collections.emptySet()));

        assertThat(users).hasSize(1);

        User user = users.get(0);
        assertThat(user.getId()).isEqualTo("user0");
        assertThat(user.getFirstName()).isEqualTo("First");
        assertThat(user.getLastName()).isEqualTo("McLast");
        assertThat(user.getFullName()).isEqualTo("First McLast");
        assertThat(user.getRoles()).containsExactlyInAnyOrder("reviewer", "manager");

        verify(storeReviewProperties).getReviewerRole();
        verify(userService).getUsersByRole(any(UserContext.class), eq("reviewer"));
    }

    @Test
    void testGetAllUsersByRole() {

        when(storeReviewProperties.getReviewerRole()).thenReturn("reviewer");
        when(userService.getAllUsersByRole(any(UserContext.class), eq("reviewer")))
                .thenReturn(List.of(User.builder()
                                .id("user0")
                                .firstName("First")
                                .lastName("McLast")
                                .role("reviewer")
                                .role("manager")
                                .build(),
                        User.builder()
                                .id("user1")
                                .firstName("First1")
                                .lastName("McLast1")
                                .role("reviewer")
                                .build()));

        List<User> users = storeReviewUserService.getAllReviewers(new UserContext(new CerberusUserInformation(), Collections.emptySet()));

        assertThat(users).hasSize(2);

        User user = users.get(0);
        assertThat(user.getId()).isEqualTo("user0");
        assertThat(user.getFirstName()).isEqualTo("First");
        assertThat(user.getLastName()).isEqualTo("McLast");
        assertThat(user.getFullName()).isEqualTo("First McLast");
        assertThat(user.getRoles()).containsExactlyInAnyOrder("reviewer", "manager");

        User user1 = users.get(1);
        assertThat(user1.getId()).isEqualTo("user1");
        assertThat(user1.getFirstName()).isEqualTo("First1");
        assertThat(user1.getLastName()).isEqualTo("McLast1");
        assertThat(user1.getFullName()).isEqualTo("First1 McLast1");
        assertThat(user1.getRoles()).containsExactlyInAnyOrder("reviewer");

        verify(storeReviewProperties).getReviewerRole();
        verify(userService).getAllUsersByRole(any(UserContext.class), eq("reviewer"));
    }

}
