package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.UserService;

public class UpdateUserPresenterImpl implements UpdateUserPresenter {

    private UserService service;

    private UpdatableUserView updateView;

    private boolean userDataChanged;

    private User user;

    private EmfSession session;

    public UpdateUserPresenterImpl(EmfSession session, User user, UserService service) {
        this.session = session;
        this.user = user;
        this.service = service;
    }

    public void display(UpdatableUserView update, UserView view) throws EmfException {
        user = service.obtainLocked(session.user(), user);

        if (!user.isLocked(session.user())) {// view mode, locked by another user
            new ViewUserPresenterImpl(user).display(view);
            update.close();// close the unnecessary
            return;
        }

        this.updateView = update;
        updateView.observe(this);

        updateView.display(user);
    }

    public void doSave() throws EmfException {
        service.updateUser(user);
        this.userDataChanged = false;// reset
    }

    public void doClose() throws EmfException {
        if (userDataChanged) {
            updateView.closeOnConfirmLosingChanges();
            return;
        }

        service.releaseLocked(user);
        updateView.close();
    }

    public void onChange() {
        this.userDataChanged = true;
    }

}
