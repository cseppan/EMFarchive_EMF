package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.admin.EMFUserAdminStub;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

public class EmfConsoleLauncherWithMockData {

    public static void main(String[] args) throws EmfException {
        EmfConsole console = new EmfConsole(null, createUserAdmin());
        console.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        EmfConsolePresenter presenter = new EmfConsolePresenter(console);
        presenter.observe();

        console.display();
    }

    static private UserServices createUserAdmin() throws EmfException {
        List users = new ArrayList();

        users.add(createUser("joe", "Joe Fullman", "joef@zukoswky.com"));
        users.add(createUser("mary", "Mary Joe", "mary@wonderful.net"));
        users.add(createUser("kevin", "Kevin Spacey", "kevin@spacey.com"));

        UserServices userAdmin = new EMFUserAdminStub(users);

        return userAdmin;
    }

    static private User createUser(String username, String name, String email) throws EmfException {
        User user = new User();
        user.setUserName(username);
        user.setFullName(name);
        user.setEmailAddr(email);

        return user;
    }

}
