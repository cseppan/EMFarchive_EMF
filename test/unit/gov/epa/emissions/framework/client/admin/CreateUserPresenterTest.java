package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.admin.CreateUserPresenter;
import gov.epa.emissions.framework.client.admin.CreateUserView;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class CreateUserPresenterTest extends MockObjectTestCase {

    private CreateUserPresenter presenter;

    private Mock view;

    protected void setUp() {
        view = mock(CreateUserView.class);
        view.stubs().method("getUsername").will(returnValue("joey"));
        view.stubs().method("getPassword").will(returnValue("passwd234"));
        view.stubs().method("getConfirmPassword").will(returnValue("passwd234"));
        view.stubs().method("getEmail").will(returnValue("joey@qqq.unc.edu"));
        view.stubs().method("getFullName").will(returnValue("Joe Shay"));
        view.stubs().method("getAffiliation").will(returnValue("UNC"));
        view.stubs().method("getPhone").will(returnValue("919-111-2222"));

        presenter = new CreateUserPresenter(null, (CreateUserView) view.proxy());
    }

    public void testShouldCreateUserAndLoginOnNotifyCreateUser() throws EmfException {
        User expectedUser = new User();
        expectedUser.setUserName("joey");

        Mock emfUserAdmin = mock(EMFUserAdmin.class);
        emfUserAdmin.expects(once()).method("createUser").with(eq(expectedUser));
        emfUserAdmin.expects(once()).method("authenticate").with(eq("joey"), eq("passwd234"), eq(false)).will(
                returnValue(null));

        Mock view = mock(CreateUserView.class);
        view.stubs().method("getUsername").will(returnValue("joey"));
        view.stubs().method("getPassword").will(returnValue("passwd234"));
        view.stubs().method("getConfirmPassword").will(returnValue("passwd234"));
        view.stubs().method("getEmail").will(returnValue("joey@qqq.unc.edu"));
        view.stubs().method("getFullName").will(returnValue("Joe Shay"));
        view.stubs().method("getAffiliation").will(returnValue("UNC"));
        view.stubs().method("getPhone").will(returnValue("919-111-2222"));

        CreateUserPresenter presenter = new CreateUserPresenter((EMFUserAdmin) emfUserAdmin.proxy(),
                (CreateUserView) view.proxy());

        presenter.notifyCreate();
    }

    public void testShouldRegisterAsObserverOnInit() {
        view.expects(once()).method("setObserver").with(eq(presenter));
        
        presenter.init();
    }
    
    public void testShouldCloseViewOnCancelAction() {
        Mock view = mock(CreateUserView.class);
        view.expects(once()).method("close").withNoArguments();

        CreateUserPresenter presenter = new CreateUserPresenter(null, (CreateUserView) view.proxy());

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
