package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

public class UpdateUserPresenter {

    private UserServices model;

    private UpdateUserView view;

    private boolean userDataChanged;

    public UpdateUserPresenter(UserServices model) {
        this.model = model;
    }

    public void observe(UpdateUserView view) {
        this.view = view;
        view.setObserver(this);

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
