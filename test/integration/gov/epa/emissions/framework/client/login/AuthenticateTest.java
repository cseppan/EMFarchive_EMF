package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.PasswordService;
import gov.epa.emissions.framework.services.UserService;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

public class AuthenticateTest extends ServicesTestCase {

    private UserService emfUserAdmin;

    protected void setUp() throws Exception {
        super.setUp();

        ServiceLocator serviceLocator = serviceLocator();
        emfUserAdmin = serviceLocator.getUserService();
    }

    public void testShouldSucceedOnValidUsernamePassword() throws EmfException {
        emfUserAdmin.authenticate("emf", PasswordService.encrypt("emf12345"));
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

}
