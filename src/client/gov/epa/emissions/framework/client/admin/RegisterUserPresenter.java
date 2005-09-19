package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

public class RegisterUserPresenter {

    private UserServices model;

    private RegisterUserView view;

    public RegisterUserPresenter(UserServices model) {
        this.model = model;
    }

    // auto-login, upon registration
    public void doRegister(User user) throws EmfException {
        model.createUser(user);

        model.authenticate(user.getUsername(), user.getEncryptedPassword());
    }

    public void doCancel() {
        view.close();
    }

    public void observe(RegisterUserView view) {
        this.view = view;
        this.view.observe(this);
        
        view.display();
    }

}
