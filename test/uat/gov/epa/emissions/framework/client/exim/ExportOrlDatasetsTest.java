package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.data.DatasetsBrowserWindow;

import java.io.File;
import java.util.Random;

import javax.swing.JTable;

public class ExportOrlDatasetsTest extends UserAcceptanceTestCase {

    private EmfConsole consoleWindow;

    public void setUp() throws Exception {
        consoleWindow = gotoConsole();
    }

    protected void tearDown() {
        // DROP the Dataset (via direct db access)
    }

    public void testShouldDisplayImportedDatasets() throws Exception {
        String value = "ORL Nonroad Inventory";
        String datasetName = value + " UAT - " + new Random().nextInt();

        doImport(datasetName, value);
        Thread.sleep(2000);// import time assumption

        DatasetsBrowserWindow browser = openDatasetsBrowser();
        assertNotNull("browser should have been opened", browser);

        JTable table = (JTable) findByName(browser, "datasetsTable");
        assertNotNull("datasets table should be displayed", table);

        assertEquals(datasetName, cell(table, table.getRowCount() - 1, 2));
    }

    private Object cell(JTable table, int row, int col) {
        return table.getValueAt(row, col);
    }

    private DatasetsBrowserWindow openDatasetsBrowser() throws Exception {
        click(consoleWindow, "manage");
        click(consoleWindow, "datasets");

        return (DatasetsBrowserWindow) findInternalFrame(consoleWindow, "datasetsBrowser");
    }

    private void doImport(String datasetName, String value) throws Exception {
        click(consoleWindow, "file");
        click(consoleWindow, "import");

        doImport("datasetTypes", datasetName, value, "arinv.nonroad.nti99d_NC.new.txt");
    }

    private void doImport(String comboBoxName, String name, String value, String filename) throws Exception {
        ImportWindow importWindow = (ImportWindow) findInternalFrame(consoleWindow, "importWindow");

        selectComboBoxItem(importWindow, comboBoxName, value);
        setTextfield(importWindow, "name", name);

        File userDir = new File(System.getProperty("user.dir"));
        String pathToFile = "test/data/orl/nc/";
        File repository = new File(userDir, pathToFile);
        setTextfield(importWindow, "folder", repository.getAbsolutePath());

        setTextfield(importWindow, "filename", filename);

        click(importWindow, "import");
    }

}
