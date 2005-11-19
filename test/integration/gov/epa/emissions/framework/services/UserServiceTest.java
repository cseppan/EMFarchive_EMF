package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;

public class UserServiceTest extends WebServicesIntegrationTestCase {
    private UserService service;

    protected void setUp() {
        service = serviceLocator.getUserService();
    }

    public void testGetUserSucceedsForEMFAdministrator() throws EmfException {
        User user = service.getUser("admin");
        assertEquals("EMF Administrator", user.getFullName());
    }

}
