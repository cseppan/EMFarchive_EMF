package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.UserService;
import gov.epa.emissions.framework.ui.ViewLayout;

import java.util.HashMap;
import java.util.Map;

public class UsersManagerPresenter {

    private UsersManagerView view;

    private UserService userServices;

    private ViewLayout layoutManager;

    private Map updateViewsMap;

    private EmfSession session;

    public UsersManagerPresenter(EmfSession session, UserService userServices, ViewLayout layoutManager) {
        this.session = session;
        this.userServices = userServices;
        this.layoutManager = layoutManager;

        updateViewsMap = new HashMap();
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

        //FIXME: obtain lock, and then delete
        userServices.deleteUser(userToDelete);
    }

    public void doRegisterNewUser(RegisterUserDesktopView registerUserView) {
        view.clearMessage();
        layoutManager.add(registerUserView, "Register New User");

        RegisterUserPresenter registerPresenter = new RegisterUserPresenter(userServices);
        registerPresenter.display(registerUserView);

        view.refresh();
    }

    public void doUpdateUser(User updateUser, UpdatableUserView updatableView) throws EmfException {
        UpdateUserPresenter updatePresenter = new UpdateUserPresenterImpl(session, updateUser, userServices);
        updateUser(updateUser, updatableView, updatePresenter);
    }

    void updateUser(User updateUser, UpdatableUserView updatableView, UpdateUserPresenter updatePresenter)
            throws EmfException {
        view.clearMessage();

        if (isUpdateUserViewAlive(updateUser)) {
            updateUserView(updateUser).bringToFront();
            return;
        }

        showUpdateUser(updateUser, updatableView, updatePresenter);
    }

    private boolean isUpdateUserViewAlive(User updateUser) {
        return updateViewsMap.containsKey(updateUser) && updateUserView(updateUser).isAlive();
    }

    private UpdatableUserView updateUserView(User updateUser) {
        return (UpdatableUserView) updateViewsMap.get(updateUser);
    }

    private void showUpdateUser(User updateUser, UpdatableUserView updateView, UpdateUserPresenter updatePresenter)
            throws EmfException {
        layoutManager.add(updateView, "Update - " + updateUser.getUsername());

        updatePresenter.display(updateView);

        view.refresh();
        updateViewsMap.put(updateUser, updateView);
    }

    public void doUpdateUsers(User[] users) throws EmfException {
        if (users.length == 0) {
            view.showMessage("To update, please select at least one User.");
            return;
        }

        for (int i = 0; i < users.length; i++) {
            UpdatableUserView updateUserView = view.getUpdateUserView(users[i]);
            doUpdateUser(users[i], updateUserView);
        }
    }

}
