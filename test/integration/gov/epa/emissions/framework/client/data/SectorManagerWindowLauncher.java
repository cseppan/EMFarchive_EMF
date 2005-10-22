package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.client.admin.UserServicesStub;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.Sector;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

import org.jmock.Mock;
import org.jmock.core.constraint.IsEqual;
import org.jmock.core.stub.ReturnStub;

public class SectorManagerWindowLauncher {

    public static void main(String[] args) throws Exception {
        SectorManagerWindowLauncher launcher = new SectorManagerWindowLauncher();

        JFrame frame = new JFrame();
        JDesktopPane desktop = new JDesktopPane();

        SectorManagerWindow view = new SectorManagerWindow(frame, desktop);
        Sector[] sectors = { new Sector(), new Sector() };
        Mock dataServices = new Mock(DataServices.class);
        dataServices.stubs().method(new IsEqual("getSectors")).will(new ReturnStub(sectors));

        SectorManagerPresenter presenter = new SectorManagerPresenter(view, (DataServices) dataServices.proxy());

        launcher.addAsInternalFrame(view, frame, desktop);

        frame.setSize(new Dimension(800, 600));
        frame.setLocation(new Point(400, 200));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        presenter.doDisplay();
    }

    private void addAsInternalFrame(EmfInternalFrame console, JFrame frame, JDesktopPane desktop) {
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
        user.setUsername(username);
        user.setFullName(name);
        user.setEmail(email);
        user.setInAdminGroup(isAdmin);

        return user;
    }
}
