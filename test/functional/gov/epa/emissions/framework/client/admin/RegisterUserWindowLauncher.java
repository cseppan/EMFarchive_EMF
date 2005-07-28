package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.login.NoOpPostRegisterStrategy;
import gov.epa.emissions.framework.client.login.RegisterUserWindow;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.util.Collections;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

public class RegisterUserWindowLauncher {

    public static void main(String[] args) throws Exception {
        EMFUserAdmin userAdmin = new EMFUserAdminStub(Collections.EMPTY_LIST);

        RegisterUserWindow view = new RegisterUserWindow(userAdmin, new NoOpPostRegisterStrategy());
        RegisterUserPresenter presenter = new RegisterUserPresenter(userAdmin, view);
        view.setVisible(true);
        
        JFrame frame = new JFrame();

        addAsInternalFrame(view, frame);
        
        frame.setSize(new Dimension(800, 600));
        frame.setLocation(new Point(400, 200));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
    }

    private static void addAsInternalFrame(Component console, JFrame frame) {
        JDesktopPane desktop = new JDesktopPane();
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop.add(console);

        frame.setContentPane(desktop);
    }

}
