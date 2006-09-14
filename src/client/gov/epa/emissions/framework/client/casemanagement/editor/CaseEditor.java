package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.inputs.EditInputsTab;
import gov.epa.emissions.framework.client.casemanagement.outputs.EditOutputsTab;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.ui.ErrorPanel;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class CaseEditor extends DisposableInteralFrame implements CaseEditorView {

    private CaseEditorPresenter presenter;

    private MessagePanel messagePanel;

    private EmfSession session;

    private EmfConsole parentConsole;
    
    public CaseEditor(EmfConsole parentConsole, EmfSession session, DesktopManager desktopManager) {
        super("Case Editor", new Dimension(750, 580), desktopManager);
        this.session = session;
        this.desktopManager = desktopManager;
        this.parentConsole = parentConsole;
    }

    private JTabbedPane createTabbedPane(Case caseObj, MessagePanel messagePanel) {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Summary", createSummaryTab(caseObj, messagePanel));
        tabbedPane.addTab("Parameters", createParameterTab(caseObj));
        tabbedPane.addTab("Inputs", createInputTab());
        tabbedPane.addTab("Programs", new JPanel());
        tabbedPane.addTab("Outputs", createOutputTab());
        tabbedPane.addTab("History", new JPanel());
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return tabbedPane;
    }

    private Component createParameterTab(Case caseObj) {
        EditableCaseParameterTab view = new EditableCaseParameterTab(caseObj, this);
        view.display();
        presenter.set(view);
        return view;
    }

    private JPanel createSummaryTab(Case caseObj, MessagePanel messagePanel) {
        try {
            EditableCaseSummaryTab view = new EditableCaseSummaryTab(caseObj, session, this, parentConsole);
            EditCaseSummaryTabPresenter summaryPresenter = new EditCaseSummaryTabPresenter(view,messagePanel,session);
            view.observe(summaryPresenter);
            view.display();
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            showError("Could not load Summary Tab." + e.getMessage());
            return createErrorTab("Could not load Summary Tab." + e.getMessage());
        }
    }
    
    private JPanel createInputTab() {
        try {
            EditInputsTab view = new EditInputsTab(parentConsole, this, messagePanel, desktopManager);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            showError("Could not load Input Tab." + e.getMessage());
            return createErrorTab("Could not load Input Tab." + e.getMessage());
        }
    }

    private JPanel createOutputTab() {
        try {
            EditOutputsTab view = new EditOutputsTab(parentConsole, this, messagePanel, desktopManager);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            showError("Could not load Output Tab." + e.getMessage());
            return createErrorTab("Could not load Output Tab." + e.getMessage());
        }
    }

    protected JPanel createErrorTab(String message) {
        return new ErrorPanel(message);
    }

    public void display(Case caseObj) {
        super.setLabel("Case Editor: " + caseObj);
        Container contentPane = super.getContentPane();
        contentPane.removeAll();

        JPanel panel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.PAGE_START);
        panel.add(createTabbedPane(caseObj, messagePanel), BorderLayout.CENTER);
        panel.add(createBottomPanel(), BorderLayout.PAGE_END);

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

        Button save = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doSave();
            }
        });
        buttonsPanel.add(save);

        Button close = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        });
        getRootPane().setDefaultButton(close);
        buttonsPanel.add(close);

        return buttonsPanel;
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
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    private String format(Date lockDate) {
        DateFormat dateFormat = new SimpleDateFormat(EmfDateFormat.format());
        return dateFormat.format(lockDate);
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
            resetChanges();
        } catch (EmfException e) {
            showError(e.getMessage());
        }
    }

}
