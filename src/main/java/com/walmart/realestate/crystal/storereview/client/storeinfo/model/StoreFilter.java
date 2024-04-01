package com.walmart.realestate.crystal.storereview.client.storeinfo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreFilter implements Serializable {

    private String countryCode;

    private Long storeNumber;

    public String getStoreNumberQuery() {
        return "number:" + this.storeNumber;
    }

    public String getCountryCodeQuery() {
        return "countryCode:(" + this.countryCode + ")";
    }

}
