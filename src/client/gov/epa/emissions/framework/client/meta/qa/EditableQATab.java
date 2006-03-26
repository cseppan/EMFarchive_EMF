package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.EmfTableModel;
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

    public EditableQATab(EmfConsole parent, ManageChangeables changeables) {
        this.parentConsole = parent;
        this.changeables = changeables;
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
                addUsingTemplate();
            }
        });
        container.add(add);

        Button remove = new BorderlessButton("Add (customized)", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                // TODO
            }
        });
        container.add(remove);

        Button update = new BorderlessButton("Perform", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                // TODO
            }
        });
        container.add(update);

        Button status = new BorderlessButton("Set Status", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                showStatusDialog();
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

    private void addUsingTemplate() {
        presenter.doAddUsingTemplate(new NewQAStepDialog(parentConsole, versions));
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
        selectModel.refresh();

        tablePanel.removeAll();
        tablePanel.add(createSortFilterPanel(parentConsole));
    }

    public void showStatusDialog() {
        presenter.doSetStatus(new QAStatusDialog(parentConsole));
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

}
