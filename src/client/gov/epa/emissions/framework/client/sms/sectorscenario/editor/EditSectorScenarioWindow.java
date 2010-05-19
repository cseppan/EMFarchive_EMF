package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.RunButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.gui.buttons.StopButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.sms.sectorscenario.SectorScenarioManagerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class EditSectorScenarioWindow extends DisposableInteralFrame implements EditSectorScenarioView {

    private EditSectorScenarioPresenter presenter;

    protected Button saveButton;

    protected Button runButton;

    protected Button stopButton;
    
//    private EditControlStrategyMeasuresTab measuresTabView;
//
//    private ControlStrategyProgramsTab programsTabView;
//
//    private EditSectorScenarioOutputTabView outputTabView;

    //private EditSectorScenarioSummaryTabView summaryTabView;
    
    final protected JTabbedPane tabbedPane = new JTabbedPane();
    
    protected SingleLineMessagePanel messagePanel;
    
    protected EmfSession session;

    protected SectorScenarioManagerPresenter managerPresenter;
    
    protected EmfConsole parentConsole;
    
    protected SectorScenario sectorScenario;
    
    protected DesktopManager desktopManager;
    
    protected String tabTitle;
    
    public EditSectorScenarioWindow(String name, DesktopManager desktopManager, EmfSession session, 
            EmfConsole parentConsole) {
        super(name, new Dimension(700, 570), desktopManager);
//      this.setMinimumSize(new Dimension(700, 300));
      this.session = session;
      this.parentConsole = parentConsole;
      this.desktopManager = desktopManager;
    }

    public void observe(EditSectorScenarioPresenter presenter) {
        this.presenter = presenter;

    }

    public void display(SectorScenario sectorScenario) {
        super.setLabel(super.getTitle() + ": " + sectorScenario.getName());
        this.sectorScenario = sectorScenario;
        //System.out.print(sectorScenario.getName() +"  abbre  "+ sectorScenario.getAbbreviation());
        super.display();
        messagePanel = new SingleLineMessagePanel();
       
        Container contentPane = super.getContentPane();
        contentPane.removeAll();

        JPanel layout = new JPanel(new BorderLayout());
        
        layout.add(messagePanel, BorderLayout.PAGE_START);
        layout.add(createTabbedPane(), BorderLayout.CENTER);
        layout.add(createButtonsPanel(), BorderLayout.PAGE_END);

//      if (controlStrategy.getRunStatus().equalsIgnoreCase("Running"))
//      enableButtons(false);

        contentPane.add(layout);
    }

    private JTabbedPane createTabbedPane() {
        tabbedPane.addTab("Summary", createSummaryTab());       
        tabbedPane.addTab("Inputs", createInputsTab());
        tabbedPane.addTab("Options", createOptionsTab());
        tabbedPane.addTab("Outputs", createOutputsTab());
        
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        return tabbedPane;
    }
    
//    protected void loadComponents() throws EmfException {
//        int tabIndex = tabbedPane.getSelectedIndex();
//        tabTitle = tabbedPane.getTitleAt(tabIndex);
//        presenter.doLoad(tabTitle);
//    }    

    private JPanel createSummaryTab() {

        EditSectorScenarioSummaryTab summaryTabView = new EditSectorScenarioSummaryTab(sectorScenario,session, this, messagePanel, parentConsole);
        
        presenter.set(summaryTabView);
        summaryTabView.display();
       
        return summaryTabView;
    }
    
    private JPanel createInputsTab() {
        
        try {
            EditSectorScenarioInputsTab inputsTabView = new EditSectorScenarioInputsTab(sectorScenario, this, 
                    messagePanel, parentConsole, 
                    session, desktopManager,
                    presenter);      
            this.presenter.set(inputsTabView);
            inputsTabView.display();
            return inputsTabView;
            //return null; 
        } catch (EmfException e) {
            //e.printStackTrace();
            showError("Could not load inputs tab." + e.getMessage());
            return createErrorTab("Could not load inputs tab." + e.getMessage());
        }
    }
    
    private JPanel createErrorTab(String message) {
        JPanel panel = new JPanel(false);
        JLabel label = new JLabel(message);
        label.setForeground(Color.RED);
        panel.add(label);

        return panel;
    } 
    
    private JPanel createOptionsTab() {
        EditSectorScenarioOptionsTab optionsTabView = new EditSectorScenarioOptionsTab(sectorScenario, 
                messagePanel, parentConsole, 
                session, desktopManager);      
        this.presenter.set(optionsTabView);
        optionsTabView.display();
        return optionsTabView;
        //return null; 
    }
    
    private JPanel createOutputsTab(){
        EditSectorScenarioOutputsTab outputsTabView = new EditSectorScenarioOutputsTab(sectorScenario, 
                messagePanel, parentConsole, 
                session, desktopManager);      
        this.presenter.set(outputsTabView);
        outputsTabView.display(sectorScenario);
        return outputsTabView;
        //return null; 
    }
    

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(15);
        container.setLayout(layout);

        saveButton = new SaveButton(saveAction());
        container.add(saveButton);

        Button copyButton = new CopyButton(null);
        copyButton.setEnabled(false);
        container.add(copyButton);

        Button closeButton = new CloseButton(closeAction());
        container.add(closeButton);
        getRootPane().setDefaultButton(saveButton);

        container.add(Box.createHorizontalStrut(20));

        runButton = new RunButton(runAction());
        container.add(runButton);
        runButton.setEnabled(false);

        Button refreshButton = new Button("Refresh", new AbstractAction(){
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doRefresh();
                } catch (EmfException e) {
                    showError(e.getMessage());
                }
            }
        }); 
        
        container.add(refreshButton);

        stopButton = new StopButton(stopAction());
        stopButton.setEnabled(false);
        container.add(stopButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }
    
