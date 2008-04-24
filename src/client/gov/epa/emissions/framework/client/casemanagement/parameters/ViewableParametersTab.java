package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class ViewableParametersTab extends JPanel implements RefreshObserver {

    private EmfConsole parentConsole;

    private ViewableParametersTabPresenterImpl presenter;

    private Case caseObj;

    private int caseId;

    private ParametersTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel tablePanel;

    private MessagePanel messagePanel;

    private DesktopManager desktopManager;

    private EmfSession session;

    public ViewableParametersTab(EmfConsole parentConsole, MessagePanel messagePanel, DesktopManager desktopManager) {
        super.setName("viewParametersTab");
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
        super.setLayout(new BorderLayout());
    }

    public void display(EmfSession session, Case caseObj, ViewableParametersTabPresenterImpl presenter) {
        super.removeAll();

        this.caseObj = caseObj;
        this.caseId = caseObj.getId();
        this.presenter = presenter;
        this.session = session;

        try {
            super.add(createLayout(new CaseParameter[0], parentConsole), BorderLayout.CENTER);
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all case parameters.");
        }

        kickPopulateThread();
    }

    private void kickPopulateThread() {
        Thread populateThread = new Thread(new Runnable() {
            public void run() {
                retrieveParams();
            }
        });

        populateThread.start();
    }

    public synchronized void retrieveParams() {
        try {
            messagePanel.setMessage("Please wait while retrieving all case parameters...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            doRefresh(presenter.getCaseParameters(caseId));
            messagePanel.clear();
            setCursor(Cursor.getDefaultCursor());
        } catch (Exception e) {
            e.printStackTrace();
            messagePanel.setError("Cannot retrieve all case parameters.");
        }
    }

    private void doRefresh(CaseParameter[] params) throws Exception {
        //super.removeAll();
        setupTableModel(params);
        table.refresh(tableData);
        panelRefresh();
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }

    private JPanel createLayout(CaseParameter[] params, EmfConsole parentConsole)
            throws Exception {
        final JPanel layout = new JPanel(new BorderLayout());

        layout.add(tablePanel(params, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel tablePanel(CaseParameter[] params, EmfConsole parentConsole) {
        setupTableModel(params);
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        tablePanel.add(table, BorderLayout.CENTER);

        return tablePanel;
    }
    
    private void setupTableModel(CaseParameter[] params){
        tableData = new ParametersTableData(params, session);
    }


    private SortCriteria sortCriteria() {
        String[] columnNames = { "Order", "Envt. Var.", "Sector", "Parameter" };
        return new SortCriteria(columnNames, new boolean[] { true, true, true, true }, new boolean[] { false, false,
                false, false });
    }

    private JPanel controlPanel() {
        Insets insets = new Insets(1, 2, 1, 2);

        JPanel container = new JPanel();

        Button view = new ViewButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    viewParameter();
                } catch (EmfException ex) {
                    setMessage(ex.getMessage());
                }
            }
        });
        view.setMargin(insets);
        container.add(view);

        final JCheckBox showAll = new JCheckBox("Show All", false);
        showAll.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // doRefresh(showAll);
                clearMessage();
            }
        });
        showAll.setEnabled(false);
        container.add(showAll);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }


    private void viewParameter() throws EmfException {
        List params = getSelectedParameters();

        if (params.size() == 0) {
            messagePanel.setMessage("Please select parameter(s) to edit.");
            return;
        }

        for (Iterator iter = params.iterator(); iter.hasNext();) {
            CaseParameter param = (CaseParameter) iter.next();
            String title = "Edit Parameter: " + param.getName() + "(" + param.getId() + ")(" + caseObj.getName() + ")";
            EditCaseParameterView parameterEditor = new EditCaseParameterWindow(title, desktopManager);
            presenter.editParameter(param, parameterEditor);
            parameterEditor.viewOnly(title);
        }
    }


    private List getSelectedParameters() {
        return table.selected();
    }

    public CaseParameter[] caseParameters() {
        return tableData.sources();
    }


    public int numberOfRecord() {
        return tableData.sources().length;
    }

    public void clearMessage() {
        messagePanel.clear();
    }

    private void setMessage(String msg) {
        messagePanel.setError(msg);
    }

    public void doRefresh() throws EmfException {
        try {
            kickPopulateThread();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

}
