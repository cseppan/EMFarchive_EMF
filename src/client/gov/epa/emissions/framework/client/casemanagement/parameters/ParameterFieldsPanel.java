package gov.epa.emissions.framework.client.casemanagement.parameters;

import java.awt.Dimension;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.EmptyStrings;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseProgram;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.ui.MessagePanel;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ParameterFieldsPanel extends JPanel implements ParameterFieldsPanelView {

    private EditableComboBox parameterName;

    private EditableComboBox program;

    private ComboBox sector;

    private EditableComboBox envtVar;

    private CheckBox required;

    private CheckBox show;

    private ManageChangeables changeablesList;

    private ParameterFieldsPanelPresenter presenter;

    private CaseParameter parameter;

    private ComboBox jobs;

    private ComboBox varTypes;

    private TextArea notes;

    private TextField envValue;

    private TextArea purpose;

    private TextField order;

    public ParameterFieldsPanel(MessagePanel messagePanel, ManageChangeables changeablesList) {
        this.changeablesList = changeablesList;
    }

    public void display(CaseParameter param, JComponent container) throws EmfException {
        this.parameter = param;
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        String width = EmptyStrings.create(65);
        Dimension preferredSize = new Dimension(20, 30);

        ParameterName[] parameterNames = presenter.getCaseParameterNames().getAll();
        parameterName = new EditableComboBox(parameterNames);
        changeablesList.addChangeable(parameterName);
        parameterName.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Parameter Name:", parameterName, panel);

        CaseProgram[] programs = presenter.getCasePrograms().getAll();
        program = new EditableComboBox(programs);
        changeablesList.addChangeable(program);
        program.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Program:", program, panel);

        ParameterEnvVar[] envtVars = presenter.getCaseParameterEnvtVars().getAll();
        envtVar = new EditableComboBox(envtVars);
        changeablesList.addChangeable(envtVar);
        envtVar.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Envt. Variable:", envtVar, panel);

        varTypes = new ComboBox(presenter.getValueTypes());
        changeablesList.addChangeable(varTypes);
        varTypes.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Type:", varTypes, panel);
        
        envValue = new TextField("value", "", 20);
        envValue.setPreferredSize(preferredSize);
        changeablesList.addChangeable(envValue);
        layoutGenerator.addLabelWidgetPair("Value:", envValue, panel);
        
        sector = new ComboBox(presenter.getSectors());
        changeablesList.addChangeable(sector);
        sector.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Sector:", sector, panel);

        jobs = new ComboBox(presenter.getCaseJobs());
        changeablesList.addChangeable(jobs);
        jobs.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Job:", jobs, panel);
        
        purpose = new TextArea("purpose", param.getPurpose());
        changeablesList.addChangeable(purpose);
        ScrollableComponent scrolpane = new ScrollableComponent(purpose);
        scrolpane.setPreferredSize(new Dimension(220, 80));
        layoutGenerator.addLabelWidgetPair("Purpose:", scrolpane, panel);
       
        order = new TextField("order", param.getOrder()+"", 20);
        order.setPreferredSize(preferredSize);
        changeablesList.addChangeable(order);
        layoutGenerator.addLabelWidgetPair("Order:", order, panel);
        
        required = new CheckBox("");
        changeablesList.addChangeable(required);
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        show = new CheckBox("");
        changeablesList.addChangeable(show);
        show.setEnabled(true);
        layoutGenerator.addLabelWidgetPair("Show?", show, panel);
        
        notes = new TextArea("notes", param.getNotes());
        changeablesList.addChangeable(notes);
        ScrollableComponent notes_scrollpane = new ScrollableComponent(notes);
        notes_scrollpane.setPreferredSize(new Dimension(220, 80));
        layoutGenerator.addLabelWidgetPair("Notes:", notes_scrollpane, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 12, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        populateFields(parameter);
        container.add(panel);
    }

    private void populateFields(CaseParameter param) throws EmfException {
        parameterName.setSelectedItem(param.getParameterName());
        program.setSelectedItem(param.getProgram());

        Sector sct = param.getSector();
        if (sct != null)
            sector.setSelectedItem(sct);

        envtVar.setSelectedItem(param.getEnvVar());
        required.setSelected(param.isRequired());
        show.setSelected(param.isShow());

        int selected = presenter.getJobIndex(param.getJobId());
        if (selected > 0)
            jobs.setSelectedIndex(selected);
    }

    public CaseParameter setFields() throws EmfException {
        updateParameterName();
        updateProgram();
        updateEnvtVar();
        updateSector();
        parameter.setRequired(required.isSelected());
        parameter.setShow(show.isSelected());
        updateJob();
        parameter.setType((ValueType)varTypes.getSelectedItem());
        parameter.setValue(envValue.getText() == null ? "" : envValue.getText().trim());
        parameter.setPurpose(purpose.getText());
        parameter.setNotes(notes.getText());
        parameter.setOrder(Float.parseFloat(order.getText()));
        
        return parameter;
    }

    private void updateJob() {
        Object job = jobs.getSelectedItem();

        if (job == null)
            return;
        
        if (((CaseJob) job).getName().equalsIgnoreCase(
                ParameterFieldsPanelPresenter.ALL_FOR_SECTOR)) {
            parameter.setJobId(0);
            return;
        }

        parameter.setJobId(((CaseJob) job).getId());
    }

    private void updateParameterName() throws EmfException {
        Object selected = parameterName.getSelectedItem();
        parameter.setParameterName(presenter.getParameterName(selected));
    }

    private void updateProgram() throws EmfException {
        Object selected = program.getSelectedItem();
        if (selected == null) {
            parameter.setProgram(null);
            return;
        }

        parameter.setProgram(presenter.getCaseProgram(selected));
    }

    private void updateEnvtVar() throws EmfException {
        Object selected = envtVar.getSelectedItem();
        if (selected == null) {
            parameter.setEnvVar(null);
            return;
        }

        System.out.println("selected var:" + selected);
        parameter.setEnvVar(presenter.getParameterEnvtVar(selected));
    }

    private void updateSector() {
        Sector selected = (Sector) sector.getSelectedItem();

        if (selected.getName().equalsIgnoreCase("All sectors")) {
            parameter.setSector(null);
            return;
        }

        parameter.setSector(selected);
    }

    public void observe(ParameterFieldsPanelPresenter presenter) {
        this.presenter = presenter;
    }

    public void validateFields() throws EmfException {
        Object selectedProg = program.getSelectedItem();
        if (parameterName.getSelectedItem() == null)
            throw new EmfException("Please specify an parameter name.");

        if (selectedProg == null || selectedProg.toString().trim().equals(""))
            throw new EmfException("Please specify a program.");
        
        try {
            Float.parseFloat(order.getText());
        } catch (NumberFormatException e) {
            throw new EmfException("Please put a float number in Order field.");
        }
    }

    public CaseParameter getParameter() {
        return this.parameter;
    }

}
