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

    public void doDelete(String username) throws EmfException {
        if (username.equals("admin"))// NOTE: super user's name is fixed
            throw new UserException("Cannot delete EMF super user - '" + username + "'");

        if (user.getUsername().equals(username))
            throw new UserException("Cannot delete yourself - '" + username + "'");

        model.deleteUser(username);
        view.refresh();
    }

}
