package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

public class UserManagerPresenter {

    private UserManagerView view;

    private UserServices model;

    private User user;

    public UserManagerPresenter(User user, UserServices model) {
        this.user = user;
        this.model = model;
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
        for (int i = 0; i < users.length; i++) {
            doDelete(users[i]);
        }

        view.refresh();
    }

    private void doDelete(User userToDelete) throws UserException, EmfException {
        // NOTE: super user's name is fixed
        if (userToDelete.getUsername().equals("admin"))
            throw new UserException("Cannot delete EMF super user - '" + userToDelete.getUsername() + "'");

        if (user.getUsername().equals(userToDelete.getUsername()))
            throw new UserException("Cannot delete yourself - '" + userToDelete.getUsername() + "'");

        model.deleteUser(userToDelete.getUsername());
    }

}
