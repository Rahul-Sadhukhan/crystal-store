package com.walmart.realestate.crystal.storereview.model;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class PmCustomPage<T> extends PageImpl<T> {
    private long agingCount;

    public PmCustomPage(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public PmCustomPage(List<T> content, Pageable pageable, long total, long agingCount) {
        super(content, pageable, total);
        this.agingCount = agingCount;
    }

    public long getAgingCount() {
        return agingCount;
    }

    public void setAgingCount(long agingCount) {
        this.agingCount = agingCount;
    }
}