package com.walmart.realestate.crystal.storereview.model.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightReport {

    private String id;

    private String referenceId;

    private String assetMappingId;

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

    private LocalDateTime createdAt;

}
