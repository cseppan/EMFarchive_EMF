package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.util.Collections;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

public class UpdateUserWindowLauncher {

    public static void main(String[] args) throws Exception {
        UserServices userAdmin = new UserServicesStub(Collections.EMPTY_LIST);
        User user = new User();
        user.setUsername("joes");
        user.setFullName("Joe Schmoe");
        user.setAffiliation("Joey Inc");
        user.setPhone("123-123-123");
        user.setEmail("joes@joey.inc");

        JDesktopPane desktop = new JDesktopPane();
        UpdateUserWindow window = new UpdateUserWindow(user, desktop);
        UpdateUserPresenter presenter = new UpdateUserPresenter(userAdmin, window);
        presenter.observe();

        window.display();

        JFrame frame = new JFrame();

        addAsInternalFrame(window, frame, desktop);

        frame.setSize(new Dimension(800, 600));
        frame.setLocation(new Point(400, 200));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void addAsInternalFrame(Container window, JFrame frame, JDesktopPane desktop) {
        desktop.setName("EMF Console");
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop.add(window);

        frame.setContentPane(desktop);
    }

}
