package com.walmart.realestate.crystal.storereview.model.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreReviewReportWorkbookContainer {

    private byte[] body;

    private String reportName;

}
