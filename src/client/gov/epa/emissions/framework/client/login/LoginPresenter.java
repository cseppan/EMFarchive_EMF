package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.PasswordService;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

public class LoginPresenter {

    private UserServices userAdmin;

    private LoginView view;

    public LoginPresenter(UserServices model) {
        this.userAdmin = model;
    }

    public User doLogin(String username, String password) throws EmfException {
        // FIXME: replace statics w/ objects
        userAdmin.authenticate(username, PasswordService.encrypt(password));
        return userAdmin.getUser(username);
    }

    public void doCancel() {
        view.close();
    }

    public void observe(LoginView view) {
        this.view = view;
        view.setObserver(this);

        view.display();
    }

}
