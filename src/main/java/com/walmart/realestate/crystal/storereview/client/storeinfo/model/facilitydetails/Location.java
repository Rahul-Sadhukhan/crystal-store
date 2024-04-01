package com.walmart.realestate.crystal.storereview.client.storeinfo.model.facilitydetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Location implements Serializable {

    private LocationTimeZone locationTimeZone;

    @Singular("locationAddress")
    private List<LocationAddress> locationAddress;

}
