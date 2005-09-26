package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

public class UserServicesTest extends ServicesTestCase {
    private UserServices service;

    protected void setUp() {
        ServiceLocator serviceLocator = new RemoteServiceLocator(baseUrl);
        service = serviceLocator.getUserServices();
    }

    public void testGetUserSucceedsForEMFAdministrator() throws EmfException {
        User user = service.getUser("admin");
        assertEquals("EMF Administrator", user.getFullName());
    }

}
