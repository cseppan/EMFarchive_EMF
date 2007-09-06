package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.services.cost.EquationTypeVariable;

public class Type2CostEquation implements CostEquation {

    private BestMeasureEffRecord bestMeasureEffRecord;
    private double discountRate;
    private Double boilerCapacity;
    private double reducedEmission;
    private Double capRecFactor;
    private Double capCostMultiplier;
    private Double capCostExponent;
    private Double annCostMultiplier;
    private Double annCostExponent;
//    private Double incCapCostMultiplier;
//    private Double incCapCostExponent;
//    private Double incAnnCostMultiplier;
//    private Double incAnnCostExponent;
    
    public Type2CostEquation(double discountRate) {
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
                } else if (variableName.equalsIgnoreCase("Capital Cost Exponent")) {
                    capCostExponent = equations[i].getValue();
                } else if (variableName.equalsIgnoreCase("Annual Cost Multiplier")) {
                    annCostMultiplier = equations[i].getValue();
                } else if (variableName.equalsIgnoreCase("Annual Cost Exponent")) {
                    annCostExponent = equations[i].getValue();
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
                || boilerCapacity == 0.0
                || annCostMultiplier == null
                || annCostMultiplier == 0.0
                || annCostExponent == null
                || annCostExponent == 0.0) return null;
        return annCostMultiplier * Math.pow(boilerCapacity, annCostExponent);
    }

    public Double getCapitalCost() {
        if (boilerCapacity == null 
                || boilerCapacity == 0.0
                || capCostMultiplier == null
                || capCostMultiplier == 0.0
                || capCostExponent == null
                || capCostExponent == 0.0) return null;
        return capCostMultiplier * Math.pow(boilerCapacity, capCostExponent);
    }

    public Double getOperationMaintenanceCost() {
        if (getAnnualCost() == null 
                || getCapitalCost() == null
                || capRecFactor == null) return null;
        return getAnnualCost() - (getCapitalCost() * capRecFactor);
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