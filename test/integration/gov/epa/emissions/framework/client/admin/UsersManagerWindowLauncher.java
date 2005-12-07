package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.UserService;
import gov.epa.emissions.framework.ui.DefaultViewLayout;
import gov.epa.emissions.framework.ui.ViewLayout;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

public class UsersManagerWindowLauncher {

    public static void main(String[] args) throws Exception {
        UsersManagerWindowLauncher launcher = new UsersManagerWindowLauncher();

        UserService userAdmin = launcher.createUserAdmin();
        JFrame frame = new JFrame();

        JDesktopPane desktop = new JDesktopPane();
        UsersManager view = new UsersManager(null, userAdmin, frame, desktop);

        ViewLayout layoutManager = new DefaultViewLayout(view);
        UsersManagerPresenter presenter = new UsersManagerPresenter(null, userAdmin, layoutManager);
        presenter.display(view);

        launcher.addAsInternalFrame(view, frame, desktop);

        frame.setSize(new Dimension(800, 600));
        frame.setLocation(new Point(400, 200));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void addAsInternalFrame(UsersManager console, JFrame frame, JDesktopPane desktop) {
        desktop.setName("EMF Console");
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop.add(console);

        frame.setContentPane(desktop);
    }

    private UserService createUserAdmin() throws Exception {
        List users = new ArrayList();

        users.add(createUser("joe", "Joe Fullman", "joef@zukoswky.com", false));
        users.add(createUser("mary", "Mary Joe", "mary@wonderful.net", true));
        users.add(createUser("kevin", "Kevin Spacey", "kevin@spacey.com", false));

        UserService userAdmin = new UserServiceStub(users);

        return userAdmin;
    }

    private User createUser(String username, String name, String email, boolean isAdmin) throws Exception {
        User user = new User();
        user.setUsername(username);
        user.setFullName(name);
        user.setEmail(email);
        user.setInAdminGroup(isAdmin);

        return user;
    }
}
