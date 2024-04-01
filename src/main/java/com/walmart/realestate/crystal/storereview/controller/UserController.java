package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.controller.hypermedia.UserAssembler;
import com.walmart.realestate.crystal.storereview.service.UserService;
import com.walmart.realestate.soteria.model.User;
import com.walmart.realestate.soteria.model.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RequiredArgsConstructor
@RestController
@RequestMapping("users")
public class UserController {

    private final UserService userService;

    private final UserAssembler userAssembler;

    @Logger
    @GetMapping
    public CollectionModel<EntityModel<User>> getUsersByRole(@RequestParam String role, @AuthenticationPrincipal UserContext userContext) {
        return userAssembler.toCollectionModel(userService.getUsersByRole(userContext, role))
                .add(linkTo(methodOn(UserController.class).getUsersByRole(role, null))
                        .withSelfRel());
    }

}
