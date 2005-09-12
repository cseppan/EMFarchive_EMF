package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UserManagerPresenterTest extends MockObjectTestCase {

    public void testShouldCloseViewOnClickOfCloseButton() {
        Mock view = mock(UserManagerView.class);

        UserManagerPresenter presenter = new UserManagerPresenter(null, null, (UserManagerView) view.proxy());

        view.expects(once()).method("setObserver").with(eq(presenter));
        view.expects(once()).method("close").withNoArguments();

        presenter.observe();
        presenter.notifyCloseView();
    }

    public void testShouldDeleteUserViaEMFUserAdminOnNotifyDelete() throws EmfException {
        Mock emfUserAdmin = mock(UserServices.class);
        emfUserAdmin.expects(once()).method("deleteUser").with(eq("matts"));

        Mock view = mock(UserManagerView.class);
        view.expects(once()).method("refresh").withNoArguments();
        User user = new User();
        user.setUserName("joe");

        UserManagerPresenter presenter = new UserManagerPresenter(user, (UserServices) emfUserAdmin.proxy(),
                (UserManagerView) view.proxy());

        presenter.notifyDelete("matts");
    }

    public void testShouldNotDeleteCurrentlyLoggedInUserOnNotifyDelete() throws EmfException {
        User user = new User();
        user.setUserName("joe");

        UserManagerPresenter presenter = new UserManagerPresenter(user, null, null);

        try {
            presenter.notifyDelete(user.getUserName());
        } catch (EmfException e) {
            assertEquals("Cannot delete yourself - '" + user.getUserName() + "'", e.getMessage());
            return;
        }

        fail("should not have allowed deletion of currently logged in user");
    }

    public void testShouldNotDeleteAdminOnNotifyDelete() {
        UserManagerPresenter presenter = new UserManagerPresenter(null, null, null);

        try {
            presenter.notifyDelete("admin");
        } catch (EmfException e) {
            assertEquals("Cannot delete EMF super user - 'admin'", e.getMessage());
            return;
        }

        fail("should not have allowed deletion of EMF super user");
    }

}
