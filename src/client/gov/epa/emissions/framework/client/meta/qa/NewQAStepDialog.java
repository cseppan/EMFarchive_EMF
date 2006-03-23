package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.versions.VersionsSet;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.Dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

public class NewQAStepDialog extends Dialog implements NewQAStepView {

    private boolean shouldCreate;

    private JComboBox versionsSelection;

    private VersionsSet versions;

    private EmfDataset dataset;

    private QAStepTemplates templates;

    private JList optional;

    public NewQAStepDialog(EmfConsole parent, Version[] versions) {
        super("New QA Step", parent);
        super.setSize(new Dimension(550, 350));
        super.center();

        this.versions = new VersionsSet(versions);
    }

    public void display(EmfDataset dataset, DatasetType type) {
        super.setTitle(super.getTitle() + ": " + type.getName());

        this.dataset = dataset;
        templates = new QAStepTemplates(type.getQaStepTemplates());

        JPanel layout = createLayout(templates);
        super.getContentPane().add(layout);
        super.display();
    }

    private JPanel createLayout(QAStepTemplates templates) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(inputPanel(templates));
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel(QAStepTemplates templates) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        versionsSelection = new JComboBox(versions.all());
        layoutGenerator.addLabelWidgetPair("Version", versionsSelection, panel);

        JList required = new JList(templates.required());
        required.setEnabled(false);
        layoutGenerator.addLabelWidgetPair("Required", createScrollPane(required), panel);

        JScrollPane optionPane = createScrollPane(optional(templates));
        layoutGenerator.addLabelWidgetPair("Optional", optionPane, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JScrollPane createScrollPane(Component component) {
        return new ScrollableComponent(component, new Dimension(300, 100));
    }

    private JList optional(QAStepTemplates templates) {
        optional = new JList(templates.optional());
        optional.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        return optional;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new Button("OK", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doNew();
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new Button("Cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                shouldCreate = false;
                close();
            }
        });
        panel.add(cancel);

        return panel;
    }

    private void doNew() {
        shouldCreate = true;
        close();
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public QAStep[] qaSteps() {
        List steps = new ArrayList();

        Version version = selectedVersion();
        List selectedValues = Arrays.asList(optional.getSelectedValues());
        QAStepTemplate[] optionalTemplates = (QAStepTemplate[]) selectedValues.toArray(new QAStepTemplate[0]);

        QAStep[] all = templates.instantiate(optionalTemplates, dataset, version);

        steps.addAll(Arrays.asList(all));

        return (QAStep[]) steps.toArray(new QAStep[0]);
    }

    private Version selectedVersion() {
        return (Version) versionsSelection.getSelectedItem();
    }

}
