package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.ConsoleActions;
import gov.epa.emissions.framework.client.DbUpdate;
import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.UserAcceptanceTestCase;

import java.util.Random;

import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.ListModel;

import abbot.tester.JComboBoxTester;

public class ImportTest extends UserAcceptanceTestCase {

    private ImportWindow importWindow;

    private EmfConsole console;

    private ImportActions importActions;

    protected void setUp() throws Exception {
        ConsoleActions consoleActions = new ConsoleActions(this);
        console = consoleActions.open();

        importActions = new ImportActions(console, this);
        importWindow = importActions.open();
        assertNotNull(importWindow);
    }

    public void tearDown() throws Exception {
        importActions.done();

        JInternalFrame importWindow = importActions.find();
        assertFalse("Import Window should be hidden from view", importWindow.isVisible());
    }

    public void testShouldShowAtleastFourORLDatasetTypesAsOptions() throws Exception {
        JComboBox comboBox = findComboBox(importWindow, "datasetTypes");

        assertNotNull(comboBox);

        ListModel model = findComboBoxList(comboBox);
        assertTrue("Should have atleast 4 ORL types", model.getSize() >= 4);
    }

    public void testShouldImportORLNonRoad() throws Exception {
        String name = datasetName("ORL Nonroad Inventory");
        try {
            importActions.importOrlNonRoad(name);
        } finally {
            DbUpdate update = new DbUpdate();
            update.delete("datasets", "name", name);
        }
    }

    public void testShouldImportORLNonPoint() throws Exception {
        String name = datasetName("ORL NonPoint Inventory");
        try {
            importActions.importOrlNonPoint(name);
        } finally {
            DbUpdate update = new DbUpdate();
            update.delete("datasets", "name", name);
        }
    }

    public void testShouldImportORLPoint() throws Exception {
        String name = datasetName("ORL Point Inventory");
        try {
            importActions.importOrlPoint(name);
        } finally {
            DbUpdate update = new DbUpdate();
            update.delete("datasets", "name", name);
        }
    }

    public void testShouldImportORLOnRoadMobile() throws Exception {
        String name = datasetName("ORL Onroad Inventory");
        try {
            importActions.importOrlOnRoadMobile(name);
        } finally {
            DbUpdate update = new DbUpdate();
            update.delete("datasets", "name", name);
        }
    }

    public void TODO_testShouldFailIfImportIsAttemptedWithDuplicateName() throws Exception {
        String name = "ORL Onroad Inventory" + " UAT - " + new Random().nextInt();
        importActions.doImport("ORL Onroad Inventory", name, "nti99.NC.onroad.SMOKE.txt");

        importActions.doImport("ORL Onroad Inventory", name, "nti99.NC.onroad.SMOKE.txt");
        // TODO: assert failure - check the status messages
    }

    private String datasetName(String value) {
        return value + " UAT - " + new Random().nextInt();
    }

    public void testShouldShowErrorMessageIfNameIsUnspecified() throws Exception {
        importActions.selectDatasetType("ORL Point Inventory");

        importActions.clickImport();

        assertErrorMessage(importWindow, "Dataset Name should be specified");
    }

    public void testShouldShowErrorMessageIfFilenameIsUnspecified() throws Exception {
        importActions.selectDatasetType("ORL Point Inventory");
        importActions.setName(" UAT - " + new Random().nextInt());
        importActions.setFolder("/folder/name");

        importActions.clickImport();

        assertErrorMessage(importWindow, "Filename should be specified");
    }

    protected ListModel findComboBoxList(JComboBox comboBox) {
        JComboBoxTester tester = new JComboBoxTester();
        tester.actionClick(comboBox);

        JList options = tester.findComboList(comboBox);
        return options.getModel();
    }

}
