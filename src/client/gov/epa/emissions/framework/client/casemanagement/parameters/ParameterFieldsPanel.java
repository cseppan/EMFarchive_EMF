package gov.epa.emissions.framework.client.casemanagement.parameters;

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
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.Dimension;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

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

    private MessagePanel messagePanel;

    public ParameterFieldsPanel(MessagePanel messagePanel, ManageChangeables changeablesList) {
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
    }

    public void display(CaseParameter param, JComponent container) throws EmfException {
        this.parameter = param;
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        String width = EmptyStrings.create(65);
        Dimension preferredSize = new Dimension(20, 30);

        parameterName = new EditableComboBox(presenter.getParameterNames());
        addPopupMenuListener(parameterName, "parameternames");
        parameterName.setSelectedItem(param.getParameterName());
        changeablesList.addChangeable(parameterName);
        parameterName.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Parameter Name:", parameterName, panel);

        program = new EditableComboBox(presenter.getPrograms());
        addPopupMenuListener(program, "programs");
        program.setSelectedItem(param.getProgram());
        changeablesList.addChangeable(program);
        program.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Program:", program, panel);

        envtVar = new EditableComboBox(presenter.getEnvtVars());
        addPopupMenuListener(envtVar, "envtvars");
        envtVar.setSelectedItem(param.getEnvVar());
        changeablesList.addChangeable(envtVar);
        envtVar.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Envt. Variable:", envtVar, panel);

        varTypes = new ComboBox(presenter.getValueTypes());
        varTypes.setSelectedItem(param.getType());
        addPopupMenuListener(varTypes, "vartypes");
        changeablesList.addChangeable(varTypes);
        varTypes.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Type:", varTypes, panel);

        envValue = new TextField("value", param.getValue(), 20);
        envValue.setPreferredSize(preferredSize);
        changeablesList.addChangeable(envValue);
        layoutGenerator.addLabelWidgetPair("Value:", envValue, panel);

        sector = new ComboBox(presenter.getSectors());
        sector.setSelectedItem(param.getSector() == null ? sector.getItemAt(0) : param.getSector());
        addPopupMenuListener(sector, "sectors");
        changeablesList.addChangeable(sector);
        sector.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Sector:", sector, panel);

        setJob(param);
        changeablesList.addChangeable(jobs);
        jobs.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Job:", jobs, panel);

        purpose = new TextArea("purpose", param.getPurpose());
        changeablesList.addChangeable(purpose);
        ScrollableComponent scrolpane = new ScrollableComponent(purpose);
        scrolpane.setPreferredSize(new Dimension(224, 80));
        layoutGenerator.addLabelWidgetPair("Purpose:", scrolpane, panel);

        order = new TextField("order", param.getOrder() + "", 20);
        order.setPreferredSize(preferredSize);
        changeablesList.addChangeable(order);
        layoutGenerator.addLabelWidgetPair("Order:", order, panel);

        show = new CheckBox("");
        changeablesList.addChangeable(show);

        required = new CheckBox("");
        changeablesList.addChangeable(required);

        JPanel checkBoxPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layout = new SpringLayoutGenerator();
        JPanel showPanel = new JPanel();
        showPanel.add(new JLabel(EmptyStrings.create(20)));
        showPanel.add(new JLabel("Show?"));
        showPanel.add(new JLabel(EmptyStrings.create(20)));
        showPanel.add(show);
        layout.addWidgetPair(required, showPanel, checkBoxPanel);
        layout.makeCompactGrid(checkBoxPanel, 1, 2, 0, 0, 0, 0);
        layoutGenerator.addLabelWidgetPair("Required?", checkBoxPanel, panel);

        notes = new TextArea("notes", param.getNotes());
        changeablesList.addChangeable(notes);
        ScrollableComponent notes_scrollpane = new ScrollableComponent(notes);
        notes_scrollpane.setPreferredSize(new Dimension(224, 80));
        layoutGenerator.addLabelWidgetPair("Notes:", notes_scrollpane, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 11, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        populateFields(parameter);
        container.add(panel);
    }

    private void addPopupMenuListener(final JComboBox box, final String toget) {
        box.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent event) {
                // NOTE Auto-generated method stub
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
                // NOTE Auto-generated method stub
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
                try {
                    Object selected = box.getSelectedItem();
                    box.setModel(new DefaultComboBoxModel(getAllObjects(toget)));
                    box.setSelectedItem(selected);
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
    }

    protected Object[] getAllObjects(String toget) throws EmfException {
        if (toget.equals("parameternames"))
            return presenter.getParameterNames();

        if (toget.equals("programs"))
            return presenter.getPrograms();

        if (toget.equals("envtvars"))
            return presenter.getEnvtVars();

        if (toget.equals("vartypes"))
            return presenter.getValueTypes();

        if (toget.equals("sectors"))
            return presenter.getSectors();

        return null;
    }

    private void setJob(CaseParameter param) throws EmfException {
        jobs = new ComboBox(presenter.getCaseJobs());
        jobs.setSelectedIndex(presenter.getJobIndex(param.getJobId()));
    }

    private void populateFields(CaseParameter param) {
        required.setSelected(param.isRequired());
        show.setSelected(param.isShow());
    }

    public CaseParameter setFields() throws EmfException {
        updateParameterName();
        updateProgram();
        updateEnvtVar();
        updateSector();
        parameter.setRequired(required.isSelected());
        parameter.setShow(show.isSelected());
        updateJob();
        parameter.setType((ValueType) varTypes.getSelectedItem());
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

        parameter.setJobId(presenter.getJobId((CaseJob) job));
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
