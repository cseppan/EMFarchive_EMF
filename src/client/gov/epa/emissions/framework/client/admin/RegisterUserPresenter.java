package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfPresenter;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

public class RegisterUserPresenter implements EmfPresenter {

    private UserServices model;

    private RegisterUserView view;

    public RegisterUserPresenter(UserServices model, RegisterUserView view) {
        this.model = model;
        this.view = view;
    }

    public void notifyRegister(User user) throws EmfException {
        model.createUser(user);

        // TODO: should not autologin if request is from UserManager.
        // TODO: what's the admin status ?
        model.authenticate(user.getUserName(), user.getPassword(), false);
    }

    public void notifyCancel() {
        view.close();
    }

    public void observe() {
        this.view.observe(this);
    }

}
