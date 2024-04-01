package com.walmart.realestate.crystal.settingchangelog.controller;

import com.walmart.realestate.crystal.settingchangelog.controller.hypermedia.SettingChangeLogAssembler;
import com.walmart.realestate.crystal.settingchangelog.model.SettingChangeLog;
import com.walmart.realestate.crystal.settingchangelog.service.SettingChangeLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RequiredArgsConstructor
@RestController
@RequestMapping("/setting-change-logs")
public class SettingChangeLogController {

    private final SettingChangeLogService settingChangeLogService;

    private final SettingChangeLogAssembler settingChangeLogAssembler;

    @PostMapping
    @PreAuthorize("hasPolicy(#settingChangeLog, 'createStoreReview')")
    public EntityModel<SettingChangeLog> createSettingChangeLog(@RequestBody @Valid SettingChangeLog settingChangeLog) {
        return settingChangeLogAssembler.toModel(settingChangeLogService.createSettingChangeLog(settingChangeLog));
    }

    @PostMapping(value = "/bulk", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPolicy('createStoreReview')")
    public CollectionModel<EntityModel<SettingChangeLog>> createSettingChangeLogs(@RequestBody @Valid List<SettingChangeLog> settingChangeLogList) {
        return settingChangeLogAssembler.toCollectionModel(settingChangeLogService.createSettingChangeLogs(settingChangeLogList));
    }

    @GetMapping
    public CollectionModel<EntityModel<SettingChangeLog>> getSettingChangeLogs(@RequestParam(required = false) Long storeNumber,
                                                                               @RequestParam(required = false) String assetId) {
        return settingChangeLogAssembler.toCollectionModel(settingChangeLogService.getSettingChangeLogs(storeNumber, assetId))
                .add(linkTo(methodOn(SettingChangeLogController.class)
                        .getSettingChangeLogs(storeNumber, assetId))
                        .withSelfRel());
    }

    @GetMapping("{settingChangeLogId}")
    @PostAuthorize("hasPolicy(returnObject.getContent(), 'viewStoreReview')")
    public EntityModel<SettingChangeLog> getSettingChangeLog(@PathVariable String settingChangeLogId) {
        return settingChangeLogAssembler.toModel(settingChangeLogService.getSettingChangeLog(settingChangeLogId));
    }

}
