package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.client.ConsoleActions;
import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.data.DatasetsBrowserActions;
import gov.epa.emissions.framework.client.exim.ImportActions;
import gov.epa.emissions.framework.db.DbUpdate;

public class MetadataEditorTest extends UserAcceptanceTestCase {

    private EmfConsole console;

    private ConsoleActions consoleActions;

    private DatasetsBrowserActions browserActions;

    protected void setUp() throws Exception {
        consoleActions = new ConsoleActions(this);
        console = consoleActions.open();
        browserActions = new DatasetsBrowserActions(console, this);
        browserActions.open();

        // String dataset = "UAT-" + new Random().nextInt();
        // doImport(dataset);
    }

    protected void tearDown() throws Exception {
        consoleActions.close();
        new DbUpdate().deleteAll("emf.datasets");
    }

    public void testShouldDisplayPropertiesOfSelectedDataset() throws Exception {
        // TODO: implement the test
        // browserActions.selectLast();
    }

    private void doImport(String datasetName) throws Exception {
        ImportActions importActions = new ImportActions(console, this);
        importActions.open();
        importActions.importOrlNonRoad(datasetName);
        importActions.done();
    }

}
