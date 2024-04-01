package com.walmart.realestate.crystal.settingchangelog.controller.hypermedia;

import com.walmart.realestate.crystal.settingchangelog.controller.InsightController;
import com.walmart.realestate.crystal.settingchangelog.model.Insight;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class InsightAssembler implements
        SimpleRepresentationModelAssembler<Insight> {

    @Override
    public void addLinks(EntityModel<Insight> resource) {

        Insight insight = Objects.requireNonNull(resource.getContent());

        resource.add(linkTo(methodOn(InsightController.class)
                .getInsight(insight.getId()))
                .withSelfRel());
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<Insight>> resources) {
        // links added in controller
    }

}
