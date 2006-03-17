package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.Dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

public class NewQAStepDialog extends Dialog implements NewQAStepView {

    private JLabel required;

    private boolean shouldCreate;
    
    private ComboBox versionsBox;
    
    private JList optionalList;

    private Version[] versions;
    
    private QAStepTemplate[] requiredTemplates;
    
    private QAStepTemplate[] optionalTemplates;

    public NewQAStepDialog(EmfConsole parent, Version[] versions) {
        super("New QA Step", parent);
        super.setSize(new Dimension(550, 300));
        super.center();
        
        this.versions = versions;
    }

    public void display(DatasetType type) {
        super.setTitle(super.getTitle() + ": " + type.getName());

        JPanel layout = createLayout(type);
        super.getContentPane().add(layout);
        super.display();
    }

    private JPanel createLayout(DatasetType type) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(inputPanel(type));
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel(DatasetType type) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        versionsBox = new ComboBox("version", versions);
        versionsBox.setSize(new Dimension(50, 50));
        layoutGenerator.addLabelWidgetPair("Version", versionsBox, panel);
        
        required = new JLabel(getRequiredSteps(type));
        layoutGenerator.addLabelWidgetPair("Required: ", required, panel);
        
        JScrollPane optionPane = createOptionPane();
        layoutGenerator.addLabelWidgetPair("Optional", optionPane, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JScrollPane createOptionPane() {
        setOptionalList();
        JScrollPane optionPane = new JScrollPane(optionalList);
        optionPane.add(optionalList);
        
        return optionPane;
    }

    private void setOptionalList() {
        optionalList = new JList(getOptionalTemplateNames());
        optionalList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        optionalList.setSize(new Dimension(30, 30));
        optionalList.setSelectedIndex(0);
    }

    private String getRequiredSteps(DatasetType type) {
        String requiredSteps = "";
        List requiredList = new ArrayList();
        List optionalList = new ArrayList();
        QAStepTemplate[] templates = type.getQaStepTemplates();
        
        for(int i = 0; i < templates.length; i++) {
            if(templates[i].isRequired()) {
                requiredSteps += templates[i].getName() + "; ";
                requiredList.add(templates[i]);
            } else
                optionalList.add(templates[i]);
        }
        
        requiredTemplates = (QAStepTemplate[])requiredList.toArray(new QAStepTemplate[0]);
        optionalTemplates = (QAStepTemplate[])optionalList.toArray(new QAStepTemplate[0]);

        return requiredSteps;
    }
    
    private String[] getOptionalTemplateNames() {
        String[] names = new String[optionalTemplates.length];
        
        for(int i = 0; i < names.length; i++)
            names[i] = optionalTemplates[i].getName();
        
        return names;
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
        List qasteps = new ArrayList();
        
        for(int i = 0; i < requiredTemplates.length; i++) {
            QAStep qastep = new QAStep(requiredTemplates[i], 
                    ((Version)versionsBox.getSelectedItem()).getVersion());
            qasteps.add(qastep);
        }
        
        return (QAStep[])qasteps.toArray(new QAStep[0]);
    }

}
