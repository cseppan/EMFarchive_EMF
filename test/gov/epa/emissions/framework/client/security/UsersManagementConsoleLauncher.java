package gov.epa.emissions.framework.client.security;

import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import java.util.ArrayList;
import java.util.List;

public class UsersManagementConsoleLauncher {

    public static void main(String[] args) {
        UsersManagementConsoleLauncher launcher = new UsersManagementConsoleLauncher();
        
        EMFUserAdmin userAdmin = launcher.createUserAdmin();        
        UsersManagementConsole console = new UsersManagementConsole(userAdmin);
        
        UsersManagementPresenter presenter = new UsersManagementPresenter(userAdmin, console);
        presenter.init();
        
        console.show();
    }

    private EMFUserAdmin createUserAdmin() {
        List users = new ArrayList();

        users.add(createUser("joe", "Joe Fullman", "joef@zukoswky.com"));
        users.add(createUser("mary", "Mary Joe", "mary@wonderful.net"));
        users.add(createUser("kevin", "Kevin Spacey", "kevin@spacey.com"));

        EMFUserAdmin userAdmin = new EMFUserAdminStub(users);
        
        return userAdmin;
    }

    private User createUser(String username, String name, String email) {
        User user = new User();
        user.setUserName(username);
        user.setFullName(name);
        user.setEmailAddr(email);

        return user;
    }
}
