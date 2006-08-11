package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.ui.Dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class NewInputDialog extends Dialog implements NewInputView {

    private TextField name;

    protected boolean shouldCreate;

    public NewInputDialog(EmfConsole parent) {
        super("Create new Input", parent);
        super.setSize(new Dimension(550, 120));
        super.center();
    }

    public void display(Case caseObj) {
        doDisplay(caseObj);
    }

    private void doDisplay(Case caseObj) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        panel.add(inputPanel());
        panel.add(buttonsPanel(caseObj.getCaseInputs()));

        super.getContentPane().add(panel);
        super.display();
    }

    private JPanel inputPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("", 40);
        layoutGenerator.addLabelWidgetPair("InputName:", name, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel buttonsPanel(final CaseInput[] inputs) {
        JPanel panel = new JPanel();
        Button ok = new Button("OK", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doNew(inputs);
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

    private void doNew(CaseInput[] inputs) {
        if (verifyInput(inputs)) {
            shouldCreate = true;
            close();
        }
    }

    protected boolean verifyInput(CaseInput[] input) {
        String inputName = name.getText().trim();
        if (inputName.length() == 0) {
            JOptionPane.showMessageDialog(super.getParent(), "Please enter Name", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (duplicate(inputName, input)) {
            JOptionPane.showMessageDialog(super.getParent(), "Name is duplicate. Please enter a different name.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private boolean duplicate(String inputName, CaseInput[] inputs) {
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i].getName().equals(inputName))
                return true;
        }

        return false;
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public CaseInput input() {
        CaseInput input = new CaseInput();
        input.setName(name.getText());

        return input;
    }

}
