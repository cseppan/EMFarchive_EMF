package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.NumberFieldVerifier;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public abstract class EfficiencyRecordWindow extends DisposableInteralFrame implements EfficiencyRecordView {

    protected ControlMeasure measure;

    protected EmfSession session;

    protected MessagePanel messagePanel;

    protected TextField efficiency;

    protected TextField costYear;

    protected TextField costperTon;

    protected TextField measureAbbreviation;

    protected TextField existingdevCode;

    protected TextField locale;

    protected TextField ruleEffectiveness;

    protected TextField rulePenetration;

    protected TextField caprecFactor;

    protected TextField discountRate;

    protected TextField detail;

    protected TextField effectiveDate;

    protected ComboBox pollutant;

    protected ComboBox equationType;

    private ManageChangeables changeablesList;

    private NumberFieldVerifier verifier;

    protected EfficiencyRecord record;

    protected Pollutant[] allPollutants;

    private String[] equationTypes = { "cpton" };

    protected static DateFormat effectiveDateFormat = new SimpleDateFormat("MM/dd/yyyy");

    public EfficiencyRecordWindow(String title, ManageChangeables changeablesList, DesktopManager desktopManager,
            EmfSession session) {
        super(title, new Dimension(650, 350), desktopManager);
        this.changeablesList = changeablesList;
        this.session = session;
        this.verifier = new NumberFieldVerifier("");
    }

    public abstract void save();

    public void display(ControlMeasure measure, EfficiencyRecord record) {
        String name = measure.getName();
        if (name == null)
            name = "New Control Measure";
        super.setLabel(super.getTitle() + " " + record.getRecordId()+ " for Control Measure: " + name);
        JPanel layout = createLayout();
        super.getContentPane().add(layout);
        super.display();
        this.record = record;

        resetChanges();
        
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(RecordPanel());
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel RecordPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        container.add(LeftRecordPanel());
        container.add(RightRecordPanel());

        panel.add(container, BorderLayout.LINE_START);

        return panel;
    }

    private Component LeftRecordPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        try {
            allPollutants = session.dataCommonsService().getPollutants();
            pollutant = new ComboBox("Select One", allPollutants);
            pollutant.setPreferredSize(new Dimension(113, 20));
        } catch (EmfException e) {
            messagePanel.setError("Could not retrieve Pollutants");
        }
        this.addChangeable(pollutant);
        layoutGenerator.addLabelWidgetPair("Pollutant:", pollutant, panel);

        locale = new TextField("Locale", 10);
        this.addChangeable(locale);
        layoutGenerator.addLabelWidgetPair("Locale:", locale, panel);

        effectiveDate = new TextField("Effective Date", 10);
        this.addChangeable(effectiveDate);
        layoutGenerator.addLabelWidgetPair("Effective Date:", effectiveDate, panel);

        measureAbbreviation = new TextField("Existing Measure Abbreviation", 10);
        this.addChangeable(measureAbbreviation);
        layoutGenerator.addLabelWidgetPair("Existing Measure Abbreviation:", measureAbbreviation, panel);

        existingdevCode = new TextField("Existing NEI Device Code", 10);
        this.addChangeable(existingdevCode);
        layoutGenerator.addLabelWidgetPair("Existing NEI Device Code:", existingdevCode, panel);

        costYear = new TextField("Cost Year", 10);
        this.addChangeable(costYear);
        layoutGenerator.addLabelWidgetPair("Cost Year:", costYear, panel);

        costperTon = new TextField("Cost Per Ton Reduced", 10);
        this.addChangeable(costperTon);
        layoutGenerator.addLabelWidgetPair("Cost Per Ton Reduced:", costperTon, panel);

        widgetLayout(7, 2, 5, 5, 10, 10, layoutGenerator, panel);

        return panel;
    }

    private Component RightRecordPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        efficiency = new TextField("", 10);
        efficiency.setName("Control Efficiency");
        this.addChangeable(efficiency);
        layoutGenerator.addLabelWidgetPair("Control Efficiency (% Red):", efficiency, panel);
        efficiency.setToolTipText("Enter the Control Efficiency as a percentage (e.g., 90%, or -10% for a disbenefit)");

        ruleEffectiveness = new TextField("Rule Effectiveness", 10);
        this.addChangeable(ruleEffectiveness);
        layoutGenerator.addLabelWidgetPair("Rule Effectiveness (%):", ruleEffectiveness, panel);

        rulePenetration = new TextField("Rule Penetration", 10);
        this.addChangeable(rulePenetration);
        layoutGenerator.addLabelWidgetPair("Rule Penetration (%):", rulePenetration, panel);

        equationType = new ComboBox(equationTypes);
        equationType.setPreferredSize(new Dimension(113, 20));
        this.addChangeable(equationType);
        layoutGenerator.addLabelWidgetPair("Equation Type:", equationType, panel);

        caprecFactor = new TextField("Capital Recovery Factor", 10);
        this.addChangeable(caprecFactor);
        layoutGenerator.addLabelWidgetPair("Capital Recovery Factor:", caprecFactor, panel);

        discountRate = new TextField("Discount Rate", 10);
        this.addChangeable(discountRate);
        layoutGenerator.addLabelWidgetPair("Discount Rate:", discountRate, panel);

        detail = new TextField("Details", 10);
        this.addChangeable(detail);
        layoutGenerator.addLabelWidgetPair("Details:", detail, panel);

        widgetLayout(7, 2, 5, 5, 10, 10, layoutGenerator, panel);

        return panel;
    }
    
    public void addChangeable(Changeable changeable){
        super.addChangeable(changeable);
        changeablesList.addChangeable(changeable);
    }

    protected String formatEffectiveDate() {
        Date effectiveDate = record.getEffectiveDate();
        return effectiveDate == null ? "" : effectiveDateFormat.format(effectiveDate);
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button save = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        getRootPane().setDefaultButton(save);
        panel.add(save);

        Button cancel = new Button("Cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                disposeView();
            }
        });
        panel.add(cancel);

        return panel;
    }

    protected void doSave() throws EmfException {
        savePollutant();
        record.setEquationType(equationType.getSelectedItem() + "");
        saveEfficiency(efficiency);
        saveCostYear();
        saveCostPerTon();
        saveLocale();
        saveRuleEffectiveness();
        saveRulePenetration();
        saveCapRecFactor();
        saveDiscountRate();
        record.setDetail(detail.getText().trim());
        saveEffectiveDate();
        record.setExistingMeasureAbbr(measureAbbreviation.getText().trim());
        saveExistingDevCode();
        
        resetChanges();
    }

    private void savePollutant() throws EmfException {
        if (pollutant.getSelectedItem() == null)
            throw new EmfException("Please Select a Pollutant");
        record.setPollutant((Pollutant) pollutant.getSelectedItem());
    }

    private void saveExistingDevCode() throws EmfException {
        if (existingdevCode.getText().trim().length() == 0)
            return;
        int value = verifier.parseInteger(existingdevCode);
        record.setExistingDevCode(value);
    }

    private void saveEffectiveDate() throws EmfException {
        try {
            String date = effectiveDate.getText().trim();
            if (date.length() == 0) {
                record.setEffectiveDate(null);
                return;
            }
            record.setEffectiveDate(effectiveDateFormat.parse(date));
        } catch (Exception e) {
            throw new EmfException("Please Correct the Date Format(MM/dd/yyyy) in Effective Date");
        }
    }

    private void saveDiscountRate() throws EmfException {
        float value = verifier.parseFloat(discountRate);
        record.setDiscountRate(value);
    }

    private void saveCapRecFactor() throws EmfException {
        float value = verifier.parseFloat(caprecFactor);
        record.setCapRecFactor(value);
    }

    private void saveRulePenetration() throws EmfException {
        float value = verifier.parseFloat(rulePenetration);
        if (value > 100)
            throw new EmfException("Enter the Rule Penetration as a percent less than 100.  1 = 1%.  0.01 = 0.01%");
        record.setRulePenetration(value);
    }

    private void saveRuleEffectiveness() throws EmfException {
        float value = verifier.parseFloat(ruleEffectiveness);
        if (value > 100)
            throw new EmfException("Enter the Rule Effectiveness as a percent less than 100.  1 = 1%.  0.01 = 0.01%");
        record.setRuleEffectiveness(value);
    }

    private void saveLocale() throws EmfException {
        if (locale.getText().trim().length() == 0) {
            record.setLocale("");
            return;
        }
        int value = verifier.parseInteger(locale);
        String string = value + "";
        if (string.length() == 2 || string.length() == 5 || string.length() == 6)
            record.setLocale(string);
        else
            throw new EmfException("Locale must be a two, five, or six digit integer.");
    }

    private void saveCostPerTon() throws EmfException {
        float value = verifier.parseFloat(costperTon);
        if (value == 0)
            throw new EmfException("Please set the Cost Per Ton");
        record.setCostPerTon(value);
    }

    private void saveCostYear() throws EmfException {
        int value = verifier.parseInteger(costYear);
        String string = value + "";
        if (string.length() != 4)
            throw new EmfException("Please enter the Cost Year as a four digit integer.");
        record.setCostYear(value);
    }

    private void saveEfficiency(TextField efficiency) throws EmfException {
        float value = verifier.parseFloat(efficiency);
        if (value > 100)
            throw new EmfException("Enter the Control Efficiency as a percentage (e.g., 90%, or -10% for a disbenefit)");
        record.setEfficiency(value);
    }

    private void widgetLayout(int rows, int cols, int initX, int initY, int xPad, int yPad,
            SpringLayoutGenerator layoutGenerator, JPanel panel) {
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, rows, cols, // rows, cols
                initX, initY, // initialX, initialY
                xPad, yPad);// xPad, yPad
    }

}