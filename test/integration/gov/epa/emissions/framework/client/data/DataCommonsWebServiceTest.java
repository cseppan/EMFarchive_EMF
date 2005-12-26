package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataCommonsServiceTestCase;

public class DataCommonsWebServiceTest extends DataCommonsServiceTestCase {

    protected void doSetUp() throws Exception {
        ServiceLocator serviceLocator = serviceLocator();
        super.setUpService(serviceLocator.dataCommonsService());
    }

}
