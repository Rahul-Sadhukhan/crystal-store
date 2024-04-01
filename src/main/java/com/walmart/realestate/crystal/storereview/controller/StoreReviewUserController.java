package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.storereview.controller.hypermedia.StoreReviewUserAssembler;
import com.walmart.realestate.crystal.storereview.service.StoreReviewUserService;
import com.walmart.realestate.soteria.model.User;
import com.walmart.realestate.soteria.model.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class StoreReviewUserController {

    private final StoreReviewUserService storeReviewUserService;

    private final StoreReviewUserAssembler storeReviewUserAssembler;

    @GetMapping("store-reviewers")
    public CollectionModel<EntityModel<User>> getReviewers(@AuthenticationPrincipal UserContext userContext) {
        return storeReviewUserAssembler.toCollectionModel(storeReviewUserService.getReviewers(userContext));
    }

    @GetMapping("store-reviewers/all")
    public CollectionModel<EntityModel<User>> getAllReviewers(@AuthenticationPrincipal UserContext userContext) {
        return storeReviewUserAssembler.toCollectionModel(storeReviewUserService.getAllReviewers(userContext));
    }

}
