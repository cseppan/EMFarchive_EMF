package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfPresenter;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

public class UpdateUserPresenter implements EmfPresenter {

    private EMFUserAdmin model;
    private UpdateUserView view;

    public UpdateUserPresenter(EMFUserAdmin model, UpdateUserView view) {
        this.model = model;
        this.view = view;
    }

    public void observe() {
        view.setObserver(this);
    }

    public void notifyUpdate(User user) throws EmfException {
        model.updateUser(user);
    }

    public void notifyCancel() {
        view.close();
    }

}
