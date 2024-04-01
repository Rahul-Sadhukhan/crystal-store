package com.walmart.realestate.crystal.settingchangelog.controller;

import com.walmart.realestate.crystal.settingchangelog.service.SettingChangeLogDeleteService;
import com.walmart.realestate.crystal.settingchangelog.service.SettingChangeLogService;
import com.walmart.realestate.soteria.model.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/setting-change-logs")
public class SettingChangeLogDeleteController {

    private final SettingChangeLogDeleteService settingChangeLogDeleteService;

    @DeleteMapping("{settingChangeLogId}")
    @PreAuthorize("hasPolicy(#settingChangeLogId, 'SettingChangeLog', 'deleteSettingChangeLog')")
    public void deleteSettingChangeLogDelete(@PathVariable String settingChangeLogId,
                                       @RequestParam String storeReviewId,
                                       @RequestParam String storeReviewState,
                                       @AuthenticationPrincipal UserContext userContext) {
        settingChangeLogDeleteService.deleteSettingChangeLog(settingChangeLogId, storeReviewId, storeReviewState, userContext);
    }

    @PostMapping("{settingChangeLogId}/delete")
    @PreAuthorize("hasPolicy(#settingChangeLogId, 'SettingChangeLog', 'deleteSettingChangeLog')")
    public void deleteSettingChangeLogPost(@PathVariable String settingChangeLogId,
                                       @RequestParam String storeReviewId,
                                       @RequestParam String storeReviewState,
                                       @AuthenticationPrincipal UserContext userContext) {
        settingChangeLogDeleteService.deleteSettingChangeLog(settingChangeLogId, storeReviewId, storeReviewState, userContext);
    }

}
