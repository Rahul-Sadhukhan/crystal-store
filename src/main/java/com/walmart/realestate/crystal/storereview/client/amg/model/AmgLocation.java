package com.walmart.realestate.crystal.storereview.client.amg.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmgLocation {

    private List<AmgNote> notes;

    private String address1;

    private String address2;

    private String city;

    private String closedDate;

    private String contact;

    private String country;

    private Integer countryId;

    private Double distance;

    private String district;

    private String email;

    private String faxNumber;

    private Boolean hasInventory;

    private Double longitude;

    private Double latitude;

    private String locationId;

    private Integer locationTypeId;

    private String name;

    private String openDate;

    private String phone;

    private String floatRadius;

    private String region;

    private String shortName;

    private String squareMeasure;

    private Double squareValue;

    private String state;

    private Integer stateId;

    private String status;

    private String storeId;

    private Integer subscriberId;

    private String supplier;

    private Integer timeShiftToEST;

    private Integer timeZoneInfoId;

    private String timeZoneShortDescription;

    private String typeLabel;

    private String zip;

}
