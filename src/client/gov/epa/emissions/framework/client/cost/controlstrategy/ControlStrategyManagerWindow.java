package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyView;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ControlStrategyManagerWindow extends ReusableInteralFrame implements ControlStrategyManagerView,
        RefreshObserver, Runnable {

    private ControlStrategiesManagerPresenter presenter;

    private ControlStrategiesTableData tableData;

    private SortFilterSelectModel selectModel;

    private EmfTableModel model;

    private JPanel layout;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;
    
    private EditControlStrategyView[] editControlStrategyViews = {};

    private volatile Thread populateThread;

    public ControlStrategyManagerWindow(EmfConsole parentConsole, EmfSession session, DesktopManager desktopManager) {
        super("Control Strategy Manager", new Dimension(700, 400), desktopManager);
        
        this.session = session;
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void observe(ControlStrategiesManagerPresenterImpl presenter) {
        this.presenter = presenter;
    }

    public void display(ControlStrategy[] controlStrategies) throws EmfException {
        doLayout(controlStrategies, this.session);
        super.display();
        //refresh control measures...
        this.populateThread = new Thread(this);
        populateThread.start();
    }

    public void run() {
        try {
            presenter.loadControlMeasures();
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all control measures.");
        }
        //refresh Edit Control Strategy windows...
        for (int i = 0; i < editControlStrategyViews.length; i++) {
            EditControlStrategyView view = editControlStrategyViews[i];
            view.endControlMeasuresRefresh();
        }
        this.populateThread = null;
    }

    public void refresh(ControlStrategy[] controlStrategies) throws EmfException {
        doLayout(controlStrategies, this.session);
        super.refreshLayout();
        //refresh Edit Control Strategy windows...
        for (int i = 0; i < editControlStrategyViews.length; i++) {
            EditControlStrategyView view = editControlStrategyViews[i];
            view.startControlMeasuresRefresh();
        }
        //refresh control measures...
        this.populateThread = new Thread(this);
        populateThread.start();
    }

    public void doRefresh() throws EmfException {
        presenter.doRefresh();
    }

    private void doLayout(ControlStrategy[] controlStrategies, EmfSession session) throws EmfException {
        tableData = new ControlStrategiesTableData(controlStrategies, session);
        model = new EmfTableModel(tableData);
        selectModel = new SortFilterSelectModel(model);
        SortFilterSelectionPanel sortFilterSelectPanel = new SortFilterSelectionPanel(parentConsole, selectModel);

        createLayout(layout, sortFilterSelectPanel);
    }

    private void createLayout(JPanel layout, JPanel sortFilterSelectPanel) {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(sortFilterSelectPanel);
        sortFilterSelectPanel.setPreferredSize(new Dimension(450, 120));

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(scrollPane, BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Cases", messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel crudPanel = createCrudPanel();

        JPanel closePanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        closePanel.add(closeButton);
        getRootPane().setDefaultButton(closeButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        controlPanel.add(crudPanel, BorderLayout.WEST);
        controlPanel.add(closePanel, BorderLayout.EAST);

        return controlPanel;
    }

    private JPanel createCrudPanel() {
        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());

        String message = "Opening too many windows. Do you want proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        crudPanel.add(viewButton(confirmDialog));
        crudPanel.add(editButton(confirmDialog));

        Button newButton = new NewButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createNewCase();
            }
        });
        crudPanel.add(newButton);

        Button removeButton = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doRemove();
                } catch (EmfException exception) {
                    messagePanel.setError(exception.getMessage());
                }
            }
        });
        crudPanel.add(removeButton);
        
        Button copyButton = new CopyButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                    try {
                        copySelectedStrategy();
                    } catch (EmfException excp) {
                        messagePanel.setError("Error in copying control strategies: " + excp.getMessage());
                    }
            }
        });
        crudPanel.add(copyButton);


        return crudPanel;
    }

    private SelectAwareButton editButton(ConfirmDialog confirmDialog) {
        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editControlStrategies();
            }

        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, selectModel, confirmDialog);
        return editButton;
    }

    private SelectAwareButton viewButton(ConfirmDialog confirmDialog) {
        Action viewAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewControlStrategies();
            }
        };

        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction, selectModel, confirmDialog);
        return viewButton;
    }

    private void viewControlStrategies() {
        tempMessage();
    }

    private void editControlStrategies() {
        List controlStrategies = selected();
        if (controlStrategies.isEmpty()) {
            messagePanel.setMessage("Please select one or more Control Strategies");
            return;
        }
        editControlStrategyViews = new EditControlStrategyView[controlStrategies.size()]; 
        for (int i = 0; i < controlStrategies.size(); i++) {
            ControlStrategy controlStrategy = (ControlStrategy) controlStrategies.get(i);
            EditControlStrategyView view = new EditControlStrategyWindow(desktopManager, session, parentConsole);
            editControlStrategyViews[i] = view;
            try {
                presenter.doEdit(view, controlStrategy);
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }
        }
    }
    
    protected void doRemove() throws EmfException {
        messagePanel.clear();
        ControlStrategy[] records = (ControlStrategy[])selected().toArray(new ControlStrategy[0]);

        if (records.length == 0) {
            messagePanel.setError("Please select an item to remove.");
            return;
        }

        String title = "Warning";
        String message = "Are you sure you want to remove the selected row(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            int[] ids = new int[records.length];
            for (int i = 0; i < records.length; i++) {
                ids[i] = records[i].getId(); 
            }
            presenter.doRemove(ids);
            doRefresh();
        }
    }
    
    private void copySelectedStrategy() throws EmfException {
        boolean error = false;
        messagePanel.clear();
        List strategies = selected();
        if (strategies.isEmpty()) {
            messagePanel.setMessage("Please select one or more control strategies.");
            return;
        }
        
        for (Iterator iter = strategies.iterator(); iter.hasNext();) {
            ControlStrategy element = (ControlStrategy) iter.next();
            
            try {
                presenter.doSaveCopiedStrategies(element.getId(), session.user());
//                ControlStrategy coppied = (ControlStrategy)DeepCopy.copy(element);
//                coppied.setName("Copy of " + element.getName());
//                presenter.doSaveCopiedStrategies(coppied, element.getName());
            } catch (Exception e) {
                messagePanel.setError(e.getMessage());
                error = true;
            }
        }
        
        if (!error) doRefresh();
    }

    private List selected() {
        return selectModel.selected();
    }

    private void createNewCase() {
        ControlStrategyView view = new ControlStrategyWindow(desktopManager);
        presenter.doNew(view);
    }

    public EmfConsole getParentConsole() {
        return parentConsole;
    }

    private void tempMessage() {
        messagePanel.clear();
        messagePanel.setMessage("Under construction");
    }
}
