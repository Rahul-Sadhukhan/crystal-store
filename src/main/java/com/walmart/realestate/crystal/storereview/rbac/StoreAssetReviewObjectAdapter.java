package com.walmart.realestate.crystal.storereview.rbac;

import com.walmart.realestate.crystal.storereview.service.StoreAssetReviewService;
import com.walmart.realestate.soteria.security.SoteriaObjectAdapter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.function.Function;

@Component
public class StoreAssetReviewObjectAdapter extends SoteriaObjectAdapter<StoreAssetReviewService> {

    public StoreAssetReviewObjectAdapter(BeanFactory beanFactory) {
        super(beanFactory);
    }

    @Override
    public String getTargetTypeName() {
        return "StoreAssetReview";
    }

    @Override
    public Class<StoreAssetReviewService> getTargetClass() {
        return StoreAssetReviewService.class;
    }

    @Override
    public Function<Serializable, Object> getAdapter(StoreAssetReviewService bean) {
        return targetId -> bean.getStoreAssetReview((String) targetId);
    }

}
