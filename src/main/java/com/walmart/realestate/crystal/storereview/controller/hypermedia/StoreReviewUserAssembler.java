package com.walmart.realestate.crystal.storereview.controller.hypermedia;

import com.walmart.realestate.crystal.storereview.controller.StoreReviewUserController;
import com.walmart.realestate.soteria.model.User;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class StoreReviewUserAssembler extends UserAssembler {

    @Override
    public void addLinks(CollectionModel<EntityModel<User>> resources) {
        resources.getContent().stream()
                .findFirst()
                .map(EntityModel::getContent)
                .ifPresent(user -> resources.add(linkTo(methodOn(StoreReviewUserController.class)
                        .getReviewers(null))
                        .withSelfRel()));
    }

}
