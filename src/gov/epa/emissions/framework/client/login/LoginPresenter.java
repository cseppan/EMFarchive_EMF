package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfPresenter;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

public class LoginPresenter implements EmfPresenter {

    private EMFUserAdmin userAdmin;

    private LoginView view;

    public LoginPresenter(EMFUserAdmin model, LoginView view) {
        this.userAdmin = model;
        this.view = view;
    }

    public User notifyLogin(String username, String password) throws EmfException {
        userAdmin.authenticate(username, password, false);
        return userAdmin.getUser(username);
    }

    public void notifyCancel() {
        view.close();
    }

    public void observe() {
        view.setObserver(this);
    }

}
