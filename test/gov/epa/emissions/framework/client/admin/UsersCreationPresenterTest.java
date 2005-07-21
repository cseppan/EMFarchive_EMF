package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.admin.UserCreationPresenter;
import gov.epa.emissions.framework.client.admin.UserCreationView;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UsersCreationPresenterTest extends MockObjectTestCase {

    public void testShouldAttemptToCreateUserViaEMFUserAdminOnNotifyCreateUser() throws EmfException {
        User expectedUser = new User();
        expectedUser.setUserName("joey");

        Mock emfUserAdmin = mock(EMFUserAdmin.class);
        emfUserAdmin.expects(once()).method("createUser").with(eq(expectedUser));

        Mock view = mock(UserCreationView.class);
        view.stubs().method("getUsername").will(returnValue("joey"));
        view.stubs().method("getPassword").will(returnValue("passwd"));
        view.stubs().method("getEmail").will(returnValue("joey@qqq.unc.edu"));
        view.stubs().method("getName").will(returnValue("Joe Shay"));
        view.stubs().method("getAffiliation").will(returnValue("UNC"));
        view.stubs().method("getPhone").will(returnValue("919 111 2222"));

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

}
