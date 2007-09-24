package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.services.cost.EquationTypeVariable;

public class Type8CostEquation implements CostEquation {

    private BestMeasureEffRecord bestMeasureEffRecord;
    private double discountRate;
    private Double stackFlowRate;
    private double reducedEmission;
    private Double capRecFactor;
    private Double capCostFactor;
    private Double operMaintCostFactor;
    private Double defaultCapitalCPTFactor;
    private Double defaultOperMaintCPTFactor;
    private Double defaultAnnualizedCPTFactor;
    private boolean hasAllVariables = true;
    
    public Type8CostEquation(double discountRate) {
        this.discountRate = discountRate / 100;     
    }

    public void setUp(double emissionReduction, BestMeasureEffRecord bestMeasureEffRecord, 
            Double stackFlowRate) {
        //define required inputs
        this.bestMeasureEffRecord = bestMeasureEffRecord;
        this.stackFlowRate = stackFlowRate;
        this.reducedEmission=emissionReduction;
        this.capRecFactor=getCapRecFactor();
        //populate variables for use with the equations
        populateVariables();
    }

    private void populateVariables() {
        ControlMeasureEquation[] equations = bestMeasureEffRecord.measure().getEquations();
        for (int i = 0; i < equations.length; i++) {
            EquationTypeVariable variable = equations[i].getEquationTypeVariable();
            if (variable != null) {
                String variableName = variable.getName();
                if (variableName.equalsIgnoreCase("Typical Capital Control Cost Factor")) {
                    capCostFactor = equations[i].getValue();
                } else if (variableName.equalsIgnoreCase("Typical O&M Control Cost Factor")) {
                    operMaintCostFactor = equations[i].getValue();
                } else if (variableName.equalsIgnoreCase("Typical Default CPT Factor - Capital")) {
                    defaultCapitalCPTFactor = equations[i].getValue();
                } else if (variableName.equalsIgnoreCase("Typical Default CPT Factor - O&M")) {
                    defaultOperMaintCPTFactor = equations[i].getValue();
                } else if (variableName.equalsIgnoreCase("Typical Default CPT Factor - Annualized")) {
                    defaultAnnualizedCPTFactor = equations[i].getValue();
                }
            }
        }
        if (capCostFactor == null
                || operMaintCostFactor == null
                || defaultCapitalCPTFactor == null
                || defaultOperMaintCPTFactor == null
                || defaultAnnualizedCPTFactor == null) 
            hasAllVariables = false;
    }

    public Double getAnnualCost() {
        Double capitalCost = getCapitalCost();
        if (!hasAllVariables
                || capitalCost == null 
                || getOperationMaintenanceCost() == null
                || capRecFactor == null) return null;
        //stack flow rate is greater than or equal 5 cfm
        if (stackFlowRate >= 5)
            return capitalCost * capRecFactor + 0.04 * capitalCost
                + getOperationMaintenanceCost();
        //stack flow rate is less than 5 cfm
        
        return defaultAnnualizedCPTFactor * reducedEmission;
    }

    public Double getCapitalCost() {
        if (!hasAllVariables
                || stackFlowRate == null 
                || stackFlowRate == 0.0) return null;
        //stack flow rate is greater than or equal 5 cfm
        if (stackFlowRate >= 5)
            return capCostFactor * stackFlowRate;
        //stack flow rate is less than 5 cfm
        return defaultCapitalCPTFactor * reducedEmission;
    }

    public Double getOperationMaintenanceCost() {
        if (!hasAllVariables
                || stackFlowRate == null 
                || stackFlowRate == 0.0) return null;
        //stack flow rate is greater than or equal 5 cfm
        if (stackFlowRate >= 5)
            return operMaintCostFactor * stackFlowRate;
        //stack flow rate is less than 5 cfm
        return defaultOperMaintCPTFactor * reducedEmission;
    }
    
    public Double getAnnualizedCapitalCost() { 
        Double capitalCost = getCapitalCost();
        if (capitalCost == null || capRecFactor == null) return null;
        return capitalCost * capRecFactor;
    }

    public Double getComputedCPT() {
        Double totalCost=getAnnualCost();
        if (totalCost==null || reducedEmission == 0.0) return null; 
        return totalCost/reducedEmission;
    }
    public Double getCapRecFactor() {
        // Calculate capital recovery factor
        return getCapRecFactor(bestMeasureEffRecord.measure().getEquipmentLife(), 
                bestMeasureEffRecord.efficiencyRecord().getCapRecFactor());
        
    }

    public Double getCapRecFactor(float equipmentLife, Double effRecCapRecFactor) {
        // Calculate capital recovery factor 
        Double capRecFactor = effRecCapRecFactor;
        if (equipmentLife!=0) 
             capRecFactor = DefaultCostEquation.calculateCapRecFactor(discountRate, equipmentLife);
        
        if (capRecFactor != null && capRecFactor != 0) {
            return capRecFactor; 
        }
        return null;
    }
    
//    public String toString(){
//        return "Typical Capital Control Cost Factor: " + capCostFactor +
//        " Annual cost: " + getAnnualCost() + "  "
//        + defaultCapitalCPTFactor +" "+defaultOperMaintCPTFactor+ " "
//        + defaultAnnualizedCPTFactor+" "+ hasAllVariables;
//    }
}