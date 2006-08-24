package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.InputEnvtVar;
import gov.epa.emissions.framework.services.casemanagement.InputName;
import gov.epa.emissions.framework.services.casemanagement.Program;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class InputFieldsPanel extends JPanel implements InputFieldsPanelView {

    private EditableComboBox inputName;

    private EditableComboBox program;

    private ComboBox sector;

    private EditableComboBox envtVar;

    private ComboBox dataset;

    private ComboBox version;

    private ComboBox dsType;

    private JLabel qaStatus;

    private TextField subDir;

    private CheckBox required;

    private CheckBox show;

    private MessagePanel messagePanel;

    private ManageChangeables changeablesList;

    private InputFieldsPanelPresenter presenter;

    private CaseInput input;

    public InputFieldsPanel(MessagePanel messagePanel, ManageChangeables changeablesList) {
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
    }

    public void display(CaseInput input, JComponent container) throws EmfException {
        this.input = input;
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        String width = "To make the combobox a bit wider ...............................";

        InputName[] inputNames = presenter.getCaseInputNames().getAll();
        inputName = new EditableComboBox(inputNames);
        changeablesList.addChangeable(inputName);
        inputName.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Input Name:", inputName, panel);

        Program[] programs = presenter.getCasePrograms().getAll();
        program = new EditableComboBox(programs);
        changeablesList.addChangeable(program);
        program.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Program:", program, panel);

        InputEnvtVar[] envtVars = presenter.getCaseInputEnvtVars().getAll();
        envtVar = new EditableComboBox(envtVars);
        changeablesList.addChangeable(envtVar);
        envtVar.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Envt. Variable:", envtVar, panel);

        sector = new ComboBox(presenter.getSectors());
        changeablesList.addChangeable(sector);
        sector.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Sector:", sector, panel);

        dsType = new ComboBox(presenter.getDSTypes());
        dsType.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                fillDatasets((DatasetType) dsType.getSelectedItem());
            }
        });
        changeablesList.addChangeable(dsType);
        dsType.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Dataset Type:", dsType, panel);

        dataset = new ComboBox();
        dataset.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                fillVersions((EmfDataset) dataset.getSelectedItem());
            }
        });
        dataset.setEnabled(false);
        changeablesList.addChangeable(dataset);
        dataset.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Dataset:", dataset, panel);

        version = new ComboBox();
        version.setEnabled(false);
        changeablesList.addChangeable(version);
        version.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Version:", version, panel);

        qaStatus = new JLabel("");
        layoutGenerator.addLabelWidgetPair("QA Status:", qaStatus, panel);

        subDir = new TextField("subdir", 30);
        changeablesList.addChangeable(subDir);
        layoutGenerator.addLabelWidgetPair("Subdirectory:", subDir, panel);

        required = new CheckBox("");
        changeablesList.addChangeable(required);
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        show = new CheckBox("");
        changeablesList.addChangeable(show);
        layoutGenerator.addLabelWidgetPair("Show?", show, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 11, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        populateFields(input);
        container.add(panel);
    }

    private void populateFields(CaseInput input) throws EmfException {
        inputName.setSelectedItem(input.getInputName());
        program.setSelectedItem(input.getProgram());

        Sector sct = input.getSector();
        sector.setSelectedItem(input.getSector());
        if (sct == null)
            sector.setSelectedItem(presenter.getSectors()[0]);

        envtVar.setSelectedItem(input.getEnvtVars());
        dsType.setSelectedItem(input.getDatasetType());
        dataset.setSelectedItem(input.getDataset());
        version.setSelectedItem(input.getVersion());
        qaStatus.setText("");
        subDir.setText(input.getSubdir());
        required.setSelected(input.isRequired());
        show.setSelected(input.isShow());
    }

    private void fillDatasets(DatasetType type) {
        try {
            List list = new ArrayList();
            list.add(new EmfDataset());
            list.addAll(Arrays.asList(presenter.getDatasets(type)));
            EmfDataset[] datasets = (EmfDataset[])list.toArray(new EmfDataset[0]);
            
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
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    public void setFields() throws EmfException {
        updateInputName();
        updateProgram();
        updateEnvtVar();
        updateSector();
        input.setDatasetType((DatasetType) dsType.getSelectedItem());
        updateDataset();
        updateVersion();
        input.setSubdir(subDir.getText());
        input.setRequired(required.isSelected());
        input.setShow(show.isSelected());
    }

    private void updateInputName() throws EmfException {
        Object selected = inputName.getSelectedItem();
        input.setInputName(presenter.getInputName(selected));
    }

    private void updateProgram() throws EmfException {
        Object selected = program.getSelectedItem();
        if (selected == null) {
            input.setProgram(null);
            return;
        }
        
        input.setProgram(presenter.getCaseProgram(selected));
    }

    private void updateEnvtVar() throws EmfException {
        Object selected = envtVar.getSelectedItem();
        if (selected == null) {
            input.setEnvtVars(null);
            return;
        }
        
        input.setEnvtVars(presenter.getInputEnvtVar(selected));
    }

    private void updateSector() {
        Sector selected = (Sector) sector.getSelectedItem();

        if (selected.getName().equalsIgnoreCase("All sectors")) {
            input.setSector(null);
            return;
        }

        input.setSector(selected);
    }

    private void updateDataset() {
        EmfDataset selected = (EmfDataset) dataset.getSelectedItem();
        if (selected != null && selected.getName() != null)
            input.setDataset(selected);
    }

    private void updateVersion() throws EmfException {
        EmfDataset ds = (EmfDataset) dataset.getSelectedItem();
        Version ver = (Version) version.getSelectedItem();
        
        if (ds == null || ds.getName() == null)
            return;
        
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

    public void validateFields() throws EmfException {
        presenter.doValidateFields();
    }

}
