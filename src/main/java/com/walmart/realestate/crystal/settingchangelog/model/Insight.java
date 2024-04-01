package com.walmart.realestate.crystal.settingchangelog.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Relation(itemRelation = "insight", collectionRelation = "insights")
public class Insight {

    @JsonProperty(access = READ_ONLY)
    private String id;

    @NotBlank
    private String referenceId;

    @JsonAlias("assetId")
    private String assetMappingId;

    @NotNull
    private Long storeNumber;

    private List<String> recommendations;

    @JsonProperty(access = READ_ONLY)
    private List<String> recommendationValues;

    private String recommendationNotes;

    private List<String> probableCauses;

    @JsonProperty(access = READ_ONLY)
    private List<String> probableCauseValues;

    private String probableCauseNotes;

    @NotBlank
    private String observation;

    @JsonProperty(access = READ_ONLY)
    private String observationValue;

    private String observationNotes;

    @JsonProperty(access = READ_ONLY)
    private String source;

    @JsonProperty(access = READ_ONLY)
    private String createdBy;

    @JsonProperty(access = READ_ONLY)
    private String createdByName;

    @JsonProperty(access = READ_ONLY)
    private Instant createdAt;

    @Deprecated
    public String getAssetId() {
        return assetMappingId;
    }

}
