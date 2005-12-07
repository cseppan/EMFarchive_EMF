package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.UserService;

public class UpdateUserPresenter {

    private UserService model;

    private UpdateUserView view;

    private boolean userDataChanged;

    public UpdateUserPresenter(UserService model) {
        this.model = model;
    }

    public void display(UpdateUserView view) {
        this.view = view;
        view.observe(this);

        view.display();
    }

    public void doSave(User user) throws EmfException {
        model.updateUser(user);
        this.userDataChanged = false;// reset
    }

    public void doClose() {
        if (userDataChanged) {
            view.closeOnConfirmLosingChanges();
            return;
        }

        view.close();
    }

    public void onChange() {
        this.userDataChanged = true;
    }

}
