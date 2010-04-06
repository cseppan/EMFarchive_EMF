package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyConstraint;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.EfficiencyRecordValidation;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditControlStrategyConstraintsTab extends JPanel implements ControlStrategyConstraintsTabView {

    private TextField emisReduction;
    private TextField contrlEff;
    private TextField costPerTon;
    private TextField annCost;
    private TextField domainWideEmisReduction;
    private TextField domainWidePctReduction;
    private TextField domainWidePctReductionIncrement;
    private TextField domainWidePctReductionStart;
    private TextField domainWidePctReductionEnd;
    private TextField replacementControlMinEfficiencyDiff;
    private TextField controlProgramMeasureMinPctRedDiff;
    
    private ManageChangeables changeablesList;

    private EditControlStrategyConstraintsTabPresenter presenter;
    
    private ControlStrategy controlStrategy;
    
    private JPanel leastCostPanel;

    private JPanel leastCostCurvePanel;

    private JPanel controlProgramPanel;
    
    private JPanel leastCostPanelContainer;
    private DecimalFormat decFormat;

    public EditControlStrategyConstraintsTab(ControlStrategy controlStrategy, ManageChangeables changeablesList,
            SingleLineMessagePanel messagePanel, EmfConsole parentConsole, EmfSession session) {
        this.changeablesList = changeablesList;
        this.controlStrategy = controlStrategy;
        this.decFormat = new DecimalFormat("###0.0#");
    }
    
    private void setupLayout(ManageChangeables changeables) {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5)); 
        this.add(getBorderedPanel(createAllStrategiesPanel(changeables), "All Strategy Types"), BorderLayout.NORTH);
        leastCostPanelContainer = new JPanel(new BorderLayout());
        leastCostPanel = getBorderedPanel(createLeastCostPanel(changeables), "Least Cost");
        leastCostCurvePanel = getBorderedPanel(createLeastCostCurvePanel(changeables), "Least Cost Curve");
        controlProgramPanel = getBorderedPanel(createControlProgramPanel(changeables), "Project Future Year Inventory");
        if (controlStrategy.getStrategyType() != null) {
            if (controlStrategy.getStrategyType().getName().equals(StrategyType.leastCost)) {
                leastCostPanelContainer.add(leastCostPanel, BorderLayout.NORTH);
            } else if (controlStrategy.getStrategyType().getName().equals(StrategyType.leastCostCurve)) {
                leastCostPanelContainer.add(leastCostCurvePanel, BorderLayout.NORTH);
            } else if (controlStrategy.getStrategyType().getName().equals(StrategyType.projectFutureYearInventory)) {
                leastCostPanelContainer.add(controlProgramPanel, BorderLayout.NORTH);
            }
        }
        this.add(leastCostPanelContainer, BorderLayout.CENTER);
        notifyStrategyTypeChange(controlStrategy.getStrategyType());
    }

    private JPanel getBorderedPanel(JPanel component, String border) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new Border(border));
        panel.add(component);

        return panel;
    }

    private JPanel createAllStrategiesPanel(ManageChangeables changeables) {
        ControlStrategyConstraint constraint = presenter.getConstraint();
        JPanel panel = new JPanel(new SpringLayout());
   //     panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); //100,80));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Constraints for Target Pollutant:", new JLabel(), panel);

        emisReduction = new TextField("emission reduction", 10);
        emisReduction.setText(constraint != null ? (constraint.getMaxEmisReduction() != null ? constraint.getMaxEmisReduction() + "" : "") : "");
        emisReduction.setToolTipText("Enter the sources target pollutant minimum emission reduction (in tons).  The control measure must be able to control greater than or equal to this tonnage.");
        changeables.addChangeable(emisReduction);
        layoutGenerator.addLabelWidgetPair("Minimum Emissions Reduction (tons)", emisReduction, panel);

        contrlEff = new TextField("control efficiency", 10);
        contrlEff.setText(constraint != null ? (constraint.getMaxControlEfficiency() != null ? constraint.getMaxControlEfficiency() + "" : "") : "");
        contrlEff.setToolTipText("Enter the sources target pollutant minimum control efficiency (%).  The control measure must be have a control efficiency greater than or equal to this perecentage.");
        changeables.addChangeable(contrlEff);
        layoutGenerator.addLabelWidgetPair("Minimum Control Efficiency (%)", contrlEff, panel);

        costPerTon = new TextField("cost per ton", 10);
        costPerTon.setText(constraint != null ? (constraint.getMinCostPerTon() != null ? constraint.getMinCostPerTon() + "" : "") : "");
        costPerTon.setToolTipText("Enter the sources target pollutant maximum cost per ton.  The control measure must be have a cost per ton less than or equal to this cost per ton.");
        changeables.addChangeable(costPerTon);
        layoutGenerator.addLabelWidgetPair("Maximum " + CostYearTable.REFERENCE_COST_YEAR + " Cost per Ton ($/ton)", costPerTon, panel);

        annCost = new TextField("annual cost", 10);
        annCost.setText(constraint != null ? (constraint.getMinAnnCost() != null ? constraint.getMinAnnCost() + "" : "") : "");
        annCost.setToolTipText("Enter the sources target pollutant maximum annualized cost.  The controlled source must have a annualized cost less than or equal to this cost.");
        changeables.addChangeable(annCost);
        layoutGenerator.addLabelWidgetPair("Maximum " + CostYearTable.REFERENCE_COST_YEAR + " Annualized Cost ($/yr)", annCost, panel);
        
        replacementControlMinEfficiencyDiff = new TextField("replacementControlMinEfficiencyDiff", 10);
        replacementControlMinEfficiencyDiff.setText(constraint != null ? (constraint.getReplacementControlMinEfficiencyDiff() != null ? constraint.getReplacementControlMinEfficiencyDiff() + "" : "10.0") : "10.0");
        replacementControlMinEfficiencyDiff.setToolTipText("Enter the minimum control percent reduction difference to use for replacement controls.");
        changeables.addChangeable(replacementControlMinEfficiencyDiff);
        layoutGenerator.addLabelWidgetPair("Minimum Percent Reduction Difference for Replacement Control (%)", replacementControlMinEfficiencyDiff, panel);
        

        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10); // xPad, yPad

        return panel;
    }

    private JPanel createControlProgramPanel(ManageChangeables changeables) {
        ControlStrategyConstraint constraint = presenter.getConstraint();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5,10,10,10)); 

        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        controlProgramMeasureMinPctRedDiff = new TextField("domain wide percent reduction", 10);
        controlProgramMeasureMinPctRedDiff.setText(constraint != null ? (constraint.getControlProgramMeasureMinPctRedDiff() != null ? constraint.getControlProgramMeasureMinPctRedDiff() + "" : "10") : "10");
        controlProgramMeasureMinPctRedDiff.setToolTipText("Enter the minimum control percent reduction difference for predicting controls.");
        changeables.addChangeable(controlProgramMeasureMinPctRedDiff);
        layoutGenerator.addLabelWidgetPair("Minimum Percent Reduction Difference for Predicting Controls (%)", controlProgramMeasureMinPctRedDiff, panel);

        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10); // xPad, yPad

        mainPanel.add(panel,BorderLayout.SOUTH);
        return mainPanel;
    }

    private JPanel createLeastCostPanel(ManageChangeables changeables) {
        ControlStrategyConstraint constraint = presenter.getConstraint();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5,10,10,10)); 
        mainPanel.add(new JLabel("Specify EITHER an emission reduction (tons) or percent reduction (%) for the Target Pollutant:"),
           BorderLayout.WEST);

        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        domainWideEmisReduction = new TextField("domain wide emission reduction", 10);
        domainWideEmisReduction.setText(constraint != null ? (constraint.getDomainWideEmisReduction() != null ? decFormat.format(constraint.getDomainWideEmisReduction()) + "" : "") : "");
        domainWideEmisReduction.setToolTipText("Enter the target domain wide emission reduction (in tons for the target pollutant).");
        changeables.addChangeable(domainWideEmisReduction);
        layoutGenerator.addLabelWidgetPair("Domain Wide Emission Reduction (tons)", domainWideEmisReduction, panel);

        domainWidePctReduction = new TextField("domain wide percent reduction", 10);
        domainWidePctReduction.setText(constraint != null ? (constraint.getDomainWidePctReduction() != null ? decFormat.format(constraint.getDomainWidePctReduction()) + "" : "") : "");
        domainWidePctReduction.setToolTipText("Enter the target domain wide precent reduction (% for the target pollutant).");
        changeables.addChangeable(domainWidePctReduction);
        layoutGenerator.addLabelWidgetPair("Domain Wide Percent Reduction (%)", domainWidePctReduction, panel);

        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10); // xPad, yPad

        mainPanel.add(panel,BorderLayout.SOUTH);
        return mainPanel;
    }

    private JPanel createLeastCostCurvePanel(ManageChangeables changeables) {
        ControlStrategyConstraint constraint = presenter.getConstraint();
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
       //layoutGenerator.addLabelWidgetPair("Constraints for Target Pollutant:", new JLabel(), panel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5,10,10,10)); 
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Specify an increment, starting and ending percent reduction (%) for the Target Pollutant:"),
                BorderLayout.WEST);
        mainPanel.add(topPanel,BorderLayout.NORTH);

        JPanel panel = new JPanel(new SpringLayout());
        domainWidePctReductionIncrement = new TextField("domain wide percent reduction increment", 10);
        domainWidePctReductionIncrement.setText(constraint != null ? (constraint.getDomainWidePctReductionIncrement() != null ? constraint.getDomainWidePctReductionIncrement() + "" : "25") : "25");
        domainWidePctReductionIncrement.setToolTipText("Enter the domain wide percent reduction increment.");
        changeables.addChangeable(domainWidePctReductionIncrement);
        layoutGenerator.addLabelWidgetPair("   Domain-wide Percent Reduction Increment (%)                                ", domainWidePctReductionIncrement, panel);

        domainWidePctReductionStart = new TextField("domain wide percent reduction start", 10);
        domainWidePctReductionStart.setText(constraint != null ? (constraint.getDomainWidePctReductionStart() != null ? constraint.getDomainWidePctReductionStart() + "" : "0") : "0");
        domainWidePctReductionStart.setToolTipText("Enter the domain wide percent reduction start precentage.");
        changeables.addChangeable(domainWidePctReductionStart);
        layoutGenerator.addLabelWidgetPair("   Domain-wide Percent Reduction Start (%)", domainWidePctReductionStart, panel);

        domainWidePctReductionEnd = new TextField("domain wide percent reduction end", 10);
        domainWidePctReductionEnd.setText(constraint != null ? (constraint.getDomainWidePctReductionEnd() != null ? constraint.getDomainWidePctReductionEnd() + "" : "100") : "100");
        domainWidePctReductionEnd.setToolTipText("Enter the domain wide percent reduction end precentage.");
        changeables.addChangeable(domainWidePctReductionEnd);
        layoutGenerator.addLabelWidgetPair("   Domain-wide Percent Reduction End (%)", domainWidePctReductionEnd, panel);

        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10); // xPad, yPad
        
        mainPanel.add(panel,BorderLayout.CENTER);

        return mainPanel;
    }

    public void run(ControlStrategy controlStrategy) throws EmfException {
        ControlStrategyConstraint constraint = null;
        constraint = new ControlStrategyConstraint();
        constraint.setControlStrategyId(controlStrategy.getId());
        EfficiencyRecordValidation erValidation = new EfficiencyRecordValidation();
        //make sure constraints are in the correct numerical format... validation will happen in the run method.
        if (contrlEff.getText().trim().length() > 0) constraint.setMaxControlEfficiency(erValidation.parseDouble("maximum control efficieny", contrlEff.getText()));
        if (emisReduction.getText().trim().length() > 0) constraint.setMaxEmisReduction(erValidation.parseDouble("maximum emission reduction", emisReduction.getText()));
        if (costPerTon.getText().trim().length() > 0) constraint.setMinCostPerTon(erValidation.parseDouble("minimum cost per ton", costPerTon.getText()));
        if (annCost.getText().trim().length() > 0) constraint.setMinAnnCost(erValidation.parseDouble("minimum annualized cost", annCost.getText()));
        if (domainWideEmisReduction.getText().trim().length() > 0) constraint.setDomainWideEmisReduction(erValidation.parseDouble("domain wide emission reduction", domainWideEmisReduction.getText()));
        if (domainWidePctReduction.getText().trim().length() > 0) constraint.setDomainWidePctReduction(erValidation.parseDouble("domain wide percent reduction", domainWidePctReduction.getText()));
        if (domainWidePctReductionIncrement.getText().trim().length() > 0) constraint.setDomainWidePctReductionIncrement(erValidation.parseDouble("domain wide percent reduction increment", domainWidePctReductionIncrement.getText()));
        if (domainWidePctReductionStart.getText().trim().length() > 0) constraint.setDomainWidePctReductionStart(erValidation.parseDouble("domain wide percent reduction start", domainWidePctReductionStart.getText()));
        if (domainWidePctReductionEnd.getText().trim().length() > 0) constraint.setDomainWidePctReductionEnd(erValidation.parseDouble("domain wide percent reduction end", domainWidePctReductionEnd.getText()));
        if (replacementControlMinEfficiencyDiff.getText().trim().length() > 0) constraint.setReplacementControlMinEfficiencyDiff(erValidation.parseDouble("replacement control minimum control efficiency difference", replacementControlMinEfficiencyDiff.getText()));
        if (controlProgramMeasureMinPctRedDiff.getText().trim().length() > 0) constraint.setControlProgramMeasureMinPctRedDiff(erValidation.parseDouble("control program minimum percent reduction difference", controlProgramMeasureMinPctRedDiff.getText()));
        if (controlStrategy.getStrategyType().getName().equalsIgnoreCase(StrategyType.leastCost)) {
            //make sure that either Emis OR Pct Reduction was specified for the Least Cost.  This is needed for the run.
            if (constraint.getReplacementControlMinEfficiencyDiff() == null || constraint.getReplacementControlMinEfficiencyDiff() <= 0.0D) 
                throw new EmfException("Constraints Tab: The replacement control minimum control efficiencny difference is missing.");
            if (constraint.getDomainWideEmisReduction() == null && constraint.getDomainWidePctReduction() == null) 
                throw new EmfException("Constraints Tab: Please specify either an emission reduction or percent reduction.");
//            if (constraint.getDomainWideEmisReduction() != null && constraint.getDomainWidePctReduction() != null) 
//                throw new EmfException("Constraints Tab: Specify only an emission reduction or a percent reduction.");
        }
        if (controlStrategy.getStrategyType().getName().equalsIgnoreCase(StrategyType.leastCostCurve)) {
            //make sure that the Pct Reduction Increment was specified for the Least Cost Curve.  This is needed for the run.
            if (constraint.getReplacementControlMinEfficiencyDiff() == null || constraint.getReplacementControlMinEfficiencyDiff() <= 0.0D) 
                throw new EmfException("Constraints Tab: The replacement control minimum control efficiencny difference is missing.");
            if (constraint.getDomainWidePctReductionIncrement() == null || constraint.getDomainWidePctReductionIncrement() <= 0.0D) 
                throw new EmfException("Constraints Tab: The percent reduction increment is missing.");
            if (constraint.getDomainWidePctReductionStart() == null) 
                throw new EmfException("Constraints Tab: The percent reduction starting percentage is missing.");
            if (constraint.getDomainWidePctReductionEnd() == null) 
                throw new EmfException("Constraints Tab: The percent reduction ending percentage is missing.");
        }
        if (controlStrategy.getStrategyType().getName().equalsIgnoreCase(StrategyType.maxEmissionsReduction)) {
            if (constraint.getReplacementControlMinEfficiencyDiff() == null || constraint.getReplacementControlMinEfficiencyDiff() <= 0.0D) 
                throw new EmfException("Constraints Tab: The replacement control minimum control efficiencny difference is missing.");
        }
        if (controlStrategy.getStrategyType().getName().equalsIgnoreCase(StrategyType.projectFutureYearInventory)) {
            if (constraint.getControlProgramMeasureMinPctRedDiff() == null || constraint.getControlProgramMeasureMinPctRedDiff() <= 0.0D) 
                throw new EmfException("Constraints Tab: The measure minimum percent reduction difference for predicting controls is missing.");
        }
    }
    
    public void save(ControlStrategy controlStrategy) throws EmfException {
        ControlStrategyConstraint constraint = null;
        constraint = new ControlStrategyConstraint();
        constraint.setControlStrategyId(controlStrategy.getId());
        EfficiencyRecordValidation erValidation = new EfficiencyRecordValidation();
        //make sure constraints are in the correct numerical format... validation will happen in the run method.
        if (contrlEff.getText().trim().length() > 0) constraint.setMaxControlEfficiency(erValidation.parseDouble("maximum control efficieny", contrlEff.getText()));
        if (emisReduction.getText().trim().length() > 0) constraint.setMaxEmisReduction(erValidation.parseDouble("maximum emission reduction", emisReduction.getText()));
        if (costPerTon.getText().trim().length() > 0) constraint.setMinCostPerTon(erValidation.parseDouble("minimum cost per ton", costPerTon.getText()));
        if (annCost.getText().trim().length() > 0) constraint.setMinAnnCost(erValidation.parseDouble("minimum annualized cost", annCost.getText()));
        if (domainWideEmisReduction.getText().trim().length() > 0) constraint.setDomainWideEmisReduction(erValidation.parseDouble("domain wide emission reduction", domainWideEmisReduction.getText()));
        if (domainWidePctReduction.getText().trim().length() > 0) constraint.setDomainWidePctReduction(erValidation.parseDouble("domain wide percent reduction", domainWidePctReduction.getText()));
        if (domainWidePctReductionIncrement.getText().trim().length() > 0) constraint.setDomainWidePctReductionIncrement(erValidation.parseDouble("domain wide percent reduction increment", domainWidePctReductionIncrement.getText()));
        if (domainWidePctReductionStart.getText().trim().length() > 0) constraint.setDomainWidePctReductionStart(erValidation.parseDouble("domain wide percent reduction start", domainWidePctReductionStart.getText()));
        if (domainWidePctReductionEnd.getText().trim().length() > 0) constraint.setDomainWidePctReductionEnd(erValidation.parseDouble("domain wide percent reduction end", domainWidePctReductionEnd.getText()));
        if (replacementControlMinEfficiencyDiff.getText().trim().length() > 0) constraint.setReplacementControlMinEfficiencyDiff(erValidation.parseDouble("replacement control minimum control efficiency difference", replacementControlMinEfficiencyDiff.getText()));
        if (controlProgramMeasureMinPctRedDiff.getText().trim().length() > 0) constraint.setControlProgramMeasureMinPctRedDiff(erValidation.parseDouble("control program minimum percent reduction difference", controlProgramMeasureMinPctRedDiff.getText()));
//        if (controlStrategy.getStrategyType().getName().equalsIgnoreCase(StrategyType.leastCost)) {
//            //make sure that either Emis OR Pct Reduction was specified for the Least Cost.  This is needed for the run.
//            if (constraint.getReplacementControlMinEfficiencyDiff() == null || constraint.getReplacementControlMinEfficiencyDiff() <= 0.0D) 
//                throw new EmfException("Constraints Tab: The replacement control minimum control efficiencny difference is missing.");
//            if (constraint.getDomainWideEmisReduction() == null && constraint.getDomainWidePctReduction() == null) 
//                throw new EmfException("Constraints Tab: Please specify either an emission reduction or percent reduction.");
//            if (constraint.getDomainWideEmisReduction() != null && constraint.getDomainWidePctReduction() != null) 
//                throw new EmfException("Constraints Tab: Specify only an emission reduction or a percent reduction.");
//        }
//        if (controlStrategy.getStrategyType().getName().equalsIgnoreCase(StrategyType.leastCostCurve)) {
//            //make sure that the Pct Reduction Increment was specified for the Least Cost Curve.  This is needed for the run.
//            if (constraint.getReplacementControlMinEfficiencyDiff() == null || constraint.getReplacementControlMinEfficiencyDiff() <= 0.0D) 
//                throw new EmfException("Constraints Tab: The replacement control minimum control efficiencny difference is missing.");
//            if (constraint.getDomainWidePctReductionIncrement() == null || constraint.getDomainWidePctReductionIncrement() <= 0.0D) 
//                throw new EmfException("Constraints Tab: The percent reduction increment is missing.");
//            if (constraint.getDomainWidePctReductionStart() == null) 
//                throw new EmfException("Constraints Tab: The percent reduction starting percentage is missing.");
//            if (constraint.getDomainWidePctReductionEnd() == null) 
//                throw new EmfException("Constraints Tab: The percent reduction ending percentage is missing.");
//        }
//        if (controlStrategy.getStrategyType().getName().equalsIgnoreCase(StrategyType.maxEmissionsReduction)) {
//            if (constraint.getReplacementControlMinEfficiencyDiff() == null || constraint.getReplacementControlMinEfficiencyDiff() <= 0.0D) 
//                throw new EmfException("Constraints Tab: The replacement control minimum control efficiencny difference is missing.");
//        }
//        if (controlStrategy.getStrategyType().getName().equalsIgnoreCase(StrategyType.projectFutureYearInventory)) {
//            if (constraint.getControlProgramMeasureMinPctRedDiff() == null || constraint.getControlProgramMeasureMinPctRedDiff() <= 0.0D) 
//                throw new EmfException("Constraints Tab: The measure minimum percent reduction difference for predicting controls is missing.");
//        }
        presenter.setConstraint(constraint);
    }

    public void refresh(ControlStrategy strategy, ControlStrategyResult[] controlStrategyResults) {
        ControlStrategyConstraint constraint = strategy.getConstraint();
        if (constraint != null) {
            domainWideEmisReduction.setText(constraint.getDomainWideEmisReduction() != null ? decFormat.format(constraint.getDomainWideEmisReduction()) + "" : "");
            domainWidePctReduction.setText(constraint.getDomainWidePctReduction() != null ? decFormat.format(constraint.getDomainWidePctReduction()) + "" : "");
        }
    }

    public void display(ControlStrategy strategy) {
        setupLayout(changeablesList);
    }

    public void observe(EditControlStrategyConstraintsTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void notifyStrategyTypeChange(StrategyType strategyType) {
        leastCostPanelContainer.removeAll();
        if (strategyType != null) {
            if (strategyType.getName().equals(StrategyType.leastCost)) {
                leastCostPanelContainer.add(leastCostPanel, BorderLayout.NORTH);
                emisReduction.setEnabled(true);
                contrlEff.setEnabled(true);
                costPerTon.setEnabled(true);
                annCost.setEnabled(true);
                replacementControlMinEfficiencyDiff.setEnabled(true);
            } else if (strategyType.getName().equals(StrategyType.leastCostCurve)) {
                leastCostPanelContainer.add(leastCostCurvePanel, BorderLayout.NORTH);
                emisReduction.setEnabled(true);
                contrlEff.setEnabled(true);
                costPerTon.setEnabled(true);
                annCost.setEnabled(true);
                replacementControlMinEfficiencyDiff.setEnabled(true);
            } else if (strategyType.getName().equals(StrategyType.maxEmissionsReduction)) {
                emisReduction.setEnabled(true);
                contrlEff.setEnabled(true);
                costPerTon.setEnabled(true);
                annCost.setEnabled(true);
                replacementControlMinEfficiencyDiff.setEnabled(true);
            } else if (strategyType.getName().equals(StrategyType.projectFutureYearInventory)) {
                leastCostPanelContainer.add(controlProgramPanel, BorderLayout.NORTH);
                emisReduction.setEnabled(false);
                contrlEff.setEnabled(false);
                costPerTon.setEnabled(false);
                annCost.setEnabled(false);
                replacementControlMinEfficiencyDiff.setEnabled(false);
            } else {
                emisReduction.setEnabled(true);
                contrlEff.setEnabled(true);
                costPerTon.setEnabled(true);
                annCost.setEnabled(true);
                replacementControlMinEfficiencyDiff.setEnabled(false);
            }
        } else {
            replacementControlMinEfficiencyDiff.setEnabled(false);
            emisReduction.setEnabled(true);
            contrlEff.setEnabled(true);
            costPerTon.setEnabled(true);
            annCost.setEnabled(true);
        }
    }

    public void notifyStrategyRun(ControlStrategy controlStrategy) {
        // NOTE Auto-generated method stub
        
    }
}
