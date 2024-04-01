package com.walmart.realestate.crystal.storereview.report;

public class MockWorksheetReporter implements WorkbookReporter {

    @Override
    public String getTemplatePath() {
        return "path";
    }

    @Override
    public byte[] getWorkbook() {
        return new byte[]{21};
    }

    @Override
    public String getReportName() {
        return "Health Review";
    }

}
