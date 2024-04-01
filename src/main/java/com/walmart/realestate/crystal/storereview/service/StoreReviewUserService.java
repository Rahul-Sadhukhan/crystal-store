package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.properties.StoreReviewProperties;
import com.walmart.realestate.soteria.model.User;
import com.walmart.realestate.soteria.model.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class StoreReviewUserService {

    private final UserService userService;

    private final StoreReviewProperties storeReviewProperties;

    @Logger
    @PostFilter("hasPolicy(filterObject, 'assignStoreReview')")
    public List<User> getReviewers(UserContext userContext) {
        return userService.getUsersByRole(userContext, storeReviewProperties.getReviewerRole());
    }

    @Logger
    @PostFilter("hasPolicy(filterObject, 'viewStoreReview')")
    public List<User> getAllReviewers(UserContext userContext) {
        return userService.getAllUsersByRole(userContext, storeReviewProperties.getReviewerRole());
    }

}
