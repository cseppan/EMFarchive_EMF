package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.UserService;
import gov.epa.emissions.framework.ui.ViewLayout;

public class UsersManagerPresenter {

    private UsersManagerView view;

    private UserService userServices;

    private ViewLayout layoutManager;

    private EmfSession session;

    public UsersManagerPresenter(EmfSession session, UserService userServices, ViewLayout layoutManager) {
        this.session = session;
        this.userServices = userServices;
        this.layoutManager = layoutManager;
    }

    public void doCloseView() {
        view.close();
    }

    public void display(UsersManagerView view) {
        this.view = view;
        view.observe(this);

        view.display();
    }

    public void doDelete(User[] users) throws EmfException {
        view.clearMessage();

        if (users.length == 0) {
            view.showMessage("To delete, please select at least one User.");
            return;
        }

        if (!view.promptDelete(users))
            return;

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
        userServices.deleteUser(userToDelete);
    }

    public void doRegisterNewUser(RegisterUserDesktopView registerUserView) {
        view.clearMessage();
        layoutManager.add(registerUserView, "Register New User");

        RegisterUserPresenter registerPresenter = new RegisterUserPresenter(userServices);
        registerPresenter.display(registerUserView);

        view.refresh();
    }

    public void doUpdateUser(User updateUser, UpdatableUserView updatable, UserView viewable) throws EmfException {
        UpdateUserPresenter updatePresenter = new UpdateUserPresenterImpl(session, updateUser, userServices);
        updateUser(updateUser, updatable, viewable, updatePresenter);
    }

    void updateUser(User user, UpdatableUserView updatable, UserView viewable, UpdateUserPresenter updatePresenter)
            throws EmfException {
        view.clearMessage();

        showUpdateUser(user, updatable, viewable, updatePresenter);
    }

    private void showUpdateUser(User updateUser, UpdatableUserView updatable, UserView viewable,
            UpdateUserPresenter updatePresenter) throws EmfException {
        layoutManager.add(updatable, "Update - " + updateUser.getUsername());

        updatePresenter.display(updatable, viewable);

        view.refresh();
    }

    public void doUpdateUsers(User[] users) throws EmfException {
        if (users.length == 0) {
            view.showMessage("To update, please select at least one User.");
            return;
        }

        for (int i = 0; i < users.length; i++) {
            UpdatableUserView updatable = view.getUpdateUserView(users[i]);
            UserView viewable = view.getUserView();
            doUpdateUser(users[i], updatable, viewable);
        }
    }

}
