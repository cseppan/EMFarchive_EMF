package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.admin.UserCreationPresenter;
import gov.epa.emissions.framework.client.admin.UserCreationView;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UsersCreationPresenterTest extends MockObjectTestCase {

    private UserCreationPresenter presenter;

    private Mock view;

    protected void setUp() {
        view = mock(UserCreationView.class);
        view.stubs().method("getUsername").will(returnValue("joey"));
        view.stubs().method("getPassword").will(returnValue("passwd234"));
        view.stubs().method("getConfirmPassword").will(returnValue("passwd234"));
        view.stubs().method("getEmail").will(returnValue("joey@qqq.unc.edu"));
        view.stubs().method("getName").will(returnValue("Joe Shay"));
        view.stubs().method("getAffiliation").will(returnValue("UNC"));
        view.stubs().method("getPhone").will(returnValue("919-111-2222"));

        presenter = new UserCreationPresenter(null, (UserCreationView) view.proxy());
    }

    public void testShouldCreateUserAndLoginOnNotifyCreateUser() throws EmfException {
        User expectedUser = new User();
        expectedUser.setUserName("joey");

        Mock emfUserAdmin = mock(EMFUserAdmin.class);
        emfUserAdmin.expects(once()).method("createUser").with(eq(expectedUser));
        emfUserAdmin.expects(once()).method("authenticate").with(eq("joey"), eq("passwd234"), eq(false)).will(
                returnValue(null));

        Mock view = mock(UserCreationView.class);
        view.stubs().method("getUsername").will(returnValue("joey"));
        view.stubs().method("getPassword").will(returnValue("passwd234"));
        view.stubs().method("getConfirmPassword").will(returnValue("passwd234"));
        view.stubs().method("getEmail").will(returnValue("joey@qqq.unc.edu"));
        view.stubs().method("getName").will(returnValue("Joe Shay"));
        view.stubs().method("getAffiliation").will(returnValue("UNC"));
        view.stubs().method("getPhone").will(returnValue("919-111-2222"));

        UserCreationPresenter presenter = new UserCreationPresenter((EMFUserAdmin) emfUserAdmin.proxy(),
                (UserCreationView) view.proxy());

        presenter.notifyCreate();
    }

    public void testShouldCloseViewOnCancelAction() {
        Mock view = mock(UserCreationView.class);
        view.expects(once()).method("close").withNoArguments();

        UserCreationPresenter presenter = new UserCreationPresenter(null, (UserCreationView) view.proxy());

        presenter.notifyCancel();
    }

    public void testShouldRaiseErrorMessageIfPasswordsDontMatch() {
        view.stubs().method("getConfirmPassword").will(returnValue("tryryhd23"));

        try {
            presenter.notifyCreate();
        } catch (EmfException e) {
            return;
        }

        fail("should have raised exception if passwords dont match");
    }

    public void testShouldRaiseErrorMessageIfEmailIsInvalid() {
        view.stubs().method("getEmail").will(returnValue("tryryhd23"));

        try {
            presenter.notifyCreate();
        } catch (EmfException e) {
            return;
        }

        fail("should have raised exception if Email is invaild");
    }

}
