package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;

public class UserServicesTest extends WebServicesIntegrationTestCase {
    private UserServices service;

    protected void setUp() {
        service = serviceLocator.getUserServices();
    }

    public void testGetUserSucceedsForEMFAdministrator() throws EmfException {
        User user = service.getUser("admin");
        assertEquals("EMF Administrator", user.getFullName());
    }

}
