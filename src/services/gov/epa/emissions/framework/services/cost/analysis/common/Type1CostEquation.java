package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.services.cost.EquationTypeVariable;

public class Type1CostEquation implements CostEquation {

    private BestMeasureEffRecord bestMeasureEffRecord;
    private double discountRate;
    private Double boilerCapacity;
    private double reducedEmission;
    private Double capRecFactor;
    private Double capCostMultiplier;
    private Double omCostMultiFixed;
    private Double omCostMultiVariable;
    private Double scalingFactorSize;
    private Double scalingFactorExponent;
    private Double capacityFactor;
//    private Double incCapCostMultiplier;
//    private Double incCapCostExponent;
//    private Double incAnnCostMultiplier;
//    private Double incAnnCostExponent;
    
    public Type1CostEquation(double discountRate) {
        this.discountRate = discountRate / 100;     
    }

    public void setUp(double emissionReduction, BestMeasureEffRecord bestMeasureEffRecord, Double boilerCapacity) {
        //define required inputs
        this.bestMeasureEffRecord = bestMeasureEffRecord;
        this.boilerCapacity = boilerCapacity;
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
                if (variableName.equalsIgnoreCase("Capital Cost Multiplier")) {
                    capCostMultiplier = equations[i].getValue();
                } else if (variableName.equalsIgnoreCase("Fixed O&M Cost Multiplier")) {
                    omCostMultiFixed = equations[i].getValue();
                } else if (variableName.equalsIgnoreCase("Variable O&M Cost Multiplier")) {
                    omCostMultiVariable = equations[i].getValue();
                } else if (variableName.equalsIgnoreCase("Scaling Factor - Model Size (MW)")) {
                    scalingFactorSize = equations[i].getValue();
                } else if (variableName.equalsIgnoreCase("Scaling Factor - Exponent")) {
                    scalingFactorExponent = equations[i].getValue();
                } else if (variableName.equalsIgnoreCase("Capacity Factor")) {
                    capacityFactor = equations[i].getValue();
//                } else if (variableName.equalsIgnoreCase("Incremental Capital Cost Multiplier")) {
//                    incCapCostMultiplier = equations[i].getValue();
//                } else if (variableName.equalsIgnoreCase("Incremental Capital Cost Exponent")) {
//                    incCapCostExponent = equations[i].getValue();
//                } else if (variableName.equalsIgnoreCase("Incremental Annual Cost Multiplier")) {
//                    incAnnCostMultiplier = equations[i].getValue();
//                } else if (variableName.equalsIgnoreCase("Incremental Annual Cost Exponent")) {
//                    incAnnCostExponent = equations[i].getValue();
                }
            }
        }
    }

    public Double getAnnualCost() {
        if (boilerCapacity == null 
                || capCostMultiplier == 0.0 || capCostMultiplier== null
                || omCostMultiFixed == 0.0 || omCostMultiFixed ==null
                || omCostMultiVariable == 0.0 || omCostMultiVariable == null
                || scalingFactorExponent == null) return null;
        return getAnnualizedCapitalCost()+ getOperationMaintenanceCost();
    }

    public Double getCapitalCost() {
        Double scallFactor=getScallingFactor();
        if (boilerCapacity == null 
                || capCostMultiplier == 0.0 || capCostMultiplier== null
                || scallFactor==0.0         || scallFactor== null) return null;
        return capCostMultiplier * boilerCapacity*scallFactor*1000;
    }

    public Double getScallingFactor() {
        String abbre=bestMeasureEffRecord.measure().getAbbreviation();
        if ((abbre=="NSCR_UBCW" ||abbre =="NSCR_UBCT") && boilerCapacity>=600)  return 1.0; 
        if (boilerCapacity>=500) return 1.0; 
        if (scalingFactorSize==0.0 ||scalingFactorSize==null
                || scalingFactorExponent==0.0 ||scalingFactorExponent==null)
            return null;
        return Math.pow(scalingFactorSize, scalingFactorExponent);
    }

    public Double getOperationMaintenanceCost() {
        Double omCostFixed=getOperationMaintenanceCostFixed();
        Double omCostvariable=getOperationMaintenanceCostVariable();
        if (omCostFixed == null 
                || omCostvariable == null) return null;
        return omCostFixed+omCostvariable;
    }
    
    public Double getOperationMaintenanceCostFixed(){
        if (boilerCapacity == null 
        || omCostMultiFixed == 0.0 || omCostMultiFixed== null) return null;
        return omCostMultiFixed*boilerCapacity*1000;
    }
    
    public Double getOperationMaintenanceCostVariable(){
        if (boilerCapacity == null 
        || capacityFactor == 0.0 || capacityFactor== null
        || omCostMultiVariable == 0.0 || omCostMultiVariable== null) return null;
        return omCostMultiVariable*boilerCapacity*1000*capacityFactor*8760/1000 ;
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
}