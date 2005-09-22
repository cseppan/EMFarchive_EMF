package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;

import java.io.File;
import java.util.Random;

import org.apache.commons.configuration.ConfigurationException;

public abstract class ExImServicesTestCase extends ServicesTestCase {

    protected ExImServices eximService;

    private UserServices userService;

    protected ExImServicesTestCase(String baseUrlProperty) throws ConfigurationException {
        super(baseUrlProperty);
    }

    protected void setUp() {
        RemoteServiceLocator serviceLocator = new RemoteServiceLocator(baseUrl);
        eximService = serviceLocator.getExImServices();
        userService = serviceLocator.getUserServices();
    }

    public void testFetchDatasetTypesReturnsFourORLTypes() throws EmfException {
        DatasetType[] datasetTypes = eximService.getDatasetTypes();
        assertTrue("Should have atleast 4 ORL types", datasetTypes.length >= 4);
    }

    public void testImportOrlNonPoint() throws EmfException {
        DatasetType datasetType = new DatasetType();
        datasetType.setName(DatasetTypes.ORL_AREA_NONPOINT_TOXICS);
        User user = userService.getUser("emf");

        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "arinv.nonpoint.nti99_NC.txt";

        EmfDataset dataset = new EmfDataset();
        Random random = new Random();// FIXME: drop test data during setup
        dataset.setName("ORL NonPoint - Test" + random.nextInt());
        dataset.setCreator("creator");

        eximService.startImport(user, repository.getAbsolutePath(), filename, dataset, datasetType);

        // FIXME: verify that import is complete
    }

    public void testExportOrlNonPoint() throws EmfException {
        DatasetType datasetType = new DatasetType();
        datasetType.setName(DatasetTypes.ORL_AREA_NONPOINT_TOXICS);
        User user = userService.getUser("emf");

        EmfDataset dataset = new EmfDataset();
        Random random = new Random();// FIXME: drop test data during setup
        dataset.setName("ORL NonPoint - Test" + random.nextInt());
        dataset.setCreator("creator");
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

    }

}
