package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.mims.analysisengine.table.SortFilterTablePanel;

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

    private ManageChangeables changeables;

    private Version[] versions;

    private MessagePanel messagePanel;

    private DesktopManager desktop;

    public EditableQATab(EmfConsole parent, DesktopManager desktop, ManageChangeables changeables,
            MessagePanel messagePanel) {
        this.parentConsole = parent;
        this.desktop = desktop;
        this.changeables = changeables;
        this.messagePanel = messagePanel;
    }

    public void display(QAStep[] steps, Version[] versions) {
        this.versions = versions;
        super.setLayout(new BorderLayout());
        super.add(tablePanel(steps), BorderLayout.CENTER);
        super.add(createButtonsSection(), BorderLayout.PAGE_END);

        super.setSize(new Dimension(700, 300));
    }

    protected JPanel tablePanel(QAStep[] steps) {
        tableData = new EditableQAStepsTableData(steps);
        changeables.addChangeable(tableData);
        selectModel = new SortFilterSelectModel(new EmfTableModel(tableData));

        tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(createSortFilterPanel(parentConsole), BorderLayout.CENTER);

        return tablePanel;
    }

    private JScrollPane createSortFilterPanel(EmfConsole parentConsole) {
        SortFilterTablePanel sortFilterPanel = new SortFilterTablePanel(parentConsole, selectModel);

        JScrollPane scrollPane = new JScrollPane(sortFilterPanel);
        sortFilterPanel.setPreferredSize(new Dimension(450, 60));
        return scrollPane;
    }

    private JPanel createButtonsSection() {
        JPanel container = new JPanel();

        Button add = new BorderlessButton("Add (using Template)", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddUsingTemplate();
            }
        });
        container.add(add);

        Button remove = new BorderlessButton("Add (customized)", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doAddCustom();
            }
        });
        container.add(remove);

        Button update = new BorderlessButton("Perform", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doPerform();
            }
        });
        container.add(update);

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
        presenter.doAddUsingTemplate(new NewQAStepDialog(parentConsole, versions));
    }

    private void doAddCustom() {
        clearMessage();
        try {
            presenter.doAddCustomized(new NewCustomQAStepDialog(parentConsole));
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void doPerform() {
        clearMessage();

        List selected = selectModel.selected();
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            QAStep step = (QAStep) iter.next();
            PerformQAStepWindow view = new PerformQAStepWindow(desktop);
            presenter.doPerform(step, view);
        }
    }

    private void clearMessage() {
        messagePanel.clear();
    }

    public void add(QAStep[] steps) {
        QASteps qaSteps = new QASteps(tableData.sources());
        QAStep[] newSteps = qaSteps.filterDuplicates(steps);
        for (int i = 0; i < newSteps.length; i++)
            tableData.add(newSteps[i]);

        refresh();
    }

    public void add(QAStep step) {
        add(new QAStep[] { step });
    }

    public void refresh() {
        tableData.refresh();
        selectModel.refresh();

        tablePanel.removeAll();
        tablePanel.add(createSortFilterPanel(parentConsole));
    }

    public void doSetStatus() {
        clearMessage();
        presenter.doSetStatus(new SetQAStatusDialog(parentConsole));
    }

    public void setStatus(QAStep step) {
        List selected = selectModel.selected();

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            QAStep selectedStep = (QAStep) iter.next();
            selectedStep.setStatus(step.getStatus());
            selectedStep.setWhen(step.getWhen());
            selectedStep.setWho(step.getWho());
            selectedStep.setResult(selectedStep.getResult() + System.getProperty("line.separator") + step.getResult());
        }

        refresh();
    }

    public void informLackOfTemplatesForAddingNewSteps(DatasetType type) {
        String message = "Dataset has no templates to choose from. Please add templates to Dataset Type: " + type.getName();
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

}
