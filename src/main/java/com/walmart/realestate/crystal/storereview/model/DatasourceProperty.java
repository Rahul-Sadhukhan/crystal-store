package com.walmart.realestate.crystal.storereview.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatasourceProperty {

    private String tenantId;

    private String url;

    private String username;

    private String password;

    private String driverClassName;

}
