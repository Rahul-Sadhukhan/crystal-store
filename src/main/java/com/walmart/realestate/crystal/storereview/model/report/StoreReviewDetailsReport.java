package com.walmart.realestate.crystal.storereview.model.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static com.walmart.realestate.crystal.storereview.service.StoreReviewService.HEALTH_REVIEW;
import static com.walmart.realestate.crystal.storereview.service.StoreReviewService.PREVENTIVE_MAINTENANCE;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreReviewDetailsReport {

    private String storeReviewId;

    private String reviewer;

    private String reviewerName;

    private LocalDate storeReviewStartDate;

    private LocalDate storeReviewEndDate;

    private String serviceModel;

    private LocalDate storeReviewPostPreventiveMaintenanceDate;
    private String reviewType;

    private String refrigerantType;

    public String getReviewType() {
        if (reviewType == null || reviewType.equalsIgnoreCase(HEALTH_REVIEW))
            return "HR";
        else if (reviewType.equalsIgnoreCase(PREVENTIVE_MAINTENANCE)) {
            return "PM";
        } else return reviewType;
    }

}
