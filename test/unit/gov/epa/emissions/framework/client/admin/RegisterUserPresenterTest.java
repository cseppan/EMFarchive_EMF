package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class RegisterUserPresenterTest extends MockObjectTestCase {

    private RegisterUserPresenter presenter;

    private Mock view;

    protected void setUp() {
        view = mock(RegisterUserView.class);

        presenter = new RegisterUserPresenter(null, (RegisterUserView) view.proxy());

        view.expects(once()).method("setObserver").with(eq(presenter));
        presenter.observe();
    }

    public void testShouldCreateUserAndLoginOnNotifyCreateUser() throws EmfException {
        User user = new User();
        user.setUserName("joey");
        user.setPassword("passwd234");

        Mock emfUserAdmin = mock(UserServices.class);
        emfUserAdmin.expects(once()).method("createUser").with(eq(user));
        emfUserAdmin.expects(once()).method("authenticate").with(eq("joey"), eq("passwd234"), eq(false));

        Mock view = mock(RegisterUserView.class);

        RegisterUserPresenter presenter = new RegisterUserPresenter((UserServices) emfUserAdmin.proxy(),
                (RegisterUserView) view.proxy());
        view.expects(once()).method("setObserver").with(eq(presenter));
        presenter.observe();

        presenter.notifyRegister(user);
    }

    public void testShouldCloseViewOnCancelAction() {
        Mock view = mock(RegisterUserView.class);
        view.expects(once()).method("close").withNoArguments();

        RegisterUserPresenter presenter = new RegisterUserPresenter(null, (RegisterUserView) view.proxy());
        view.expects(once()).method("setObserver").with(eq(presenter));
        presenter.observe();

        presenter.notifyCancel();
    }

}
