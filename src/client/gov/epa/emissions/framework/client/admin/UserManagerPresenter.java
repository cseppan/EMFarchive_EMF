package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;
import gov.epa.emissions.framework.ui.WindowLayoutManager;

public class UserManagerPresenter {

    private UserManagerView view;

    private UserServices userServices;

    private User user;

    private WindowLayoutManager layoutManager;

    public UserManagerPresenter(User user, UserServices userServices, WindowLayoutManager layoutManager) {
        this.user = user;
        this.userServices = userServices;
        this.layoutManager = layoutManager;
    }

    public void doCloseView() {
        view.close();
    }

    public void display(UserManagerView view) {
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

    private void doDelete(User userToDelete) throws UserException, EmfException {
        // NOTE: super user's name is fixed
        if (userToDelete.getUsername().equals("admin"))
            throw new UserException("Cannot delete EMF super user - '" + userToDelete.getUsername() + "'");

        if (user.getUsername().equals(userToDelete.getUsername()))
            throw new UserException("Cannot delete yourself - '" + userToDelete.getUsername() + "'");

        userServices.deleteUser(userToDelete.getUsername());
    }

    public void doRegisterNewUser(RegisterUserView registerUserView) {
        view.clearMessage();
        layoutManager.add(registerUserView);

        RegisterUserPresenter registerPresenter = new RegisterUserPresenter(userServices);
        registerPresenter.display(registerUserView);

        view.refresh();
    }

    public void doUpdateUser(UpdateUserView updateUserView) {
        view.clearMessage();
        layoutManager.add(updateUserView);

        UpdateUserPresenter updateUserPresenter = new UpdateUserPresenter(userServices);
        updateUserPresenter.display(updateUserView);

        view.refresh();
    }

    public void doUpdateUsers(User[] users) {
        if (users.length == 0) {
            view.showMessage("To update, please select at least one User.");
            return;
        }

        for (int i = 0; i < users.length; i++) {
            UpdateUserView updateUserView = view.getUpdateUserView(users[i]);
            doUpdateUser(updateUserView);
        }
    }

}
