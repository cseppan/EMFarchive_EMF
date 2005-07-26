package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class LoginPresenterTest extends MockObjectTestCase {

    public void testShouldAuthenticateWithEmfUserAdminOnNotifyLogin() throws EmfException {
        Mock userAdmin = mock(EMFUserAdmin.class);
        userAdmin.expects(once()).method("authenticate").with(eq("username"), eq("password"), eq(false)).will(
                returnValue(null));

        LoginPresenter presenter = new LoginPresenter((EMFUserAdmin) userAdmin.proxy(), null);

        presenter.notifyLogin("username", "password");
    }

    public void testShouldFailIfAuthenticateFailsOnNotifyLogin() throws EmfException {
        Mock userAdmin = mock(EMFUserAdmin.class);
        Throwable exception = new EmfException("authentication failure");
        userAdmin.expects(once()).method("authenticate").with(eq("username"), eq("password"), eq(false)).will(
                throwException(exception));

        LoginPresenter presenter = new LoginPresenter((EMFUserAdmin) userAdmin.proxy(), null);

        try {
            presenter.notifyLogin("username", "password");
        } catch (EmfException e) {
            assertSame(exception, e);
            return;
        }

        fail("should have raised an exception on authentication failure");
    }

    public void testShouldCloseViewOnNotifyCancel() throws EmfException {
        Mock view = mock(LoginView.class);
        view.expects(once()).method("close").withNoArguments();

        LoginPresenter presenter = new LoginPresenter(null, (LoginView) view.proxy());

        presenter.notifyCancel();
    }

}
