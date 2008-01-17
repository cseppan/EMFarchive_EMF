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
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
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
    
    private String threadAction;

    private EmfConsole parentConsole;

    private EmfSession session;
    
    private Button refreshButton, copyButton, removeButton;
    
    private EditControlStrategyView[] editControlStrategyViews = {};

    private volatile Thread populateThread;

    public ControlStrategyManagerWindow(EmfConsole parentConsole, EmfSession session, DesktopManager desktopManager) {
        super("Control Strategy Manager", new Dimension(850, 400), desktopManager);
        
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
        doRefresh();
    }

    public void run() {
        if (this.threadAction == "refresh") {
            try {
                setButton(false);
                messagePanel.setMessage("Please wait while retrieving control strategies...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                presenter.doRefresh();
                messagePanel.clear();
            } catch (Exception e1) {
                messagePanel.setError("Cannot retrieve control strategies.  " + e1.getMessage());
            } finally  {
                setButton(true);
                setCursor(Cursor.getDefaultCursor());
                this.populateThread = null;
            }
        } else if (this.threadAction == "copy") {
            try {
                setButton(false);
                messagePanel.setMessage("Please wait while copying control strategies...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                copySelectedStrategy();
                this.populateThread = null;
                doRefresh();
                messagePanel.clear();
            } catch (Exception e2) {
                messagePanel.setError("Cannot copy control strategies.  " + e2.getMessage());
            } finally  {
                setButton(true);
                setCursor(Cursor.getDefaultCursor());
                this.populateThread = null;
            }
        }else if (this.threadAction == "remove") {
            try {
                setButton(false);
                messagePanel.setMessage("Please wait while removing control strategies...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                removeStrategies();
                this.populateThread = null;
                doRefresh();
                messagePanel.clear();
            } catch (Exception e3) {
                messagePanel.setError("Cannot remove control strategies.  " + e3.getMessage());
            } finally  {
                setButton(true);
                setCursor(Cursor.getDefaultCursor());
                this.populateThread = null;
            }
        }
        
    }

    public void refresh(ControlStrategy[] controlStrategies) throws EmfException {
        doLayout(controlStrategies, this.session);
        super.refreshLayout();
        //refresh Edit Control Strategy windows...
        for (int i = 0; i < editControlStrategyViews.length; i++) {
            EditControlStrategyView view = editControlStrategyViews[i];
            if (editControlStrategyViews != null) {
                view.startControlMeasuresRefresh();
            }
        }
        //refresh control measures...
        refreshCM();
    }
    
    public void refreshCM(){
        try {
            presenter.loadControlMeasures();
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all control measures.");
            //refresh Edit Control Strategy windows...
            for (int i = 0; i < editControlStrategyViews.length; i++) {
                EditControlStrategyView view = editControlStrategyViews[i];
                view.endControlMeasuresRefresh();
            }
        }
    }

    public void setButton(boolean boo){
        removeButton.setEnabled(boo);
        copyButton.setEnabled(boo);
        refreshButton.setEnabled(boo);
    }
    public void doRefresh() {
        this.threadAction = "refresh";
        this.populateThread = new Thread(this);
        this.populateThread.start();
    }
    
    public void doCopy() {
        this.threadAction = "copy";
        this.populateThread = new Thread(this);
        this.populateThread.start();
    }

    public void doRemove() {
        this.threadAction = "remove";
        this.populateThread = new Thread(this);
        this.populateThread.start();
    }
    
    private void doLayout(ControlStrategy[] controlStrategies, EmfSession session) throws EmfException {
        tableData = new ControlStrategiesTableData(controlStrategies, session);
        model = new EmfTableModel(tableData);
        selectModel = new SortFilterSelectModel(model);
        SortFilterSelectionPanel sortFilterSelectPanel = new SortFilterSelectionPanel(parentConsole, selectModel);
        sortFilterSelectPanel.sort(sortCriteria());
        createLayout(layout, sortFilterSelectPanel);
    }
    
    private SortCriteria sortCriteria() {
        String[] columnNames = { "Last Modified" };
        return new SortCriteria(columnNames, new boolean[] { false }, new boolean[] { true });
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

        refreshButton = new RefreshButton(this, "Refresh Cases", messagePanel);
        panel.add(refreshButton, BorderLayout.EAST);

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
                createNewStrategy();
            }
        });
        crudPanel.add(newButton);

        removeButton = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doRemove();
            }
        });
        crudPanel.add(removeButton);
        
        copyButton = new CopyButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                    doCopy();
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
        viewButton.setEnabled(false);
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
    
    protected void removeStrategies() throws EmfException {
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
            try {
                presenter.doRemove(ids);
            } catch (EmfException ex) {
                throw ex;
            }
        }
    }
    
    private void copySelectedStrategy() {
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
            }
        }
    }

    private List selected() {
        return selectModel.selected();
    }

    private void createNewStrategy() {
        ControlStrategyView view = new ControlStrategyWindow(parentConsole, session, desktopManager);
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
