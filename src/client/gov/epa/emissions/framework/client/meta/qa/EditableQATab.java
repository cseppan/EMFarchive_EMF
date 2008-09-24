package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.versions.VersionsSet;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPanel;

public class EditableQATab extends JPanel implements EditableQATabView {

    private EditableQATabPresenter presenter;

    private EditableQAStepsTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel tablePanel;

    private EmfConsole parentConsole;

    private VersionsSet versions;

    private MessagePanel messagePanel;

    private DesktopManager desktop;

    private int datasetID;

    public EditableQATab(EmfConsole parent, DesktopManager desktop, MessagePanel messagePanel) {
        this.parentConsole = parent;
        this.desktop = desktop;
        this.messagePanel = messagePanel;
    }

    public void display(Dataset dataset, QAStep[] steps, QAStepResult[] qaStepResults, Version[] versions) {
        this.datasetID = dataset.getId(); // for uniqueness of window naming
        this.versions = new VersionsSet(versions);
        super.setLayout(new BorderLayout());
        super.add(tablePanel(steps, qaStepResults), BorderLayout.CENTER);
        super.add(createButtonsSection(), BorderLayout.PAGE_END);

        super.setSize(new Dimension(700, 300));
    }

    protected JPanel tablePanel(QAStep[] steps, QAStepResult[] qaStepResults) {
        setupTableModel(steps, qaStepResults);

        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        tablePanel.add(table, BorderLayout.CENTER);

        return tablePanel;
    }
    
    private void setupTableModel(QAStep[] steps, QAStepResult[] qaStepResults) {
        tableData = new EditableQAStepsTableData(steps, qaStepResults);
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Version", "Order", "Name" };
        return new SortCriteria(columnNames, new boolean[] { false, true, true }, new boolean[] { true, true, true });
        //return new SortCriteria(columnNames, new boolean[] { false, false }, new boolean[] { true, true });
    }

    private JPanel createButtonsSection() {
        JPanel container = new JPanel();

        Button add = new BorderlessButton("Add from Template", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddUsingTemplate();
            }
        });
        container.add(add);

        Button remove = new BorderlessButton("Add Custom", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doAddCustom();
            }
        });
        container.add(remove);

        Button edit = new BorderlessButton("Edit", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doEdit();
            }
        });
        container.add(edit);

        Button status = new BorderlessButton("Set Status", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doSetStatus();
            }
        });
        container.add(status);
        
        Button runStatus = new BorderlessButton("Run", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                runStatus();
            }
        });
        container.add(runStatus);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    public void observe(EditableQATabPresenter presenter) {
        this.presenter = presenter;
    }

    public QAStep[] steps() {
        return tableData.sources();
    }

    private void doAddUsingTemplate() {
        clearMessage();
        presenter.doAddUsingTemplate(new NewQAStepDialog(parentConsole, versions.all()));
    }

    private void doAddCustom() {
        clearMessage();
        try {
            presenter.doAddCustomized(new NewCustomQAStepWindow(desktop));
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void doEdit() {
        clearMessage();

        List selected = table.selected();
        if (selected == null || selected.size() == 0) {
            messagePanel.setMessage("Please select a QA step.");
            return;
        }

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            QAStep step = (QAStep) iter.next();
            EditQAStepWindow view = new EditQAStepWindow(desktop, parentConsole);
            try {
                presenter.doEdit(step, view, versions.name(step.getVersion()));
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }
        }
    }

    private void clearMessage() {
        messagePanel.clear();
    }

    public void addFromTemplate(QAStep[] steps) {
        QAStep[] newSteps = filterDuplicates(steps);
        try {
            presenter.addFromTemplates(newSteps);
            addNewStepsToTable(newSteps);
        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        }
    }

    public void addCustomQAStep(QAStep step) {
        QAStep[] steps = new QAStep[] { step };
        steps = filterDuplicates(steps);
        addNewStepsToTable(steps);
    }


    private QAStep[] filterDuplicates(QAStep[] steps) {
        QASteps qaSteps = new QASteps(tableData.sources());
        QAStep[] newSteps = qaSteps.filterDuplicates(steps);
        for (int i = 0; i < newSteps.length; i++)
            newSteps[i].setStatus("Not Started");
        return newSteps;
    }
    
    private void addNewStepsToTable(QAStep[] newSteps) {
        for (int i = 0; i < newSteps.length; i++)
            tableData.add(newSteps[i]);
        refresh();
    }

    public void refresh(QAStep step, QAStepResult result) {
        tableData.add(step, result);
        refresh();
    }
    
    public void refresh() {
        tableData.refresh();
        table.refresh(tableData);
        panelRefresh();
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }

    private void doSetStatus() {
        clearMessage();

        List selected = table.selected();
        QAStep[] steps = (QAStep[]) selected.toArray(new QAStep[0]);
        if (steps.length > 0)
            presenter.doSetStatus(new SetQAStatusWindow(desktop, datasetID), steps);
        else
            messagePanel.setMessage("Please select a QA step.");
    }
    
    private void runStatus() {
        clearMessage();

        List selected = table.selected();
        QAStep[] steps = (QAStep[]) selected.toArray(new QAStep[0]);
        if (steps==null ||steps.length == 0) {
            messagePanel.setMessage("Please select a QA step.");
            return; 
        }
        if (steps.length > 3 ){
            messagePanel.setMessage("You may only run three at the same time.");
            return;
        }
        for (int i=0; i<steps.length; i++){
            try {
                messagePanel.setMessage("Started " + (steps.length==1? " One Run. " : steps.length+" Runs. ")+" Please monitor the Status Window. ");
                 presenter.runStatus(steps[i]);
            } catch (EmfException e) {
                messagePanel.setMessage(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void informLackOfTemplatesForAddingNewSteps(DatasetType type) {
        String message = "Dataset has no templates to choose from. Please add templates to Dataset Type: "
                + type.getName();
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

}
