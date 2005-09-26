package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;
import gov.epa.mims.analysisengine.table.OverallTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class UserManagerWindow extends ReusableInteralFrame implements UserManagerView {

    private UserManagerPresenter presenter;

    private SortFilterSelectModel selectModel;

    private UserManagerTableModel model;

    private UserServices userServices;

    private JPanel layout;

    private MessagePanel messagePanel;

    private JFrame parentConsole;

    private User user;

    private JDesktopPane desktop;

    private SortFilterSelectionPanel sortFilterSelectPanel;

    // FIXME: this class needs to be refactored into smaller components
    public UserManagerWindow(User user, UserServices userServices, JFrame parentConsole, JDesktopPane desktop)
            throws Exception {
        super("User Management Console", desktop);
        this.user = user;
        this.userServices = userServices;
        this.parentConsole = parentConsole;
        this.desktop = desktop;

        model = new UserManagerTableModel(userServices);
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
        sortFilterSelectPanel = new SortFilterSelectionPanel(parentConsole, selectModel);
        createLayout(layout, sortFilterSelectPanel);
        listenForUpdateSelection(sortFilterSelectPanel.getTable());

        this.setSize(new Dimension(550, 300));
    }

    private void listenForUpdateSelection(final JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    // FIXME: hack - need to use Overall Table Model to get
                    // 'selected row'
                    int selectedRow = table.getSelectedRow();
                    OverallTableModel overallTableModel = sortFilterSelectPanel.getOverallTableModel();
                    User user = model.getUser(overallTableModel.getBaseModelRowIndex(selectedRow));
                    updateUser(user);
                }
            }
        });
    }

    private void updateUser(User updateUser) {
        // FIXME: drive this logic via Presenter
        UpdateUserWindow view = updateUser.equals(user) ? new DisposableUpdateUserWindow(updateUser)
                : new DisposableUpdateUserWindow(updateUser, new AddAdminOption());

        getDesktopPane().add(view);

        view.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent event) {
                doSimpleRefresh();
            }
        });

        UpdateUserPresenter presenter = new UpdateUserPresenter(userServices);
        presenter.display(view);
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
                    presenter.doCloseView();
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
        List users = getSelectedUsers();
        for (Iterator iter = users.iterator(); iter.hasNext();) {
            User user = (User) iter.next();
            updateUser(user);
        }
    }

    // FIXME: if no users are selected, add appropriate behavior to Presenter
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
        int option = JOptionPane.showConfirmDialog(null, "Are you sure about deleting user(s)", "Delete User",
                JOptionPane.YES_NO_OPTION);
        if (option == 1) {
            refresh();
            return;
        }

        List users = getSelectedUsers();
        try {
            presenter.doDelete((User[]) users.toArray(new User[0]));
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
            // TODO: temp, until the HACK is addressed (then, use refresh)
            doSimpleRefresh();
        }
    }

    private void displayRegisterUser() {
        RegisterUserInternalFrame container = new RegisterUserInternalFrame(new NoOpPostRegisterStrategy(), desktop);

        getDesktopPane().add(container);

        container.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent event) {
                refresh();
            }
        });

        RegisterUserPresenter presenter = new RegisterUserPresenter(userServices);
        presenter.display(container.getView());
    }

    public void observe(UserManagerPresenter presenter) {
        this.presenter = presenter;
    }

    public void refresh() {
        selectModel.refresh();
        // TODO: A HACK, until we fix row-count issues w/ SortFilterSelectPanel
        createLayout(parentConsole);
        refreshLayout();
    }

    private void refreshLayout() {
        super.validate();
    }

    private void doSimpleRefresh() {
        model.refresh();
        selectModel.refresh();
        refreshLayout();
    }

}
