package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.UserAcceptanceTestCase;

import java.io.File;

public class ImportActions {

    private EmfConsole console;

    private UserAcceptanceTestCase testcase;

    private ImportWindow importWindow;

    public ImportActions(EmfConsole console, UserAcceptanceTestCase testcase) {
        this.console = console;
        this.testcase = testcase;
    }

    public void importOrlNonRoad(String name) throws Exception {
        doImport("ORL Nonroad Inventory", name, "arinv.nonroad.nti99d_NC.new.txt");
    }

    public void importOrlNonPoint(String name) throws Exception {
        doImport("ORL Nonpoint Inventory", name, "arinv.nonpoint.nti99_NC.txt");
    }

    public void importOrlPoint(String name) throws Exception {
        doImport("ORL Point Inventory", name, "ptinv.nti99_NC.txt");
    }

    public void importOrlOnRoadMobile(String name) throws Exception {
        doImport("ORL Onroad Inventory", name, "nti99.NC.onroad.SMOKE.txt");
    }

    public ImportWindow open() throws Exception {
        testcase.click(console, "file");
        testcase.click(console, "import");

        importWindow = find();
        return importWindow;
    }

    public void doImport(String type, String name, String filename) throws Exception {
        testcase.selectComboBoxItem(importWindow, "datasetTypes", type);
        testcase.setText(importWindow, "name", name);

        File userDir = new File(System.getProperty("user.dir"));
        String pathToFile = "test/data/orl/nc/";
        File repository = new File(userDir, pathToFile);
        testcase.setText(importWindow, "folder", repository.getAbsolutePath());

        testcase.setText(importWindow, "filename", filename);

        testcase.click(importWindow, "import");

        Thread.sleep(4000);// import time assumption
    }

    public void done() throws Exception {
        testcase.click(importWindow, "done");
    }

    public ImportWindow find() throws Exception {
        return (ImportWindow) testcase.findInternalFrame(console, "importWindow");
    }

    public void selectDatasetType(String value) throws Exception {
        testcase.selectComboBoxItem(importWindow, "datasetTypes", value);
    }

    public void clickImport() throws Exception {
        testcase.click(importWindow, "import");
    }

    public void setName(String name) throws Exception {
        testcase.setText(importWindow, "name", name);
    }

    public void setFolder(String folder) throws Exception {
        testcase.setText(importWindow, "folder", folder);
    }

}
