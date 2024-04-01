package com.walmart.realestate.crystal.storereview.client.amg.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmgNote implements Serializable {

    @JsonProperty("Id")
    private Long id;

    @JsonProperty("HeaderId")
    private Long headerId;

    @JsonProperty("Value")
    private String value;

    @JsonProperty("Header")
    private String header;

}
