package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.services.User;

public class DisposableUpdateUserWindow extends UpdateUserWindow {

    public DisposableUpdateUserWindow(User user, AdminOption adminOption) {
        super(user, adminOption);
    }

    public DisposableUpdateUserWindow(User user) {
        super(user);
    }

    public void close() {
        super.dispose();
    }

    public boolean isAlive() {
        return !super.isClosed();
    }
}
