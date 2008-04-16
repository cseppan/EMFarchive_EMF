package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditor;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Abbreviation;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;

public class SensitivityWindow extends DisposableInteralFrame implements SensitivityView {
    private SensitivityPresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    private JRadioButton newSenCase;

    private JRadioButton existSenCase;

    private TextField senName;

    private TextField senAbrev;

    private EmfConsole parentConsole;
    
    private SensitivityWindow sensitivityWindow;

    private Case parentCase;

    public SensitivityWindow(DesktopManager desktopManager, EmfConsole parentConsole) {
        super("Sensitivity", new Dimension(480, 280), desktopManager);

        this.parentConsole = parentConsole;
        this.sensitivityWindow = this;
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    private void doLayout(JPanel layout, Case parentCase) {
        this.parentCase = parentCase;
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(createInputPanel(parentCase));
        layout.add(createButtonsPanel());
    }

    public void observe(SensitivityPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Case case1) {
        super.setLabel("Sensitivity");
        layout.removeAll();
        doLayout(layout, case1);

        super.display();
    }

    private JPanel createInputPanel(Case case1) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        Label caseName = new Label("Case Name", case1.getName());
        layoutGenerator.addLabelWidgetPair("Sensitivity for Case:", caseName, panel);

        layoutGenerator.addLabelWidgetPair("Sensitivity for Case:", newOrExistRadios(), panel);

        String[] types = { "Adjust AQM-ready Emissions" };
        ComboBox senType = new ComboBox("Select One", types);
        layoutGenerator.addLabelWidgetPair("Sensitivity Type:", senType, panel);

        senName = new TextField("Sensitivity Name", 25);
        addChangeable(senName);
        layoutGenerator.addLabelWidgetPair("Sensitivity Name:", senName, panel);

        senAbrev = new TextField("Abbreviation", 25);
        addChangeable(senAbrev);
        layoutGenerator.addLabelWidgetPair("Sensitivity Abbreviation:", senAbrev, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 0, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel newOrExistRadios() {
        newSenCase = new JRadioButton("New");
        // newSenCase.addActionListener(radioButtonAction());
        newSenCase.setSelected(true);
        existSenCase = new JRadioButton("Add to existing");
        // existSenCase.addActionListener(radioButtonAction());
        existSenCase.setEnabled(false);
        // Create logical relationship between JradioButtons
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(newSenCase);
        buttonGroup.add(existSenCase);

        JPanel radioPanel = new JPanel();
        radioPanel.add(newSenCase);
        radioPanel.add(existSenCase);
        return radioPanel;
    }

    private Action saveAction() {
        Action action = new AbstractAction() {

            public void actionPerformed(ActionEvent event) {
                resetChanges();

                try {
                    Case sensitivityCase = presenter.copyCase(parentCase.getId());
                    sensitivityCase.setName(senName.getText());
                    sensitivityCase.setAbbreviation(new Abbreviation(senAbrev.getText()));
                    Case updated = presenter.updateCase(sensitivityCase);

                    CaseEditor view = new CaseEditor(parentConsole, presenter.getSession(), desktopManager);
                    presenter.editCase(view, updated);
                    sensitivityWindow.disposeView();
                } catch (EmfException e) {
                    e.printStackTrace();
                    messagePanel.setError(e.getMessage());
                }
            }
        };

        return action;
    }

    public void windowClosing() {
        doClose();
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        };

        return action;
    }

    private void doClose() {
        if (shouldDiscardChanges())
            presenter.doClose();
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(10);
        container.setLayout(layout);

        Button saveButton = new OKButton(saveAction());
        container.add(saveButton);
        container.add(new CancelButton(closeAction()));
        getRootPane().setDefaultButton(saveButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

}
