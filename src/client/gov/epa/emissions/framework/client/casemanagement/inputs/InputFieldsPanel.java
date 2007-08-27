package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
//import gov.epa.emissions.framework.services.casemanagement.CaseProgram;
//import gov.epa.emissions.framework.services.casemanagement.InputEnvtVar;
//import gov.epa.emissions.framework.services.casemanagement.InputName;
//import gov.epa.emissions.framework.services.casemanagement.SubDir;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class InputFieldsPanel extends JPanel implements InputFieldsPanelView {

    private EditableComboBox inputName;

    private EditableComboBox program;

    private ComboBox sector;

    private EditableComboBox envtVar;

    private ComboBox dataset;

    private ComboBox version;

    private ComboBox dsType;

    private JLabel qaStatus;

    private EditableComboBox subDir;

    private CheckBox required;

    private CheckBox show;

    private MessagePanel messagePanel;

    private ManageChangeables changeablesList;

    private InputFieldsPanelPresenter presenter;

    private CaseInput input;

    private ComboBox jobs;

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
        inputName.setSelectedItem(input.getInputName());
        addPopupMenuListener(inputName, "inputnames");
        changeablesList.addChangeable(inputName);
        inputName.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Input Name:", inputName, panel);

        program = new EditableComboBox(presenter.getPrograms());
        program.setSelectedItem(input.getProgram());
        addPopupMenuListener(program, "programs");
        changeablesList.addChangeable(program);
        program.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Program:", program, panel);

        envtVar = new EditableComboBox(presenter.getEnvtVars());
        envtVar.setSelectedItem(input.getEnvtVars());
        addPopupMenuListener(envtVar, "envtvars");
        changeablesList.addChangeable(envtVar);
        envtVar.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Envt. Variable:", envtVar, panel);

        sector = new ComboBox(presenter.getSectors());
        if (input.getSector() == null)
        {
            sector.setSelectedIndex(0); // set to all sectors
        }
        else
        {    
           sector.setSelectedItem(input.getSector());
        }   
        addPopupMenuListener(sector, "sectors");
        changeablesList.addChangeable(sector);
        sector.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Sector:", sector, panel);

        dsType = new ComboBox(presenter.getDSTypes());
        addPopupMenuListener(dsType, "dstypes");
        dsType.setSelectedItem(input.getDatasetType());
        dsType.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                fillDatasets((DatasetType) dsType.getSelectedItem());
            }
        });
        changeablesList.addChangeable(dsType);
        dsType.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Dataset Type:", dsType, panel);

        dataset = new ComboBox(new EmfDataset[]{input.getDataset()});
//        dataset.setEnabled(false);
        dataset.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                fillVersions((EmfDataset) dataset.getSelectedItem());
            }
        });
        changeablesList.addChangeable(dataset);
        dataset.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Dataset:", dataset, panel);

        version = new ComboBox(new Version[]{input.getVersion()});
//        version.setEnabled(false);
        changeablesList.addChangeable(version);
        version.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Version:", version, panel);

        setJob();
        changeablesList.addChangeable(jobs);
        jobs.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Job:", jobs, panel);

        qaStatus = new JLabel("");
        layoutGenerator.addLabelWidgetPair("QA Status:", qaStatus, panel);

        // subDir = new TextField("subdir", 30);
        // changeablesList.addChangeable(subDir);
        // layoutGenerator.addLabelWidgetPair("Subdirectory:", subDir, panel);

        subDir = new EditableComboBox(presenter.getSubdirs());
        subDir.setSelectedItem(input.getSubdirObj());
        addPopupMenuListener(subDir, "subdirs");
        changeablesList.addChangeable(subDir);
        subDir.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Subdirectory:", subDir, panel);

        required = new CheckBox("");
        changeablesList.addChangeable(required);
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        show = new CheckBox("");
        changeablesList.addChangeable(show);
        layoutGenerator.addLabelWidgetPair("Show?", show, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 12, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        container.add(panel);
    }

    private void addPopupMenuListener(final JComboBox box, final String toget) {
        box.addPopupMenuListener(new PopupMenuListener(){
            public void popupMenuCanceled(PopupMenuEvent event) {
                // NOTE Auto-generated method stub
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
                // NOTE Auto-generated method stub
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
                try {
                    System.out.println("Refreshing menu for "+toget);
                    box.setModel(new DefaultComboBoxModel(getAllObjects(toget)));
                    box.revalidate();
                    refresh();
                } catch (Exception e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
    }

    protected Object[] getAllObjects(String toget) throws EmfException {
        System.out.println("InputFieldsPanel: getAllObjects for "+toget);
        if (toget.equals("dstypes"))
            return presenter.getDSTypes();

        else if (toget.equals("inputnames"))
            return presenter.getInputNames();
        
        else if (toget.equals("programs"))
            return presenter.getPrograms();
        
        else if (toget.equals("envtvars"))
            return presenter.getEnvtVars();
        
        else if (toget.equals("sectors"))
            return presenter.getSectors();
        
        else if (toget.equals("subdirs"))
            return presenter.getSubdirs();
        
        else
            throw new EmfException("Unknown object type: "+toget);
        
    }
    
    private void setJob() throws EmfException {
        CaseJob [] jobArray = presenter.getCaseJobs();
        jobs = new ComboBox(jobArray);
        jobs.setSelectedIndex(presenter.getJobIndex(input.getCaseJobID(),jobArray));
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
        } 
        catch (EmfException e) {
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
        updateInputName();
        updateProgram();
        updateEnvtVar();
        updateSector();
        input.setDatasetType((DatasetType) dsType.getSelectedItem());
        updateDataset();
        updateVersion();
        updateSubdir();
        input.setRequired(required.isSelected());
        input.setShow(show.isSelected());
        updateJob();
        return input;
    }

    private void updateJob() {
        Object job = jobs.getSelectedItem();

        if (job == null)
            return;
        
        if (((CaseJob) job).getName().equalsIgnoreCase(
                InputFieldsPanelPresenter.ALL_FOR_SECTOR)) {
            input.setCaseJobID(0);
            return;
        }

        input.setCaseJobID(((CaseJob) job).getId());
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

    private void updateSubdir() throws EmfException {
        Object selected = subDir.getSelectedItem();
        if (selected == null) {
            input.setSubdirObj(null);
            return;
        }

        input.setSubdirObj(presenter.getSubDir(selected));
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

    public void validateFields() throws EmfException {
        Object selectedProg = program.getSelectedItem();
        if (inputName.getSelectedItem() == null)
            throw new EmfException("Please specify an input name.");

        if (selectedProg == null || selectedProg.toString().trim().equals(""))
            throw new EmfException("Please specify a program.");

        setFields();
    }
    
    private void refresh() {
        super.revalidate();
    }

}
