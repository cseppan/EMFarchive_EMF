package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.ConsoleActions;
import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.data.DatasetsBrowserActions;

import java.io.File;
import java.util.Random;

public class ExportOrlDatasetsTest extends UserAcceptanceTestCase {

    private EmfConsole console;

    private DatasetsBrowserActions browserActions;

    public void setUp() throws Exception {
        ConsoleActions consoleActions = new ConsoleActions(this);
        console = consoleActions.open();

        browserActions = new DatasetsBrowserActions(console, this);
        browserActions.open();
    }

    public void testShouldExportDatasetToFileOnClickOfExportButton() throws Exception {
        String datasetName = "UAT-" + new Random().nextInt();
        try {
            doExport(datasetName);
        } finally {
            new ExImDbUpdate().deleteDataset(datasetName);
        }
    }

    public void itestShouldFailToExportIfOverwriteIsUncheckedAndFileAlreadyExists() throws Exception {
        String datasetName = "ORL Nonroad Inventory" + " UAT - " + new Random().nextInt();
        try {
            doExport(datasetName);
            failOnExportDueToOverwrite(datasetName);
        } finally {
            new ExImDbUpdate().deleteDataset(datasetName);
        }
    }

    private void failOnExportDueToOverwrite(String datasetName) throws Exception {
        importOrlNonRoad(datasetName);
        ExportWindow exportWindow = selectDatasetToExport(datasetName);

        String folder = System.getProperty("java.io.tmpdir");
        try {
            ExportActions exportActions = new ExportActions(exportWindow, this);
            exportActions.setFolder(folder);
            exportActions.setOverwriteFalse();

            exportActions.clickExport();
        } catch (Exception e) {
            return;
        }

        File file = file(datasetName, folder);
        fail("should have failed if Overwrite was unchecked, and the file " + file.getAbsolutePath()
                + " already exists");
    }

    public void itestShouldExportMultipleSelectedDatasetsToFilesOnClickOfExportButton() throws Exception {
        String dataset1 = "ORL Nonroad Inventory" + " UAT 1- " + new Random().nextInt();
        String dataset2 = "ORL Nonroad Inventory" + " UAT 2- " + new Random().nextInt();
        String folder = System.getProperty("java.io.tmpdir");

        try {
            importOrlNonRoad(dataset1);
            importOrlNonRoad(dataset2);

            int total = browserActions.table().getRowCount();
            browserActions.select(total - 2);
            browserActions.select(total - 1);

            exportSelectedDataset(browserActions.export(), folder);
        } finally {
            new ExImDbUpdate().deleteDataset(dataset1);
            new ExImDbUpdate().deleteDataset(dataset2);
        }

        assertExportedFileExists(dataset1, folder);
        assertExportedFileExists(dataset2, folder);
    }

    private void assertExportedFileExists(String dataset, String folder) {
        File file = file(dataset, folder);
        assertTrue("Should have exported dataset '" + dataset + "' to file - " + file.getAbsolutePath(), file.exists());
        file.deleteOnExit();
    }

    private void importOrlNonRoad(String datasetName) throws Exception {
        ImportActions importActions = new ImportActions(console, this);
        importActions.open();
        importActions.importOrlNonRoad(datasetName);
        importActions.done();
    }

    private File file(String datasetName, String folder) {
        String filename = datasetName.replace(' ', '_') + ".txt";
        return new File(folder, filename);
    }

    private void doExport(String datasetName) throws Exception {
        importOrlNonRoad(datasetName);
        ExportWindow exportWindow = selectDatasetToExport(datasetName);

        String folder = System.getProperty("java.io.tmpdir");
        exportSelectedDataset(exportWindow, folder);

        assertExportedFileExists(datasetName, folder);
    }

    private ExportWindow selectDatasetToExport(String datasetName) throws Exception {
        browserActions.selectDataset(datasetName);
        return browserActions.export();
    }

    private void exportSelectedDataset(ExportWindow exportWindow, String folder) throws Exception {
        ExportActions exportActions = new ExportActions(exportWindow, this);
        exportActions.setFolder(folder);

        exportActions.clickExport();

        Thread.sleep(2000);// pause, until export is complete
    }

}
