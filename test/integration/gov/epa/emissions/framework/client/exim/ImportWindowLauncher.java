package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.EmfConsolePresenter;
import gov.epa.emissions.framework.client.admin.EMFUserAdminStub;
import gov.epa.emissions.framework.commons.User;
import gov.epa.emissions.framework.commons.UserServices;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

public class ImportWindowLauncher {

    public static void main(String[] args) throws EmfException {
        User user = createUser("joe", "Joe Fullman", "joef@zukoswky.com");
        
        EmfConsole console = new EmfConsole(user, createUserAdmin(user));
        console.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        EmfConsolePresenter presenter = new EmfConsolePresenter(console);
        presenter.observe();

        console.display();
    }

    static private UserServices createUserAdmin(User user) throws EmfException {
        List users = new ArrayList();
        users.add(user);

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
