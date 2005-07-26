package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

public class LoginPresenter {

    private EMFUserAdmin userAdmin;

    private LoginView view;

    public LoginPresenter(EMFUserAdmin userAdmin, LoginView view) {
        this.userAdmin = userAdmin;
        this.view = view;
    }

    public void notifyLogin(String username, String password) throws EmfException {
        userAdmin.authenticate(username, password, false);
    }

    public void notifyCancel() {
        view.close();
    }

}
