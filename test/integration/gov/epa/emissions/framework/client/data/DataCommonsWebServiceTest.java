package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataCommonsServiceTestCase;

public class DataCommonsWebServiceTest extends DataCommonsServiceTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        
        ServiceLocator serviceLocator = serviceLocator();
        super.setUpService(serviceLocator.getDataCommonsService());
    }

}
