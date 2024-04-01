package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.storereview.client.asset.model.StoreReviewQuery;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.StoreReviewPagedEntity;
import com.walmart.realestate.crystal.storereview.client.storeinfo.model.StoreDetail;
import com.walmart.realestate.crystal.storereview.service.StoreService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
public class StoreController {

    private final StoreService storeService;

    @GetMapping("store-info")
    @PreAuthorize("hasPolicy('viewStoreInfo')")
    public StoreDetail getStoreInfo(@RequestParam Long storeNumber) {
        return storeService.getStoreInfo(storeNumber);
    }

    @GetMapping(value = "store-plan/{storeNumber}", produces = MediaType.TEXT_HTML_VALUE)
    public String getStorePlan(@PathVariable Long storeNumber) {
        return storeService.getStorePlan(storeNumber);
    }

    @GetMapping("stores")
    @PageableAsQueryParam
    public PagedModel<EntityModel<StoreReviewPagedEntity>> getStoreList(@Valid StoreReviewQuery query,
                                                                        @Parameter(hidden = true) Pageable pageable) {
        return storeService.getStoreList(query, pageable);
    }
}
