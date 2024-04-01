package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.entity.UserAccountEntity;
import com.walmart.realestate.crystal.storereview.entity.UserMembershipEntity;
import com.walmart.realestate.crystal.storereview.repository.UserRepository;
import com.walmart.realestate.soteria.model.User;
import com.walmart.realestate.soteria.util.SoteriaUtil;
import de.qaware.tools.collectioncacheableforspring.CollectionCacheable;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserAccountService {

    private final UserRepository userRepository;

    @Logger
    @Cacheable("users")
    public User getUser(String userId) {
        return userRepository.findById(userId)
                .map(this::toUser)
                .orElseThrow(NoSuchElementException::new);
    }

    @Logger
    @Cacheable(value = "users", unless = "#result == null")
    public User findUser(String userId) {
        return userRepository.findById(userId)
                .map(this::toUser)
                .orElse(null);
    }

    @Logger
    @CollectionCacheable("users")
    public Map<String, User> findByLoginIds(List<String> loginIds) {
        Set<String> userIds = loginIds.stream()
                .map(SoteriaUtil::buildPartialUserId)
                .map(partialUserId -> partialUserId + ".wal-mart.com")
                .collect(Collectors.toSet());
        return toUsers(userRepository.findAllById(userIds)).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    @Logger
    public List<User> findByMemberships(List<String> memberships) {
        return toUsers(userRepository.findDistinctByMembershipsGroupNameIn(memberships));
    }

    private List<User> toUsers(List<UserAccountEntity> entities) {
        return Optional.ofNullable(entities)
                .orElseGet(Collections::emptyList).stream()
                .map(this::toUser)
                .collect(Collectors.toList());
    }

    private User toUser(UserAccountEntity entity) {
        List<String> memberships = entity.getMemberships().stream()
                .map(UserMembershipEntity::getGroupName)
                .collect(Collectors.toList());

        return User.builder()
                .id(entity.getUserId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .memberships(memberships)
                .build();
    }

}
