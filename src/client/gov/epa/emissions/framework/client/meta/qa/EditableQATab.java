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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditableQATab extends JPanel implements EditableQATabView {

    private EditableQAStepsPresenter presenter;

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

        Button add = new BorderlessButton("Add From Template", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addExisting();
            }
        });
        container.add(add);

        Button remove = new BorderlessButton("Add New", new AbstractAction() {
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

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    public void observe(EditableQAStepsPresenter presenter) {
        this.presenter = presenter;
    }

    public QAStep[] steps() {
        return tableData.sources();
    }

    private void addExisting() {
        presenter.doAdd(new NewQAStepDialog(parentConsole, versions));
    }

    public void add(QAStep[] steps) {
        QAStep[] nonexistingSteps = removeDuplicate(steps);
        for (int i = 0; i < nonexistingSteps.length; i++)
            tableData.add(nonexistingSteps[i]);

        refresh();
    }

    private QAStep[] removeDuplicate(QAStep[] steps) {
        List stepsList = new ArrayList();
        QAStep[] existed = tableData.sources();

        for (int i = 0; i < steps.length; i++) {
            if (!contains(existed, steps[i]))
                stepsList.add(steps[i]);
        }

        return (QAStep[]) stepsList.toArray(new QAStep[0]);
    }

    private boolean contains(QAStep[] step1, QAStep step2) {
        boolean contains = false;
        for (int i = 0; i < step1.length; i++) {
            if (step1[i].getName().equals(step2.getName()) && step1[i].getVersion() == step2.getVersion()) {
                contains = true;
                break;
            }
        }

        return contains;
    }

    public void refresh() {
        selectModel.refresh();

        tablePanel.removeAll();
        tablePanel.add(createSortFilterPanel(parentConsole));
    }

}
