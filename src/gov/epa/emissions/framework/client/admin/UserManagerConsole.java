package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfInteralFrame;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class UserManagerConsole extends EmfInteralFrame implements UsersManagementView {

    private UserManagerPresenter presenter;

    private SortFilterSelectModel selectModel;

    private UserManagerTableModel model;

    private JPanel sortFilterSelectPanel;

    private EMFUserAdmin userAdmin;

    private JPanel layout;

    public UserManagerConsole(EMFUserAdmin userAdmin) throws Exception {
        super("User Management Console");
        this.userAdmin = userAdmin;
        model = new UserManagerTableModel(userAdmin);
        selectModel = new SortFilterSelectModel(model);

        layout = new JPanel();
        this.getContentPane().add(layout);
        
        // TODO: OverallTableModel has a bug w/ respect to row-count &
        // cannot refresh itself. So, we will regen the layout on every
        // refresh - it's a HACK. Will need to be addressed
        createLayout();        

        this.setSize(new Dimension(500, 300));
    }

    private void createLayout() {
        layout.removeAll();
        sortFilterSelectPanel = new SortFilterSelectionPanel(this, selectModel);
        createLayout(layout, sortFilterSelectPanel);
    }

    private void createLayout(JPanel layout, JPanel sortFilterSelectPanel) {
        layout.setLayout(new BorderLayout());
        
        JScrollPane scrollPane = new JScrollPane(sortFilterSelectPanel);
        sortFilterSelectPanel.setPreferredSize(new Dimension(450, 120));

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
                if (presenter != null) {
                    int[] selected = selectModel.getSelectedIndexes();
                    for (int i = 0; i < selected.length; i++) {
                        try {
                            presenter.notifyDelete(model.getUser(selected[i]).getUserName());
                        } catch (EmfException e) {
                            // TODO: handle exceptions
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());
        crudPanel.add(newButton);
        crudPanel.add(deleteButton);

        return crudPanel;
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
        createLayout();//TODO: A HACK, until we fix row-count issues w/ SortFilterSelectPanel
        sortFilterSelectPanel.validate();
    }

}
