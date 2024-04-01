package com.walmart.realestate.crystal.storereview.report;

import com.walmart.core.realestate.cerberus.exception.BadRequestException;
import com.walmart.realestate.crystal.metadata.properties.MetadataProperties;
import com.walmart.realestate.crystal.storereview.model.report.StoreReviewReport;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

@RequiredArgsConstructor
@Component
@Scope(SCOPE_SINGLETON)
public class WorkbookReporterFactory {

    private final MetadataProperties metadataProperties;

    private final WorkbookUtil workbookUtil;


    public WorkbookReporter getWorkbookReporter(StoreReviewReport storeReviewReport) {
        if (storeReviewReport.getStoreReviewDetails().getReviewType().equalsIgnoreCase("HR")) {
            return new HealthReviewSummaryWorkbookReporter(metadataProperties, storeReviewReport, workbookUtil);
        } else if (storeReviewReport.getStoreReviewDetails().getReviewType().equalsIgnoreCase("PM") && storeReviewReport.getStoreReviewDetails().getStoreReviewPostPreventiveMaintenanceDate() == null) {
            return new PrePMSummaryWorkbookReporter(metadataProperties, storeReviewReport, workbookUtil);
        } else if (storeReviewReport.getStoreReviewDetails().getReviewType().equalsIgnoreCase("PM") && storeReviewReport.getStoreReviewDetails().getStoreReviewPostPreventiveMaintenanceDate() != null) {
            return new PostPMSummaryWorkbookReporter(metadataProperties, storeReviewReport, workbookUtil);
        } else throw new BadRequestException("Invalid review type!");
    }

}
