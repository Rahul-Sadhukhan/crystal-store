package com.walmart.realestate.crystal.storereview.tenant;

import java.util.Objects;

import static com.walmart.realestate.crystal.storereview.model.Constants.DEFAULT_TENANT;

public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static String getCurrentTenant() {
        if (Objects.nonNull(CURRENT_TENANT.get()))return CURRENT_TENANT.get().toUpperCase();
        else return DEFAULT_TENANT;
    }

    public static void setCurrentTenant(String tenant) {
        CURRENT_TENANT.set(tenant);
    }

}
