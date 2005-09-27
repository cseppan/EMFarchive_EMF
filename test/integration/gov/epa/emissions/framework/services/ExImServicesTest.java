package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.db.ExImDbUpdate;

import java.io.File;
import java.util.Random;

public class ExImServicesTest extends ServicesTestCase {

    protected ExImServices eximService;

    private UserServices userService;

    private EmfDataset dataset;

    protected void setUp() {
        eximService = serviceLocator.getExImServices();
        userService = serviceLocator.getUserServices();

        dataset = new EmfDataset();
        Random random = new Random();
        dataset.setName("ORL NonPoint - ExImServicesTest" + random.nextInt());
        dataset.setCreator("creator");
    }

    protected void tearDown() throws Exception {
        ExImDbUpdate dbUpdate = new ExImDbUpdate();
        dbUpdate.deleteAll("emf.dataset_access_logs");
        dbUpdate.deleteAll("emf.datasets");
    }

    public void testFetchDatasetTypesReturnsFourORLTypes() throws EmfException {
        DatasetType[] datasetTypes = eximService.getDatasetTypes();
        assertTrue("Should have atleast 4 ORL types", datasetTypes.length >= 4);
    }

    public void testImportOrlNonPoint() throws Exception {
        DatasetType datasetType = new DatasetType();
        datasetType.setName(DatasetTypes.ORL_AREA_NONPOINT_TOXICS);
        User user = userService.getUser("emf");

        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "arinv.nonpoint.nti99_NC.txt";

        eximService.startImport(user, repository.getAbsolutePath(), filename, dataset, datasetType);

        // FIXME: verify that import is complete
    }

    public void testExportOrlNonPoint() throws Exception {
        DatasetType datasetType = new DatasetType();
        datasetType.setName(DatasetTypes.ORL_AREA_NONPOINT_TOXICS);
        User user = userService.getUser("emf");

        dataset.setDatasetType("ORL Nonpoint Inventory");
        dataset.setDescription("description");
        dataset.setStatus("imported");

        // import
        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "arinv.nonpoint.nti99_NC.txt";
        eximService.startImport(user, repository.getAbsolutePath(), filename, dataset, datasetType);

        // FIXME: verify that import is complete

        // export
        File outputFile = new File(System.getProperty("java.io.tmpdir"));
        outputFile.deleteOnExit();
        if (!outputFile.exists())
            outputFile.mkdir();

        eximService.startExport(user, new EmfDataset[] { dataset }, outputFile.getAbsolutePath(), true,
                "HELLO EMF ACCESSLOGS TESTCASE");

        // FIXME: verify the exported file exists
        Thread.sleep(2000);// wait, until the export is complete
    }

}
