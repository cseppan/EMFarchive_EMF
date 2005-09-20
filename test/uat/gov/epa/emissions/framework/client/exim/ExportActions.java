package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.UserAcceptanceTestCase;

public class ExportActions {

    private UserAcceptanceTestCase testcase;

    private ExportWindow exportWindow;

    public ExportActions(ExportWindow exportWindow, UserAcceptanceTestCase testcase) {
        this.exportWindow = exportWindow;
        this.testcase = testcase;
    }

    public void setFolder(String folder) throws Exception {
        testcase.setTextfield(exportWindow, "folder", folder);
    }

    public void clickExport() throws Exception {
        testcase.click(exportWindow, "export");
    }

    public void setOverwriteFalse() throws Exception {
        testcase.click(exportWindow, "overwrite");// by default, it's true
    }

}
