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
import gov.epa.emissions.framework.services.cost.controlmeasure.EfficiencyRecordValidation;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;

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

    private ManageChangeables changeablesList;

    private EditControlStrategyConstraintsTabPresenter presenter;
    
    public EditControlStrategyConstraintsTab(ControlStrategy controlStrategy, ManageChangeables changeablesList,
            SingleLineMessagePanel messagePanel, EmfConsole parentConsole, EmfSession session) {
        this.changeablesList = changeablesList;
    }
    
    private void setupLayout(ManageChangeables changeables) {
        this.setLayout(new BorderLayout());
        this.add(createConstraintPanel(changeables), BorderLayout.CENTER);
    }

    private JPanel createConstraintPanel(ManageChangeables changeables) {
        ControlStrategyConstraint constraint = presenter.getConstraint();
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(80,80,100,80));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Constraints for Target Pollutant:", new JLabel(), panel);

        emisReduction = new TextField("emission reduction", 10);
        emisReduction.setText(constraint != null ? (constraint.getMaxEmisReduction() != null ? constraint.getMaxEmisReduction() + "" : "") : "");
        changeables.addChangeable(emisReduction);
        layoutGenerator.addLabelWidgetPair("Minimum Emissions Reduction (tons)", emisReduction, panel);

        contrlEff = new TextField("control efficiency", 10);
        contrlEff.setText(constraint != null ? (constraint.getMaxControlEfficiency() != null ? constraint.getMaxControlEfficiency() + "" : "") : "");
        changeables.addChangeable(contrlEff);
        layoutGenerator.addLabelWidgetPair("Minimum Control Efficiency (%)", contrlEff, panel);

        costPerTon = new TextField("cost per ton", 10);
        costPerTon.setText(constraint != null ? (constraint.getMinCostPerTon() != null ? constraint.getMinCostPerTon() + "" : "") : "");
        changeables.addChangeable(costPerTon);
        layoutGenerator.addLabelWidgetPair("Maximum 2006 Cost per Ton ($/ton)", costPerTon, panel);

        annCost = new TextField("annual cost", 10);
        annCost.setText(constraint != null ? (constraint.getMinAnnCost() != null ? constraint.getMinAnnCost() + "" : "") : "");
        changeables.addChangeable(annCost);
        layoutGenerator.addLabelWidgetPair("Maximum 2006 Annualized Cost ($/yr)", annCost, panel);

        domainWideEmisReduction = new TextField("domain wide emission reduction", 10);
        domainWideEmisReduction.setText(constraint != null ? (constraint.getDomainWideEmisReduction() != null ? constraint.getDomainWideEmisReduction() + "" : "") : "");
        changeables.addChangeable(domainWideEmisReduction);
        layoutGenerator.addLabelWidgetPair("Domain Wide Emission Reduction (ton)", domainWideEmisReduction, panel);

        domainWidePctReduction = new TextField("domain wide percent reduction", 10);
        domainWidePctReduction.setText(constraint != null ? (constraint.getDomainWidePctReduction() != null ? constraint.getDomainWidePctReduction() + "" : "") : "");
        changeables.addChangeable(domainWidePctReduction);
        layoutGenerator.addLabelWidgetPair("Domain Wide Percent Reduction (%)", domainWidePctReduction, panel);

        layoutGenerator.makeCompactGrid(panel, 7, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 20); // xPad, yPad

        return panel;
    }

    public void save(ControlStrategy controlStrategy) throws EmfException {
        ControlStrategyConstraint constraint = null;
        constraint = new ControlStrategyConstraint();
        constraint.setControlStrategyId(controlStrategy.getId());
        EfficiencyRecordValidation erValidation = new EfficiencyRecordValidation();
        if (contrlEff.getText().trim().length() > 0) constraint.setMaxControlEfficiency(erValidation.parseDouble("maximum control efficieny", contrlEff.getText()));
        if (emisReduction.getText().trim().length() > 0) constraint.setMaxEmisReduction(erValidation.parseDouble("maximum emission reduction", emisReduction.getText()));
        if (costPerTon.getText().trim().length() > 0) constraint.setMinCostPerTon(erValidation.parseDouble("minimum cost per ton", costPerTon.getText()));
        if (annCost.getText().trim().length() > 0) constraint.setMinAnnCost(erValidation.parseDouble("minimum annualized cost", annCost.getText()));
        if (domainWideEmisReduction.getText().trim().length() > 0) constraint.setDomainWideEmisReduction(erValidation.parseDouble("domain wide emission reduction", domainWideEmisReduction.getText()));
        if (domainWidePctReduction.getText().trim().length() > 0) constraint.setDomainWidePctReduction(erValidation.parseDouble("domain wide percent reduction", domainWidePctReduction.getText()));
        if (controlStrategy.getStrategyType().getName().equalsIgnoreCase(StrategyType.leastCost)) {
            //make sure that either Emis OR Pct Reduction was specified for the Least Cost.  This is needed for the run.
            if (constraint.getDomainWideEmisReduction() == null && constraint.getDomainWidePctReduction() == null) 
                throw new EmfException("Please specify either an emission reduction or percent reduction for the Least Cost strategy type.");
        }
        presenter.setConstraint(constraint);
    }

    public void refresh(ControlStrategyResult[] controlStrategyResults) {
        // NOTE Auto-generated method stub

    }

    public void display(ControlStrategy strategy) {
        setupLayout(changeablesList);
    }

    public void observe(EditControlStrategyConstraintsTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void notifyStrategyTypeChange(StrategyType strategyType) {
        if (strategyType != null && strategyType.getName().equals(StrategyType.leastCost)) {
            domainWideEmisReduction.setEnabled(true);
            domainWidePctReduction.setEnabled(true);
        }
        else {
            domainWideEmisReduction.setEnabled(false);
            domainWidePctReduction.setEnabled(false);
        }
    }

}
