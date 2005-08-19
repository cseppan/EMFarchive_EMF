package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import junit.framework.TestCase;

public abstract class UserServicesTestCase extends TestCase {
    private UserServices service;
    private String baseUrl;
    
    protected UserServicesTestCase(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    protected void setUp() {
        ServiceLocator serviceLocator = new RemoteServiceLocator(baseUrl);
        service = serviceLocator.getUserServices();
    }
    
    public void testGetUserSucceedsForEMFAdministrator() throws EmfException {
        User user = service.getUser("admin");
        assertEquals("EMF Administrator", user.getFullName());
    }

    public void testSystemHasTwoUsersOnInstallation() throws EmfException {
        User[] users = service.getUsers();
        assertEquals(2, users.length);
    }

}
