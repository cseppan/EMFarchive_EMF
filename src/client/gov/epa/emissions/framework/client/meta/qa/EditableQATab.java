package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.EditableTable;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.qa.QAService;
import gov.epa.emissions.framework.ui.EditableEmfTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class EditableQATab extends JPanel implements EditableQATabView {

    private EditableQAStepsPresenter presenter;

    private QAService service;

    private EditableQAStepsTableData tableData;

    private EditableEmfTableModel tableModel;

    private EditableTable table;

    private EmfConsole parent;

    private Version[] versions;

    ManageChangeables changeablesList;

    public EditableQATab(Version[] versions, QAService service, ManageChangeables changeablesList, EmfConsole parent) {
        this.service = service;
        this.versions = versions;

        this.parent = parent;
        this.changeablesList = changeablesList;
    }

    private JPanel createTableSection(QAStep[] steps) {
        JPanel container = new JPanel(new BorderLayout());
        container.add(table(steps), BorderLayout.CENTER);
        return container;
    }

    protected JScrollPane table(QAStep[] steps) {
        tableData = new EditableQAStepsTableData(steps);
        tableModel = new EditableEmfTableModel(tableData);
        table = new EditableTable(tableModel);
        table.setRowHeight(16);
        table.setPreferredScrollableViewportSize(new Dimension(500, 320));
        changeablesList.addChangeable(table);

        return new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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

    public void save() throws EmfException {
        QAStep[] sources = tableData.sources();
        service.update(sources);
    }

    private void addExisting() {
        presenter.doAdd(new NewQAStepDialog(parent, versions));
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
        tableModel.refresh();
        super.revalidate();
    }

    public void display(QAStep[] steps) {
        super.setLayout(new BorderLayout());
        super.add(createTableSection(steps), BorderLayout.PAGE_START);
        super.add(createButtonsSection(), BorderLayout.CENTER);
        super.setSize(new Dimension(700, 300));
    }

}
