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

        inputName = new EditableComboBox(presenter.getInputNames());
        changeablesList.addChangeable(inputName);
        inputName.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Input Name:", inputName, panel);

        program = new EditableComboBox(presenter.getPrograms());
        changeablesList.addChangeable(program);
        program.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Program:", program, panel);

        envtVar = new EditableComboBox(presenter.getEnvtVars());
        changeablesList.addChangeable(envtVar);
        envtVar.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Envt. Variable:", envtVar, panel);
        
        sector = new ComboBox(presenter.getSectors());
        changeablesList.addChangeable(sector);
        sector.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Sector:", sector, panel);

        dsType = new ComboBox(presenter.getDSTypes());
        dsType.addActionListener( new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                fillDatasets((DatasetType)dsType.getSelectedItem());
            }
        });
        changeablesList.addChangeable(dsType);
        dsType.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Dataset Type:", dsType, panel);

        DatasetType type = (DatasetType) dsType.getSelectedItem();
        dataset = new ComboBox(presenter.getDatasets(type));
        dataset.addActionListener( new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                fillVersions((EmfDataset)dataset.getSelectedItem());
            }
        });
        dataset.setEnabled(false);
        changeablesList.addChangeable(dataset);
        dataset.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Dataset:", dataset, panel);

        EmfDataset selected = (EmfDataset) dataset.getSelectedItem();
        version = new ComboBox(presenter.getVersions(selected));
        version.setEnabled(false);
        changeablesList.addChangeable(version);
        version.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Version:", version, panel);

        qaStatus = new JLabel("");
        layoutGenerator.addLabelWidgetPair("QA Status:", qaStatus, panel);

        subDir = new TextField("subdir", 35);
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
        dataset.setSelectedItem(input.getDataset());
        version.setSelectedItem(input.getVersion());
        dsType.setSelectedItem(input.getDatasetType());
        qaStatus.setText("");
        subDir.setText(input.getSubdir());
        required.setSelected(input.isRequired());
        show.setSelected(input.isShow());
    }

    private void fillDatasets(DatasetType type) {
        dataset.setEnabled(true);
        try {
            EmfDataset[] datasets = presenter.getDatasets(type);
            dataset.removeAllItems();
            dataset.setModel(new DefaultComboBoxModel(datasets));
            dataset.revalidate();
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
        input.setName(inputName.getSelectedItem().toString());
        updateInputName();
        updateProgram();
        updateEnvtVar();
        updateSector();
        input.setDatasetType((DatasetType)dsType.getSelectedItem());
        input.setDataset((EmfDataset)dataset.getSelectedItem());
        updateVersion();
        input.setSubdir(subDir.getText());
        input.setRequired(required.isSelected());
        input.setShow(show.isSelected());
    }
    
    private void updateInputName() {
        Object selected = inputName.getSelectedItem();
        if (selected instanceof String) {
            String newInputName = (String) selected;
            if (newInputName.length() > 0) {
                InputName name = new InputName(newInputName);
                input.setInputName(name);
            }
        } else if (selected instanceof InputName) {
            input.setInputName((InputName) selected);
        }
    }
    
    private void updateProgram() {
        Object selected = program.getSelectedItem();
        if (selected instanceof String) {
            String newProgramName = (String) selected;
            if (newProgramName.length() > 0) {
                Program name = new Program(newProgramName);
                input.setProgram(name);
            }
        } else if (selected instanceof Program) {
            input.setProgram((Program) selected);
        }
    }
    
    private void updateEnvtVar() {
        Object selected = envtVar.getSelectedItem();
        if (selected instanceof String) {
            String newEnvtVar = (String) selected;
            if (newEnvtVar.length() > 0) {
                InputEnvtVar name = new InputEnvtVar(newEnvtVar);
                input.setEnvtVars(name);
            }
        } else if (selected instanceof InputName) {
            input.setEnvtVars((InputEnvtVar) selected);
        }
    }
    
    private void updateSector() {
        Sector selected = (Sector)sector.getSelectedItem();
        
        if (selected.getName().equalsIgnoreCase("All sectors")) {
            input.setSector(null);
            return;
        }
        
        input.setSector(selected);
    }
    
    private void updateVersion() throws EmfException {
        EmfDataset ds = (EmfDataset)dataset.getSelectedItem();
        Version ver = (Version)version.getSelectedItem();
        
        if (ds != null && ver == null)
            throw new EmfException("Please select a dataset version.");
        
        input.setVersion(ver);
    }
    
    public void observe(InputFieldsPanelPresenter presenter) {
        this.presenter = presenter;
    }

    public void populateFields() {
        try {
            this.presenter.getInputNames();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    public CaseInput getInput() {
        return this.input;
    }
}
