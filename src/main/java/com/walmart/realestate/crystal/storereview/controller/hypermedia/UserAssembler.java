package com.walmart.realestate.crystal.storereview.controller.hypermedia;

import com.walmart.realestate.soteria.model.User;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class UserAssembler implements SimpleRepresentationModelAssembler<User> {

    @Override
    public void addLinks(EntityModel<User> resource) {
        // no links for user to be added
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<User>> resources) {
        // links to be added in controller
    }

}
