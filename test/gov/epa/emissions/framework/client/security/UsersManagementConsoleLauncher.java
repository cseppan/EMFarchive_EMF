package gov.epa.emissions.framework.client.security;

import gov.epa.emissions.framework.client.security.UsersManagementConsole;
import gov.epa.emissions.framework.client.security.UsersManagementPresenter;
import gov.epa.emissions.framework.client.transport.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Mock;
import org.jmock.core.stub.ReturnStub;

public class UsersManagementConsoleLauncher {

    public static void main(String[] args) {
        UsersManagementConsoleLauncher launcher = new UsersManagementConsoleLauncher();
        
        Mock userAdmin = launcher.createUserAdmin();
        EMFUserAdmin userAdminProxy = (EMFUserAdmin) userAdmin.proxy();
        
        UsersManagementConsole console = new UsersManagementConsole(userAdminProxy);
        
        UsersManagementPresenter presenter = new UsersManagementPresenter(userAdminProxy, console);
        presenter.init();
        
        console.show();
    }

    private Mock createUserAdmin() {
        List users = new ArrayList();

        users.add(createUser("joe", "Joe Fullman", "joef@zukoswky.com"));
        users.add(createUser("mary", "Mary Joe", "mary@wonderful.net"));
        users.add(createUser("kevin", "Kevin Spacey", "kevin@spacey.com"));

        Mock userAdmin = new Mock(EMFUserAdmin.class);
        userAdmin.stubs().method("getUsers").withNoArguments().will(new ReturnStub(users));
        userAdmin.stubs().method("deleteUser");
        
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
