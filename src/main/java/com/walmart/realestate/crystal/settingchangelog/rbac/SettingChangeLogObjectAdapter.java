package com.walmart.realestate.crystal.settingchangelog.rbac;

import com.walmart.realestate.crystal.settingchangelog.service.SettingChangeLogService;
import com.walmart.realestate.soteria.security.SoteriaObjectAdapter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.function.Function;

@Component
public class SettingChangeLogObjectAdapter extends SoteriaObjectAdapter<SettingChangeLogService> {

    public SettingChangeLogObjectAdapter(BeanFactory beanFactory) {
        super(beanFactory);
    }

    @Override
    public String getTargetTypeName() {
        return "SettingChangeLog";
    }

    @Override
    public Class<SettingChangeLogService> getTargetClass() {
        return SettingChangeLogService.class;
    }

    @Override
    public Function<Serializable, Object> getAdapter(SettingChangeLogService bean) {
        return targetId -> bean.getSettingChangeLog((String) targetId);
    }

}
