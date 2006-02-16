package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.UserService;
import gov.epa.emissions.framework.ui.ViewLayout;

import org.jmock.Mock;
import org.jmock.core.constraint.IsInstanceOf;

public class UsersManagerPresenterTest extends EmfMockObjectTestCase {

    private Mock view;

    private UsersManagerPresenter presenter;

    private Mock layoutManager;

    private Mock session;

    private Mock service;

    protected void setUp() throws EmfException {
        layoutManager = mock(ViewLayout.class);
        session = mock(EmfSession.class);
        service = mock(UserService.class);
        presenter = new UsersManagerPresenter((EmfSession) session.proxy(), (UserService) service.proxy(),
                (ViewLayout) layoutManager.proxy());

        view = mock(UsersManagerView.class);

        view.expects(once()).method("observe").with(eq(presenter));

        User[] users = new User[0];
        stub(service, "getUsers", users);
        expectsOnce(view, "display", users);

        presenter.display((UsersManagerView) view.proxy());
    }

    public void testShouldCloseViewOnClickOfCloseButton() {
        view.expects(once()).method("close").withNoArguments();
        presenter.doClose();
    }

    public void testShouldDeleteUserOnNotifyDelete() throws Exception {
        User user = new User();
        user.setUsername("joe");
        stub(session, "user", user);

        view.expects(once()).method("refresh").withNoArguments();

        User matts = new User();
        matts.setUsername("matts");
        service.expects(once()).method("deleteUser").with(same(matts));

        presenter.doDelete(new User[] { matts });
    }

    public void testShouldDisplayRegisterNewUserViewOnDisplayRegisterNewUser() {
        Mock registerUserView = mock(RegisterUserDesktopView.class);
        registerUserView.expects(once()).method("observe").with(new IsInstanceOf(RegisterUserPresenter.class));
        registerUserView.expects(once()).method("display");

        RegisterUserDesktopView viewProxy = (RegisterUserDesktopView) registerUserView.proxy();
        layoutManager.expects(once()).method("add").with(eq(viewProxy), new IsInstanceOf(String.class));

        view.expects(once()).method("refresh").withNoArguments();

        presenter.doRegisterNewUser(viewProxy);
    }

    public void testShouldDisplayUpdateUserViewOnDisplayUpdateUser() throws Exception {
        Mock updateUserView = mock(UpdatableUserView.class);

        UpdatableUserView viewProxy = (UpdatableUserView) updateUserView.proxy();
        layoutManager.expects(once()).method("add").with(eq(viewProxy), new IsInstanceOf(String.class));

        view.expects(once()).method("refresh").withNoArguments();

        User user = new User();
        user.setUsername("name");

        Mock updatePresenter = mock(UpdateUserPresenter.class);
        updatePresenter.expects(once()).method("display").with(same(viewProxy), eq(null));

        presenter.updateUser(user, viewProxy, null, (UpdateUserPresenter) updatePresenter.proxy());
    }

    public void testShouldRefreshViewOnClickOfRefreshButton() throws EmfException {
        User[] users = new User[0];
        view.expects(once()).method("refresh").with(same(users));
        service.stubs().method("getUsers").will(returnValue(users));

        presenter.doRefresh();
    }

    public void testShouldNotDeleteCurrentlyLoggedInUserOnNotifyDelete() throws Exception {
        User user = new User();
        user.setUsername("joe");
        stub(session, "user", user);

        try {
            presenter.doDelete(new User[] { user });
        } catch (EmfException e) {
            assertEquals("Cannot delete yourself - '" + user.getUsername() + "'", e.getMessage());
            return;
        }

        fail("should not have allowed deletion of currently logged in user");
    }

    public void testShouldNotDeleteAdminOnNotifyDelete() throws Exception {
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
