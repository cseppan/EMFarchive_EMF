package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class ControlMeasureSummaryTab extends JPanel implements ControlMeasureTabView {

    private ControlMeasure measure;

    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(EmfDateFormat.format());

    protected TextField name;

    protected TextArea description;

    private JLabel creator;

    protected EditableComboBox majorPollutant;

    protected TextField deviceCode;

    protected TextField ruleEffectiveness;

    protected TextField rulePenetration;

    protected TextField equipmentLife;

    protected TextField costYear;

    protected TextField abbreviation;

    protected TextField minUncontrolledEmission;

    protected TextField maxUncontrolledEmission;

    protected JLabel lastModifiedTime;

    protected ComboBox region;

    protected ComboBox cmClass;

    protected ListWidget sectors;

    protected ListWidget controlPrograms;

    protected MessagePanel messagePanel;

    private ManageChangeables changeablesList;

    protected EmfSession session;

    private String[] pollutants = { "NOx                                      ", "PM10", "PM2.5", "SO2", "VOC", "CO",
            "CO2", "EC", "OC", "NH3", "Hg" };

    private String[] classes = { "Production", "Developmental", "Hypothetical", "Obselete" };

    protected int deviceId, year;

    protected float cost, life, effectivness, penetration, minUnctrldEmiss, maxUnctrldEmiss;

    public ControlMeasureSummaryTab(ControlMeasure measure, EmfSession session, MessagePanel messagePanel,
            ManageChangeables changeablesList) {
        super.setName("summary");
        this.measure = measure;
        this.messagePanel = messagePanel;
        this.changeablesList = changeablesList;
        this.session = session;

        super.setLayout(new BorderLayout());
        super.add(createOverviewSection(), BorderLayout.PAGE_START);
        super.add(createAttributeSection(), BorderLayout.CENTER);
    }

    protected void populateFields() {
        String cmName = measure.getName();
        Date modifiedTime = measure.getLastModifiedTime();
        Region cmRegion = measure.getRegion();
        name.setText(getText(cmName));
        description.setText(getText(measure.getDescription()));
        creator.setText(getText(measure.getCreator().getName()));
        majorPollutant.setSelectedItem(getText(measure.getMajorPollutant()));
        cmClass.setSelectedItem(getText(measure.getCmClass()));
        //costYear.setText(measure.getCostYear() + "");
        deviceCode.setText(measure.getDeviceCode() + "");
        equipmentLife.setText(measure.getEquipmentLife() + "");
        ruleEffectiveness.setText((measure.getRuleEffectiveness()*100) + "");
        rulePenetration.setText((measure.getRulePenetration()*100) + "");
        if (modifiedTime != null)
            lastModifiedTime.setText(DATE_FORMATTER.format(modifiedTime));
        if (cmRegion != null)
            region.setSelectedItem(cmRegion);
        minUncontrolledEmission.setText(measure.getMinUncontrolledEmissions() + "");
        maxUncontrolledEmission.setText(measure.getMaxUncontrolledEmissions() + "");
        abbreviation.setText(getText(measure.getAbbreviation()));
    }
    
    protected void disableFields() {
        name.setEditable(false);
        description.setEditable(false);
        majorPollutant.setEnabled(false);
        costYear.setEditable(false);
        deviceCode.setEditable(false);
        equipmentLife.setEditable(false);
        ruleEffectiveness.setEditable(false);
        rulePenetration.setEditable(false);
        region.setEnabled(false);
        cmClass.setEnabled(false);
        sectors.setEnabled(false);
        controlPrograms.setEnabled(false);
    }

    private String getText(String value) {
        return (value != null) ? value : "";
    }

    private JPanel createOverviewSection() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addWidgetPair(createLeftOverview(), createRightOverview(), panel);
        widgetLayout(1, 2, 5, 5, 5, 5, layoutGenerator, panel);

        return panel;
    }

    private JPanel createLeftOverview() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("Control measure name", 27);
        changeablesList.addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        // description
        description = new TextArea("description", measure.getDescription());
        changeablesList.addChangeable(description);
        ScrollableComponent descPane = new ScrollableComponent(description);
        descPane.setPreferredSize(new Dimension(300, 50));
        layoutGenerator.addLabelWidgetPair("Description:", descPane, panel);

        widgetLayout(2, 2, 5, 5, 10, 10, layoutGenerator, panel);

        return panel;
    }

    private JPanel createRightOverview() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        abbreviation = new TextField("Abbreviation", 12);
        changeablesList.addChangeable(abbreviation);
        layoutGenerator.addLabelWidgetPair("Abbreviation:", abbreviation, panel);

        cmClass = new ComboBox("Choose a class", classes);
        cmClass.setPreferredSize(new Dimension(135, 20));
        changeablesList.addChangeable(cmClass);
        layoutGenerator.addLabelWidgetPair("Class:", cmClass, panel);

        lastModifiedTime = new JLabel("");
        layoutGenerator.addLabelWidgetPair("Last Modified Time:", lastModifiedTime, panel);

        widgetLayout(3, 2, 5, 5, 10, 10, layoutGenerator, panel);

        return panel;
    }

    private JPanel createAttributeSection() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        container.add(createLeftPanel());
        container.add(createRightPanel());

        panel.add(container, BorderLayout.LINE_START);

        return panel;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        creator = new JLabel(session.user().getName());
        layoutGenerator.addLabelWidgetPair("Creator:", creator, panel);

        majorPollutant = new EditableComboBox(pollutants);
        majorPollutant.setSelectedIndex(0);
        changeablesList.addChangeable(majorPollutant);
        layoutGenerator.addLabelWidgetPair("Major pollutant:", majorPollutant, panel);
        
        try {
            region = new ComboBox("Choose a region", session.dataCommonsService().getRegions());
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        region.setPreferredSize(new Dimension(168, 20));
        changeablesList.addChangeable(region);
        layoutGenerator.addLabelWidgetPair("Region:", region, panel);
        
        ruleEffectiveness = new TextField("Rule Effectiveness (%)", 15);
        changeablesList.addChangeable(ruleEffectiveness);
        layoutGenerator.addLabelWidgetPair("Rule Effectiveness (%):", ruleEffectiveness, panel);

        rulePenetration = new TextField("Rule Penetration (%)", 15);
        changeablesList.addChangeable(rulePenetration);
        layoutGenerator.addLabelWidgetPair("Rule Penetration (%):", rulePenetration, panel);

        sectors = new ListWidget(new String[] { "               " }, new String[] { "" });
        JScrollPane listScroller = new JScrollPane(sectors);
        listScroller.setPreferredSize(new Dimension(170, 60));
        layoutGenerator.addLabelWidgetPair("Sectors:", listScroller, panel);

        layoutGenerator.addLabelWidgetPair("", addRemoveButtonPanel(), panel);
        widgetLayout(7, 2, 5, 5, 10, 10, layoutGenerator, panel);

        return panel;
    }

    private Component createRightPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

