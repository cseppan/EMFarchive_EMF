package gov.epa.emissions.framework.client.security;

import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class UsersManagementConsole extends JFrame implements UsersManagementView {

    private UsersManagementPresenter presenter;
    private SortFilterSelectModel selectModel;
    private UsersManagementTableModel model;
    private JPanel sortFilterSelectPanel;

    public UsersManagementConsole(EMFUserAdmin userAdmin) {        
        model = new UsersManagementTableModel(userAdmin);
        selectModel = new SortFilterSelectModel(model);
        
        //TODO: fix the row-count issue w/ OverallTableModel hierarchy
        sortFilterSelectPanel = new SortFilterSelectionPanel(this, selectModel);
//        sortFilterSelectPanel = new JPanel();
//        JTable table = new JTable(selectModel);
//        sortFilterSelectPanel.add(table);
        
        JPanel layoutPanel = layout(sortFilterSelectPanel);

        this.setSize(new Dimension(500, 200));
        this.setLocation(new Point(400, 200));
        this.setTitle("User Management Console");

        this.getContentPane().add(layoutPanel);
    }

    private JPanel layout(JPanel sortFilterSelectPanel) {
        JScrollPane scrollPane = new JScrollPane(sortFilterSelectPanel);
        sortFilterSelectPanel.setPreferredSize(new Dimension(450, 120));

        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());

        layout.add(scrollPane, BorderLayout.CENTER);        
        layout.add(createControlPanel(), BorderLayout.SOUTH);

        return layout;
    }

    private JPanel createControlPanel() {
        JPanel crudPanel = createCrudPanel();

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(presenter != null) {
                    presenter.notifyCloseView();
                }
            }
        });

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
       
        
        controlPanel.add(crudPanel, BorderLayout.WEST);
        controlPanel.add(closeButton, BorderLayout.EAST);

        return controlPanel;
    }

    private JPanel createCrudPanel() {
        JButton newButton = new JButton("New");
        
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(presenter != null) {
                    int[] selected = selectModel.getSelectedIndexes();
                    for (int i = 0; i < selected.length; i++) {
                      try {
                        presenter.notifyDelete(model.getUser(selected[i]).getUserName());
                    } catch (EmfException e) {
                        //TODO: handle exceptions
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

    public void setViewObserver(UsersManagementPresenter presenter) {
        this.presenter = presenter;
    }

    public void close() {
        super.hide();
    }

    public void refresh() {
        selectModel.refresh();
        sortFilterSelectPanel.validate();
        System.out.println("UI refresh complete");
    }

}
