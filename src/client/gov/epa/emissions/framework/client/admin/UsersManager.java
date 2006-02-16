package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.SimpleTableModel;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.table.SortFilterTablePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class UsersManager extends ReusableInteralFrame implements UsersManagerView {

    private UsersManagerPresenter presenter;

    private SortFilterSelectModel selectModel;

    private JPanel layout;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;

    private UsersTableData tableData;

    private EmfTableModel model;

    // FIXME: this class needs to be refactored into smaller components
    public UsersManager(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("User Manager", new Dimension(550, 300), parentConsole.desktop(), desktopManager);
        super.setName("userManager");

        this.session = session;
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);

    }

    public void display(User[] users) {
        layout.removeAll();

        tableData = new UsersTableData(users);
        model = new EmfTableModel(tableData);
        createLayout(layout);
    }

    private void updateUsers() {
        User[] selected = getSelectedUsers();
        if (selected.length == 0) {
            showMessage("To update, please select at least one User.");
            return;
        }

        try {
            presenter.doUpdateUsers(selected);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    public UpdatableUserView getUpdateUserView(User updateUser) {
        UpdateUserWindow view = updateUser.equals(session.user()) ? new DisposableUpdateUserWindow(desktopManager)
                : new DisposableUpdateUserWindow(new AddAdminOption(), desktopManager);
        desktop.add(view);

        view.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent event) {
                refresh();
            }
        });

        return view;
    }

    public UserView getUserView() {
        ViewUserWindow view = new DisposableViewUserWindow(desktopManager);
        desktop.add(view);

        view.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent event) {
                refresh();
            }
        });

        return view;
    }

    private void createLayout(JPanel layout) {
        layout.setLayout(new BorderLayout());
        layout.add(topPanel(), BorderLayout.NORTH);
        layout.add(sortFilterScrollPane(), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel topPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(presenter, "Refresh Datasets", messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    private JScrollPane sortFilterScrollPane() {
        SimpleTableModel wrapperModel = new SimpleTableModel(model);
        SortFilterTablePanel panel = new SortFilterTablePanel(parentConsole, wrapperModel);
        JScrollPane scrollPane = new JScrollPane(panel);
        panel.setPreferredSize(new Dimension(450, 120));
        return scrollPane;
    }

    private JPanel createControlPanel() {
        JPanel crudPanel = createCrudPanel();

        JPanel closePanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                messagePanel.clear();
                presenter.doClose();
            }
        });
        closePanel.add(closeButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        controlPanel.add(crudPanel, BorderLayout.WEST);
        controlPanel.add(closePanel, BorderLayout.EAST);

        return controlPanel;
    }

    private JPanel createCrudPanel() {
        Action newAction = new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                messagePanel.clear();
                displayRegisterUser();
            }
        };
        Button newButton = new Button("New", newAction);

        Action deleteAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                deleteUsers();
            }
        };
        Button deleteButton = new Button("Delete", deleteAction);

        String messageTooManyWindows = "Opening too many windows. Do you want proceed?";
        ConfirmDialog confirmUpdateDialog = new ConfirmDialog(messageTooManyWindows, "Warning", this);
        Action updateAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                updateUsers();
            }
        };
        SelectAwareButton updateButton = new SelectAwareButton("Update", updateAction, selectModel, confirmUpdateDialog);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());
        crudPanel.add(newButton);
        crudPanel.add(updateButton);
        crudPanel.add(deleteButton);

        return crudPanel;
    }

    // FIXME: if no users are selected, add appropriate behavior to Presenter
    private User[] getSelectedUsers() {
        int[] selected = selectModel.getSelectedIndexes();
        if (selected.length == 0)
            return new User[0];

        return tableData.selected();
    }

    private void deleteUsers() {
        User[] selected = getSelectedUsers();
        if (selected.length == 0) {
            showMessage("To delete, please select at least one User.");
            return;
        }

        if (!promptDelete(selected))
            return;

        try {
            presenter.doDelete(selected);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void showMessage(String message) {
        messagePanel.setMessage(message);
    }

    public boolean promptDelete(User[] users) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < users.length; i++) {
            buffer.append("'" + users[i].getUsername() + "'");
            if (i + 1 < users.length)
                buffer.append(", ");
        }

        int option = JOptionPane.showConfirmDialog(this, "Are you sure about deleting user(s) - " + buffer.toString(),
                "Delete User", JOptionPane.YES_NO_OPTION);
        return (option == 0);
    }

    private void displayRegisterUser() {
        RegisterUserInternalFrame registerUserView = new RegisterUserInternalFrame(new NoOpPostRegisterStrategy(),
                desktop, desktopManager);
        desktop.add(registerUserView);
        presenter.doRegisterNewUser(registerUserView);
    }

    public void observe(UsersManagerPresenter presenter) {
        this.presenter = presenter;
    }

    public void refresh() {
        messagePanel.clear();
        refresh(tableData.getValues());
    }

    public void refresh(User[] users) {
        messagePanel.clear();

        model.refresh(new UsersTableData(users));
        selectModel.refresh();

        // TODO: A HACK, until we fix row-count issues w/ SortFilterSelectPanel
        createLayout(layout);
        super.refreshLayout();
    }

}
