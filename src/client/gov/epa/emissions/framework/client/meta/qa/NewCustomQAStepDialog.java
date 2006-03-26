package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.versions.VersionsSet;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.Dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class NewCustomQAStepDialog extends Dialog implements NewCustomQAStepView {

    private boolean shouldCreate;

    private JComboBox versionsSelection;

    private VersionsSet versions;

    private EmfDataset dataset;

    public NewCustomQAStepDialog(EmfConsole parent) {
        super("New (custom) QA Step", parent);
        super.setSize(new Dimension(550, 350));
        super.center();

    }

    public void display(EmfDataset dataset, Version[] versions) {
        DatasetType type = dataset.getDatasetType();
        super.setTitle(super.getTitle() + ": " + type.getName());

        this.dataset = dataset;
        this.versions = new VersionsSet(versions);

        JPanel layout = createLayout();
        super.getContentPane().add(layout);
        super.display();
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(inputPanel());
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        versionsSelection = new JComboBox(versions.all());
        layoutGenerator.addLabelWidgetPair("Version", versionsSelection, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
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

    public QAStep step() {
        QAStep step = new QAStep();
        step.setDatasetId(dataset.getId());
        step.setVersion(selectedVersion().getVersion());

        return step;
    }

    private Version selectedVersion() {
        return (Version) versionsSelection.getSelectedItem();
    }

}
