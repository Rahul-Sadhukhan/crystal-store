package com.walmart.realestate.crystal.storereview.rbac;

import com.walmart.realestate.crystal.storereview.service.StoreReviewService;
import com.walmart.realestate.soteria.security.SoteriaObjectAdapter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.function.Function;

@Component
public class StoreReviewObjectAdapter extends SoteriaObjectAdapter<StoreReviewService> {

    public StoreReviewObjectAdapter(BeanFactory beanFactory) {
        super(beanFactory);
    }

    @Override
    public String getTargetTypeName() {
        return "StoreReview";
    }

    @Override
    public Class<StoreReviewService> getTargetClass() {
        return StoreReviewService.class;
    }

    @Override
    public Function<Serializable, Object> getAdapter(StoreReviewService bean) {
        return targetId -> bean.getStoreReview((String) targetId);
    }

}
