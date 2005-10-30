package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.PasswordService;
import gov.epa.emissions.framework.services.WebServicesIntegrationTestCase;
import gov.epa.emissions.framework.services.UserServices;

public class AuthenticateTest extends WebServicesIntegrationTestCase {

	private UserServices emfUserAdmin;

	protected void setUp() {
		emfUserAdmin = serviceLocator.getUserServices();
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
