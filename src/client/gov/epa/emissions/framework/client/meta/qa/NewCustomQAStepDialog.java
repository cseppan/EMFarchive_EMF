package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.versions.VersionsSet;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.emissions.framework.ui.NumberFormattedTextField;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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

    private TextField name;

    private EditableComboBox program;

    private TextArea programParameters;

    private NumberFormattedTextField order;

    private TextArea description;

    private SingleLineMessagePanel messagePanel;

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

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);

        panel.add(inputPanel());
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        String[] defaultProgram = { "EmisView", "Smkreport", "Smkinven" };

        name = new TextField("", 40);
        layoutGenerator.addLabelWidgetPair("Name", name, panel);

        program = new EditableComboBox(defaultProgram);
        layoutGenerator.addLabelWidgetPair("Program", program, panel);

        programParameters = new TextArea("", "", 40, 3);
        ScrollableComponent scrollableDetails = ScrollableComponent.createWithVerticalScrollBar(programParameters);
        layoutGenerator.addLabelWidgetPair("Parameters", scrollableDetails, panel);

        order = new NumberFormattedTextField(5, orderAction());
        order.addEditTrackingListener();
        order.addKeyListener(keyListener());
        layoutGenerator.addLabelWidgetPair("Order", order, panel);

        CheckBox required = new CheckBox("", false);
        required.setEnabled(false);
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        description = new TextArea("", "", 40, 10);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        ScrollableComponent scrollableDesc = ScrollableComponent.createWithVerticalScrollBar(description);
        layoutGenerator.addLabelWidgetPair("Description", scrollableDesc, panel);

        versionsSelection = new JComboBox(versions.all());
        layoutGenerator.addLabelWidgetPair("Version", versionsSelection, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private KeyListener keyListener() {
        return new KeyListener() {
            public void keyTyped(KeyEvent e) {
                keyActions();
            }

            public void keyReleased(KeyEvent e) {
                keyActions();
            }

            public void keyPressed(KeyEvent e) {
                keyActions();
            }
        };
    }

    private void keyActions() {
        try {
            messagePanel.clear();
            Float.parseFloat(order.getText());
        } catch (NumberFormatException ex) {
            messagePanel.setError("Order should be a floating point number");
        }
    }

    private AbstractAction orderAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Float.parseFloat(order.getText());
                } catch (NumberFormatException ex) {
                    messagePanel.setError("Order should be a floating point number");
                }
            }
        };
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
        step.setRequired(false);

        return step;
    }

    private Version selectedVersion() {
        return (Version) versionsSelection.getSelectedItem();
    }

}
