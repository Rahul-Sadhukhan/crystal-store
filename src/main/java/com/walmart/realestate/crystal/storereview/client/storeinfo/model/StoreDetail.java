package com.walmart.realestate.crystal.storereview.client.storeinfo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.walmart.realestate.crystal.storereview.client.storeinfo.model.businessunit.BusinessUnit;
import com.walmart.realestate.crystal.storereview.client.storeinfo.model.facilitydetails.FacilityDetail;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreDetail implements Serializable {

    private BusinessUnit businessUnit;

    @Singular
    private List<FacilityDetail> facilityDetails;

}
