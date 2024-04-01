package com.walmart.realestate.crystal.settingchangelog.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@EqualsAndHashCode
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("insight")
public class InsightEntity {

    @Id
    private String id;

    private String referenceId;

    @Deprecated
    @Indexed(useGeneratedName = true)
    private Long assetId;

    @Indexed(useGeneratedName = true)
    private String assetMappingId;

    @Indexed(useGeneratedName = true)
    private Long storeNumber;

    private List<String> recommendations;

    private List<String> recommendationValues;

    private String recommendationNotes;

    private List<String> probableCauses;

    private List<String> probableCauseValues;

    private String probableCauseNotes;

    private String observation;

    private String observationValue;

    private String observationNotes;

    private String source;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private Instant createdAt;

}
