package com.walmart.realestate.crystal.storereview.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreventiveMaintenanceReadyToStart implements Serializable {

    private Long recordIdNbr;

    private Long storeNumber;

    private String activePm;

    private String pmStatus;

    private Long fiscalYear;

    private String ccpmProjectYear;

    private LocalDate storeInventoryDate;

    private LocalDate spockCurrentCeremonyGrandOpeningDate;

    private LocalDate dateStoreWasSelectedForCcpm;

    private Long caseCleaningPriorityOrderBySubRegion;

    private String vendorVerificationCaseCleaningCompleted;

    private Long fsRegion;

    private String fsSubRegion;

    private String deliveryModel;

    private String caseCleaningVendorName;

    private Long caseCleaningTracking;

    private LocalDate caseCleaningScheduleDate;

    private LocalDate estimatedCaseCleaningCompletion;

    private Long majorRefrigerationPmWorkOrder;

    private LocalDate pmStartDate;

    private LocalDate docPrePmDeliveryDate;

    private String docReport;

    private LocalDate pmCompletionDate;

    private LocalDate postDocReportStartDate;

    private Long postDocReport;

    private String docfitTeamAssociate;

    private String hvacRManagerName;

    private String hvacRPmTechnician;

    private String hvacRTechnician;

    private String fsRegionalManagerName;

    private String fsDirector;

    private String lastModifiedBy;

    private Boolean isActive;

    private String isActiveDesc;

    private Instant dateModified;

    private Instant odsUpdatedDatetime;

    private Double latestTimeInTarget;

    private Double pmStartTimeInTarget;

    private Double pmCompletionTimeInTarget;

    private Boolean isAging;
}
