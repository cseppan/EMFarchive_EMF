package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.SimpleDateFormat;

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

    protected TextField anualizedCost;

    private ComboBox region;

    private ComboBox cmClass;

    private ListWidget sectors;

    private ListWidget controlPrograms;

    protected MessagePanel messagePanel;

    private ManageChangeables changeablesList;

    protected EmfSession session;

    private String[] pollutants = { "NOx                                      ", "PM10", "PM2.5", "SO2", "VOC", "CO",
            "CO2", "EC", "OC", "NH3", "Hg" };

    protected int deviceId = 1;

    protected float cost = 0;

    protected float life = 0;

    protected float eff = 0;

    protected float penetr = 0;

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
        populateFields();
    }

    private void populateFields() {
        String cmName = measure.getName();
        name.setText(getText(cmName));
        description.setText(getText(measure.getDescription()));
        creator.setText(getText(measure.getCreator().getName()));
        majorPollutant.setSelectedItem(getText(measure.getMajorPollutant()));
        costYear.setText("");
        anualizedCost.setText(measure.getAnnualizedCost() + "");
        deviceCode.setText(measure.getDeviceCode() + "");
        equipmentLife.setText(measure.getEquipmentLife() + "");
        ruleEffectiveness.setText(measure.getRuleEffectiveness() + "");
        rulePenetration.setText(measure.getRulePenetration() + "");

    }

    private String getText(String value) {
        return (value!=null)?value:"";
    }

    private JPanel createOverviewSection() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // name
        name = new TextField("Control measure name", 40);
        changeablesList.addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        // description
        description = new TextArea("description", measure.getDescription());
        changeablesList.addChangeable(description);
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableComponent(description), panel);

        widgetLayout(2, 2, 50, 5, 10, 10, layoutGenerator, panel);

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

        costYear = new TextField("Cost year", 15);
        changeablesList.addChangeable(costYear);
        layoutGenerator.addLabelWidgetPair("Cost year:", costYear, panel);

        deviceCode = new TextField("Device code", 15);
        changeablesList.addChangeable(deviceCode);
        layoutGenerator.addLabelWidgetPair("Device code:", deviceCode, panel);

        ruleEffectiveness = new TextField("Rule effectiveness", 15);
        changeablesList.addChangeable(ruleEffectiveness);
        layoutGenerator.addLabelWidgetPair("Rule effectiveness:", ruleEffectiveness, panel);

        region = new ComboBox("Choose a region", new String[] { "" });
        region.setPreferredSize(new Dimension(168, 20));
        changeablesList.addChangeable(region);
        layoutGenerator.addLabelWidgetPair("Region:", region, panel);

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

        majorPollutant = new EditableComboBox(pollutants);
        changeablesList.addChangeable(majorPollutant);
        layoutGenerator.addLabelWidgetPair("Major pollutant:", majorPollutant, panel);

        anualizedCost = new TextField("Annualized cost", 15);
        changeablesList.addChangeable(anualizedCost);
        layoutGenerator.addLabelWidgetPair("Annualized cost:", anualizedCost, panel);

        equipmentLife = new TextField("Equipment life", 15);
        changeablesList.addChangeable(equipmentLife);
        layoutGenerator.addLabelWidgetPair("Equipment life:", equipmentLife, panel);

        rulePenetration = new TextField("Rule penetration", 15);
        changeablesList.addChangeable(rulePenetration);
        layoutGenerator.addLabelWidgetPair("Rule penetration:", rulePenetration, panel);

        cmClass = new ComboBox("Choose a class", new String[] { "" });
        cmClass.setPreferredSize(new Dimension(168, 20));
        changeablesList.addChangeable(cmClass);
        layoutGenerator.addLabelWidgetPair("Class:", cmClass, panel);

        controlPrograms = new ListWidget(new String[] { "               " }, new String[] { "" });
        JScrollPane listScroller = new JScrollPane(controlPrograms);
        listScroller.setPreferredSize(new Dimension(170, 60));
        layoutGenerator.addLabelWidgetPair("Control programs:", listScroller, panel);

        layoutGenerator.addLabelWidgetPair("", addRemoveButtonPanel(), panel);
        widgetLayout(7, 2, 5, 5, 10, 10, layoutGenerator, panel);

        return panel;
    }

    private void widgetLayout(int i, int j, int k, int l, int m, int n, SpringLayoutGenerator layoutGenerator,
            JPanel panel) {
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, i, j, // rows, cols
                k, l, // initialX, initialY
                m, n);// xPad, yPad
    }

    private JPanel addRemoveButtonPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(new JButton("Add"), BorderLayout.LINE_START);
        panel.add(new JButton("Remove"), BorderLayout.CENTER);

        return panel;
    }

}
