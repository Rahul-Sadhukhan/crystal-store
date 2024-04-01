package com.walmart.realestate.crystal.storereview.model.report;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreReviewReportSpreadsheetAssetItem {

    @CsvBindByPosition(position = 0)
    @CsvBindByName(column = "Initials")
    private String initials;

    @CsvBindByPosition(position = 1)
    @CsvBindByName(column = "Store Number")
    private Long storeNumber;

    @CsvBindByPosition(position = 2)
    @CsvBindByName(column = "Case Name")
    private String caseName;

    @CsvBindByPosition(position = 3)
    @CsvBindByName(column = "Work Order")
    private String workOrder;

    @CsvBindByPosition(position = 4)
    @CsvBindByName(column = "Review Type")
    private String reviewType;

    @CsvBindByPosition(position = 5)
    @CsvBindByName(column = "SDM")
    private String serviceModel;

    @CsvBindByPosition(position = 6)
    @CsvBindByName(column = "Remote")
    private String remote;

    @CsvBindByPosition(position = 7)
    @CsvBindByName(column = "Is Reviewed")
    private String isReviewed;

    @CsvBindByPosition(position = 8)
    @CsvBindByName(column = "Pre-Review Date")
    private LocalDate preReviewDate;

    @CsvBindByPosition(position = 9)
    @CsvBindByName(column = "Pre-Review Health Score")
    private Double preReviewScore;

    @CsvBindByPosition(position = 10)
    @CsvBindByName(column = "Post-Review Date")
    private LocalDate postReviewDate;

    @CsvBindByPosition(position = 11)
    @CsvBindByName(column = "Post-Review Health Score")
    private Double postReviewScore;

    @CsvBindByPosition(position = 12)
    @CsvBindByName(column = "Post-Maintenance Date")
    private LocalDate postMaintenanceDate;

    @CsvBindByPosition(position = 13)
    @CsvBindByName(column = "Post-Maintenance Health Score")
    private Double postMaintenanceScore;

    @CsvBindByPosition(position = 14)
    @CsvBindByName(column = "Today's Date")
    private LocalDate today;

    @CsvBindByPosition(position = 15)
    @CsvBindByName(column = "Today's Date Health Score")
    private Double todayHealthScore;

    @CsvBindByPosition(position = 16)
    @CsvBindByName(column = "Asset Tag")
    private String assetTag;

    @CsvBindByPosition(position = 17)
    @CsvBindByName(column = "Tag ID")
    private String tagId;

    @CsvBindByPosition(position = 18)
    @CsvBindByName(column = "Model #")
    private String modelNo;

}
