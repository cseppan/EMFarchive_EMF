package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.EditButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
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
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditParametersTab extends JPanel implements EditCaseParametersTabView, RefreshObserver {

    private EmfConsole parentConsole;

    private EditParametersTabPresenterImpl presenter;

    private Case caseObj;

    private int caseId;

    private ParametersTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel tablePanel;
    
    private ComboBox sectorsComboBox;

    private JCheckBox showAll;

    private MessagePanel messagePanel;

    private DesktopManager desktopManager;

    private EmfSession session;

    public EditParametersTab(EmfConsole parentConsole, MessagePanel messagePanel, DesktopManager desktopManager) {
        super.setName("editParametersTab");
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
        super.setLayout(new BorderLayout());
    }

    public void display(EmfSession session, Case caseObj, EditParametersTabPresenter presenter) {
        super.removeAll();

        this.caseObj = caseObj;
        this.caseId = caseObj.getId();
        this.presenter = (EditParametersTabPresenterImpl)presenter;
        this.session = session;

        try {
            super.add(createLayout(new CaseParameter[0], presenter, parentConsole), BorderLayout.CENTER);
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
            doRefresh(presenter.getCaseParameters(caseId, getSelectedSector(), showAll.isSelected()));
            messagePanel.clear();
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all case parameters.");
        } finally {
            setCursor(Cursor.getDefaultCursor());
            
            try {
                presenter.checkIfLockedByCurrentUser();
            } catch (Exception e) {
                messagePanel.setMessage(e.getMessage());
            }
        }
    }

    private void doRefresh(CaseParameter[] params) throws Exception {
        //super.removeAll();
        setupTableModel(params);
        table.refresh(tableData);
        panelRefresh();
    }

    private JPanel createLayout(CaseParameter[] params, EditParametersTabPresenter presenter, EmfConsole parentConsole)
            throws Exception {
        final JPanel layout = new JPanel(new BorderLayout());

        layout.add(createSectorPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(params, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(presenter), BorderLayout.PAGE_END);

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

    private JPanel createSectorPanel() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        sectorsComboBox = new ComboBox("Select a Sector", presenter.getAllSetcors());
        layoutGenerator.addLabelWidgetPair("Sector:", sectorsComboBox, panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }
    
    private JPanel controlPanel(final EditParametersTabPresenter presenter) {
        Insets insets = new Insets(1, 2, 1, 2);

        JPanel container = new JPanel();

        Button add = new AddButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                doNewInput(presenter);
            }
        });
        add.setMargin(insets);
        container.add(add);

        Button remove = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                try {
                    removeParameter(presenter);
                } catch (EmfException exc) {
                    setErrorMessage(exc.getMessage());
                }
            }
        });
        remove.setMargin(insets);
        container.add(remove);

        Button edit = new EditButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    editParameter(presenter);
                } catch (EmfException ex) {
                    setErrorMessage(ex.getMessage());
                }
            }
        });
        edit.setMargin(insets);
        container.add(edit);

        String message = "You have asked to copy too many parameters. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);
        SelectAwareButton copy = new SelectAwareButton("Copy", copyAction(presenter), table, confirmDialog);
        copy.setMargin(insets);
        container.add(copy);

        showAll = new JCheckBox("Show All", false);
        showAll.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                try {
                    doRefresh(presenter.getCaseParameters(caseId, getSelectedSector(), showAll.isSelected()));
                } catch (Exception e1) {
                    setErrorMessage(e1.getMessage());
                }
            }
        });
        container.add(showAll);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    private Action copyAction(final EditParametersTabPresenter localPresenter) {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    clearMessage();
                    copyParameters(localPresenter);
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };
    }

    protected void doNewInput(EditParametersTabPresenter presenter) {
        NewCaseParameterDialog view = new NewCaseParameterDialog(parentConsole);
        try {
            CaseParameter newParameter = new CaseParameter();
            newParameter.setShow(true);
            newParameter.setRequired(true);
            presenter.addNewParameterDialog(view, newParameter);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    protected void removeParameter(EditParametersTabPresenter presenter) throws EmfException {
        CaseParameter[] params = (CaseParameter[]) getSelectedParameters().toArray(new CaseParameter[0]);

        if (params.length == 0) {
            messagePanel.setMessage("Please select parameter(s) to remove.");
            return;
        }

        String title = "Warning";
        String message = "Are you sure you want to remove the selected parameter(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(params);
            refresh();
            presenter.removeParameters(params);
        }
    }

    private void editParameter(EditParametersTabPresenter presenter) throws EmfException {
        List params = getSelectedParameters();

        if (params.size() == 0) {
            messagePanel.setMessage("Please select parameter(s) to edit.");
            return;
        }

        for (Iterator iter = params.iterator(); iter.hasNext();) {
            CaseParameter param = (CaseParameter) iter.next();
            String title = param.getName() + "(" + param.getId() + ")(" + caseObj.getName() + ")";
            EditCaseParameterView parameterEditor = new EditCaseParameterWindow(title, desktopManager);
            presenter.editParameter(param, parameterEditor);
        }
    }

    private void copyParameters(EditParametersTabPresenter presenter) throws Exception {
        List params = getSelectedParameters();

        if (params.size() == 0) {
            messagePanel.setMessage("Please select parameter(s) to copy.");
            return;
        }

        for (Iterator iter = params.iterator(); iter.hasNext();) {
            CaseParameter param = (CaseParameter) iter.next();
            NewCaseParameterDialog view = new NewCaseParameterDialog(parentConsole);
            view.setModal(false);
            view.setLocationByPlatform(true);
            presenter.copyParameter(view, param);
        }
    }

    public void addParameter(CaseParameter param) {
        tableData.add(param);
        table.refresh(tableData);
        panelRefresh();
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }

    private List getSelectedParameters() {
        return table.selected();
    }

    public CaseParameter[] caseParameters() {
        return tableData.sources();
    }

    public void refresh() {
        // note that this will get called when the case is save
        try {
            if (tableData != null) // it's still null if you've never displayed this tab
                doRefresh(tableData.sources());
        } catch (Exception e) {
            messagePanel.setError("Cannot refresh current tab. " + e.getMessage());
        }
    }

    public int numberOfRecord() {
        return tableData.sources().length;
    }

    public void clearMessage() {
        messagePanel.clear();
    }

    private void setErrorMessage(String msg) {
        messagePanel.setError(msg);
    }

    private Sector getSelectedSector() {
        return (Sector)sectorsComboBox.getSelectedItem();
    }

    public void doRefresh() throws EmfException {
        try {
            kickPopulateThread();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

}
