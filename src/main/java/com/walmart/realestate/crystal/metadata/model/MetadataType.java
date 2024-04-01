package com.walmart.realestate.crystal.metadata.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.server.core.Relation;

import javax.validation.constraints.NotBlank;
import java.time.Instant;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Builder
@Data
@Relation(itemRelation = "metadataType", collectionRelation = "metadataTypes")
public class MetadataType {

    @NotBlank
    private String id;

    @NotBlank
    private String name;

    @JsonProperty(access = READ_ONLY)
    private String createdBy;

    @JsonProperty(access = READ_ONLY)
    private Instant createdAt;

    @JsonProperty(access = READ_ONLY)
    private String lastModifiedBy;

    @JsonProperty(access = READ_ONLY)
    private Instant lastModifiedAt;

}
