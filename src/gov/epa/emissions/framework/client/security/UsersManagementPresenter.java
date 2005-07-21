package gov.epa.emissions.framework.client.security;

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

    public void notifyDelete(String username) {
        model.deleteUser(username);
        view.refresh();
    }

}
