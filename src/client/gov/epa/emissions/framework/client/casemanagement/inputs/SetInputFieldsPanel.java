package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EmptyStrings;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class SetInputFieldsPanel extends JPanel implements InputFieldsPanelView {

    private ComboBox dataset;

    private ComboBox version;

    private MessagePanel messagePanel;

    private ManageChangeables changeablesList;

    private InputFieldsPanelPresenter presenter;

    private CaseInput input;

    public SetInputFieldsPanel(MessagePanel messagePanel, ManageChangeables changeablesList) {
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
    }

    public void display(CaseInput input, JComponent container) throws EmfException {
        this.input = input;
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        String width = EmptyStrings.create(125);
        Dimension preferredSize = new Dimension(380, 25);
        JLabel inputName = new JLabel(input.getInputName().toString());
        layoutGenerator.addLabelWidgetPair("Input Name:", inputName, panel);

//        JLabel program = new JLabel(input.getProgram().toString());
//        layoutGenerator.addLabelWidgetPair("Program:", program, panel);

        JLabel envtVar = new JLabel(input.getEnvtVars()==null? "":input.getEnvtVars().toString());
        layoutGenerator.addLabelWidgetPair("Envt. Variable:", envtVar, panel);

        JLabel sector = new JLabel(input.getSector()==null? "All jobs for sector" :input.getSector().toString());
        layoutGenerator.addLabelWidgetPair("Sector:", sector, panel);
        
        JLabel jobName = new JLabel(presenter.getJobName(input.getCaseJobID()));
        layoutGenerator.addLabelWidgetPair("Job:", jobName, panel);
        
        JLabel dsType = new JLabel(input.getDatasetType()==null? "":input.getDatasetType().toString());
        layoutGenerator.addLabelWidgetPair("Dataset Type:", dsType, panel);
        

        dataset = new ComboBox(new EmfDataset[0]);
        DatasetType type = input.getDatasetType();
        EmfDataset ds = input.getDataset();
        fillDatasets(type);

        if (type != null && ds != null)
            dataset.setSelectedItem(input.getDataset());

        dataset.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                fillVersions((EmfDataset) dataset.getSelectedItem());
            }
        });
        changeablesList.addChangeable(dataset);
        dataset.setPreferredSize(preferredSize);
        dataset.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Dataset:", dataset, panel);

        version = new ComboBox(new Version[] { input.getVersion() });
        fillVersions(input.getDataset());
        
        if (input.getVersion() != null)
            version.setSelectedItem(input.getVersion());
        
        changeablesList.addChangeable(version);
        version.setPreferredSize(preferredSize);
        version.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Version:", version, panel);
        

        JLabel required = new JLabel(input.isRequired()? "True" : "False" );
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 8, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        container.add(panel);
    }


    private void fillDatasets(DatasetType type) {
        try {
            List list = new ArrayList();
            EmfDataset blank = new EmfDataset();
            blank.setName("Not selected");
            list.add(blank);
            list.addAll(Arrays.asList(presenter.getDatasets(type)));
            EmfDataset[] datasets = (EmfDataset[]) list.toArray(new EmfDataset[0]);

            dataset.removeAllItems();
            dataset.setModel(new DefaultComboBoxModel(datasets));
            dataset.revalidate();
            dataset.setEnabled(true);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void fillVersions(EmfDataset dataset) {
        version.setEnabled(true);
        try {
            Version[] versions = presenter.getVersions(dataset);
            version.removeAllItems();
            version.setModel(new DefaultComboBoxModel(versions));
            version.revalidate();
            if (versions.length > 0)
                version.setSelectedIndex(getDefaultVersionIndex(versions, dataset));
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private int getDefaultVersionIndex(Version[] versions, EmfDataset dataset) {
        int defaultversion = dataset.getDefaultVersion();

        for (int i = 0; i < versions.length; i++)
            if (defaultversion == versions[i].getVersion())
                return i;

        return 0;
    }

    public CaseInput setFields() throws EmfException {
        updateDataset();
        updateVersion();
        input.setLastModifiedDate(new Date());
        return input;
    }


    private void updateDataset() {
        EmfDataset selected = (EmfDataset) dataset.getSelectedItem();
        if (selected != null && selected.getName().equalsIgnoreCase("Not selected")) {
            input.setDataset(null);
            return;
        }

        input.setDataset(selected);
    }

    private void updateVersion() throws EmfException {
        EmfDataset ds = (EmfDataset) dataset.getSelectedItem();
        Version ver = (Version) version.getSelectedItem();

        if (ds == null || ds.getName().equalsIgnoreCase("Not selected")) {
            input.setVersion(null);
            return;
        }

        String type = ds.getDatasetType().getName();
        if (ds.getName() != null && ver == null && type.indexOf("External") < 0)
            throw new EmfException("Please select a dataset version.");

        input.setVersion(ver);
    }

    public void observe(InputFieldsPanelPresenter presenter) {
        this.presenter = presenter;
    }

    public CaseInput getInput() {
        return this.input;
    }

    public void validateFields() {
        // NOTE Auto-generated method stub
        
    }

//    public void validateFields() {
//        setFields();
//    }
    
}
