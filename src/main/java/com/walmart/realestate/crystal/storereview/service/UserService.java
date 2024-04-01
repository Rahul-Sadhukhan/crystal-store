package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.soteria.model.User;
import com.walmart.realestate.soteria.model.UserContext;
import com.walmart.realestate.soteria.service.SoteriaUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final SoteriaUserService soteriaUserService;

    @Logger
    @PostFilter("hasPolicy(filterObject,'createStoreReview')")
    @Cacheable(value = "usersByRole")
    public List<User> getUsersByRole(UserContext userContext, String role) {
        return soteriaUserService.getUsersByRole(userContext.getCerberusUserInformation(), role);
    }

    @Logger
    @Cacheable(value = "allUsersByRole")
    public List<User> getAllUsersByRole(UserContext userContext, String role) {
        return soteriaUserService.getUsersByRole(userContext.getCerberusUserInformation(), role);
    }

}
