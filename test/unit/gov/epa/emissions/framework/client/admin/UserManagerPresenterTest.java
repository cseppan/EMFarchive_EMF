package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UserManagerPresenterTest extends MockObjectTestCase {

    private Mock view;
    private UserManagerPresenter presenter;

    protected void setUp() {
        presenter = new UserManagerPresenter(null, null);

        view = mock(UserManagerView.class);

        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();    
        
        presenter.display((UserManagerView) view.proxy());
    }
    
    public void testShouldCloseViewOnClickOfCloseButton() {
        view.expects(once()).method("close").withNoArguments();
       
        presenter.doCloseView();
    }

    public void testShouldDeleteUserViaEMFUserAdminOnNotifyDelete() throws EmfException {
        Mock userServices = mock(UserServices.class);
        userServices.expects(once()).method("deleteUser").with(eq("matts"));

        Mock view = mock(UserManagerView.class);        
        view.expects(once()).method("display").withNoArguments();    
        view.expects(once()).method("refresh").withNoArguments();

        User user = new User();
        user.setUsername("joe");

        UserManagerPresenter presenter = new UserManagerPresenter(user, (UserServices) userServices.proxy());
        view.expects(once()).method("observe").with(eq(presenter));
        
        presenter.display((UserManagerView) view.proxy());
        
        presenter.doDelete("matts");
    }

    public void testShouldNotDeleteCurrentlyLoggedInUserOnNotifyDelete() throws EmfException {
        User user = new User();
        user.setUsername("joe");

        UserManagerPresenter presenter = new UserManagerPresenter(user, null);

        try {
            presenter.doDelete(user.getUsername());
        } catch (EmfException e) {
            assertEquals("Cannot delete yourself - '" + user.getUsername() + "'", e.getMessage());
            return;
        }

        fail("should not have allowed deletion of currently logged in user");
    }

    public void testShouldNotDeleteAdminOnNotifyDelete() {
        UserManagerPresenter presenter = new UserManagerPresenter(null, null);

        try {
            presenter.doDelete("admin");
        } catch (EmfException e) {
            assertEquals("Cannot delete EMF super user - 'admin'", e.getMessage());
            return;
        }

        fail("should not have allowed deletion of EMF super user");
    }

}
