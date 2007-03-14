package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.YearValidation;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.NumberFieldVerifier;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public abstract class EfficiencyRecordWindow extends DisposableInteralFrame {

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

    protected TextArea detail;

    protected TextField effectiveDate;

    protected ComboBox pollutant;

    protected ComboBox equationType;

    private ManageChangeables changeablesList;

    private NumberFieldVerifier verifier;

    protected EfficiencyRecord record;

    protected Pollutant[] allPollutants;

    private String[] equationTypes = { "cpton" };

    protected TextField lastModifiedTime;

    protected TextField lastModifiedBy;

    protected CostYearTable costYearTable;
    
    protected JLabel refYrCostPerTon;

    protected final int refYear = 2000;

    public EfficiencyRecordWindow(String title, ManageChangeables changeablesList, DesktopManager desktopManager,
            EmfSession session, CostYearTable costYearTable) {
        super(title, new Dimension(650, 425), desktopManager);
        this.changeablesList = changeablesList;
        this.session = session;
        this.verifier = new NumberFieldVerifier("");
        this.costYearTable = costYearTable;
    }

    public abstract void save();

    public void display(ControlMeasure measure, EfficiencyRecord record) {
        String name = measure.getName();
        if (name == null)
            name = "New Control Measure";
        super.setLabel(super.getTitle() + " " + record.getRecordId() + " for Control Measure: " + name);
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
//        JPanel pollutantContainer = new JPanel(new SpringLayout());
//        JPanel pollutantCenterPanel = new JPanel();
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

//        try {
//            allPollutants = session.dataCommonsService().getPollutants();
//            pollutant = new ComboBox("Select One", allPollutants);
//            pollutant.setPreferredSize(new Dimension(113, 20));
//        } catch (EmfException e) {
//            messagePanel.setError("Could not retrieve Pollutants");
//        }
//        this.addChangeable(pollutant);
//        layoutGenerator.addLabelWidgetPair("Pollutant:*", pollutant, pollutantContainer);
//
//        widgetLayout(1, 2, 5, 5, 10, 10, layoutGenerator, pollutantContainer);
//
        container.add(LeftRecordPanel());
        container.add(RightRecordPanel());
//        container.add(LeftRecordPanel(), BorderLayout.WEST);
//        container.add(RightRecordPanel(), BorderLayout.EAST);
//        pollutantCenterPanel.add(pollutantContainer, BorderLayout.CENTER);
//        panel.add(pollutantCenterPanel, BorderLayout.NORTH);
        panel.add(container, BorderLayout.CENTER);//LINE_START);

        JPanel detailContainer = new JPanel(new SpringLayout());
        layoutGenerator = new SpringLayoutGenerator();
        detail = new TextArea("Details", "");
        ScrollableComponent detailPane = new ScrollableComponent(detail);
        detailPane.setPreferredSize(new Dimension(540, 50));
        this.addChangeable(detail);
        layoutGenerator.addLabelWidgetPair("Details:", detailPane, detailContainer);
        
        widgetLayout(1, 2, 5, 5, 10, 10, layoutGenerator, detailContainer);

        JPanel detailCenterPanel = new JPanel();
        detailCenterPanel.add(detailContainer, BorderLayout.CENTER);
        panel.add(detailCenterPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private Component LeftRecordPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

//        try {
//            allPollutants = session.dataCommonsService().getPollutants();
//            pollutant = new ComboBox("Select One", allPollutants);
//            pollutant.setPreferredSize(new Dimension(113, 20));
//        } catch (EmfException e) {
//            messagePanel.setError("Could not retrieve Pollutants");
//        }
//        this.addChangeable(pollutant);
//        layoutGenerator.addLabelWidgetPair("Pollutant:*", pollutant, panel);

        try {
            allPollutants = session.dataCommonsService().getPollutants();
            pollutant = new ComboBox("Select One", allPollutants);
            pollutant.setPreferredSize(new Dimension(113, 20));
        } catch (EmfException e) {
            messagePanel.setError("Could not retrieve Pollutants");
        }
        this.addChangeable(pollutant);
        layoutGenerator.addLabelWidgetPair("Pollutant:*", pollutant, panel);

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
//        editor.addFocusListener(new FocusAdapter() {
//            public void focusLost(FocusEvent e) {
        costYear.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                try {
                    refreshRefYrCostPerTon();
                } catch (EmfException e1) {
                    messagePanel.setError("Could not update reference year cost per ton");
                }
            }
        });
        this.addChangeable(costYear);
        layoutGenerator.addLabelWidgetPair("Cost Year:*", costYear, panel);

        costperTon = new TextField("Cost Per Ton Reduced", 10);
        costperTon.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                try {
                    refreshRefYrCostPerTon();
                } catch (EmfException e1) {
                    messagePanel.setError("Could not update reference year cost per ton");
                }
            }
        });
        this.addChangeable(costperTon);
        layoutGenerator.addLabelWidgetPair("Cost Per Ton Reduced:*", costperTon, panel);

        refYrCostPerTon = new JLabel("");
        layoutGenerator.addLabelWidgetPair("Ref Yr Cost Per Ton Reduced:", refYrCostPerTon, panel);

        widgetLayout(8, 2, 5, 5, 10, 10, layoutGenerator, panel);

        return panel;
    }


    private void refreshRefYrCostPerTon() throws EmfException {
        int costYearValue = verifier.parseInteger(costYear);
        float costPerTonValue = verifier.parseFloat(costperTon);
        costYearTable.setTargetYear(refYear);
        DecimalFormat currency = new DecimalFormat("#0.00"); 
        refYrCostPerTon.setText(currency.format(costPerTonValue * costYearTable.factor(costYearValue)));
    }

    private Component RightRecordPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        efficiency = new TextField("", 10);
        efficiency.setName("Control Efficiency");
        this.addChangeable(efficiency);
        layoutGenerator.addLabelWidgetPair("Control Efficiency (% Red):*", efficiency, panel);
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
        layoutGenerator.addLabelWidgetPair("Discount Rate (%):", discountRate, panel);

        lastModifiedBy = new TextField("Last Modified By", 10);
        changeablesList.addChangeable(lastModifiedBy);
        lastModifiedBy.setEnabled(false);
        lastModifiedBy.setOpaque(false);
        lastModifiedBy.setDisabledTextColor(Color.BLACK);
        lastModifiedBy.setBorder(BorderFactory.createEmptyBorder());
        layoutGenerator.addLabelWidgetPair("Last Modified By:", lastModifiedBy, panel);

        lastModifiedTime = new TextField("Last Modified Time", 10);
        changeablesList.addChangeable(lastModifiedTime);
        lastModifiedTime.setEnabled(false);
        lastModifiedTime.setOpaque(false);
        lastModifiedTime.setDisabledTextColor(Color.BLACK);
        lastModifiedTime.setBorder(BorderFactory.createEmptyBorder());
        layoutGenerator.addLabelWidgetPair("Last Modified Time:", lastModifiedTime, panel);

        widgetLayout(8, 2, 5, 5, 10, 10, layoutGenerator, panel);

        return panel;
    }

    public void addChangeable(Changeable changeable) {
        super.addChangeable(changeable);
        changeablesList.addChangeable(changeable);
    }

    protected String formatEffectiveDate() {
        Date effectiveDate = record.getEffectiveDate();
        return effectiveDate == null ? "" : EmfDateFormat.format_MM_DD_YYYY(effectiveDate);
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

        Button cancel = new CancelButton(new AbstractAction() {
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
        record.setRefYrCostPerTon(new Float(refYrCostPerTon.getText()));
        saveLocale();
        saveRuleEffectiveness();
        saveRulePenetration();
        saveCapRecFactor();
        saveDiscountRate();
        record.setDetail(detail.getText().trim());
        saveEffectiveDate();
        record.setExistingMeasureAbbr(measureAbbreviation.getText().trim());
        record.setLastModifiedBy(session.user().getName());
        record.setLastModifiedTime(new Date());
        saveExistingDevCode();
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
            record.setEffectiveDate(EmfDateFormat.parse_MMddyyyy(date));
        } catch (Exception e) {
            throw new EmfException("Please Correct the Date Format(MM/dd/yyyy) in Effective Date");
        }
    }

    private void saveDiscountRate() throws EmfException {
        float value = verifier.parseFloat(discountRate);
        if (value < 0 || value > 20)
            throw new EmfException("Enter the Discount Rate as a percent between 0 and 20. Eg: 1 = 1%.  0.01 = 0.01%");
        record.setDiscountRate(value);
    }

    private void saveCapRecFactor() throws EmfException {
        record.setCapRecFactor(verifier.parseFloat(caprecFactor));
    }

    private void saveRulePenetration() throws EmfException {
        float value = verifier.parseFloat(rulePenetration);
        if (value <= 0 || value > 100)
            throw new EmfException(
                    "Enter the Rule Penetration as a percent between 0 and 100. Eg: 1 = 1%.  0.01 = 0.01%");
        record.setRulePenetration(value);
    }

    private void saveRuleEffectiveness() throws EmfException {
        float value = verifier.parseFloat(ruleEffectiveness);
        if (value <= 0 || value > 100)
            throw new EmfException(
                    "Enter the Rule Effectiveness as a percent between 0 and 100. Eg: 1 = 1%.  0.01 = 0.01%");
        record.setRuleEffectiveness(value);
    }

    private void saveLocale() throws EmfException {
        String localeText = locale.getText().trim();
        if (localeText.length() == 0) {
            record.setLocale("");
            return;
        }
        verifier.parseInteger(locale);
        if (localeText.length() == 2 || localeText.length() == 5 || localeText.length() == 6)
            record.setLocale(localeText);
        else
            throw new EmfException("Locale must be a two, five, or six digit integer.");
    }

    private void saveCostPerTon() throws EmfException {
        if (costperTon.getText().trim().length() == 0)
            throw new EmfException("Please set the Cost Per Ton");

        float value = verifier.parseFloat(costperTon);
        record.setCostPerTon(value);
    }

    private void saveCostYear() throws EmfException {
        YearValidation validation = new YearValidation("Cost Year");
        record.setCostYear(validation.value(costYear.getText()));
    }

    private void saveEfficiency(TextField efficiency) throws EmfException {
        if (efficiency.getText().length() == 0)
            throw new EmfException("Enter the Control Efficiency as a percentage (e.g., 90%, or -10% for a disbenefit)");

        float value = verifier.parseFloat(efficiency);
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