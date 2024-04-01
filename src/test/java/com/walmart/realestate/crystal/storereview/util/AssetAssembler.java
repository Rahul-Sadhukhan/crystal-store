package com.walmart.realestate.crystal.storereview.util;

import com.walmart.realestate.crystal.storereview.client.asset.model.Asset;
import com.walmart.realestate.crystal.storereview.controller.hypermedia.PagedModelAssembler;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

@TestComponent
public class AssetAssembler extends PagedModelAssembler<Asset> {

    public AssetAssembler(PagedResourcesAssembler<Asset> pagedResourcesAssembler) {
        super(pagedResourcesAssembler);
    }

    @Override
    public void addLinks(EntityModel<Asset> resource) {
        // links added in controller
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<Asset>> resources) {
        // links added in controller
    }

}
