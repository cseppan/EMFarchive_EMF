package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.UserServicesTransport;
import gov.epa.emissions.framework.commons.UserServices;
import junit.framework.TestCase;

public class AuthenticateTest extends TestCase {

    public void testShouldSucceedOnValidUsernamePassword() throws EmfException {
        UserServices emfUserAdmin = new UserServicesTransport(
                "http://ben.cep.unc.edu:8080/emf/services/gov.epa.emf.UserServices");

        emfUserAdmin.authenticate("cdcruz", "conrad12345", false);
    }

    public void testShouldFailOnInvalidPassword() {
        UserServices emfUserAdmin = new UserServicesTransport(
                "http://ben.cep.unc.edu:8080/emf/services/gov.epa.emf.UserServices");
        try {
            emfUserAdmin.authenticate("cdcruz", "password", false);
        } catch (EmfException ex) {
            return;
        }

        fail("should have failed on invalid username/password");
    }
    
    public void testShouldFailOnUnknownUsername() {
        UserServices emfUserAdmin = new UserServicesTransport(
                "http://ben.cep.unc.edu:8080/emf/services/gov.epa.emf.UserServices");
        try {
            emfUserAdmin.authenticate("sdfsfr45gn", "password", false);
        } catch (EmfException ex) {
            return;
        }

        fail("should have failed on unknown username");
    }    

}
