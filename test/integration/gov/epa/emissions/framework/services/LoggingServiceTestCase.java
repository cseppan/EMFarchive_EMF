package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.db.ExImDbUpdate;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

import java.io.File;
import java.util.Date;
import java.util.Random;

//FIXME: what's this trying to test?
public abstract class LoggingServiceTestCase extends ServicesTestCase {

    protected ExImService eximService;

    private UserService userService;

    private EmfDataset dataset;

    private ServiceLocator serviceLocator;

    protected void setUp() throws Exception {
        super.setUp();
        cleanData();

        serviceLocator = serviceLocator();
        eximService = serviceLocator.eximService();
        userService = serviceLocator.userService();

        dataset = new EmfDataset();
        Random random = new Random();
        dataset.setName("ORL_NonPoint_LoggingServiceTest" + Math.abs(random.nextInt()));
        dataset.setCreator("creator");
        dataset.setAccessedDateTime(new Date());

        DatasetType datasetType = orlNonPointType(serviceLocator.datasetTypeService());
        dataset.setDatasetType(datasetType);
    }

    protected void setUpService(LoggingService service) throws Exception {
    }

    private DatasetType orlNonPointType(DatasetTypeService service) throws Exception {
        DatasetType[] types = service.getDatasetTypes();
        for (int i = 0; i < types.length; i++) {
            if (types[i].getName().equals("ORL Nonpoint Inventory"))
                return types[i];
        }

        return null;
    }

    protected void tearDown() throws Exception {
        cleanData();
    }

    private void cleanData() throws Exception {
        ExImDbUpdate dbUpdate = new ExImDbUpdate();
        dbUpdate.deleteAllDatasets();

        dbUpdate.deleteAll("emf.dataset_access_logs");
        dbUpdate.deleteAll("emf.statusmessages");
    }

    public void testAccessLogFetchAfterSuccessfulExport() throws Exception {
        User user = userService.getUser("emf");

        dataset.setDescription("description");
        dataset.setStatus("imported");

        doImport(dataset, user);
        EmfDataset importedDataset = serviceLocator.dataService().getDatasets()[0];
        File folder = new File(System.getProperty("java.io.tmpdir"));
        doExport(importedDataset, user, folder);

        assertTrue(folder.exists());

        verifyAccessLog(importedDataset.getDatasetid());
    }

    private void doImport(EmfDataset dataset, User user) throws Exception {
        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "NonPoint_WithComments.txt";
        eximService.startImport(user, repository.getAbsolutePath(), filename, dataset);
        verifyTaskComplete(user, "Import");
    }

    private void doExport(EmfDataset dataset, User user, File folder) throws Exception {
        eximService.startExportWithOverwrite(user, new EmfDataset[] { dataset }, folder.getAbsolutePath(),
                "Exporting NonPoint file");
        verifyTaskComplete(user, "Export");
    }

    private void verifyTaskComplete(User user, String task) throws Exception {
        StatusService service = serviceLocator.statusService();
        int counter = 0;
        for (int i = 0; i < 3000; i += 500) {
            Status[] status = service.getAll(user.getUsername());
            counter += status.length;
            if (counter == 2)
                return;
            Thread.sleep(500);
        }

        fail(task + " failed.");
    }

    private void verifyAccessLog(long datasetId) throws Exception {
        Thread.sleep(3000);

        LoggingService loggingService = serviceLocator.loggingService();

        AccessLog[] logs = loggingService.getAccessLogs(datasetId);
        assertEquals(1, logs.length);
    }

}
