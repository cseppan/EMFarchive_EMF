package gov.epa.emissions.framework.client.login.old;

import gov.epa.emissions.framework.commons.User;

public class Main {

    public static User CurrentUser = null;

    public static boolean LoggedOnAdmin = false;

    public static void main(String[] args) {
        UserManagerClient umc = new UserManagerClient();
        User CurrentUser = new User("", "", "", "", "", "", false, false);

        while (true) {
            LoginWindow logwin = new LoginWindow(CurrentUser, umc);
            if (logwin.getGoodReturn()) {
                CurrentUser = logwin.getCurrentUser();
                LoggedOnAdmin = logwin.getAdmLogin();
            }

            MainGUI mgwin = new MainGUI(CurrentUser, umc, LoggedOnAdmin);
            while (!mgwin.getDoneFlag()) {
            }
            mgwin.dispose();
        }
    }
}
