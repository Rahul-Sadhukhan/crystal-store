package com.walmart.realestate.crystal.storereview.client.asset.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IotNormalization implements Serializable {

    private String uniqueId;

    private String rackName;

    private String caseName;

    private String rackCallLetter;

    private String caseCallLetter;

    private String circuitCallNumber;

    private String sensorLabel;

    private String sensorDescription;

}
