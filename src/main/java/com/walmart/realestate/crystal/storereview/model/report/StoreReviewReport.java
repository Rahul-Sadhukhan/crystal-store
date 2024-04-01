package com.walmart.realestate.crystal.storereview.model.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.Year;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreReviewReport {

    private StoreReviewDetailsReport storeReviewDetails;

    private StoreDetailsReport storeDetails;

    private List<AssetReviewSummaryReport> assetReviewSummary;

    private List<AssetReviewDetailsReport> assets;

    private String user;

    private String userName;

    private Instant timestamp;

    private Year currentYear;

    private String traceId;

    private String reportName;

}
