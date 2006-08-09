package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.gui.Button;
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

public class EditEfficiencyRecordWindow extends DisposableInteralFrame implements EditEfficiencyRecordView {

    protected ControlMeasure measure;

    protected EmfSession session;

    private MessagePanel messagePanel;

    private TextField efficiency;

    protected TextField costYear;

    protected TextField costperTon;

    protected TextField locale;
    
    protected TextField ruleEffectiveness;
    
    protected TextField rulePenetration;
    
    protected TextField caprecFactor;
    
    protected TextField discountRate;
    
    protected TextField detail;
    
    protected TextField effectiveDate;

    private ComboBox pollutant;
    
    private ComboBox equationType;

    private ManageChangeables changeablesList;

    private NumberFieldVerifier verifier;

    private EfficiencyRecord record;

    private static int count = 0;

    protected Pollutant[] allPollutants;
    
    private String[] equationTypes = { "cpton" };
    
    protected static DateFormat effectiveDateFormat = new SimpleDateFormat("MM/dd/yyyy");

    private EditEfficiencyRecordPresenter presenter;

    public EditEfficiencyRecordWindow(ManageChangeables changeablesList, DesktopManager desktopManager,
            EmfSession session) {
        super("Add Efficiency Record", new Dimension(600, 300), desktopManager);
        this.changeablesList = changeablesList;
        this.session = session;
        this.verifier = new NumberFieldVerifier("");
    }

    public void display(ControlMeasure measure, EfficiencyRecord record) {
        String name = measure.getName();
        if (name == null)
            name = "New Control Measure";
        super.setLabel(super.getTitle() + " " + ++count + " for Control Measure: " + name);
        JPanel layout = createLayout();
        super.getContentPane().add(layout);
        super.display();
        this.record = record;

        if (record != null)
            populateFields();
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
            pollutant = new ComboBox(allPollutants);
            pollutant.setPreferredSize(new Dimension(113, 20));
        } catch (EmfException e) {
            messagePanel.setError("Could not retrieve Pollutants");
        }
        // majorPollutant.setSelectedIndex(0);
        changeablesList.addChangeable(pollutant);
        layoutGenerator.addLabelWidgetPair("Pollutant:", pollutant, panel);

        efficiency = new TextField("", 10);
        efficiency.setName("Control Efficiency");
        changeablesList.addChangeable(efficiency);
        layoutGenerator.addLabelWidgetPair("Control Efficiency (% Red):", efficiency, panel);
        efficiency.setToolTipText("Enter the Control Efficiency as a percentage (e.g., 90%, or -10% for a disbenefit)");

        costYear = new TextField("Cost Year", 10);
        changeablesList.addChangeable(costYear);
        layoutGenerator.addLabelWidgetPair("Cost Year:", costYear, panel);

        costperTon = new TextField("Cost Per Ton Reduced", 10);
        changeablesList.addChangeable(costperTon);
        layoutGenerator.addLabelWidgetPair("Cost Per Ton Reduced:", costperTon, panel);

        locale = new TextField("Locale", 10);
        changeablesList.addChangeable(locale);
        layoutGenerator.addLabelWidgetPair("Locale:", locale, panel);
        
        detail = new TextField("Details", 20);
        changeablesList.addChangeable(detail);
        layoutGenerator.addLabelWidgetPair("Details:", detail, panel);
        
        widgetLayout(6, 2, 5, 5, 10, 10, layoutGenerator, panel);

        return panel;
    }

    private Component RightRecordPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        equationType = new ComboBox(equationTypes);
        equationType.setPreferredSize(new Dimension(113, 20));
        changeablesList.addChangeable(equationType);
        layoutGenerator.addLabelWidgetPair("Equation Type", equationType, panel);
        
        ruleEffectiveness = new TextField("Rule Effectiveness", 10);
        changeablesList.addChangeable(ruleEffectiveness);
        layoutGenerator.addLabelWidgetPair("Rule Effectiveness (%):", ruleEffectiveness, panel);

        rulePenetration = new TextField("Rule Penetration", 10);
        changeablesList.addChangeable(rulePenetration);
        layoutGenerator.addLabelWidgetPair("Rule Penetration (%):", rulePenetration, panel);
        
        caprecFactor = new TextField("Capital Recovery Factor", 10);
        changeablesList.addChangeable(caprecFactor);
        layoutGenerator.addLabelWidgetPair("Capital Recovery Factor:", caprecFactor, panel);
        
        discountRate = new TextField("Discount Rate", 10);
        changeablesList.addChangeable(discountRate);
        layoutGenerator.addLabelWidgetPair("Discount Rate:", discountRate, panel);
        
        effectiveDate = new TextField("Effective Date", 10);
        changeablesList.addChangeable(effectiveDate);
        layoutGenerator.addLabelWidgetPair("Effective Date:", effectiveDate, panel);
        
        widgetLayout(6, 2, 5, 5, 10, 10, layoutGenerator, panel);
        
        return panel;
    }
    
    private void populateFields() {
        pollutant.setSelectedItem(record.getPollutant());
        equationType.setSelectedItem(record.getEquationType());
        efficiency.setText(record.getEfficiency() + "");
        costYear.setText(record.getCostYear() + "");
        costperTon.setText(record.getCostPerTon() + "");
        locale.setText(record.getLocale());
        ruleEffectiveness.setText(record.getRuleEffectiveness() + "");
        rulePenetration.setText(record.getRulePenetration() + "");
        caprecFactor.setText(record.getCapRecFactor() + "");
        discountRate.setText(record.getDiscountRate() + "");
        detail.setText(record.getDetail());
        effectiveDate.setText(formatEffectiveDate());
    }

    private String formatEffectiveDate() {
        Date effectiveDate = record.getEffectiveDate();
        return effectiveDate == null ? "" : effectiveDateFormat.format(effectiveDate);
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button save = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doSave();
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

    private void doSave() {
        try {
            record.setPollutant((Pollutant) pollutant.getSelectedItem());
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
            
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
            return;
        }
        presenter.refresh();
        disposeView();
    }

    private void saveEffectiveDate() throws EmfException {
        try {
            String date = effectiveDate.getText().trim();
            if (date.length() == 0){
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
        int value = verifier.parseInteger(locale);
        String string = value + "";
        if (string.length() == 2 || string.length() == 5 || string.length() == 6)
            record.setLocale(string);
        else
            throw new EmfException("Locale must be a two, five, or six digit integer.");
    }

    private void saveCostPerTon() throws EmfException {
        float value = verifier.parseFloat(costperTon);
        record.setCostPerTon(value);
    }

    private void saveCostYear() throws EmfException {
        int value = verifier.parseInteger(costYear);
        String string = value + "";
        if (string.length() != 4)
            throw new EmfException("Cost Year must be a four digit integer.");
        record.setCostYear(value);
    }

    private void saveEfficiency(TextField efficiency) throws EmfException {
        float value = verifier.parseFloat(efficiency);
        if (value > 100)
            throw new EmfException("Enter the Control Efficiency as a percentage (e.g., 90%, or -10% for a disbenefit)");
        record.setEfficiency(value);
    }

    public void observe(EditEfficiencyRecordPresenter presenter) {
        this.presenter = presenter;
    }

    private void widgetLayout(int rows, int cols, int initX, int initY, int xPad, int yPad,
            SpringLayoutGenerator layoutGenerator, JPanel panel) {
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, rows, cols, // rows, cols
                initX, initY, // initialX, initialY
                xPad, yPad);// xPad, yPad
    }
}