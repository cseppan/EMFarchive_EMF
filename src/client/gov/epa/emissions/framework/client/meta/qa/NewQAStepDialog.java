package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.Dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class NewQAStepDialog extends Dialog implements NewQAStepView {

    private JLabel required;

    private boolean shouldCreate;

    private JComboBox versionsBox;

    private JList optionalList;

    private Version[] versions;

    private QAStepTemplate[] requiredTemplates;

    private QAStepTemplate[] optionalTemplates;

    private QAStepTemplate[] selectedOptionalTemplates;

    private HashMap optionalTemplatesMap;

    private EmfDataset dataset;

    public NewQAStepDialog(EmfConsole parent, Version[] versions) {
        super("New QA Step", parent);
        super.setSize(new Dimension(550, 350));
        super.center();

        optionalTemplatesMap = new HashMap();
        this.versions = versions;
    }

    public void display(EmfDataset dataset, DatasetType type) {
        super.setTitle(super.getTitle() + ": " + type.getName());

        this.dataset = dataset;
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

        setQAStepTemplates(type);
        setOptionalList();

        versionsBox = new JComboBox(getVersions());
        layoutGenerator.addLabelWidgetPair("Version", versionsBox, panel);

        required = new JLabel(getRequiredSteps());
        JScrollPane requiredPane = createScrollPane(required);
        layoutGenerator.addLabelWidgetPair("Required", requiredPane, panel);

        JScrollPane optionPane = createScrollPane(optionalList);
        layoutGenerator.addLabelWidgetPair("Optional", optionPane, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private void setQAStepTemplates(DatasetType type) {
        List requiredList = new ArrayList();
        List optionalList = new ArrayList();
        QAStepTemplate[] templates = type.getQaStepTemplates();

        for (int i = 0; i < templates.length; i++) {
            if (templates[i].isRequired()) {
                requiredList.add(templates[i]);
            } else
                optionalList.add(templates[i]);
        }

        requiredTemplates = (QAStepTemplate[]) requiredList.toArray(new QAStepTemplate[0]);
        optionalTemplates = (QAStepTemplate[]) optionalList.toArray(new QAStepTemplate[0]);
    }

    private void setOptionalList() {
        optionalList = new JList(getOptionalTemplateNames());
        optionalList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        optionalList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                getSelectedItems();
            }
        });
    }

    private JScrollPane createScrollPane(Component component) {
        JScrollPane optionPane = new JScrollPane(optionalList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        optionPane.getViewport().add(component, null);
        optionPane.setPreferredSize(new Dimension(300, 100));

        return optionPane;
    }

    protected void getSelectedItems() {
        Object[] selected = optionalList.getSelectedValues();
        selectedOptionalTemplates = new QAStepTemplate[selected.length];

        for (int i = 0; i < selected.length; i++)
            selectedOptionalTemplates[i] = (QAStepTemplate) optionalTemplatesMap.get(selected[i]);
    }

    private String getRequiredSteps() {
        String requiredSteps = "<html>";
        for (int i = 0; i < requiredTemplates.length; i++)
            requiredSteps += requiredTemplates[i].getName() + "<br>";

        return requiredSteps + "</html>";
    }

    private String[] getOptionalTemplateNames() {
        String[] names = new String[optionalTemplates.length];

        for (int i = 0; i < names.length; i++) {
            names[i] = optionalTemplates[i].getName();
            optionalTemplatesMap.put(names[i], optionalTemplates[i]);
        }

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
        addRequired(qasteps, getVersionNumber());
        addOptional(qasteps, getVersionNumber());

        return (QAStep[]) qasteps.toArray(new QAStep[0]);
    }

    private void addOptional(List qasteps, int versionNumber) {
        if (selectedOptionalTemplates != null)
            for (int j = 0; j < selectedOptionalTemplates.length; j++) {
                QAStep step = new QAStep(selectedOptionalTemplates[j], versionNumber);
                step.setDatasetId(dataset.getId());
                qasteps.add(step);
            }
    }

    private void addRequired(List qasteps, int versionNumber) {
        for (int i = 0; i < requiredTemplates.length; i++) {
            QAStep step = new QAStep(requiredTemplates[i], versionNumber);
            step.setDatasetId(dataset.getId());
            qasteps.add(step);
        }
    }

    private String[] getVersions() {
        List versionsList = new ArrayList();
        for (int i = 0; i < versions.length; i++)
            versionsList.add(versions[i].getVersion() + " - " + versions[i].getName());

        return (String[]) versionsList.toArray(new String[0]);
    }

    private int getVersionNumber() {
        String version = (String) versionsBox.getSelectedItem();
        return Integer.parseInt(version.substring(0, version.indexOf(" - ")));
    }

}
