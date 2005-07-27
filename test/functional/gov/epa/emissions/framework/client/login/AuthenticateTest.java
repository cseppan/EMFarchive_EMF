package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.EMFUserAdminTransport;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import junit.framework.TestCase;

public class AuthenticateTest extends TestCase {

    public void testShouldSucceedOnValidUsernamePassword() throws EmfException {
        EMFUserAdmin emfUserAdmin = new EMFUserAdminTransport(
                "http://localhost:8080/emf/services/EMFUserManagerService");

        emfUserAdmin.authenticate("cdcruz", "conrad12345", false);
    }

    //TODO: idled until the authenticate interface changes to throw exception on failures
    public void IDLED_testShouldFailOnInvalidUsernamePassword() {
        EMFUserAdmin emfUserAdmin = new EMFUserAdminTransport(
                "http://localhost:8080/emf/services/EMFUserManagerService");
        try {
            emfUserAdmin.authenticate("unknown", "password", false);
        } catch (EmfException ex) {
            return;
        }

        fail("should have failed on invalid username/password");
    }

}
