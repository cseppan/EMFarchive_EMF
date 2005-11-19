package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.db.ExImDbUpdate;
import gov.epa.emissions.framework.services.DatasetTypesServices;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;
import gov.epa.emissions.framework.services.WebServicesIntegrationTestCase;

import java.io.File;
import java.util.Random;

public class ExImServicesTest extends WebServicesIntegrationTestCase {

    protected ExImServices eximService;

    private UserServices userService;

    private EmfDataset dataset;

    protected void setUp() throws Exception {
        eximService = serviceLocator.getExImServices();
        userService = serviceLocator.getUserServices();
        dataset = new EmfDataset();
        Random random = new Random();
        dataset.setName("ORL NonPoint - ExImServicesTest" + random.nextInt());
        dataset.setCreator("creator");

        DatasetType datasetType = orlNonPointType(serviceLocator.getDatasetTypesServices());
        dataset.setDatasetType(datasetType);
    }

    private DatasetType orlNonPointType(DatasetTypesServices service) throws Exception {
        DatasetType[] types = service.getDatasetTypes();
        for (int i = 0; i < types.length; i++) {
            if (types[i].getName().equals("ORL Nonpoint Inventory"))
                return types[i];
        }

        return null;
    }

    protected void tearDown() throws Exception {
        ExImDbUpdate dbUpdate = new ExImDbUpdate();
        dbUpdate.deleteAllDatasets();
    }

    public void testImportOrlNonPoint() throws Exception {
        User user = userService.getUser("emf");

        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "NonPoint_WithComments.txt";

        eximService.startImport(user, repository.getAbsolutePath(), filename, dataset);

        // FIXME: verify that import is complete
    }

    public void testExportWithOverwrite() throws Exception {
        User user = userService.getUser("emf");

        dataset.setDescription("description");
        dataset.setStatus("imported");

        // import
        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "NonPoint_WithComments.txt";
        eximService.startImport(user, repository.getAbsolutePath(), filename, dataset);

        // FIXME: verify that import is complete

        // export
        File outputFile = new File(System.getProperty("java.io.tmpdir"));
        outputFile.deleteOnExit();
        if (!outputFile.exists())
            outputFile.mkdir();

        eximService.startExportWithOverwrite(user, new EmfDataset[] { dataset }, outputFile.getAbsolutePath(),
                "Exporting NonPoint file");

        // FIXME: verify the exported file exists
        Thread.sleep(2000);// wait, until the export is complete
    }

    public void testExport() throws Exception {
        User user = userService.getUser("emf");

        dataset.setDescription("description");
        dataset.setStatus("imported");

        // import
        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "NonPoint_WithComments.txt";
        eximService.startImport(user, repository.getAbsolutePath(), filename, dataset);

        // FIXME: verify that import is complete

        // export
        File outputFile = new File(System.getProperty("java.io.tmpdir"));
        outputFile.deleteOnExit();
        if (!outputFile.exists())
            outputFile.mkdir();

        eximService.startExport(user, new EmfDataset[] { dataset }, outputFile.getAbsolutePath(),
                "Exporting NonPoint file");

        // FIXME: verify the exported file exists
        Thread.sleep(2000);// wait, until the export is complete
    }

}
