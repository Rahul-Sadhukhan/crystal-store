package com.walmart.realestate.crystal.storereview.client.estr.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@EqualsAndHashCode
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstrState {

    @JsonProperty("stateId")
    private String state;

    @JsonProperty("flowId")
    private String flow;

}
