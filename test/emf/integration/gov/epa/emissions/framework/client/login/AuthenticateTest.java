package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.UserServices;
import junit.framework.TestCase;

public class AuthenticateTest extends TestCase {

    private UserServices emfUserAdmin;

    protected void setUp() {
        String baseUrl = "http://localhost:8080/emf/services";
        ServiceLocator serviceLocator = new RemoteServiceLocator(baseUrl);
        emfUserAdmin = serviceLocator.getUserServices();
    }

    public void testShouldSucceedOnValidUsernamePassword() throws EmfException {
        emfUserAdmin.authenticate("emf", "emf12345", false);
    }

    public void testShouldFailOnInvalidPassword() {
        try {
            emfUserAdmin.authenticate("cdcruz", "password", false);
        } catch (EmfException ex) {
            return;
        }

        fail("should have failed on invalid username/password");
    }

    public void testShouldFailOnUnknownUsername() {
        try {
            emfUserAdmin.authenticate("sdfsfr45gn", "password", false);
        } catch (EmfException ex) {
            return;
        }

        fail("should have failed on unknown username");
    }

}
