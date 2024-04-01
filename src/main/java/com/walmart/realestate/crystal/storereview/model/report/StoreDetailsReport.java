package com.walmart.realestate.crystal.storereview.model.report;

import com.walmart.realestate.crystal.storereview.client.storeinfo.model.facilitydetails.LocationAddress;
import com.walmart.realestate.crystal.storereview.client.storeinfo.model.facilitydetails.LocationTimeZone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDetailsReport {

    private Long storeNumber;

    private LocationAddress storeAddress;

    private LocationTimeZone storeTimeZone;

    private HealthReport storeHealthStart;

    private HealthReport storeHealthEnd;

    private HealthReport storeHealthPreReview;

    private HealthReport storeHealthPostReview;

    private HealthReport storeHealthPostMaintenance;

    private HealthReport storeHealthPostPreventiveMaintenance;

    private HealthReport storeHealthReportDownload;

}
