package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.client.DbUpdate;
import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.exim.ImportActions;

import java.util.Random;

import javax.swing.JTable;

public class ManageDatasetsTest extends UserAcceptanceTestCase {

    private EmfConsole consoleWindow;

    private DatasetsBrowserActions browserActions;

    public void setUp() throws Exception {
        browserActions = new DatasetsBrowserActions(this);
        consoleWindow = browserActions.openConsole();
    }

    public void testShouldDisplayImportedDatasets() throws Exception {
        String datasetName = "ORL Nonroad Inventory UAT - " + new Random().nextInt();
        try {
            doShouldDisplayImportedDatasets(datasetName);
        } finally {
            DbUpdate update = new DbUpdate();
            update.delete("datasets", "name", datasetName);
        }
    }

    private void doShouldDisplayImportedDatasets(String datasetName) throws Exception, InterruptedException {
        ImportActions importActions = new ImportActions(consoleWindow, this);
        importActions.doImport(datasetName, "ORL Nonroad Inventory");

        DatasetsBrowserWindow browser = browserActions.open();
        assertNotNull("browser should have been opened", browser);

        JTable table = browserActions.table();
        assertNotNull("datasets table should be displayed", table);

        assertEquals(datasetName, browserActions.cell(table.getRowCount() - 1, 2));
    }

    public void testShouldCloseWindowOnClose() throws Exception {
        browserActions.open();
        browserActions.close();

        try {
            findInternalFrame(consoleWindow, "datasetsBrowser");
        } catch (Exception e) {
            return;
        }

        fail("Datasets Browser should not be present and displayed on Close");
    }

}
