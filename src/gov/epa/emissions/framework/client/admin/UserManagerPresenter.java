package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfPresenter;
import gov.epa.emissions.framework.services.UserServices;

public class UserManagerPresenter implements EmfPresenter{

    private UsersManagementView view;
    private UserServices model;

    public UserManagerPresenter(UserServices model, UsersManagementView view) {
        this.model = model;
        this.view = view;
    }

    public void notifyCloseView() {
        view.close();
    }

    public void observe() {
        view.setViewObserver(this);
    }

    public void notifyDelete(String username) throws EmfException {
        model.deleteUser(username);
        view.refresh();
    }

}
