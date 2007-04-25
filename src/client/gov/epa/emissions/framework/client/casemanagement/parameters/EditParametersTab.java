package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.EditButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditParametersTab extends JPanel implements EditCaseParametersTabView, Runnable {

    private EmfConsole parentConsole;

    private EditParametersTabPresenter presenter;

    private Case caseObj;

    private int caseId;

    private ParametersTableData tableData;

    private SortFilterSelectModel selectModel;

    private JPanel tablePanel;

    private MessagePanel messagePanel;

    private DesktopManager desktopManager;

    private EmfSession session;

    private volatile Thread populateThread;

    public EditParametersTab(EmfConsole parentConsole, MessagePanel messagePanel,
            DesktopManager desktopManager) {
        super.setName("editParametersTab");
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
        this.populateThread = new Thread(this);

        super.setLayout(new BorderLayout());
    }

    public void display(EmfSession session, Case caseObj, EditParametersTabPresenter presenter) {
        super.removeAll();

        this.caseObj = caseObj;
        this.caseId = caseObj.getId();
        this.presenter = presenter;
        this.session = session;

        try {
            super.add(createLayout(new CaseParameter[0], presenter, parentConsole), BorderLayout.CENTER);
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all case parameters.");
        }

        populateThread.start();
    }
    
    public void run() {
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
        super.removeAll();
        super.add(createLayout(params, presenter, parentConsole), BorderLayout.CENTER);
    }

    private JPanel createLayout(CaseParameter[] params, EditParametersTabPresenter presenter, EmfConsole parentConsole)
            throws Exception {
        final JPanel layout = new JPanel(new BorderLayout());

        layout.add(tablePanel(params, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(presenter), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel tablePanel(CaseParameter[] params, EmfConsole parentConsole) {
        tableData = new ParametersTableData(params, session);
        selectModel = new SortFilterSelectModel(new EmfTableModel(tableData));

        tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(createSortFilterPanel(parentConsole), BorderLayout.CENTER);

        return tablePanel;
    }

    private JScrollPane createSortFilterPanel(EmfConsole parentConsole) {
        SortFilterSelectionPanel sortFilterPanel = new SortFilterSelectionPanel(parentConsole, selectModel);
        sortFilterPanel.sort(sortCriteria());

        JScrollPane scrollPane = new JScrollPane(sortFilterPanel);
        sortFilterPanel.setPreferredSize(new Dimension(450, 60));
        return scrollPane;
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Sector", "Program", "Parameter" };
        return new SortCriteria(columnNames, new boolean[] { true, true, true }, new boolean[] { false, false, false });
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
                removeParameter(presenter);
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
                    messagePanel.setError(ex.getMessage());
                }
            }
        });
        edit.setMargin(insets);
        container.add(edit);
        
        Button copy = new CopyButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        copy.setMargin(insets);
        copy.setEnabled(false);
        container.add(copy);

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

    protected void doNewInput(EditParametersTabPresenter presenter) {
        NewCaseParameterDialog view = new NewCaseParameterDialog(parentConsole);
        try {
            presenter.addNewParameterDialog(view);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    protected void removeParameter(EditParametersTabPresenter presenter) {
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

    public void addParameter(CaseParameter param) {
        tableData.add(param);
        selectModel.refresh();

        tablePanel.removeAll();
        tablePanel.add(createSortFilterPanel(parentConsole));
    }

    private List getSelectedParameters() {
        return selectModel.selected();
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

}
