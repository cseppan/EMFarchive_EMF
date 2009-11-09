package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class MeasureReferenceWindow extends DisposableInteralFrame {

    private MessagePanel messagePanel;

    private TextArea descriptionField;

    private SaveButton saveButton;

    private Button cancelButton;

    private MeasureReferencePresenter presenter;

    private Reference reference;

    private boolean newReference = false;

    private ManageChangeables changeablesList;

    private static int counter = 0;

    private static final Dimension DIMENSION = new Dimension(400, 200);

    public MeasureReferenceWindow(String title, ManageChangeables changeablesList, DesktopManager desktopManager,
            EmfSession session) {

        super(title, DIMENSION, desktopManager);
        this.setMinimumSize(DIMENSION);
        this.changeablesList = changeablesList;
    }

    public void save() {

        messagePanel.clear();
        doSave();

        if (!newReference) {
            presenter.refresh();
        } else {
            presenter.add(reference);
        }

        disposeView();
    }

    public void display(ControlMeasure measure, Reference reference) {

        String name = measure.getName();
        if (name == null) {
            name = "New Control Measure";
        }

        this.setLabel(this.getTitle() + " " + (counter++) + " for " + name);

        JPanel layout = createLayout();
        this.getContentPane().add(layout);
        this.display();
        this.reference = reference;

        populateFields();
        resetChanges();

    }

    // use this method when adding a new property
    public void display(ControlMeasure measure) {

        display(measure, new Reference());
        this.newReference = true;
    }

    private void populateFields() {

        String description = this.reference.getDescription();
        if (description == null) {
            description = "";
        }
        this.descriptionField.setText(description);
    }

    public void observe(MeasureReferencePresenter presenter) {
        this.presenter = presenter;

    }

    private JPanel createLayout() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(this.inputPanel());

        // panel.add(detailPanel());
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel() {

        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        this.descriptionField = new TextArea("", "", 25, 6);
        this.changeablesList.addChangeable(this.descriptionField);
        
        JScrollPane scrollPane = new JScrollPane(this.descriptionField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        layoutGenerator.addLabelWidgetPair("Description", scrollPane, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 15, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        saveButton = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        getRootPane().setDefaultButton(saveButton);
        panel.add(saveButton);

        cancelButton = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                closeWindow();
            }
        });
        panel.add(cancelButton);

        return panel;
    }

    protected void doSave() {
        this.reference.setDescription(this.descriptionField.getText().trim());
    }

    private void closeWindow() {
        if (shouldDiscardChanges())
            disposeView();
    }

    public void viewOnly() {

        saveButton.setVisible(false);
        cancelButton.setText("Close");
    }
}