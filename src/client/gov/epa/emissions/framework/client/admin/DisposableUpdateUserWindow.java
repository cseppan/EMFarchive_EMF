package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.console.DesktopManager;

public class DisposableUpdateUserWindow extends UpdateUserWindow {

    public DisposableUpdateUserWindow(User user, AdminOption adminOption, DesktopManager desktopManager) {
        super(user, adminOption, desktopManager);
    }

    public DisposableUpdateUserWindow(User user, DesktopManager desktopManager) {
        super(user, desktopManager);
    }

    public void close() {
        super.dispose();
    }

    public boolean isAlive() {
        return !super.isClosed();
    }

    public void windowClosing() {
        close();
    }
}
