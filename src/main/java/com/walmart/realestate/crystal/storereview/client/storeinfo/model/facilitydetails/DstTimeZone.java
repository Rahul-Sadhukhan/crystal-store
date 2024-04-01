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
public class DstTimeZone implements Serializable {

    private String dstOffset;

    private Integer rawOffset;

    private String timeZoneId;

    private String timeZoneCode;

    private String name;

}
