package com.walmart.realestate.crystal.settingchangelog.controller.hypermedia;

import com.walmart.realestate.crystal.settingchangelog.controller.SettingChangeLogController;
import com.walmart.realestate.crystal.settingchangelog.model.SettingChangeLog;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SettingChangeLogAssembler implements
        SimpleRepresentationModelAssembler<SettingChangeLog> {

    @Override
    public void addLinks(EntityModel<SettingChangeLog> resource) {

        SettingChangeLog settingChangeLog = Objects.requireNonNull(resource.getContent());

        resource.add(linkTo(methodOn(SettingChangeLogController.class)
                .getSettingChangeLog(settingChangeLog.getId()))
                .withSelfRel());
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<SettingChangeLog>> resources) {
        // links added in controller
    }

}
