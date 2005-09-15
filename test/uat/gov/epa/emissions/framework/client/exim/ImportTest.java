package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.UserAcceptanceTestCase;

import java.io.File;
import java.util.Random;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListModel;

import abbot.tester.JComboBoxTester;

public class ImportTest extends UserAcceptanceTestCase {

    private ImportWindow importWindow;

    private EmfConsole consoleWindow;

    protected void setUp() throws Exception {
        consoleWindow = gotoConsole();

        click(consoleWindow, "file");
        click(consoleWindow, "import");

        importWindow = (ImportWindow) findInternalFrame(consoleWindow, "importWindow");
        assertNotNull(importWindow);
    }

    public void tearDown() throws Exception {
        click(importWindow, "done");

        try {
            findInternalFrame(consoleWindow, "importWindow");
        } catch (Exception e) {
            return;
        }

        fail("should have closed the Import window on clicking Done");
    }

    public void testShouldShowAtleastFourORLDatasetTypesAsOptions() throws Exception {
        JComboBox comboBox = findComboBox(importWindow, "datasetTypes");

        assertNotNull(comboBox);

        ListModel model = findComboBoxList(comboBox);
        assertTrue("Should have atleast 4 ORL types", model.getSize() >= 4);
    }

    public void testShouldImportORLNonRoad() throws Exception {
        doImport("datasetTypes", "ORL Nonroad Inventory", "arinv.nonroad.nti99d_NC.new.txt");
    }

    public void testShouldImportORLNonPoint() throws Exception {
        doImport("datasetTypes", "ORL Nonpoint Inventory", "arinv.nonpoint.nti99_NC.txt");
    }

    public void testShouldImportORLPoint() throws Exception {
        doImport("datasetTypes", "ORL Point Inventory", "ptinv.nti99_NC.txt");
    }

    public void testShouldImportORLOnRoadMobile() throws Exception {
        doImport("datasetTypes", "ORL Onroad Inventory", "nti99.NC.onroad.SMOKE.txt");
    }

    public void TODO_testShouldFailIfImportIsAttemptedWithDuplicateName() throws Exception {
        String name = "ORL Onroad Inventory" + " UAT - " + new Random().nextInt();
        doImport("datasetTypes", name, "ORL Onroad Inventory", "nti99.NC.onroad.SMOKE.txt");

        doImport("datasetTypes", name, "ORL Onroad Inventory", "nti99.NC.onroad.SMOKE.txt");
        // TODO: assert failure - check the status messages
    }

    private void doImport(String comboBoxName, String value, String filename) throws Exception {
        doImport(comboBoxName, value + " UAT - " + new Random().nextInt(), value, filename);
    }

    private void doImport(String comboBoxName, String name, String value, String filename) throws Exception {
        selectComboBoxItem(importWindow, comboBoxName, value);
        setTextfield(importWindow, "name", name);

        File userDir = new File(System.getProperty("user.dir"));
        String pathToFile = "test/data/orl/nc/";
        File repository = new File(userDir, pathToFile);
        setTextfield(importWindow, "folder", repository.getAbsolutePath());

        setTextfield(importWindow, "filename", filename);

        clickImport();

        // TODO: assert status messages
    }

    public void testShouldShowErrorMessageIfNameIsUnspecified() throws Exception {
        selectComboBoxItem(importWindow, "datasetTypes", "ORL Point Inventory");

        clickImport();

        assertErrorMessage(importWindow, "Dataset Name should be specified");
    }

    public void testShouldShowErrorMessageIfFilenameIsUnspecified() throws Exception {
        selectComboBoxItem(importWindow, "datasetTypes", "ORL Point Inventory");
        setTextfield(importWindow, "name", " UAT - " + new Random().nextInt());
        setTextfield(importWindow, "folder", "/folder/name");

        clickImport();

        assertErrorMessage(importWindow, "Filename should be specified");
    }

    private void clickImport() throws Exception {
        click(importWindow, "import");
    }

    protected ListModel findComboBoxList(JComboBox comboBox) {
        JComboBoxTester tester = new JComboBoxTester();
        tester.actionClick(comboBox);

        JList options = tester.findComboList(comboBox);
        return options.getModel();
    }

}
