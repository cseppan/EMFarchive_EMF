package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;

public abstract class ExImServicesTestCase extends ServicesTestCase {

    protected ExImServices eximService;

    private UserServices userService;

    protected ExImServicesTestCase(String baseUrlProperty) throws ConfigurationException {
        super(baseUrlProperty);
    }

    protected void setUp() {
        RemoteServiceLocator serviceLocator = new RemoteServiceLocator(baseUrl);
        eximService = serviceLocator.getEximServices();
        userService = serviceLocator.getUserServices();
    }

    public void testFetchDatasetTypesReturnsFourORLTypes() throws EmfException {
        DatasetType[] datasetTypes = eximService.getDatasetTypes();
        assertEquals(4, datasetTypes.length);
    }

    public void testImportOrlNonPoint() throws EmfException {
        DatasetType datasetType = new DatasetType();
        datasetType.setName(DatasetTypes.ORL_AREA_NONPOINT_TOXICS);
        User user = userService.getUser("emf");

        File userDir = new File(System.getProperty("user.dir"));
        File file = new File(userDir, "test/commons/data/orl/nc/arinv.nonpoint.nti99_NC.txt");

        EmfDataset dataset = new EmfDataset();
        dataset.setCreator(user.getFullName());
        dataset.setName("ORL NonPoint - Test");
        
        eximService.startImport(user, file.getPath(), dataset, datasetType);

        // TODO: verify status
    }

    public void testExportOrlNonPoint() throws EmfException, IOException{
        DatasetType datasetType = new DatasetType();
        datasetType.setName(DatasetTypes.ORL_AREA_NONPOINT_TOXICS);
        User user = userService.getUser("emf");

        File userDir = new File(System.getProperty("user.dir"));
        File file = new File(userDir, "test/commons/data/orl/nc/arinv.nonpoint.nti99_NC.txt");

        EmfDataset dataset = new EmfDataset();
        dataset.setCreator(user.getFullName());
        dataset.setName("ORL NonPoint - Test");
        
        eximService.startImport(user, file.getPath(), dataset, datasetType);
        File fileOut = new File(System.getProperty("tmp.dir"),"orlnonpoint.txt");
        
        eximService.startExport(user,dataset,fileOut.getCanonicalPath());
        
    }
    // TODO: rename & redo this test
    public void FIXME_testWHAT() throws EmfException {
        // emfData1.startImport("jcapowski","arinv.nonpoint.nti99_NC.txt","ORL");
        // System.out.println("END IMPORT CLIENT");
        // ExImTransport emfData2 = new ExImTransport(endpoint1);
        // emfData1.startImport("cdcruz","FOOBAR_TWO","IDA");
        DatasetType[] datasetTypes = eximService.getDatasetTypes();
        assertEquals(4, datasetTypes.length);

        // DatasetType dst = new DatasetType();
        // dst.setDescription("Hello ORL");
        // dst.setName("Hello ORL");
        // dst.setMaxfiles(99);
        // dst.setMinfiles(99);
        // emfData1.insertDatasetType(dst);
    }

}
