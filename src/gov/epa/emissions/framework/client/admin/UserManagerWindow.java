package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfInteralFrame;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

        // FIXME: OverallTableModel has a bug w/ respect to row-count &
        // cannot refresh itself. So, we will regen the layout on every
        // refresh - it's a HACK. Will need to be addressed
        createLayout(parentConsole);
    }

    private void createLayout(JFrame parentConsole) {
        layout.removeAll();
        SortFilterSelectionPanel sortFilterSelectPanel = new SortFilterSelectionPanel(parentConsole, selectModel);
        createLayout(layout, sortFilterSelectPanel);
        listenForUpdateSelection(sortFilterSelectPanel.getTable());

        this.setSize(new Dimension(500, 300));
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
                doSimpleRefresh();
            }
        });

        view.display();
    }

    private void createLayout(JPanel layout, JPanel sortFilterSelectPanel) {
        layout.setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(sortFilterSelectPanel);
        sortFilterSelectPanel.setPreferredSize(new Dimension(450, 120));

        messagePanel = new SingleLineMessagePanel();
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
                // FIXME: should be notifying the Presenter
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
        List users = getSelectedUsers();
        for (Iterator iter = users.iterator(); iter.hasNext();) {
            User user = (User) iter.next();
            updateUser(user);
        }
    }

    private List getSelectedUsers() {
        List users = new ArrayList();

        int[] selected = selectModel.getSelectedIndexes();
        if (selected.length == 0)
            return users;
        for (int i = 0; i < selected.length; i++) {
            users.add(model.getUser(selected[i]));
        }

        return users;
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

        List users = getSelectedUsers();
        for (Iterator iter = users.iterator(); iter.hasNext();) {
            User user = (User) iter.next();
            try {
                presenter.notifyDelete(user.getUserName());
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
                // TODO: temp, until the HACK is addressed (then, use refresh)
                validate();
                break;// TODO: should continue ?
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

    public void setObserver(UserManagerPresenter presenter) {
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

    private void doSimpleRefresh() {
        model.refresh();
        selectModel.refresh();
        super.validate();
    }

}
