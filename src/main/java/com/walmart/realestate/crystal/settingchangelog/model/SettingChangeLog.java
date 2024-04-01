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

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Relation(itemRelation = "settingChangeLog", collectionRelation = "settingChangeLogs")
public class SettingChangeLog {

    @JsonProperty(access = READ_ONLY)
    private String id;

    @NotBlank
    private String referenceId;

    @JsonAlias("assetId")
    private String assetMappingId;

    @NotNull
    private Long storeNumber;

    @NotBlank
    private String setting;

    @JsonProperty(access = READ_ONLY)
    private String settingValue;

    @NotBlank
    private String oldValue;

    @NotBlank
    private String newValue;

    @NotBlank
    private String unit;

    private String notes;

    private String reason;

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
