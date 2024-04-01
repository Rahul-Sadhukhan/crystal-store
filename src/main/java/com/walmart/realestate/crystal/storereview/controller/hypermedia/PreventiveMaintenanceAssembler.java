package com.walmart.realestate.crystal.storereview.controller.hypermedia;

import com.walmart.realestate.crystal.storereview.model.PagedWithAgingCount;
import com.walmart.realestate.crystal.storereview.model.PmCustomPage;
import com.walmart.realestate.crystal.storereview.model.PreventiveMaintenanceReadyToStart;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class PreventiveMaintenanceAssembler extends PagedModelAssembler<PreventiveMaintenanceReadyToStart> {

    public PreventiveMaintenanceAssembler(PagedResourcesAssembler<PreventiveMaintenanceReadyToStart> pagedResourcesAssembler) {
        super(pagedResourcesAssembler);
    }

    @Override
    public void addLinks(EntityModel<PreventiveMaintenanceReadyToStart> resource) {

    }

    @Override
    public void addLinks(CollectionModel<EntityModel<PreventiveMaintenanceReadyToStart>> resources) {

    }

    @Override
    public PagedModel<EntityModel<PreventiveMaintenanceReadyToStart>> toPagedModel(Page<PreventiveMaintenanceReadyToStart> page) {
        PagedModel<EntityModel<PreventiveMaintenanceReadyToStart>> pagedModel = super.toPagedModel(page);

        return new PagedWithAgingCount<>(new ArrayList<>(pagedModel.getContent()), page.getPageable(), page.getTotalElements(), ((PmCustomPage<?>) page).getAgingCount());
    }

}
