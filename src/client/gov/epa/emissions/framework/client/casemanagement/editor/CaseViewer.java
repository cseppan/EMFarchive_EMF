package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.history.ViewableHistoryTab;
import gov.epa.emissions.framework.client.casemanagement.inputs.ViewableInputsTab;
import gov.epa.emissions.framework.client.casemanagement.jobs.ViewableJobsTab;
import gov.epa.emissions.framework.client.casemanagement.outputs.ViewableOutputsTab;
import gov.epa.emissions.framework.client.casemanagement.parameters.ViewableParametersTab;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.ui.ErrorPanel;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CaseViewer extends DisposableInteralFrame implements CaseViewerView{

    private CaseViewerPresenter presenter;

    private MessagePanel messagePanel;

    private EmfSession session;

    private EmfConsole parentConsole;
    
    private String tabTitle;
    
    private JTabbedPane tabbedPane;
    
    public CaseViewer(EmfConsole parentConsole, EmfSession session, DesktopManager desktopManager) {
        super("Case Viewer", new Dimension(780, 580), desktopManager);
        this.session = session;
        this.desktopManager = desktopManager;
        this.parentConsole = parentConsole;
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

        tabbedPane.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                try {
                    localMsgPanel.clear();
                    loadComponents();
                } catch (EmfException exc) {
                    showError("Could not load component: "  + tabbedPane.getSelectedComponent().getName());
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
        ViewableCaseSummaryTab view = new ViewableCaseSummaryTab(caseObj, session, parentConsole);
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
        ViewableParametersTab view = new ViewableParametersTab(parentConsole, messagePanel, desktopManager);
        presenter.set(view);
        return view;
    }

    private JPanel createInputTab() {
        ViewableInputsTab view = new ViewableInputsTab(parentConsole, messagePanel, desktopManager);
            try {
                presenter.set(view);
            } catch (EmfException e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }
        return view;
    }

    private Component createJobsTab() {
        ViewableJobsTab view = new ViewableJobsTab(parentConsole, messagePanel, desktopManager, session);
            try {
                presenter.set(view);
            } catch (EmfException e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }
        return view;
    }
    
    private JPanel createOutputTab() {
        ViewableOutputsTab view = new ViewableOutputsTab(parentConsole, messagePanel, desktopManager, session);
            try {
                presenter.set(view);
            } catch (EmfException e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }
        return view;
    }
    
    private JPanel createHistoryTab() {
        ViewableHistoryTab view = new ViewableHistoryTab(parentConsole, messagePanel, session);
        presenter.set(view);
        return view;
    }

    protected JPanel createErrorTab(String message) {
        return new ErrorPanel(message);
    }

    public void display(Case caseObj, String caseJobSummaryMsg) {
        super.setLabel("Case Viewer: " + caseObj);
        Container contentPane = super.getContentPane();
        contentPane.removeAll();

        JPanel panel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.PAGE_START);
        panel.add(createTabbedPane(caseObj, messagePanel), BorderLayout.CENTER);
        panel.add(createBottomPanel(), BorderLayout.PAGE_END);
        
        if (caseJobSummaryMsg != null && !caseJobSummaryMsg.isEmpty())
            messagePanel.setMessage(caseJobSummaryMsg);


        contentPane.add(panel);
        super.display();
        resetChanges();
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
        RefreshObserver tab = (RefreshObserver)tabbedPane.getSelectedComponent();
        try {
            messagePanel.clear();
            tab.doRefresh();
        } catch (Exception e) {
            throw new EmfException(tabbedPane.getSelectedComponent().getName() + e.getMessage());
        }
    }

    public void observe(CaseViewerPresenter presenter) {
        this.presenter = presenter;
    }

    public void showError(String message) {
        messagePanel.setError(message);
    }

//    public void notifyLockFailure(Case caseObj) {
//        String message = "Cannot edit Properties of Case: " + caseObj + System.getProperty("line.separator")
//                + " as it was locked by User: " + caseObj.getLockOwner() + "(at " + format(caseObj.getLockDate()) + ")";
//        InfoDialog dialog = new InfoDialog(this, "Message", message);
//        dialog.confirm();
//    }

//    private String format(Date lockDate) {
//        return CustomDateFormat.format_YYYY_MM_DD_HH_MM(lockDate);
//    }

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
    
    public void showLockingMsg(String msg) {
        InfoDialog dialog = new InfoDialog(parentConsole, "Message", msg);
        dialog.confirm();
    }

}