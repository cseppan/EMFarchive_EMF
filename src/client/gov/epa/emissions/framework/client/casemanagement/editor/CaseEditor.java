package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.history.ShowHistoryTab;
import gov.epa.emissions.framework.client.casemanagement.inputs.EditInputsTab;
import gov.epa.emissions.framework.client.casemanagement.jobs.EditJobsTab;
import gov.epa.emissions.framework.client.casemanagement.outputs.EditOutputsTab;
import gov.epa.emissions.framework.client.casemanagement.parameters.EditParametersTab;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.ui.ErrorPanel;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CaseEditor extends DisposableInteralFrame implements CaseEditorView {

    private CaseEditorPresenter presenter;

    private MessagePanel messagePanel;

    private EmfSession session;

    private EmfConsole parentConsole;

    private String tabTitle;

    private JTabbedPane tabbedPane;
    
    private boolean hasValues = false; 
    
    public CaseEditor(EmfConsole parentConsole, EmfSession session, DesktopManager desktopManager) {
        super("Case Editor", new Dimension(820, 580), desktopManager);
        this.session = session;
        this.desktopManager = desktopManager;
        this.parentConsole = parentConsole;
    }

    public void display(Case caseObj, String msg) throws EmfException {
        super.setLabel("Case Editor: " + caseObj);
        Container contentPane = super.getContentPane();
        contentPane.removeAll();

        JPanel panel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.PAGE_START);
        panel.add(createTabbedPane(caseObj, messagePanel), BorderLayout.CENTER);
        panel.add(createBottomPanel(), BorderLayout.PAGE_END);

        if (msg != null && !msg.isEmpty())
            messagePanel.setMessage(msg);

        contentPane.add(panel);
        super.display();
        resetChanges();
        String validationMsg = validateValues(caseObj);
        if (hasValues){
            showMessageDialog(validationMsg);
        }
    }
    
    private JTabbedPane createTabbedPane(Case caseObj, MessagePanel messagePanel) {
        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Summary", createSummaryTab(caseObj, messagePanel));
        tabbedPane.addTab("Jobs", createJobsTab());
        tabbedPane.addTab("Inputs", createInputTab());
        tabbedPane.addTab("Parameters", createParameterTab(messagePanel));
        tabbedPane.addTab("Outputs", createOutputTab());
        tabbedPane.addTab("History", createHistoryTab());
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        final MessagePanel localMsgPanel = this.messagePanel;

        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                try {
                    localMsgPanel.clear();
                    loadComponents();
                } catch (EmfException exc) {
                    showError("Could not load component: " + tabbedPane.getSelectedComponent().getName());
                }
            }
        });

        return tabbedPane;
    }

    protected void loadComponents() throws EmfException {
        int tabIndex = tabbedPane.getSelectedIndex();
        tabTitle = tabbedPane.getTitleAt(tabIndex);
        presenter.doLoad(tabTitle);
    }

    private JPanel createSummaryTab(Case caseObj, MessagePanel messagePanel) {
        EditableCaseSummaryTab view = new EditableCaseSummaryTab(caseObj, session, this, parentConsole);
        EditCaseSummaryTabPresenter summaryPresenter = new EditCaseSummaryTabPresenter(session);
        view.observe(summaryPresenter);
        try {
            view.display();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        presenter.set(view);
        return view;
    }

    private Component createParameterTab(MessagePanel messagePanel) {
        EditParametersTab view = new EditParametersTab(parentConsole, messagePanel, desktopManager);
        presenter.set(view);
        return view;
    }

    private JPanel createInputTab() {
        try {
            EditInputsTab view = new EditInputsTab(parentConsole, this, messagePanel, desktopManager);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            showError("Could not load Parameter Tab." + e.getMessage());
            return createErrorTab("Could not load Parameter Tab." + e.getMessage());
        }
    }

    private Component createJobsTab() {
        try {
            EditJobsTab view = new EditJobsTab(parentConsole, this, messagePanel, desktopManager, session);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            showError("Could not load CaseJob Tab." + e.getMessage());
            return createErrorTab("Could not load CaseJob Tab." + e.getMessage());
        }
    }

    private JPanel createOutputTab() {
        try {
            EditOutputsTab view = new EditOutputsTab(parentConsole, this, messagePanel, desktopManager, session);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            showError("Could not load Output Tab. " + e.getMessage());
            return createErrorTab("Could not load Output Tab. " + e.getMessage());
        }
    }

    private JPanel createHistoryTab() {
        ShowHistoryTab view = new ShowHistoryTab(parentConsole, messagePanel, session);
        presenter.set(view);
        return view;
    }

    protected JPanel createErrorTab(String message) {
        return new ErrorPanel(message);
    }


    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createControlPanel(), BorderLayout.LINE_END);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel buttonsPanel = new JPanel();

        Button refresh = new Button("Refresh", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    refreshCurrentTab();
                } catch (EmfException e) {
                    showError(e.getMessage());
                }
            }
        });
        buttonsPanel.add(refresh);
        refresh.setToolTipText("Refresh only the current tab with focus.");

        Button save = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doSave();
            }
        });
        buttonsPanel.add(save);
        save.setToolTipText("Saves only the information on the Summary tab and the input and output folders.");

        Button close = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        });
        getRootPane().setDefaultButton(close);
        buttonsPanel.add(close);

        return buttonsPanel;
    }

    private void refreshCurrentTab() throws EmfException {
        RefreshObserver tab = (RefreshObserver) tabbedPane.getSelectedComponent();

        try {
            messagePanel.clear();
            tab.doRefresh();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } 
    }

    public void observe(CaseEditorPresenter presenter) {
        this.presenter = presenter;
    }

    public void showError(String message) {
        messagePanel.setError(message);
    }

    public void notifyLockFailure(Case caseObj) {
        String message = "Cannot edit Properties of Case: " + caseObj + System.getProperty("line.separator")
                + " as it was locked by User: " + caseObj.getLockOwner() + "(at " + format(caseObj.getLockDate()) + ")";
        InfoDialog dialog = new InfoDialog(parentConsole, "Message", message);
        dialog.confirm();
    }
    
    private void showMessageDialog(String msg) {
        int width = 50;
        int height = (msg.length() / 50)+3;
        String title = "Jobs in the case may not run until the following items are corrected:";
        JDialog dialog =new JDialog(parentConsole, title, false);
        dialog.getContentPane().add(createMsgScrollPane(msg, width, height));
        dialog.setLocation(ScreenUtils.getPointToCenter(this));
        dialog.pack();
        dialog.setPreferredSize(new Dimension(400, 300));
        dialog.setModal(false);
        dialog.setVisible(true);
    }
    
    private ScrollableComponent createMsgScrollPane(String msg, int width, int height) {
        TextArea message = new TextArea("msgArea", msg, width, height);
        message.setEditable(false);
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(message);
        return descScrollableTextArea;
    }
    
    private String validateValues(Case caseObj) throws EmfException{
        String noLocalValues = "";
        CaseInput[] inputList = presenter.getCaseInput(caseObj.getId(), new Sector("All", "All"), true);
        noLocalValues += "The following non-local inputs do not have datasets specified: \n";
        for (CaseInput input :inputList){
            if ( !input.isLocal() && input.getDataset()==null){
                hasValues = true; 
                noLocalValues += getInputValues(input) +"\n";
            }
        }
        CaseParameter[] paraList = presenter.getCaseParameters(caseObj.getId(), new Sector("All", "All"), true);
        noLocalValues += "\nThe following non-local parameters do not have values: \n"; 
        for (CaseParameter par :paraList){
            if ( !par.isLocal() && par.getValue().trim().isEmpty()){
                noLocalValues += getParamValues(par) + "\n";
                hasValues = true; 
            }
        }
        return noLocalValues;
    }
    
    private String getInputValues(CaseInput input) throws EmfException{
        String Value = (input.getEnvtVars() == null ? "" : input.getEnvtVars().getName()) + "; " 
                     + (input.getSector() == null ? "All sectors" : input.getSector().getName())+ "; "
                     + presenter.getJobName(input.getCaseJobID()) + "; "
                     + input.getName();
        return Value; 
    }
    
    private String getParamValues(CaseParameter parameter) throws EmfException{
        String Value = (parameter.getEnvVar() == null ? "" : parameter.getEnvVar().getName()) + "; " 
                     + (parameter.getSector() == null ? "All sectors" : parameter.getSector().getName())+ "; " 
                     + presenter.getJobName(parameter.getJobId()) + "; "
                     + parameter.getName();
        return Value; 
    }

    public void showRemindingMessage(String msg) {
        messagePanel.setMessage(msg);
    }

    private String format(Date lockDate) {
        return CustomDateFormat.format_YYYY_MM_DD_HH_MM(lockDate);
    }

    public void windowClosing() {
        doClose();
    }

    private void doClose() {
        try {
            if (shouldDiscardChanges())
                presenter.doClose();
        } catch (EmfException e) {
            showError("Could not close: " + e.getMessage());
        }
    }

    private void doSave() {
        try {
            presenter.doSave();
            messagePanel.setMessage("Case was saved successfully.");
            resetChanges();
        } catch (EmfException e) {
            showError(e.getMessage());
        }
    }

    public void showLockingMsg(String msg) {
        InfoDialog dialog = new InfoDialog(parentConsole, "Message", msg);
        dialog.confirm();
    }

}
