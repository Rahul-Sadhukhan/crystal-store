package com.walmart.realestate.crystal.storereview.config;
import com.walmart.realestate.crystal.storereview.tenant.TenantContext;
import org.springframework.core.task.TaskDecorator;


public class MultiTenantTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        String tenantName = TenantContext.getCurrentTenant();
        return () -> {
            TenantContext.setCurrentTenant(tenantName);
            runnable.run();
        };
    }
}
