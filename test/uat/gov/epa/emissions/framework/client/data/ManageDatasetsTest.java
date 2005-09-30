package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.client.ConsoleActions;
import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.exim.ImportActions;
import gov.epa.emissions.framework.db.DbUpdate;

import java.util.Random;

import javax.swing.JTable;

public class ManageDatasetsTest extends UserAcceptanceTestCase {

    private EmfConsole console;

    private DatasetsBrowserActions browserActions;

    private ConsoleActions consoleActions;

    protected void setUp() {
        consoleActions = new ConsoleActions(this);
        console = consoleActions.open();
        browserActions = new DatasetsBrowserActions(console, this);
    }

    protected void tearDown() throws Exception {
        consoleActions.close();
        new DbUpdate().deleteAll("emf.datasets");
    }

    // FIXME:If multiple users try to do an Import simultaneously, EMF runs into
    // a database synchronization error. Only the first import succeeds, whereas
    // the rest of them would fail.
    public void testShouldDisplayImportedDatasets() throws Exception {
        String dataset = "UAT-" + new Random().nextInt();
        doImport(dataset);

        DatasetsBrowserWindow browser = browserActions.open();
        assertNotNull("browser should have been opened", browser);

        JTable table = browserActions.table();
        assertNotNull("datasets table should be displayed", table);

        assertEquals(dataset, browserActions.cell(browserActions.rowCount() - 1, 2));
    }

    public void testShouldCloseWindowOnClose() throws Exception {
        browserActions.open();
        browserActions.close();

        try {
            findInternalFrame(console, "datasetsBrowser");
        } catch (Exception e) {
            return;
        }

        fail("Datasets Browser should not be present and displayed on Close");
    }

    public void testShouldDisplayExportWindowOnClickOfExportButton() throws Exception {
        String datasetName = "UAT-" + new Random().nextInt();
        doImport(datasetName);

        browserActions.open();

        browserActions.select(0);
        browserActions.export();

        findByName(console, "exportWindow");
    }

    private void doImport(String datasetName) throws Exception {
        ImportActions importActions = new ImportActions(console, this);
        importActions.open();
        importActions.importOrlNonRoad(datasetName);
        importActions.done();
    }

}