//    private void refreshCurrentTab() throws EmfException {
//        RefreshObserver tab = (RefreshObserver) tabbedPane.getSelectedComponent();
//        RefreshObserver summaryTab = (RefreshObserver) tabbedPane.getComponentAt(0);
//        try {
//            messagePanel.clear();
//            tab.doRefresh();
//            if (tabbedPane.getSelectedIndex() != 0)
//                summaryTab.doRefresh();
//        } catch (Exception e) {
//            throw new EmfException(e.getMessage());
//        }
//    }
    

    private Action stopAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                clearMessage();
                String title = "Warning";
                
                String message = "Would you like to stop the strategy run?  This could several minutes to cancel the run.";
                int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                boolean cancel = false;
                if (selection == JOptionPane.CANCEL_OPTION) {
                    return;
                } else if (selection == JOptionPane.YES_OPTION){
                    cancel = true; 
                } else if (selection == JOptionPane.NO_OPTION){
                    cancel = false; 
                }
                if (cancel) {
                    //presenter.stopRun();
                }
            }
        };
    }


    public void enableButtons(boolean enable) {
        saveButton.setEnabled(enable);
        runButton.setEnabled(enable);
        stopButton.setEnabled(!enable);
    }

    private void save() throws EmfException {
        clearMessage();
        presenter.doSave(sectorScenario);
        messagePanel
            .setMessage("Sector Scenario was saved successfully.");
        resetChanges();
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                clearMessage();
                doClose();
            }
        };

        return action;
    }

    protected void doClose() {
        try {
            //first check whether cs is running before checking the discard changes
            //if(shouldDiscardChanges()) System.out.print("  true  ");
            //if (isRunButtonClicked() || shouldDiscardChanges())
            if (shouldDiscardChanges())
                presenter.doClose();
        } catch (EmfException e) {
            messagePanel.setError("Could not close: " + e.getMessage());
        }
    }

    protected boolean isRunButtonClicked() {
        return !runButton.isEnabled();
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    clearMessage();
                    save();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        };

        return action;
    }

    public void notifyLockFailure() {
        String message = "Cannot edit Control Strategy: " + sectorScenario
                + System.getProperty("line.separator") + " as it was locked by User: " + sectorScenario.getLockOwner()
                + "(at " + format(sectorScenario.getLockDate()) + ")";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    public void notifyEditFailure() {
        String message = "Cannot edit Control Strategy: " + sectorScenario.getName()
                + " because you must be the creator of the strategy or an Administrator";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    private String format(Date lockDate) {
        return CustomDateFormat.format_YYYY_MM_DD_HH_MM(lockDate);
    }

    public void windowClosing() {
        doClose();
    }

    private void clearMessage() {
        messagePanel.clear();
    }

    public void signalChanges() {
        clearMessage();
        super.signalChanges();
    }

//    public void refresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
//        ControlStrategyResultsSummary summary = new ControlStrategyResultsSummary(controlStrategyResults);
//        if (summary.getRunStatus().equalsIgnoreCase("Completed."))
//            stopButton.setEnabled(false);
//    }

    public void stopRun()  {
        enableButtons(true);
        //stopButton.setEnabled(false);
    }

    private Action runAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                //
            }
        };
    }

    public void notifyEditFailure(SectorScenario sectorScenario) {
        String message = "Cannot edit Case: " + sectorScenario.getName() + System.getProperty("line.separator")
        + " because you must be the creator of the sector scenario or an Administrator";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
        
    }

    public void notifyLockFailure(SectorScenario sectorScenario) {
        String message = "Cannot edit Case: " + sectorScenario.getName() + System.getProperty("line.separator")
        + " as it was locked by User: " + sectorScenario.getLockOwner() + "(at " + format(sectorScenario.getLockDate()) + ")";
           InfoDialog dialog = new InfoDialog(parentConsole, "Message", message);
         dialog.confirm();
        
    }

    public void showError(String message) {
        messagePanel.setError(message);    
    }

    public void showRemindingMessage(String msg) {
        messagePanel.setMessage(msg);  
        
    }

    public void run(SectorScenario sectorScenario) {
        // NOTE Auto-generated method stub
        
    }

//    public void notifyTypeChange(SectorScenarioOutputType type) {
//        String message = "Cannot edit Case: " + sectorScenario.getName() + System.getProperty("line.separator")
//        + " as it was locked by User: " + sectorScenario.getLockOwner() + "(at " + format(sectorScenario.getLockDate()) + ")";
//           InfoDialog dialog = new InfoDialog(parentConsole, "Message", message);
//         dialog.confirm();        
//        
//    }

}