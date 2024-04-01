package com.walmart.realestate.crystal.storereview.controller;

import com.walmart.realestate.crystal.storereview.controller.hypermedia.PreventiveMaintenanceAssembler;
import com.walmart.realestate.crystal.storereview.model.PreventiveMaintenanceReadyToStart;
import com.walmart.realestate.crystal.storereview.service.PreventiveMaintenanceService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/pm-schedules")
public class PreventiveMaintenanceController {

    private final PreventiveMaintenanceService preventiveMaintenanceService;

    private final PreventiveMaintenanceAssembler preventiveMaintenanceAssembler;

    @GetMapping("/ready")
    @PageableAsQueryParam
    public PagedModel<EntityModel<PreventiveMaintenanceReadyToStart>> getPmReadyToStart(@RequestParam(required = false) String filters, @RequestParam Boolean isSuspended, @Parameter(hidden = true) Pageable pageable) {

        Page<PreventiveMaintenanceReadyToStart> pagedResponse = preventiveMaintenanceService.getPmReadyToStart(filters == null ? "" : filters, isSuspended, pageable);
        return preventiveMaintenanceAssembler.toPagedModel(pagedResponse);
    }

}