//        costYear = new TextField("Cost year", 15);
//        changeablesList.addChangeable(costYear);
        layoutGenerator.addLabelWidgetPair(" ", new JLabel(" "), panel); // to hold the place

        minUncontrolledEmission = new TextField("Min Uncntrld. Emissions (T/yr)", 15);
        changeablesList.addChangeable(minUncontrolledEmission);
        layoutGenerator.addLabelWidgetPair("Min Uncntrld. Emissions (T/yr):", minUncontrolledEmission, panel);

        maxUncontrolledEmission = new TextField("Max Uncntrld. Emissions (T/yr)", 15);
        changeablesList.addChangeable(maxUncontrolledEmission);
        layoutGenerator.addLabelWidgetPair("Max Uncntrld. Emissions (T/yr):", maxUncontrolledEmission, panel);

        equipmentLife = new TextField("Equipment life", 15);
        changeablesList.addChangeable(equipmentLife);
        layoutGenerator.addLabelWidgetPair("Equipment life (yrs):", equipmentLife, panel);

        deviceCode = new TextField("NEI Device code", 15);
        changeablesList.addChangeable(deviceCode);
        layoutGenerator.addLabelWidgetPair("NEI Device code:", deviceCode, panel);
        
        controlPrograms = new ListWidget(new String[] { "               " }, new String[] { "" });
        JScrollPane listScroller = new JScrollPane(controlPrograms);
        listScroller.setPreferredSize(new Dimension(170, 60));
        layoutGenerator.addLabelWidgetPair("Control programs:", listScroller, panel);

        layoutGenerator.addLabelWidgetPair("", addRemoveButtonPanel(), panel);
        widgetLayout(7, 2, 5, 5, 10, 10, layoutGenerator, panel);

        return panel;
    }

    private void widgetLayout(int rows, int cols, int initX, int initY, int xPad, int yPad, SpringLayoutGenerator layoutGenerator,
            JPanel panel) {
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, rows, cols, // rows, cols
                initX, initY, // initialX, initialY
                xPad, yPad);// xPad, yPad
    }

    private JPanel addRemoveButtonPanel() {
        JPanel panel = new JPanel();
        // TBD: this needs to be changed so you have handles to the buttons
        // and can set actions
        JButton addButton = new JButton("Add");
        addButton.setEnabled(false);
        JButton removeButton = new JButton("Remove");
        removeButton.setEnabled(false);

        panel.add(addButton);
        panel.add(removeButton);
        // for now, disable these
        
        return panel;
    }

}
