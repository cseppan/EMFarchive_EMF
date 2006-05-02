package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.UserService;

public class RegisterUserPresenter {

    private UserService model;

    private RegisterUserView view;

    public RegisterUserPresenter(UserService model) {
        this.model = model;
    }

    // auto-login, upon registration
    public void doRegister(User user) throws EmfException {
        model.createUser(user);

        model.authenticate(user.getUsername(), user.getEncryptedPassword());
    }

    public void doCancel() {
        view.disposeView();
    }

    public void display(RegisterUserView view) {
        this.view = view;
        this.view.observe(this);
        
        view.display();
    }

}
