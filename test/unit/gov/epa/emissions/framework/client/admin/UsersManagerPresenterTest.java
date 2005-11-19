package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserService;
import gov.epa.emissions.framework.ui.ViewLayout;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class UsersManagerPresenterTest extends MockObjectTestCase {

    private Mock view;

    private UsersManagerPresenter presenter;

    private Mock layoutManager;

    protected void setUp() {
        layoutManager = mock(ViewLayout.class);
        Mock userServices = mock(UserService.class);
        presenter = new UsersManagerPresenter(null, (UserService) userServices.proxy(),
                (ViewLayout) layoutManager.proxy());

        view = mock(UsersManagerView.class);

        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();

        presenter.display((UsersManagerView) view.proxy());
    }

    public void testShouldCloseViewOnClickOfCloseButton() {
        view.expects(once()).method("close").withNoArguments();

        presenter.doCloseView();
    }

    public void testShouldDeleteUserOnNotifyDelete() throws EmfException {
        Mock userServices = mock(UserService.class);
        userServices.expects(once()).method("deleteUser").with(eq("matts"));

        User user = new User();
        user.setUsername("joe");

        Mock layoutManager = mock(ViewLayout.class);
        UsersManagerPresenter presenter = new UsersManagerPresenter(user, (UserService) userServices.proxy(),
                (ViewLayout) layoutManager.proxy());

        Mock view = createView();
        view.expects(once()).method("refresh").withNoArguments();
        displayView(presenter, view);

        User matts = new User();
        matts.setUsername("matts");
        presenter.doDelete(new User[] { matts });
    }

    private void displayView(UsersManagerPresenter presenter, Mock view) {
        view.expects(once()).method("observe").with(eq(presenter));
        presenter.display((UsersManagerView) view.proxy());
    }

    private Mock createView() {
        Mock view = mock(UsersManagerView.class);
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
        Mock registerUserView = mock(RegisterUserDesktopView.class);
        registerUserView.expects(once()).method("observe").with(new IsInstanceOf(RegisterUserPresenter.class));
        registerUserView.expects(once()).method("display").withNoArguments();

        RegisterUserDesktopView viewProxy = (RegisterUserDesktopView) registerUserView.proxy();
        layoutManager.expects(once()).method("add").with(eq(viewProxy), new IsInstanceOf(String.class));

        view.expects(once()).method("clearMessage").withNoArguments();
        view.expects(once()).method("refresh").withNoArguments();

        presenter.doRegisterNewUser(viewProxy);
    }

    public void testShouldDisplayUpdateUserViewOnDisplayUpdateUser() throws UserException {
        Mock updateUserView = mock(UpdateUserView.class);
        updateUserView.expects(once()).method("observe").with(new IsInstanceOf(UpdateUserPresenter.class));
        updateUserView.expects(once()).method("display").withNoArguments();

        UpdateUserView viewProxy = (UpdateUserView) updateUserView.proxy();
        layoutManager.expects(once()).method("add").with(eq(viewProxy), new IsInstanceOf(String.class));

        view.expects(once()).method("clearMessage").withNoArguments();
        view.expects(once()).method("refresh").withNoArguments();

        User user = new User();
        user.setUsername("name");

        presenter.doUpdateUser(user, viewProxy);
    }

    public void testShouldDisplayTheSameUpdateViewAsPreviouslyDisplayedOnSelectingTheSameUserAndClickingUpdate()
            throws UserException {
        User user = new User();
        user.setUsername("name");

        // 1st attempt
        Mock updateUserView = mock(UpdateUserView.class);
        UpdateUserView viewProxy = createUpdateView(updateUserView);

        view.expects(atLeastOnce()).method("clearMessage").withNoArguments();
        view.expects(once()).method("refresh").withNoArguments();

        presenter.doUpdateUser(user, viewProxy);

        // 2nd attempt
        updateUserView.stubs().method("isAlive").withNoArguments().will(returnValue(Boolean.TRUE));
        updateUserView.expects(once()).method("bringToFront").withNoArguments();

        presenter.doUpdateUser(user, viewProxy);
    }

    public void testShouldDisplayNewUpdateViewIfPreviouslyOpenedUpdateViewIsClosedOnClickingOfUpdateButton()
            throws UserException {
        User user = new User();
        user.setUsername("name");

        // 1st attempt
        Mock view1 = mock(UpdateUserView.class);
        UpdateUserView view1Proxy = createUpdateView(view1);

        view.expects(atLeastOnce()).method("clearMessage").withNoArguments();
        view.expects(atLeastOnce()).method("refresh").withNoArguments();

        presenter.doUpdateUser(user, view1Proxy);

        // 2nd attempt - view1 is closed, view2 will be displayed
        view1.stubs().method("isAlive").withNoArguments().will(returnValue(Boolean.FALSE));

        Mock view2 = mock(UpdateUserView.class);
        UpdateUserView view2Proxy = createUpdateView(view2);

        presenter.doUpdateUser(user, view2Proxy);
    }

    private UpdateUserView createUpdateView(Mock updateView) {
        updateView.expects(once()).method("observe").with(new IsInstanceOf(UpdateUserPresenter.class));
        updateView.expects(once()).method("display").withNoArguments();

        UpdateUserView viewProxy = (UpdateUserView) updateView.proxy();
        layoutManager.expects(once()).method("add").with(eq(viewProxy), new IsInstanceOf(String.class));

        return viewProxy;
    }

    public void testShouldDisplayUpdateUserViewForEachUserSelectedOnDisplayUpdateUser() throws UserException {
        // each user
        Mock updateUserView = mock(UpdateUserView.class);
        updateUserView.expects(once()).method("observe").with(new IsInstanceOf(UpdateUserPresenter.class));
        updateUserView.expects(once()).method("display").withNoArguments();

        UpdateUserView updateUserViewProxy = (UpdateUserView) updateUserView.proxy();
        layoutManager.expects(once()).method("add").with(eq(updateUserViewProxy), new IsInstanceOf(String.class));

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

        UsersManagerPresenter presenter = new UsersManagerPresenter(user, null, null);
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
        UsersManagerPresenter presenter = new UsersManagerPresenter(null, null, null);
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
