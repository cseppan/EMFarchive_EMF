package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.db.ExImDbUpdate;
import gov.epa.emissions.framework.services.DatasetTypeService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImService;
import gov.epa.emissions.framework.services.UserService;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

import java.io.File;
import java.util.Random;

//FIXME: revisit this test. Does not Assert !
public class ExImServiceTest extends ServicesTestCase {

    protected ExImService eximService;

    private UserService userService;

    private EmfDataset dataset;

    protected void doSetUp() throws Exception {
        ServiceLocator serviceLocator = serviceLocator();

        eximService = serviceLocator.eximService();
        userService = serviceLocator.userService();
        
        dataset = new EmfDataset();
        Random random = new Random();
        dataset.setName("ORL NonPoint - ExImServicesTest" + random.nextInt());
        dataset.setCreator("creator");

        DatasetType datasetType = orlNonPointType(serviceLocator.datasetTypeService());
        dataset.setDatasetType(datasetType);
    }

    private DatasetType orlNonPointType(DatasetTypeService service) throws Exception {
        DatasetType[] types = service.getDatasetTypes();
        for (int i = 0; i < types.length; i++) {
            if (types[i].getName().equals("ORL Nonpoint Inventory"))
                return types[i];
        }

        return null;
    }

    protected void doTearDown() throws Exception {
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
