package com.walmart.realestate.crystal.storereview.controller;

import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.walmart.realestate.crystal.report.util.SpreadsheetMappingStrategy;
import com.walmart.realestate.crystal.storereview.model.report.StoreReviewReport;
import com.walmart.realestate.crystal.storereview.model.report.StoreReviewReportSpreadsheetAssetItem;
import com.walmart.realestate.crystal.storereview.model.report.StoreReviewReportSpreadsheetContainer;
import com.walmart.realestate.crystal.storereview.model.report.StoreReviewReportWorkbookContainer;
import com.walmart.realestate.crystal.storereview.service.StoreReviewReportService;
import com.walmart.realestate.soteria.model.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@RequiredArgsConstructor
@RestController
@Validated
public class StoreReviewReportController {

    private final StoreReviewReportService storeReviewReportService;

    @GetMapping("store-reviews/{storeReviewId}/report-data")
    @PreAuthorize("hasPolicy(#storeReviewId, 'StoreReview', 'viewStoreReview')")
    public StoreReviewReport getStoreReviewReportData(@PathVariable String storeReviewId,
                                                      @AuthenticationPrincipal UserContext userContext) {
        return storeReviewReportService.getStoreReviewReportData(storeReviewId, userContext);
    }

    @SneakyThrows
    @GetMapping("store-reviews/{storeReviewId}/report.csv")
    @PreAuthorize("hasPolicy(#storeReviewId, 'StoreReview', 'viewStoreReview')")
    public void getStoreReviewReportSpreadsheet(@PathVariable String storeReviewId,
                                                boolean download,
                                                HttpServletResponse response) {
        StoreReviewReportSpreadsheetContainer report = storeReviewReportService.getStoreReviewReportSpreadsheet(storeReviewId);

        SpreadsheetMappingStrategy<StoreReviewReportSpreadsheetAssetItem> strategy = new SpreadsheetMappingStrategy<>();
        strategy.setType(StoreReviewReportSpreadsheetAssetItem.class);

        try (PrintWriter writer = response.getWriter()) {
            String contentDisposition = ContentDisposition.builder(download ? "attachment" : "inline")
                    .filename(report.getReportName() + ".csv")
                    .build()
                    .toString();
            response.setContentType("text/csv");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);

            new StatefulBeanToCsvBuilder<StoreReviewReportSpreadsheetAssetItem>(writer)
                    .withMappingStrategy(strategy)
                    .build()
                    .write(report.getItems());
        }
    }

    @GetMapping("store-reviews/{storeReviewId}/report.xls")
    @PreAuthorize("hasPolicy(#storeReviewId, 'StoreReview', 'viewStoreReview')")
    public ResponseEntity<byte[]> getStoreReviewReportWorkbook(@PathVariable String storeReviewId,
                                                               @AuthenticationPrincipal UserContext userContext) {
        StoreReviewReportWorkbookContainer report = storeReviewReportService.getStoreReviewReportWorkbook(storeReviewId, userContext);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentDisposition(ContentDisposition.attachment()
                .filename(report.getReportName() + ".xlsx")
                .build());
        httpHeaders.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(report.getBody());
    }

}
