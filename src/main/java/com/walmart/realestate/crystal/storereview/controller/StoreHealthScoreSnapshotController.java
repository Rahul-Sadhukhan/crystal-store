package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.storereview.service.StoreHealthScoreSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/store-health-score-snapshot")
public class StoreHealthScoreSnapshotController {

    private final StoreHealthScoreSnapshotService storeHealthScoreSnapshotService;

    @PostMapping
    @PreAuthorize("hasPolicy('editStoreReview')")
    @ResponseStatus(value = HttpStatus.OK)
    public void updateStoreHealthScoreSnapshot() {
        storeHealthScoreSnapshotService.updateStoreHealthScoreSnapshot();
    }

}
