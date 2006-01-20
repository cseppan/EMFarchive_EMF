package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.security.PasswordGenerator;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.UserService;

public class AuthenticateTest extends ServicesTestCase {

    private UserService emfUserAdmin;

    protected void doSetUp() throws Exception {
        ServiceLocator serviceLocator = serviceLocator();
        emfUserAdmin = serviceLocator.userService();
    }

    public void testShouldSucceedOnValidUsernamePassword() throws Exception {
        emfUserAdmin.authenticate("emf", new PasswordGenerator().encrypt("emf12345"));
    }

    public void testShouldFailOnInvalidPassword() {
        try {
            emfUserAdmin.authenticate("emf", "password");
        } catch (EmfException ex) {
            return;
        }

        fail("should have failed on invalid username/password");
    }

    public void testShouldFailOnUnknownUsername() {
        try {
            emfUserAdmin.authenticate("sdfsfr45gn", "password");
        } catch (EmfException ex) {
            return;
        }

        fail("should have failed on unknown username");
    }

    protected void doTearDown() throws Exception {// no op
    }

}
