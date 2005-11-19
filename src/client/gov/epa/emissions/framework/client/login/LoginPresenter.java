package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.PasswordService;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserService;

public class LoginPresenter {

    private UserService userAdmin;

    private LoginView view;

    public LoginPresenter(UserService model) {
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

    public void display(LoginView view) {
        this.view = view;
        view.observe(this);

        view.display();
    }

}
