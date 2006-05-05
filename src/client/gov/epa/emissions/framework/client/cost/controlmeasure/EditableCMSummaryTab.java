package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.ComboBox;
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
import gov.epa.emissions.framework.ui.NumberFormattedTextField;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class EditableCMSummaryTab extends JPanel implements EditableCMSummaryTabView {

    private ControlMeasure measure;

    public final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(EmfDateFormat.format());

    private TextField name;

    private TextArea description;

    private TextField creator;

    private TextField majorPollutant;

    private NumberFormattedTextField deviceCode;

    private NumberFormattedTextField ruleEffectiveness;

    private NumberFormattedTextField rulePenetration;

    private NumberFormattedTextField equipmentLife;

    private NumberFormattedTextField costYear;

    private NumberFormattedTextField anualizedCost;

    private ComboBox region;

    private ComboBox cmClass;

    private ListWidget sectors;

    private ListWidget controlPrograms;

    private MessagePanel messagePanel;

    private ManageChangeables changeablesList;

    private EmfSession session;

    public EditableCMSummaryTab(ControlMeasure measure, EmfSession session, MessagePanel messagePanel,
            ManageChangeables changeablesList, String newOrEdit) {
        super.setName("summary");
        this.measure = measure;
        this.messagePanel = messagePanel;
        this.changeablesList = changeablesList;
        this.session = session;

        super.setLayout(new BorderLayout());
        super.add(createOverviewSection(), BorderLayout.PAGE_START);
        super.add(createAttributeSection(), BorderLayout.CENTER);
        if(newOrEdit.equalsIgnoreCase("edit"))
            populateFields();
    }

    private void populateFields() {
        name.setText(measure.getName());
        description.setText(measure.getDescription());
    }

    private JPanel createOverviewSection() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // name
        name = new TextField("Control measure name", 25);
        name.setText(measure.getName());
        name.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(name);

        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        // description
        description = new TextArea("description", measure.getDescription());
        changeablesList.addChangeable(description);
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableComponent(description), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

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

        creator = new TextField("", 15);
        changeablesList.addChangeable(creator);
        layoutGenerator.addLabelWidgetPair("Creator:", creator, panel);

        costYear = new NumberFormattedTextField(15, fieldAction(costYear));
        changeablesList.addChangeable(costYear);
        costYear.addKeyListener(keyListener(costYear));
        costYear.setName("Cost year");
        layoutGenerator.addLabelWidgetPair("Cost year:", costYear, panel);

        deviceCode = new NumberFormattedTextField(15, fieldAction(deviceCode));
        changeablesList.addChangeable(deviceCode);
        deviceCode.addKeyListener(keyListener(deviceCode));
        deviceCode.setName("Device code");
        layoutGenerator.addLabelWidgetPair("Device code:", deviceCode, panel);

        ruleEffectiveness = new NumberFormattedTextField(15, fieldAction(ruleEffectiveness));
        changeablesList.addChangeable(ruleEffectiveness);
        ruleEffectiveness.addKeyListener(keyListener(ruleEffectiveness));
        ruleEffectiveness.setName("Rule efficiency");
        layoutGenerator.addLabelWidgetPair("Rule efficiency:", ruleEffectiveness, panel);

        region = new ComboBox("Choose a region", new String[] { "" });
        region.setPreferredSize(new Dimension(168,20));
        changeablesList.addChangeable(region);
        layoutGenerator.addLabelWidgetPair("Region:", region, panel);

        sectors = new ListWidget(new String[] { "               " }, new String[] { "" });
        JScrollPane listScroller = new JScrollPane(sectors);
        listScroller.setPreferredSize(new Dimension(170, 60));
        layoutGenerator.addLabelWidgetPair("Sectors:", listScroller, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private Component createRightPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        majorPollutant = new TextField("", 15);
        changeablesList.addChangeable(majorPollutant);
        layoutGenerator.addLabelWidgetPair("Major pollutant:", majorPollutant, panel);

        anualizedCost = new NumberFormattedTextField(15, fieldAction(anualizedCost));
        changeablesList.addChangeable(anualizedCost);
        anualizedCost.addKeyListener(keyListener(anualizedCost));
        anualizedCost.setName("Annualized cost");
        layoutGenerator.addLabelWidgetPair("Annualized cost:", anualizedCost, panel);

        equipmentLife = new NumberFormattedTextField(15, fieldAction(equipmentLife));
        changeablesList.addChangeable(equipmentLife);
        equipmentLife.addKeyListener(keyListener(equipmentLife));
        equipmentLife.setName("Equipment life");
        layoutGenerator.addLabelWidgetPair("Equipment life:", equipmentLife, panel);

        rulePenetration = new NumberFormattedTextField(15, fieldAction(rulePenetration));
        changeablesList.addChangeable(rulePenetration);
        rulePenetration.addKeyListener(keyListener(rulePenetration));
        rulePenetration.setName("Rule penetration");
        layoutGenerator.addLabelWidgetPair("Rule penetration:", rulePenetration, panel);

        cmClass = new ComboBox("Choose a class", new String[] { "" });
        cmClass.setPreferredSize(new Dimension(168, 20));
        changeablesList.addChangeable(cmClass);
        layoutGenerator.addLabelWidgetPair("Class:", cmClass, panel);

        controlPrograms = new ListWidget(new String[] { "               " }, new String[] { "" });
        JScrollPane listScroller = new JScrollPane(controlPrograms);
        listScroller.setPreferredSize(new Dimension(170, 60));
        layoutGenerator.addLabelWidgetPair("Control programs:", listScroller, panel);
        
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private AbstractAction fieldAction(final NumberFormattedTextField numberField) {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    parseNumber(numberField);
                } catch (NumberFormatException ex) {
                    messagePanel.setError(numberField.getName() + " should be a floating point number");
                }
            }
        };
    }

    private KeyListener keyListener(final NumberFormattedTextField field) {
        return new KeyListener() {
            public void keyTyped(KeyEvent e) {
                keyActions(field);
            }

            public void keyReleased(KeyEvent e) {
                keyActions(field);
            }

            public void keyPressed(KeyEvent e) {
                keyActions(field);
            }
        };
    }

    private void keyActions(NumberFormattedTextField field) {
        try {
            messagePanel.clear();
            parseNumber(field);
        } catch (NumberFormatException ex) {
            messagePanel.setError(field.getName() + " should be a floating point number");
        }
    }

    private void parseNumber(NumberFormattedTextField numberField) {
        if (!numberField.getText().equals(""))
            Float.parseFloat(numberField.getText());
    }

    public void save(ControlMeasure measure) {
        messagePanel.clear();

        if (name.getText().equals("")) {
            messagePanel.setError("Name field should be a non-empty string.");
            return;
        }

        measure.setName(name.getText());
        measure.setDescription(description.getText());
        measure.setAnnualizedCost((float)anualizedCost.getDouble());
        measure.setCreator(session.user());
        measure.setDeviceCode(deviceCode.getInt());
        measure.setEquipmentLife((float)equipmentLife.getDouble());
        measure.setMajorPollutant(majorPollutant.getText());
        measure.setRuleEffectiveness((float)ruleEffectiveness.getDouble());
        measure.setRulePenetration((float)rulePenetration.getDouble());
    }

}
