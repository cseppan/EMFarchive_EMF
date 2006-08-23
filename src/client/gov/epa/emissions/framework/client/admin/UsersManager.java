package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class UsersManager extends ReusableInteralFrame implements UsersManagerView, RefreshObserver {

    private UsersManagerPresenter presenter;

    private JPanel layout;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;

    private UsersTableData tableData;

    private EmfTableModel model;

    private SortFilterSelectModel selectModel;

    // FIXME: this class needs to be refactored into smaller components
    public UsersManager(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("User Manager", new Dimension(550, 300), desktopManager);
        super.setName("userManager");

        this.session = session;
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void display(User[] users) {
        tableData = new UsersTableData(users);
        model = new EmfTableModel(tableData);
        selectModel = new SortFilterSelectModel(model);
        createLayout(selectModel, layout);
        super.display();
    }

    public void refresh() {
        messagePanel.clear();
        refresh(tableData.getValues());
    }

    public void refresh(User[] users) {
        messagePanel.clear();

        // TODO: A HACK, until we fix row-count issues w/ SortFilterSelectPanel
        tableData = new UsersTableData(users);

        model.refresh(tableData);
        selectModel.refresh();
        createLayout(selectModel, layout);
        super.refreshLayout();
    }

    public void doRefresh() throws EmfException {
        presenter.doRefresh();
    }

    private void createLayout(SortFilterSelectModel selectModel, JPanel layout) {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        layout.add(topPanel(), BorderLayout.NORTH);
        layout.add(sortFilterScrollPane(selectModel), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel topPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Datasets", messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    private JScrollPane sortFilterScrollPane(SortFilterSelectModel selectModel) {
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parentConsole, selectModel);

        JScrollPane scrollPane = new JScrollPane(panel);
        panel.setPreferredSize(new Dimension(450, 120));

        return scrollPane;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        controlPanel.add(createCrudPanel(), BorderLayout.WEST);
        controlPanel.add(closePanel(), BorderLayout.EAST);

        return controlPanel;
    }

    private JPanel closePanel() {
        JPanel closePanel = new JPanel();
        Button closeButton = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                presenter.doClose();
            }
        });
        closePanel.add(closeButton);

        return closePanel;
    }

    private JPanel createCrudPanel() {
        Action newAction = new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                messagePanel.clear();
                displayRegisterUser();
            }
        };
        Button newButton = new NewButton(newAction);

        Action deleteAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                deleteUsers();
            }
        };
        Button deleteButton = new Button("Delete", deleteAction);

        String messageTooManyWindows = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmUpdateDialog = new ConfirmDialog(messageTooManyWindows, "Warning", this);
        Action updateAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                updateUsers();
            }
        };
        SelectAwareButton updateButton = new SelectAwareButton("Edit", updateAction, selectModel, confirmUpdateDialog);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());
        crudPanel.add(newButton);
        crudPanel.add(updateButton);
        crudPanel.add(deleteButton);

        return crudPanel;
    }

    public UserView getUserView() {
        ViewUserWindow view = new DisposableViewUserWindow(desktopManager);

        view.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent event) {
                refresh();
            }
        });

        return view;
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
        DisposableUpdateUserWindow updateUserWindowWithAdmin = new DisposableUpdateUserWindow(new AddAdminOption(
                updateUser), desktopManager);
        DisposableUpdateUserWindow updateUserWindow = new DisposableUpdateUserWindow(desktopManager);
        UpdateUserWindow view = updateUser.equals(session.user()) ? updateUserWindow : updateUserWindowWithAdmin;

        view.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent event) {
                refresh();
            }
        });

        return view;
    }

    private User[] getSelectedUsers() {
        List selected = selectModel.selected();
        return (User[]) selected.toArray(new User[0]);
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
                desktopManager);
        presenter.doRegisterNewUser(registerUserView);
    }

    public void observe(UsersManagerPresenter presenter) {
        this.presenter = presenter;
    }

}
