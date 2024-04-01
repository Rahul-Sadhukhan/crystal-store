package com.walmart.realestate.crystal.storereview.report;

public interface WorkbookReporter {

    String getTemplatePath();

    byte[] getWorkbook();

    String getReportName();

}
