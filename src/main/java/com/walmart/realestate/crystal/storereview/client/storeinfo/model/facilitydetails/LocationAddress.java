package com.walmart.realestate.crystal.storereview.client.storeinfo.model.facilitydetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationAddress implements Serializable {

    private String addressLine1;

    private String city;

    private String county;

    private String state;

    private Country country;

    private String postalCode;

}
