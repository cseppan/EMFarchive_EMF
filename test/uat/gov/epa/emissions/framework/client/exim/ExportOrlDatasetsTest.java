package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.DbUpdate;
import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.data.DatasetsBrowserActions;

import java.util.Random;

public class ExportOrlDatasetsTest extends UserAcceptanceTestCase {

    private EmfConsole consoleWindow;

    private DatasetsBrowserActions browserActions;

    public void setUp() throws Exception {
        browserActions = new DatasetsBrowserActions(this);
        consoleWindow = browserActions.openConsole();
    }

    public void testShouldDisplayExportWindowOnClickOfExportInDatasetsBrowser() throws Exception {
        String datasetName = "ORL Nonroad Inventory" + " UAT - " + new Random().nextInt();
        try {
            doShouldDisplayExportWindowOnClickOfExportInDatasetsBrowser(datasetName);
        } finally {
            DbUpdate update = new DbUpdate();
            update.delete("datasets", "name", datasetName);
        }
    }

    private void doShouldDisplayExportWindowOnClickOfExportInDatasetsBrowser(String datasetName) throws Exception {
        ImportActions importActions = new ImportActions(consoleWindow, this);
        importActions.doImport(datasetName, "ORL Nonroad Inventory");

        browserActions.open();
    }

}
