package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.PasswordService;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class LoginPresenterTest extends MockObjectTestCase {

    private Mock view;

    private LoginPresenter presenter;

    protected void setUp() {
        view = mock(LoginView.class);

        presenter = new LoginPresenter(null);
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();
        
        presenter.display((LoginView) view.proxy());
    }

    public void testShouldAuthenticateWithEmfUserAdminOnNotifyLogin() throws EmfException {
        User user = new User();
        user.setUsername("joey");
        user.setPassword("joeymoey12");

        Mock userAdmin = mock(UserServices.class);
        userAdmin.expects(once()).method("authenticate").with(eq(user.getUsername()), eq(user.getEncryptedPassword()));
        userAdmin.expects(once()).method("getUser").with(eq(user.getUsername())).will(returnValue(user));

        LoginPresenter presenter = new LoginPresenter((UserServices) userAdmin.proxy());

        assertSame(user, presenter.doLogin("joey", "joeymoey12"));
    }

    public void testShouldFailIfAuthenticateFailsOnNotifyLogin() throws EmfException {
        Mock userAdmin = mock(UserServices.class);
        Throwable exception = new EmfException("authentication failure");
        String encryptedPassword = PasswordService.encrypt("password");
        userAdmin.expects(once()).method("authenticate").with(eq("username"), eq(encryptedPassword)).will(
                throwException(exception));

        LoginPresenter presenter = new LoginPresenter((UserServices) userAdmin.proxy());

        try {
            presenter.doLogin("username", "password");
        } catch (EmfException e) {
            assertSame(exception, e);
            return;
        }

        fail("should have raised an exception on authentication failure");
    }

    public void testShouldCloseViewOnNotifyCancel() {
        view.expects(once()).method("close").withNoArguments();

        presenter.doCancel();
    }

}
