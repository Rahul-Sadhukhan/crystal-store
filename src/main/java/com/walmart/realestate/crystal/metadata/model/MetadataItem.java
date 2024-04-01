package com.walmart.realestate.crystal.metadata.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.server.core.Relation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Data
@Builder
@Relation(itemRelation = "metadataItem", collectionRelation = "metadataItems")
public class MetadataItem {

    @NotNull
    private String id;

    @NotBlank
    private String metadataType;

    private List<String> assetTypes;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String offsetValue;

    private Integer index;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String defaultValue;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<LocalizedValue> values;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String minValue;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String maxValue;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String unit;

    private Boolean isEnabled;

    @JsonProperty(access = READ_ONLY)
    private String createdBy;

    @JsonProperty(access = READ_ONLY)
    private Instant createdAt;

    @JsonProperty(access = READ_ONLY)
    private String lastModifiedBy;

    @JsonProperty(access = READ_ONLY)
    private Instant lastModifiedAt;

}
