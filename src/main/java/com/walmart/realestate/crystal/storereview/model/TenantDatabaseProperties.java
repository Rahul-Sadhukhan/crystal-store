package com.walmart.realestate.crystal.storereview.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantDatabaseProperties {

    private DatasourcePropertyMongo storeReview;

    private DatasourcePropertyMongo settingChangeLog;

    private DatasourcePropertyMongo metadata;

    private DatasourceProperty azure;

}
