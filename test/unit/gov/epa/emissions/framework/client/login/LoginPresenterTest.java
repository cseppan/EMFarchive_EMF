package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.PasswordService;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class LoginPresenterTest extends MockObjectTestCase {

	public void testShouldAuthenticateWithEmfUserAdminOnNotifyLogin()
			throws EmfException {
		User user = new User();
		user.setUsername("joey");
		user.setPassword("joeymoey12");

		Mock userAdmin = mock(UserServices.class);
		userAdmin.expects(once()).method("authenticate").with(
				eq(user.getUsername()), eq(user.getEncryptedPassword()));
		userAdmin.expects(once()).method("getUser")
				.with(eq(user.getUsername())).will(returnValue(user));

		LoginPresenter presenter = new LoginPresenter((UserServices) userAdmin
				.proxy(), null);

		assertSame(user, presenter.notifyLogin("joey", "joeymoey12"));
	}

	public void testShouldFailIfAuthenticateFailsOnNotifyLogin()
			throws EmfException {
		Mock userAdmin = mock(UserServices.class);
		Throwable exception = new EmfException("authentication failure");
		String encryptedPassword = PasswordService.encrypt("password");
		userAdmin.expects(once()).method("authenticate").with(eq("username"),
				eq(encryptedPassword)).will(throwException(exception));

		LoginPresenter presenter = new LoginPresenter((UserServices) userAdmin
				.proxy(), null);

		try {
			presenter.notifyLogin("username", "password");
		} catch (EmfException e) {
			assertSame(exception, e);
			return;
		}

		fail("should have raised an exception on authentication failure");
	}

	public void testShouldCloseViewOnNotifyCancel() {
		Mock view = mock(LoginView.class);
		view.expects(once()).method("close").withNoArguments();

		LoginPresenter presenter = new LoginPresenter(null, (LoginView) view
				.proxy());

		presenter.notifyCancel();
	}

	public void testShouldRegisterWithViewOnInit() {
		Mock view = mock(LoginView.class);

		LoginPresenter presenter = new LoginPresenter(null, (LoginView) view
				.proxy());
		view.expects(once()).method("setObserver").with(eq(presenter));

		presenter.observe();
	}

}
