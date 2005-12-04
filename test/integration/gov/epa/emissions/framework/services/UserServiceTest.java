package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

public class UserServiceTest extends ServicesTestCase {
    private UserService service;

    protected void setUp() throws Exception {
        super.setUp();

        ServiceLocator serviceLocator = serviceLocator();
        service = serviceLocator.getUserService();
    }

    public void testGetUserSucceedsForEMFAdministrator() throws EmfException {
        User user = service.getUser("admin");
        assertEquals("EMF Administrator", user.getFullName());
    }

    public void testCreateUser() throws EmfException {
        User user = new User();
        user.setUsername("test-user");
        user.setPassword("user12345");
        user.setFullName("test");
        user.setAffiliation("aff");
        user.setPhone("111-222-3333");
        user.setEmail("email@email.edu");

        int initialCount = service.getUsers().length;

        service.createUser(user);

        assertNotNull(service.getUser("test-user"));
        assertEquals(initialCount + 1, service.getUsers().length);

        service.deleteUser("test-user");
    }

    public void testUpdateUser() throws EmfException {
        User user = new User();
        user.setUsername("test-user");
        user.setPassword("user12345");
        user.setFullName("name");
        user.setAffiliation("aff");
        user.setPhone("111-222-3333");
        user.setEmail("email@email.edu");

        service.createUser(user);

        user.setFullName("modified-name");
        service.updateUser(user);

        User result = service.getUser("test-user");
        assertNotNull(result);
        assertEquals("modified-name", result.getFullName());

        service.deleteUser("test-user");
    }

}
