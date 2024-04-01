package com.walmart.realestate.crystal.storereview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.realestate.crystal.storereview.config.PropertiesConfig;
import com.walmart.realestate.crystal.storereview.entity.UserAccountEntity;
import com.walmart.realestate.crystal.storereview.entity.UserMembershipEntity;
import com.walmart.realestate.crystal.storereview.repository.UserRepository;
import com.walmart.realestate.soteria.model.User;
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
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {UserAccountService.class, PropertiesConfig.class,
        SoteriaProperties.class, SoteriaPolicyService.class, ObjectMapper.class})
@ActiveProfiles("test")
public class UserAccountServiceTest {

    @Autowired
    private UserAccountService userAccountService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void testGetUser() {
        when(userRepository.findById("user0")).thenReturn(Optional.of(UserAccountEntity.builder()
                .userId("user0")
                .firstName("First")
                .lastName("McLast")
                .membership(UserMembershipEntity.builder()
                        .groupName("associates")
                        .build())
                .build()));

        User user = userAccountService.getUser("user0");

        assertThat(user.getId()).isEqualTo("user0");
        assertThat(user.getFirstName()).isEqualTo("First");
        assertThat(user.getLastName()).isEqualTo("McLast");
        assertThat(user.getFullName()).isEqualTo("First McLast");
        assertThat(user.getMemberships()).containsExactly("associates");

        verify(userRepository).findById("user0");
    }

    @Test
    void testGetUserNotFound() {
        when(userRepository.findById("user0")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAccountService.getUser("user0")).isInstanceOf(NoSuchElementException.class);

        verify(userRepository).findById("user0");
    }

}
