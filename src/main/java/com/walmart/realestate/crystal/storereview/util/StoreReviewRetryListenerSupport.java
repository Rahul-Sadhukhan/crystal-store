package com.walmart.realestate.crystal.storereview.util;

import com.walmart.realestate.crystal.storereview.exception.StoreReviewRetryFailedException;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;

import java.util.Objects;

public class StoreReviewRetryListenerSupport extends RetryListenerSupport {

    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        super.onError(context, callback, throwable);
        if (context.hasAttribute(RetryContext.EXHAUSTED) && Objects.nonNull(throwable)) {
            throw new StoreReviewRetryFailedException(throwable.getMessage());
        }
    }

}
