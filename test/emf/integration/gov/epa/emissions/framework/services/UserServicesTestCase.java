package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

import org.apache.commons.configuration.ConfigurationException;

public abstract class UserServicesTestCase extends ServicesTestCase {
    private UserServices service;

    protected UserServicesTestCase(String baseUrlProperty) throws ConfigurationException {
        super(baseUrlProperty);
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
