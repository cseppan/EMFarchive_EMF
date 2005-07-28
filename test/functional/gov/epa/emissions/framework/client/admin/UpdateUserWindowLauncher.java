package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.util.Collections;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

public class UpdateUserWindowLauncher {

    public static void main(String[] args) throws Exception {
        EMFUserAdmin userAdmin = new EMFUserAdminStub(Collections.EMPTY_LIST);
        User user = new User();
        user.setUserName("joes");
        user.setFullName("Joe Schmoe");
        user.setAffiliation("Joey Inc");
        user.setWorkPhone("123-123-123");
        user.setEmailAddr("joes@joey.inc");
        
        UpdateUserWindow window = new UpdateUserWindow(user);
        UpdateUserPresenter presenter = new UpdateUserPresenter(userAdmin, window);
        presenter.observe();
        
        window.display();
        
        JFrame frame = new JFrame();

        addAsInternalFrame(window, frame);
        
        frame.setSize(new Dimension(800, 600));
        frame.setLocation(new Point(400, 200));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
    }

    private static void addAsInternalFrame(Container window, JFrame frame) {
        JDesktopPane desktop = new JDesktopPane();
        desktop.setName("EMF Console");
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop.add(window);

        frame.setContentPane(desktop);
    }

}
