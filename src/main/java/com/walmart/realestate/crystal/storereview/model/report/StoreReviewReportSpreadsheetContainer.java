package com.walmart.realestate.crystal.storereview.model.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreReviewReportSpreadsheetContainer {

    private List<StoreReviewReportSpreadsheetAssetItem> items;

    private String reportName;

}
