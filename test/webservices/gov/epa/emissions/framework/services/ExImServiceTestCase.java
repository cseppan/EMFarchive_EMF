package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.db.ExImDbUpdate;

import java.io.File;
import java.util.Random;

//FIXME: revisit this test. Does not Assert !
public abstract class ExImServiceTestCase extends ServiceTestCase {

    protected ExImService eximService;

    private UserService userService;

    private DataCommonsService dataCommonsService;

    private EmfDataset dataset;

    protected void setUpService(ExImService eximService, UserService userService, DataCommonsService commonsService)
            throws Exception {
        this.eximService = eximService;
        this.userService = userService;
        this.dataCommonsService = commonsService;

        dataset = new EmfDataset();
        Random random = new Random();
        dataset.setName("ORL NonPoint - ExImServicesTest" + random.nextInt());
        dataset.setCreator("creator");

        DatasetType datasetType = orlNonPointType(commonsService);
        dataset.setDatasetType(datasetType);
    }

    private DatasetType orlNonPointType(DataCommonsService service) throws Exception {
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

    public void testMultipleExternalFileImport() throws EmfException {
        User user = userService.getUser("emf");

        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");

        String[] fileNames = new String[] { "*.txt" };

        eximService.startMultipleFileImport(user, repository.getAbsolutePath(), fileNames,
                externalDatasetType(dataCommonsService));
    }

    private DatasetType externalDatasetType(DataCommonsService service) throws EmfException {
        DatasetType[] types = service.getDatasetTypes();
        for (int i = 0; i < types.length; i++) {
            if (types[i].getName().equals("External File"))
                return types[i];
        }

        return null;

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
