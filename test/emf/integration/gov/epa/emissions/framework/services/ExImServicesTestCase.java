package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import junit.framework.TestCase;

public abstract class ExImServicesTestCase extends TestCase {

    private String baseUrl;

    protected ExImServices eximService;

    private UserServices userService;

    protected ExImServicesTestCase(String baseUrl) {
        this.baseUrl = baseUrl;
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
        DatasetType[] datasetTypes = eximService.getDatasetTypes();
        User user = userService.getUser("emf");

        // FIXME: this should be generic, and not hard-coded. ??
        String filename = "c:/CEP/projects/EMF/test/commons/data/orl/nc/arinv.nonpoint.nti99_NC.txt";
        eximService.startImport(user, filename, datasetTypes[0]);
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
