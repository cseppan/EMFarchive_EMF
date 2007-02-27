package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.commons.CommonsException;
import gov.epa.emissions.commons.security.PasswordGenerator;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.UserService;

public class LoginPresenter {

    private UserService userAdmin;

    private LoginView view;
    
    private String update;

    public LoginPresenter(UserService model) {
        this.userAdmin = model;
    }

    public User doLogin(String username, String password) throws EmfException {
        try {
            userAdmin.authenticate(username, new PasswordGenerator().encrypt(password));
        } catch (CommonsException e) {
            throw new EmfException(e.getMessage());
        }
        return userAdmin.getUser(username);
    }

    public boolean checkEmfVersion(String current) throws EmfException {
        try {
            update = userAdmin.getEmfVersion();
            
            if (update == null)
                return true;
            
            return update.trim().equalsIgnoreCase(current);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }
    
    public String getUpdatedEmfVersion() {
        return update;
    }

    public void doCancel() {
        view.disposeView();
    }

    public void display(LoginView view) {
        this.view = view;
        view.observe(this);

        view.display();
    }

}
