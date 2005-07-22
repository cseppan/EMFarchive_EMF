package gov.epa.emissions.commons.gui;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.admin.UsersManagementTableModel;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
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

public class SortFilterSelectUsersLauncher {

    public static void main(String[] args) throws EmfException {
        SortFilterSelectUsersLauncher launcher = new SortFilterSelectUsersLauncher();
        
        RefreshableTableModel delegate = launcher.createUserManagementTableModel();
        SortFilterSelectModel model = new SortFilterSelectModel(delegate);
        
        JFrame frame = new JFrame();
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(frame, model);

        frame.getContentPane().add(panel);
        
        frame.setSize(new Dimension(500, 200));
        frame.setLocation(new Point(400, 200));

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                System.exit(0);
            }
        });

        frame.show();
        
    }

    private RefreshableTableModel createUserManagementTableModel() throws EmfException {
        List users = new ArrayList();

        users.add(createUser("joe", "Joe Fullman", "joef@fullman.com"));
        users.add(createUser("mary", "Mary Joe", "mary@wonderful.net"));
        users.add(createUser("kevin", "Kevin Spacey", "kevin@spacey.com"));

        Mock userAdmin = new Mock(EMFUserAdmin.class);
        userAdmin.stubs().method("getUsers").withNoArguments().will(new ReturnStub(users));

        return new UsersManagementTableModel((EMFUserAdmin) userAdmin.proxy());
    }

    private User createUser(String username, String name, String email) throws EmfException {
        User user = new User();
        user.setUserName(username);
        user.setFullName(name);
        user.setEmailAddr(email);

        return user;
    }
}
