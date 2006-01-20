package gov.epa.emissions.framework.services;

public class DataViewWebServiceTest extends DataViewServiceTestCase {

    protected void doSetUp() throws Exception {
        super.setUpService(serviceLocator().dataViewService());
    }

}
