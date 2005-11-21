/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFClient
 * Package: package gov.epa.emissions.framework.service.axis;
 * File Name: EMFClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.db.ExImDbUpdate;

import java.io.File;
import java.util.Random;

public class LoggingServiceTest extends WebServicesIntegrationTestCase {

    protected ExImService eximService;

    private UserService userService;

    private EmfDataset dataset;

    private LoggingService loggingService;

    protected void setUp() throws Exception {
        eximService = serviceLocator.getExImService();
        userService = serviceLocator.getUserService();
        dataset = new EmfDataset();
        Random random = new Random();
        dataset.setName("ORL NonPoint - LoggingServiceTest" + random.nextInt());
        dataset.setCreator("creator");

        DatasetType datasetType = orlNonPointType(serviceLocator.getDatasetTypesService());
        dataset.setDatasetType(datasetType);

        loggingService = serviceLocator.getLoggingService();
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
        ExImDbUpdate dbUpdate = new ExImDbUpdate();
        dbUpdate.deleteAllDatasets();

        dbUpdate.deleteAll("emf.dataset_access_logs");
    }

    public void testAccessLogFetchAfterSuccessfulExport() throws Exception {
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

        Thread.sleep(4000);// wait, until the export is complete

        assertTrue(outputFile.exists());
        
        verifyAccessLog(dataset.getDatasetid());
    }

    private void verifyAccessLog(long datasetId) throws Exception {
        AccessLog[] logs = loggingService.getAccessLogs(datasetId);
//        assertEquals(1, logs.length);
    }

}
