package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.transport.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.jmock.Mock;
import org.jmock.core.stub.ReturnStub;

public class UserManagementConsoleLauncher {

    public static void main(String[] args) {
        UserManagementConsoleLauncher launcher = new UserManagementConsoleLauncher();
        
        UserManagementConsole console = launcher.createConsole();
        launcher.show(console);        
    }

    private void show(UserManagementConsole console) {        
        JFrame frame = new JFrame();
        frame.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent event) {
                System.exit(0);
            }
        });
        frame.setSize(new Dimension(500, 150));
        frame.setLocation(new Point(400, 200));
        frame.setTitle("User Management Console");
        
        frame.getContentPane().add(console);
        
        frame.show();	
    }

    private UserManagementConsole createConsole() {
        Mock userAdmin = createUserAdmin();

        UserManagementConsole console = new UserManagementConsole(
                (EMFUserAdmin) userAdmin.proxy());
        return console;
    }

    private Mock createUserAdmin() {
        List users = new ArrayList();
        
        users.add(createUser("joe", "Joe Fullman", "joef@zukoswky.com"));
        users.add(createUser("mary", "Mary Joe", "mary@wonderful.net"));
        users.add(createUser("kevin", "Kevin Spacey", "kevin@spacey.com"));
        
        
        Mock userAdmin = new Mock(EMFUserAdmin.class);
        userAdmin.stubs().method("getUsers").withNoArguments().will(new ReturnStub(users));

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
