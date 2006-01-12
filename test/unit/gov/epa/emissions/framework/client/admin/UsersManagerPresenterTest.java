package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.UserService;
import gov.epa.emissions.framework.ui.ViewLayout;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class UsersManagerPresenterTest extends MockObjectTestCase {

    private Mock view;

    private UsersManagerPresenter presenter;

    private Mock layoutManager;

    private Mock session;

    protected void setUp() {
        layoutManager = mock(ViewLayout.class);
        session = mock(EmfSession.class);
        Mock service = mock(UserService.class);
        presenter = new UsersManagerPresenter((EmfSession) session.proxy(), (UserService) service.proxy(),
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

    public void testShouldDeleteUserOnNotifyDelete() throws Exception {
        Mock userServices = mock(UserService.class);

        User user = new User();
        user.setUsername("joe");

        Mock layoutManager = mock(ViewLayout.class);
        Mock session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(user));
        UsersManagerPresenter presenter = new UsersManagerPresenter((EmfSession) session.proxy(),
                (UserService) userServices.proxy(), (ViewLayout) layoutManager.proxy());

        Mock view = createView();
        view.expects(once()).method("refresh").withNoArguments();
        displayView(presenter, view);

        User matts = new User();
        matts.setUsername("matts");

        userServices.expects(once()).method("deleteUser").with(same(matts));

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

    public void testShouldDisplayUpdateUserViewOnDisplayUpdateUser() throws Exception {
        Mock updateUserView = mock(UpdatableUserView.class);

        UpdatableUserView viewProxy = (UpdatableUserView) updateUserView.proxy();
        layoutManager.expects(once()).method("add").with(eq(viewProxy), new IsInstanceOf(String.class));

        view.expects(once()).method("clearMessage").withNoArguments();
        view.expects(once()).method("refresh").withNoArguments();

        User user = new User();
        user.setUsername("name");

        Mock updatePresenter = mock(UpdateUserPresenter.class);
        updatePresenter.expects(once()).method("display").with(same(viewProxy), eq(null));

        presenter.updateUser(user, viewProxy, null, (UpdateUserPresenter) updatePresenter.proxy());
    }

    public void testShouldDisplayNewUpdateViewIfPreviouslyOpenedUpdateViewIsClosedOnClickingOfUpdateButton()
            throws Exception {
        User user = new User();
        user.setUsername("name");

        // 1st attempt
        Mock view1 = mock(UpdatableUserView.class);
        UpdatableUserView view1Proxy = (UpdatableUserView) view1.proxy();
        layoutManager.expects(once()).method("add").with(eq(view1Proxy), new IsInstanceOf(String.class));

        view.expects(atLeastOnce()).method("clearMessage").withNoArguments();
        view.expects(atLeastOnce()).method("refresh").withNoArguments();

        Mock updatePresenter = mock(UpdateUserPresenter.class);
        updatePresenter.expects(once()).method("display").with(same(view1Proxy), eq(null));

        presenter.updateUser(user, view1Proxy, null, (UpdateUserPresenter) updatePresenter.proxy());

        // 2nd attempt - view1 is closed, view2 will be displayed
        view1.stubs().method("isAlive").withNoArguments().will(returnValue(Boolean.FALSE));

        Mock view2 = mock(UpdatableUserView.class);
        UpdatableUserView view2Proxy = (UpdatableUserView) view2.proxy();
        layoutManager.expects(once()).method("add").with(eq(view2Proxy), new IsInstanceOf(String.class));
        updatePresenter.expects(once()).method("display").with(same(view2Proxy), eq(null));

        presenter.updateUser(user, view2Proxy, null, (UpdateUserPresenter) updatePresenter.proxy());
    }

    public void testShouldDisplayMessageIfNoUsersAreSelectedForUpdate() throws Exception {
        view.expects(once()).method("showMessage").with(eq("To update, please select at least one User."));

        presenter.doUpdateUsers(new User[] {});
    }

    public void testShouldNotDeleteCurrentlyLoggedInUserOnNotifyDelete() throws Exception {
        User user = new User();
        user.setUsername("joe");

        Mock session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(user));

        UsersManagerPresenter presenter = new UsersManagerPresenter((EmfSession) session.proxy(), null, null);
        displayView(presenter, createView());

        try {
            presenter.doDelete(new User[] { user });
        } catch (EmfException e) {
            assertEquals("Cannot delete yourself - '" + user.getUsername() + "'", e.getMessage());
            return;
        }

        fail("should not have allowed deletion of currently logged in user");
    }

    public void testShouldNotDeleteAdminOnNotifyDelete() throws Exception {
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
