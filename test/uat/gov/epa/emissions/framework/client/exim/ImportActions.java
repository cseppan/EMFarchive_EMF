package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.UserAcceptanceTestCase;

import java.io.File;

public class ImportActions {

    private EmfConsole console;

    private UserAcceptanceTestCase testcase;

    public ImportActions(EmfConsole consoleWindow, UserAcceptanceTestCase testcase) {
        this.console = consoleWindow;
        this.testcase = testcase;
    }

    public void doImport(String datasetName, String value) throws Exception {
        testcase.click(console, "file");
        testcase.click(console, "import");

        doImport("datasetTypes", datasetName, value, "arinv.nonroad.nti99d_NC.new.txt");
        Thread.sleep(2000);// import time assumption
    }

    private void doImport(String comboBoxName, String name, String value, String filename) throws Exception {
        ImportWindow importWindow = (ImportWindow) testcase.findInternalFrame(console, "importWindow");

        testcase.selectComboBoxItem(importWindow, comboBoxName, value);
        testcase.setTextfield(importWindow, "name", name);

        File userDir = new File(System.getProperty("user.dir"));
        String pathToFile = "test/data/orl/nc/";
        File repository = new File(userDir, pathToFile);
        testcase.setTextfield(importWindow, "folder", repository.getAbsolutePath());

        testcase.setTextfield(importWindow, "filename", filename);

        testcase.click(importWindow, "import");
    }

}
