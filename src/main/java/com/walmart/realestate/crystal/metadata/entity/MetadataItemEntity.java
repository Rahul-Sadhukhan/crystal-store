package com.walmart.realestate.crystal.metadata.entity;

import com.walmart.realestate.crystal.metadata.model.LocalizedValue;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@Document("metadataItem")
public class MetadataItemEntity {

    @Id
    private String id;

    @Indexed(useGeneratedName = true)
    private String type;

    @Indexed(useGeneratedName = true)
    private Integer index;

    @Indexed(useGeneratedName = true)
    private List<String> assetTypes;

    private String defaultValue;

    private List<LocalizedValue> values;

    private String minValue;

    private String maxValue;

    private String unit;

    private String offsetValue;

    private Boolean isEnabled;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedBy
    private String lastModifiedBy;

    @LastModifiedDate
    private Instant lastModifiedAt;

    @Version
    private Integer version;

}
