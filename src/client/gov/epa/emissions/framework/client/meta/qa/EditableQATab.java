package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.versions.VersionsSet;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditableQATab extends JPanel implements EditableQATabView {

    private EditableQATabPresenter presenter;

    private EditableQAStepsTableData tableData;

    private SortFilterSelectModel selectModel;

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

    public void display(Dataset dataset, QAStep[] steps, Version[] versions) {
        this.datasetID = dataset.getId(); // for uniqueness of window naming
        this.versions = new VersionsSet(versions);
        super.setLayout(new BorderLayout());
        super.add(tablePanel(steps), BorderLayout.CENTER);
        super.add(createButtonsSection(), BorderLayout.PAGE_END);

        super.setSize(new Dimension(700, 300));
    }

    protected JPanel tablePanel(QAStep[] steps) {
        tableData = new EditableQAStepsTableData(steps);
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
        String[] columnNames = { "Version", "Order" };
        return new SortCriteria(columnNames, new boolean[] { false, true }, new boolean[] { true, true });
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

        List selected = selectModel.selected();
        if (selected.size() == 0) {
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

    
    public void refresh() {
        tableData.refresh();
        selectModel.refresh();

        tablePanel.removeAll();
        tablePanel.add(createSortFilterPanel(parentConsole));
    }

    public void doSetStatus() {
        clearMessage();

        List selected = selectModel.selected();
        QAStep[] steps = (QAStep[]) selected.toArray(new QAStep[0]);
        if (steps.length > 0)
            presenter.doSetStatus(new SetQAStatusWindow(desktop, datasetID), steps);
        else
            messagePanel.setMessage("Please select a QA step.");
    }

    public void informLackOfTemplatesForAddingNewSteps(DatasetType type) {
        String message = "Dataset has no templates to choose from. Please add templates to Dataset Type: "
                + type.getName();
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

}
