package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.UserService;
import gov.epa.emissions.framework.ui.ViewLayout;

public class UsersManagerPresenter {

    private UsersManagerView view;

    private UserService service;

    private ViewLayout layoutManager;

    private EmfSession session;

    public UsersManagerPresenter(EmfSession session, UserService service, ViewLayout layoutManager) {
        this.session = session;
        this.service = service;
        this.layoutManager = layoutManager;
    }

    public void doClose() {
        view.close();
    }

    public void display(UsersManagerView view) throws EmfException {
        this.view = view;
        view.observe(this);

        view.display(service.getUsers());
    }

    public void doDelete(User[] users) throws EmfException {
        for (int i = 0; i < users.length; i++)
            doDelete(users[i]);

        view.refresh();
    }

    private void doDelete(User userToDelete) throws EmfException {
        // NOTE: super user's name is fixed
        if (userToDelete.getUsername().equals("admin"))
            throw new EmfException("Cannot delete EMF super user - '" + userToDelete.getUsername() + "'");

        User loggedIn = session.user();
        if (loggedIn.getUsername().equals(userToDelete.getUsername()))
            throw new EmfException("Cannot delete yourself - '" + userToDelete.getUsername() + "'");

        // FIXME: obtain lock, and then delete
        service.deleteUser(userToDelete);
    }

    public void doRegisterNewUser(RegisterUserDesktopView registerUserView) {
        layoutManager.add(registerUserView, "Register New User");

        RegisterUserPresenter registerPresenter = new RegisterUserPresenter(service);
        registerPresenter.display(registerUserView);

        view.refresh();
    }

    public void doUpdateUser(User updateUser, UpdatableUserView updatable, UserView viewable) throws EmfException {
        UpdateUserPresenter updatePresenter = new UpdateUserPresenterImpl(session, updateUser, service);
        updateUser(updateUser, updatable, viewable, updatePresenter);
    }

    void updateUser(User user, UpdatableUserView updatable, UserView viewable, UpdateUserPresenter updatePresenter)
            throws EmfException {
        showUpdateUser(user, updatable, viewable, updatePresenter);
    }

    private void showUpdateUser(User updateUser, UpdatableUserView updatable, UserView viewable,
            UpdateUserPresenter updatePresenter) throws EmfException {
        layoutManager.add(updatable, "Update - " + updateUser.getUsername());
        updatePresenter.display(updatable, viewable);
        view.refresh();
    }

    public void doRefresh() throws EmfException {
        view.refresh(service.getUsers());
    }

    public void doUpdateUsers(User[] users) throws EmfException {
        for (int i = 0; i < users.length; i++) {
            UpdatableUserView updatable = view.getUpdateUserView(users[i]);
            UserView viewable = view.getUserView();
            doUpdateUser(users[i], updatable, viewable);
        }
    }

}
