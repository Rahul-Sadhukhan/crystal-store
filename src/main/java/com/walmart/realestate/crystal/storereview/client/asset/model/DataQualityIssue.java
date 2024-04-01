package com.walmart.realestate.crystal.storereview.client.asset.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataQualityIssue implements Serializable {

    private Integer ruleId;

    private String ruleDescription;

    private String ruleGroup;

    private List<String> targetAttributes;

}
