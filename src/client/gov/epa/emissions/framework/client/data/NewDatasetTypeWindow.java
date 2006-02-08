package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class NewDatasetTypeWindow extends DisposableInteralFrame implements NewDatasetTypeView {
    private NewDatasetTypePresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    private TextField name;

    private TextField minFiles;

    private TextField maxFiles;

    private JComboBox derivedFrom;
    
    private static int counter = 0;

    // private DatasetTypesManagerView manager;

    private static final String[] types = { "External File", "CSV File", "Line-based File", "SMOKE Report File" };

    public NewDatasetTypeWindow(DesktopManager desktopManager) {
        super("Create New Dataset Type", new Dimension(600, 260), desktopManager);
        // this.manager = manager;
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    private void doLayout(JPanel layout) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(createInputPanel());
        layout.add(createButtonsPanel());
    }

    public void observe(NewDatasetTypePresenter presenter) {
        this.presenter = presenter;
    }

    public void display() {
        counter++;
        String name = "Create New Dataset Type"+counter;
        super.setTitle(name);
        layout.removeAll();
        doLayout(layout);

        super.display();
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("name", 40);
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        minFiles = new TextField("minfiles", "1", 20);
        layoutGenerator.addLabelWidgetPair("Min Files:", minFiles, panel);

        maxFiles = new TextField("maxfiles", "-1", 20);
        layoutGenerator.addLabelWidgetPair("Max Files:", maxFiles, panel);

        derivedFrom = new JComboBox(types);
        layoutGenerator.addLabelWidgetPair("Derived From:", derivedFrom, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 0, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private boolean isDigit(String text) {
        if (!Character.isDigit(text.charAt(0)) && text.charAt(0) != '-')
            return false;

        for (int n = 1; n < text.length(); n++) {
            if (!Character.isDigit(text.charAt(n))) {
                return false;
            }
        }

        return true;
    }

    private boolean checkTextFields() {
        if (name.getText().equals(""))
            messagePanel.setError("Name field should be a non-empty string.");
        else if (!isDigit(minFiles.getText()))
            messagePanel.setError("Min Files field should only contain a number.");
        else if (!isDigit(maxFiles.getText()))
            messagePanel.setError("Max Files field should only contain a number.");
        else {
            messagePanel.clear();
            return true;
        }

        return false;
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (checkTextFields()) {
                    try {
                        presenter.doSave(name.getText(), minFiles.getText(), maxFiles.getText(), (String) derivedFrom
                                .getSelectedItem());
                    } catch (EmfException e) {
                        messagePanel.setError(e.getMessage());
                    }
                }
            }
        };

        return action;
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        };

        return action;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        Button saveButton = new Button("Save", saveAction());
        container.add(saveButton);
        container.add(new Button("Close", closeAction()));
        getRootPane().setDefaultButton(saveButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

}
