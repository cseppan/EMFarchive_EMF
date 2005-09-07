package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

public class UserManagerWindowLauncher {

    public static void main(String[] args) throws Exception {
        UserManagerWindowLauncher launcher = new UserManagerWindowLauncher();

        UserServices userAdmin = launcher.createUserAdmin();
        JFrame frame = new JFrame();

        UserManagerWindow console = new UserManagerWindow(userAdmin, frame);
        UserManagerPresenter presenter = new UserManagerPresenter(userAdmin, console);
        presenter.observe();

        console.setVisible(true);

        launcher.addAsInternalFrame(console, frame);
        
        frame.setSize(new Dimension(800, 600));
        frame.setLocation(new Point(400, 200));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
    }

    private void addAsInternalFrame(UserManagerWindow console, JFrame frame) {
        JDesktopPane desktop = new JDesktopPane();
        desktop.setName("EMF Console");
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop.add(console);

        frame.setContentPane(desktop);
    }

    private UserServices createUserAdmin() throws EmfException {
        List users = new ArrayList();

        users.add(createUser("joe", "Joe Fullman", "joef@zukoswky.com", false));
        users.add(createUser("mary", "Mary Joe", "mary@wonderful.net", true));
        users.add(createUser("kevin", "Kevin Spacey", "kevin@spacey.com", false));

        UserServices userAdmin = new UserServicesStub(users);

        return userAdmin;
    }

    private User createUser(String username, String name, String email, boolean isAdmin) throws EmfException {
        User user = new User();
        user.setUserName(username);
        user.setFullName(name);
        user.setEmailAddr(email);
        user.setInAdminGroup(isAdmin);
        
        return user;
    }
}
