package com.walmart.realestate.crystal.storereview.controller.hypermedia;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;

@AllArgsConstructor
public abstract class PagedModelAssembler<T> implements SimpleRepresentationModelAssembler<T> {

    private final PagedResourcesAssembler<T> pagedResourcesAssembler;

    public PagedModel<EntityModel<T>> toPagedModel(Page<T> page) {
        return pagedResourcesAssembler.toModel(page, this);
    }

}
