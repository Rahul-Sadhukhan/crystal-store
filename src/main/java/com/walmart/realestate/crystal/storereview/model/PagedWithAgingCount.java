package com.walmart.realestate.crystal.storereview.model;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import java.util.List;


public class PagedWithAgingCount<T> extends PagedModel<EntityModel<T>> {
    private long agingCount;

    public PagedWithAgingCount(List<EntityModel<T>> content, Pageable pageable, long totalElements, long agingCount) {
        super(content, new PageMetadata(pageable.getPageSize(), pageable.getPageNumber(), totalElements));
        this.agingCount = agingCount;
    }

    public long getAgingCount() {
        return agingCount;
    }
}