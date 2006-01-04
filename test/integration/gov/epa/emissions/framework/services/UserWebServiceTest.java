package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.client.transport.ServiceLocator;

public class UserWebServiceTest extends UserServiceTestCase {

    private UserService service;

    protected void doSetUp() throws Exception {
        ServiceLocator serviceLocator = serviceLocator();
        service = serviceLocator.userService();

        super.setUpService(service);
    }

}
