package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

public class UserManagerConsoleLauncher {

    public static void main(String[] args) throws Exception {
        UserManagerConsoleLauncher launcher = new UserManagerConsoleLauncher();

        EMFUserAdmin userAdmin = launcher.createUserAdmin();
        UserManagerConsole console = new UserManagerConsole(userAdmin);
        UserManagerPresenter presenter = new UserManagerPresenter(userAdmin, console);
        presenter.observe();

        console.setVisible(true);

        JFrame frame = new JFrame();

        launcher.addAsInternalFrame(console, frame);
        
        frame.setSize(new Dimension(800, 600));
        frame.setLocation(new Point(400, 200));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
    }

    private void addAsInternalFrame(UserManagerConsole console, JFrame frame) {
        JDesktopPane desktop = new JDesktopPane();
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop.add(console);

        frame.setContentPane(desktop);
    }

    private EMFUserAdmin createUserAdmin() throws EmfException {
        List users = new ArrayList();

        users.add(createUser("joe", "Joe Fullman", "joef@zukoswky.com"));
        users.add(createUser("mary", "Mary Joe", "mary@wonderful.net"));
        users.add(createUser("kevin", "Kevin Spacey", "kevin@spacey.com"));

        EMFUserAdmin userAdmin = new EMFUserAdminStub(users);

        return userAdmin;
    }

    private User createUser(String username, String name, String email) throws EmfException {
        User user = new User();
        user.setUserName(username);
        user.setFullName(name);
        user.setEmailAddr(email);

        return user;
    }
}
