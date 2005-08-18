package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfPresenter;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

public class UpdateUserPresenter implements EmfPresenter {

    private UserServices model;

    private UpdateUserView view;

    private boolean userDataChanged;

    public UpdateUserPresenter(UserServices model, UpdateUserView view) {
        this.model = model;
        this.view = view;
    }

    public void observe() {
        view.setObserver(this);
    }

    public void notifySave(User user) throws EmfException {
        model.updateUser(user);
        this.userDataChanged = false;// reset
    }

    public void notifyClose() {
        if (userDataChanged) {
            view.closeOnConfirmLosingChanges();
            return;
        }

        view.close();
    }

    public void notifyChanges() {
        this.userDataChanged = true;
    }

}
