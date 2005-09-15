package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.client.EmfPresenter;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

public class UserManagerPresenter implements EmfPresenter{

    private UserManagerView view;
    private UserServices model;
    private User user;

    public UserManagerPresenter(User user, UserServices model, UserManagerView view) {
        this.user = user;
        this.model = model;
        this.view = view;
    }

    public void notifyCloseView() {
        view.close();
    }

    public void observe() {
        view.setObserver(this);
    }

    public void notifyDelete(String username) throws EmfException {
        if(username.equals("admin"))//NOTE: super user's name is fixed
            throw new UserException("Cannot delete EMF super user - '" + username + "'");
        
        if(user.getUsername().equals(username))
            throw new UserException("Cannot delete yourself - '" + username + "'");
        
        model.deleteUser(username);
        view.refresh();
    }

}
