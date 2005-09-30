package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;
import gov.epa.emissions.framework.ui.WindowLayoutManager;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class UserManagerPresenterTest extends MockObjectTestCase {

    private Mock view;

    private UserManagerPresenter presenter;

    private Mock layoutManager;

    protected void setUp() {
        layoutManager = mock(WindowLayoutManager.class);
        Mock userServices = mock(UserServices.class);
        presenter = new UserManagerPresenter(null, (UserServices) userServices.proxy(),
                (WindowLayoutManager) layoutManager.proxy());

        view = mock(UserManagerView.class);

        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();

        presenter.display((UserManagerView) view.proxy());
    }

    public void testShouldCloseViewOnClickOfCloseButton() {
        view.expects(once()).method("close").withNoArguments();

        presenter.doCloseView();
    }

    public void testShouldDeleteUserOnNotifyDelete() throws EmfException {
        Mock userServices = mock(UserServices.class);
        userServices.expects(once()).method("deleteUser").with(eq("matts"));

        User user = new User();
        user.setUsername("joe");

        Mock layoutManager = mock(WindowLayoutManager.class);
        UserManagerPresenter presenter = new UserManagerPresenter(user, (UserServices) userServices.proxy(),
                (WindowLayoutManager) layoutManager.proxy());

        Mock view = createView();
        view.expects(once()).method("refresh").withNoArguments();
        displayView(presenter, view);

        User matts = new User();
        matts.setUsername("matts");
        presenter.doDelete(new User[] { matts });
    }

    private void displayView(UserManagerPresenter presenter, Mock view) {
        view.expects(once()).method("observe").with(eq(presenter));
        presenter.display((UserManagerView) view.proxy());
    }

    private Mock createView() {
        Mock view = mock(UserManagerView.class);
        view.expects(once()).method("display").withNoArguments();
        view.expects(once()).method("promptDelete").withAnyArguments().will(returnValue(Boolean.TRUE));
        view.expects(once()).method("clearMessage").withNoArguments();
        
        return view;
    }

    public void testShouldDisplayMessageIfNoUsersAreSelectedAndActionIsDelete() throws EmfException {
        view.expects(once()).method("clearMessage").withNoArguments();
        view.expects(once()).method("showMessage").with(eq("To delete, please select at least one User."));

        presenter.doDelete(new User[] {});
    }

    public void testShouldDisplayRegisterNewUserViewOnDisplayRegisterNewUser() {
        Mock registerUserView = mock(RegisterUserView.class);
        registerUserView.expects(once()).method("observe").with(new IsInstanceOf(RegisterUserPresenter.class));
        registerUserView.expects(once()).method("display").withNoArguments();

        RegisterUserView viewProxy = (RegisterUserView) registerUserView.proxy();
        layoutManager.expects(once()).method("add").with(eq(viewProxy));

        view.expects(once()).method("clearMessage").withNoArguments();
        view.expects(once()).method("refresh").withNoArguments();

        presenter.doRegisterNewUser(viewProxy);
    }

    public void testShouldDisplayUpdateUserViewOnDisplayUpdateUser() {
        Mock updateUserView = mock(UpdateUserView.class);
        updateUserView.expects(once()).method("observe").with(new IsInstanceOf(UpdateUserPresenter.class));
        updateUserView.expects(once()).method("display").withNoArguments();

        UpdateUserView viewProxy = (UpdateUserView) updateUserView.proxy();
        layoutManager.expects(once()).method("add").with(eq(viewProxy));

        view.expects(once()).method("clearMessage").withNoArguments();
        view.expects(once()).method("refresh").withNoArguments();

        presenter.doUpdateUser(viewProxy);
    }

    public void testShouldDisplayUpdateUserViewForEachUserSelectedOnDisplayUpdateUser() throws UserException {
        // each user
        Mock updateUserView = mock(UpdateUserView.class);
        updateUserView.expects(once()).method("observe").with(new IsInstanceOf(UpdateUserPresenter.class));
        updateUserView.expects(once()).method("display").withNoArguments();

        UpdateUserView updateUserViewProxy = (UpdateUserView) updateUserView.proxy();
        layoutManager.expects(once()).method("add").with(eq(updateUserViewProxy));

        view.expects(once()).method("clearMessage").withNoArguments();
        view.expects(once()).method("refresh").withNoArguments();

        User user = new User();
        user.setUsername("user1");
        view.expects(once()).method("getUpdateUserView").with(eq(user)).will(returnValue(updateUserViewProxy));

        presenter.doUpdateUsers(new User[] { user });
    }

    public void testShouldDisplayMessageIfNoUsersAreSelectedForUpdate() {
        view.expects(once()).method("showMessage").with(eq("To update, please select at least one User."));

        presenter.doUpdateUsers(new User[] {});
    }

    public void testShouldNotDeleteCurrentlyLoggedInUserOnNotifyDelete() throws EmfException {
        User user = new User();
        user.setUsername("joe");

        UserManagerPresenter presenter = new UserManagerPresenter(user, null, null);
        displayView(presenter, createView());

        try {
            presenter.doDelete(new User[] { user });
        } catch (EmfException e) {
            assertEquals("Cannot delete yourself - '" + user.getUsername() + "'", e.getMessage());
            return;
        }

        fail("should not have allowed deletion of currently logged in user");
    }

    public void testShouldNotDeleteAdminOnNotifyDelete() throws UserException {
        UserManagerPresenter presenter = new UserManagerPresenter(null, null, null);
        displayView(presenter, createView());

        User admin = new User();
        admin.setUsername("admin");
        try {
            presenter.doDelete(new User[] { admin });
        } catch (EmfException e) {
            assertEquals("Cannot delete EMF super user - 'admin'", e.getMessage());
            return;
        }

        fail("should not have allowed deletion of EMF super user");
    }

}
