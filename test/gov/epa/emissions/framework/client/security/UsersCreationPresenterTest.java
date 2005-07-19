package gov.epa.emissions.framework.client.security;

import gov.epa.emissions.framework.client.transport.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UsersCreationPresenterTest extends MockObjectTestCase {

    public void testShouldAttemptToCreateUserViaEMFUserAdminOnNotifyCreateUser() {
        User expectedUser = new User();
        expectedUser.setUserName("joey");
        
        Mock emfUserAdmin = mock(EMFUserAdmin.class);
        emfUserAdmin.expects(once()).method("createUser").with(eq(expectedUser));

        Mock view = mock(UserCreationView.class);
        view.stubs().method("getUsername").will(returnValue("joey"));
                
        UserCreationPresenter presenter = new UserCreationPresenter((EMFUserAdmin) emfUserAdmin.proxy(),
                (UserCreationView) view.proxy());
                
        presenter.notifyCreate();
    }

}
