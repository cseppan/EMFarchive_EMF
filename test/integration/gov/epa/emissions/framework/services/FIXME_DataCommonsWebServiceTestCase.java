package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.client.transport.ServiceLocator;

public abstract class FIXME_DataCommonsWebServiceTestCase extends DataCommonsServiceTestCase {

    protected void doSetUp() throws Exception {
        ServiceLocator serviceLocator = serviceLocator();
        super.setUpService(serviceLocator.dataCommonsService(), serviceLocator.userService());
    }

}
