package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfInteralFrame;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class UserManagerWindow extends EmfInteralFrame implements UsersManagementView {

    private UserManagerPresenter presenter;

    private SortFilterSelectModel selectModel;

    private UserManagerTableModel model;

    private SortFilterSelectionPanel sortFilterSelectPanel;

    private UserServices userAdmin;

    private JPanel layout;

    private MessagePanel messagePanel;

    private JFrame parentConsole;

    public UserManagerWindow(UserServices userAdmin, JFrame parentConsole) throws Exception {
        super("User Management Console");
        this.userAdmin = userAdmin;
        this.parentConsole = parentConsole;
        model = new UserManagerTableModel(userAdmin);
        selectModel = new SortFilterSelectModel(model);

        layout = new JPanel();
        this.getContentPane().add(layout);

        // TODO: OverallTableModel has a bug w/ respect to row-count &
        // cannot refresh itself. So, we will regen the layout on every
        // refresh - it's a HACK. Will need to be addressed
        createLayout(parentConsole);

        this.setSize(new Dimension(500, 300));
    }

    private void createLayout(JFrame parentConsole) {
        layout.removeAll();
        sortFilterSelectPanel = new SortFilterSelectionPanel(parentConsole, selectModel);
        createLayout(layout, sortFilterSelectPanel);
        listenForUpdateSelection(sortFilterSelectPanel.getTable());
    }

    private void listenForUpdateSelection(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ListSelectionModel selectionModel = table.getSelectionModel();
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting())
                    return;// Ignore extra messages.

                ListSelectionModel selectionModel = (ListSelectionModel) event.getSource();
                if (!selectionModel.isSelectionEmpty()) {
                    User user = model.getUser(selectionModel.getMinSelectionIndex());
                    updateUser(user);
                }
            }
        });
    }

    private void updateUser(User user) {
        UpdateUserWindow view = new UpdateUserWindow(user);
        UpdateUserPresenter presenter = new UpdateUserPresenter(userAdmin, view);
        presenter.observe();

        getDesktopPane().add(view);

        view.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent event) {
                refresh();
            }
        });

        view.display();
    }

    private void createLayout(JPanel layout, JPanel sortFilterSelectPanel) {
        layout.setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(sortFilterSelectPanel);
        sortFilterSelectPanel.setPreferredSize(new Dimension(450, 120));

        messagePanel = new MessagePanel();
        layout.add(messagePanel, BorderLayout.NORTH);
        layout.add(scrollPane, BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createControlPanel() {
        JPanel crudPanel = createCrudPanel();

        JPanel closePanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (presenter != null) {
                    presenter.notifyCloseView();
                }
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
        JButton newButton = new JButton("New");
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                displayRegisterUser();
            }
        });

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                deleteUser();
            }
        });

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                updateUsers();
            }
        });

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());
        crudPanel.add(newButton);
        crudPanel.add(updateButton);
        crudPanel.add(deleteButton);

        return crudPanel;
    }

    private void updateUsers() {
        if (presenter == null)
            return;
        int[] selected = selectModel.getSelectedIndexes();
        if (selected.length == 0)
            return;

        for (int i = 0; i < selected.length; i++) {
            updateUser(model.getUser(i));
        }
    }

    private void deleteUser() {
        if (presenter == null)
            return;
        int[] selected = selectModel.getSelectedIndexes();
        if (selected.length == 0)
            return;
        int option = JOptionPane.showConfirmDialog(null, "Are you sure about deleting user(s)", "Delete User",
                JOptionPane.YES_NO_OPTION);
        if (option == 1) {
            refresh();
            return;
        }

        for (int i = 0; i < selected.length; i++) {
            try {
                String username = model.getUser(selected[i]).getUserName();
                presenter.notifyDelete(username);
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
                // TODO: temp, until the HACK is addressed (then, use refresh)
                validate();
            }
        }
    }

    private void displayRegisterUser() {
        RegisterUserInternalFrame container = new RegisterUserInternalFrame(userAdmin, new NoOpPostRegisterStrategy());
        RegisterUserPresenter presenter = new RegisterUserPresenter(userAdmin, container.getView());
        presenter.observe();

        getDesktopPane().add(container);

        container.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent event) {
                refresh();
            }
        });

        container.display();
    }

    public void setViewObserver(UserManagerPresenter presenter) {
        this.presenter = presenter;
    }

    public void close() {
        super.dispose();
    }

    public void refresh() {
        selectModel.refresh();
        // TODO: A HACK, until we fix row-count issues w/ SortFilterSelectPanel
        createLayout(parentConsole);
        super.validate();
    }

    public void display() {
        super.setVisible(true);
    }

}
