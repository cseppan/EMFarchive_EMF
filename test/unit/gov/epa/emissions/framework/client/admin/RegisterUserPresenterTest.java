package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.admin.RegisterUserPresenter;
import gov.epa.emissions.framework.client.admin.RegisterUserView;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class RegisterUserPresenterTest extends MockObjectTestCase {

    private RegisterUserPresenter presenter;

    private Mock view;

    protected void setUp() {
        view = mock(RegisterUserView.class);
        view.stubs().method("getUsername").will(returnValue("joey"));
        view.stubs().method("getPassword").will(returnValue("passwd234"));
        view.stubs().method("getConfirmPassword").will(returnValue("passwd234"));
        view.stubs().method("getEmail").will(returnValue("joey@qqq.unc.edu"));
        view.stubs().method("getFullName").will(returnValue("Joe Shay"));
        view.stubs().method("getAffiliation").will(returnValue("UNC"));
        view.stubs().method("getPhone").will(returnValue("919-111-2222"));

        presenter = new RegisterUserPresenter(null, (RegisterUserView) view.proxy());

        view.expects(once()).method("setObserver").with(eq(presenter));        
        presenter.observe();
    }

    public void testShouldCreateUserAndLoginOnNotifyCreateUser() throws EmfException {
        User expectedUser = new User();
        expectedUser.setUserName("joey");

        Mock emfUserAdmin = mock(EMFUserAdmin.class);
        emfUserAdmin.expects(once()).method("createUser").with(eq(expectedUser));
        emfUserAdmin.expects(once()).method("authenticate").with(eq("joey"), eq("passwd234"), eq(false));

        Mock view = mock(RegisterUserView.class);
        view.stubs().method("getUsername").will(returnValue("joey"));
        view.stubs().method("getPassword").will(returnValue("passwd234"));
        view.stubs().method("getConfirmPassword").will(returnValue("passwd234"));
        view.stubs().method("getEmail").will(returnValue("joey@qqq.unc.edu"));
        view.stubs().method("getFullName").will(returnValue("Joe Shay"));
        view.stubs().method("getAffiliation").will(returnValue("UNC"));
        view.stubs().method("getPhone").will(returnValue("919-111-2222"));

        RegisterUserPresenter presenter = new RegisterUserPresenter((EMFUserAdmin) emfUserAdmin.proxy(),
                (RegisterUserView) view.proxy());
        view.expects(once()).method("setObserver").with(eq(presenter));        
        presenter.observe();

        presenter.notifyRegister();
    }

    public void testShouldCloseViewOnCancelAction() {
        Mock view = mock(RegisterUserView.class);
        view.expects(once()).method("close").withNoArguments();

        RegisterUserPresenter presenter = new RegisterUserPresenter(null, (RegisterUserView) view.proxy());
        view.expects(once()).method("setObserver").with(eq(presenter));        
        presenter.observe();

        presenter.notifyCancel();
    }

    public void testShouldRaiseErrorMessageIfPasswordsDontMatch() {
        view.stubs().method("getConfirmPassword").will(returnValue("tryryhd23"));

        try {
            presenter.notifyRegister();
        } catch (EmfException e) {
            return;
        }

        fail("should have raised exception if passwords dont match");
    }

    public void testShouldRaiseErrorMessageIfEmailIsInvalid() {
        view.stubs().method("getEmail").will(returnValue("tryryhd23"));

        try {
            presenter.notifyRegister();
        } catch (EmfException e) {
            return;
        }

        fail("should have raised exception if Email is invaild");
    }

}
