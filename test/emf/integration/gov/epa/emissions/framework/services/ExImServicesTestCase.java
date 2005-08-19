package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import junit.framework.TestCase;

public abstract class ExImServicesTestCase extends TestCase {

    private String baseUrl;

    protected ExImServices service;

    protected ExImServicesTestCase(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    protected void setUp() {
        service = new RemoteServiceLocator(baseUrl).getEximServices();
    }

    public void testFetchDatasetTypesReturnsFourORLTypes() throws EmfException {
        DatasetType[] datasetTypes = service.getDatasetTypes();
        assertEquals(4, datasetTypes.length);
    }

    //TODO: rename & redo this test 
    public void FIXME_testWHAT() throws EmfException {
        // emfData1.startImport("jcapowski","arinv.nonpoint.nti99_NC.txt","ORL");
        // System.out.println("END IMPORT CLIENT");
        // ExImTransport emfData2 = new ExImTransport(endpoint1);
        // emfData1.startImport("cdcruz","FOOBAR_TWO","IDA");
        DatasetType[] datasetTypes = service.getDatasetTypes();
        assertEquals(4, datasetTypes.length);

        // DatasetType dst = new DatasetType();
        // dst.setDescription("Hello ORL");
        // dst.setName("Hello ORL");
        // dst.setMaxfiles(99);
        // dst.setMinfiles(99);
        // emfData1.insertDatasetType(dst);
    }

}
