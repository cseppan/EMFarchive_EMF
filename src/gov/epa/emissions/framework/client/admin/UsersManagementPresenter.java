package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

public class UsersManagementPresenter {

    private UsersManagementView view;
    private EMFUserAdmin model;

    public UsersManagementPresenter(EMFUserAdmin model, UsersManagementView view) {
        this.model = model;
        this.view = view;
    }

    public void notifyCloseView() {
        view.close();
    }

    public void init() {
        view.setViewObserver(this);
    }

    public void notifyDelete(String username) throws EmfException {
        model.deleteUser(username);
        view.refresh();
    }

}
