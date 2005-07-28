package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfPresenter;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

public class LoginPresenter implements EmfPresenter {

    private EMFUserAdmin userAdmin;

    private LoginView view;

    public LoginPresenter(EMFUserAdmin model, LoginView view) {
        this.userAdmin = model;
        this.view = view;
    }

    public void notifyLogin(String username, String password) throws EmfException {
        userAdmin.authenticate(username, password, false);
    }

    public void notifyCancel() {
        view.close();
    }

    public void observe() {
        view.setObserver(this);
    }

    public void notifyResetPassword(String username) {
    }

}
