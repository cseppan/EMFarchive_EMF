package gov.epa.emissions.gui;

import gov.epa.emissions.framework.client.security.UsersManagementTableModel;
import gov.epa.emissions.framework.commons.User;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.table.TableModel;

public class SortFilterSelectUsersLauncher {

    public static void main(String[] args) {
        SortFilterSelectUsersLauncher launcher = new SortFilterSelectUsersLauncher();
        
        TableModel delegate = launcher.createUserManagementTableModel();
        SortFilterSelectModel model = new SortFilterSelectModel(delegate);
        
        JFrame frame = new JFrame();
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(frame, model);

        frame.getContentPane().add(panel);
        
        frame.setSize(new Dimension(500, 200));
        frame.setLocation(new Point(400, 200));

        frame.show();
        
    }

    private TableModel createUserManagementTableModel() {
        List users = new ArrayList();

        users.add(createUser("joe", "Joe Fullman", "joef@zukoswky.com"));
        users.add(createUser("mary", "Mary Joe", "mary@wonderful.net"));
        users.add(createUser("kevin", "Kevin Spacey", "kevin@spacey.com"));

        return new UsersManagementTableModel(users);
    }

    private User createUser(String username, String name, String email) {
        User user = new User();
        user.setUserName(username);
        user.setFullName(name);
        user.setEmailAddr(email);

        return user;
    }
}
