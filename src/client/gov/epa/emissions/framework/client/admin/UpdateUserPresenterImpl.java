package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.UserService;

public class UpdateUserPresenterImpl implements UpdateUserPresenter {

    private UserService service;

    private UpdatableUserView view;

    private boolean userDataChanged;

    private User user;

    private EmfSession session;

    public UpdateUserPresenterImpl(EmfSession session, User user, UserService service) {
        this.session = session;
        this.user = user;
        this.service = service;
    }

    public void display(UpdatableUserView view) throws EmfException {
        user = service.obtainLocked(session.user(), user);

        this.view = view;
        view.observe(this);

        view.display();
    }

    public void doSave() throws EmfException {
        service.updateUser(user);
        this.userDataChanged = false;// reset
    }

    public void doClose() throws EmfException {
        if (userDataChanged) {
            view.closeOnConfirmLosingChanges();
            return;
        }

        service.releaseLocked(user);
        view.close();
    }

    public void onChange() {
        this.userDataChanged = true;
    }

}
