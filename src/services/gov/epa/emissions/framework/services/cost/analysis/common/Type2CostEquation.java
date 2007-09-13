package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.services.cost.EquationTypeVariable;

public class Type2CostEquation implements CostEquation {

    private BestMeasureEffRecord bestMeasureEffRecord;
    private double discountRate;
    private Double designCapacity;
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
    private boolean hasAllVariables = true;
    
    public Type2CostEquation(double discountRate) {
        this.discountRate = discountRate / 100;     
    }

    public void setUp(double emissionReduction, BestMeasureEffRecord bestMeasureEffRecord, 
            Double designCapacity, String designCapacityUnitNumerator,
            String designCapacityUnitDenominator) {
        //define required inputs
        this.bestMeasureEffRecord = bestMeasureEffRecord;
        this.designCapacity = convertDesignCapacity(designCapacity, designCapacityUnitNumerator,
                designCapacityUnitDenominator);
        this.reducedEmission=emissionReduction;
        this.capRecFactor=getCapRecFactor();
        //populate variables for use with the equations
        populateVariables();
    }

    //convert design capacity to the correct units -- MW, for use with the equation calculations
    public Double convertDesignCapacity(Double designCapacity, String designCapacityUnitNumerator,
            String designCapacityUnitDenominator) {
        Double convertedDesignCapacity = null;

        //if you don't know the units then you can't convert the design capacity
        if (designCapacityUnitNumerator == null || designCapacityUnitDenominator == null)
            return convertedDesignCapacity;
        
        if (designCapacityUnitNumerator.equalsIgnoreCase("MMBtu") && designCapacityUnitDenominator.equalsIgnoreCase("hr")) {
            //no conversion is necessary, these are the expected units.
            if (designCapacityUnitNumerator.equalsIgnoreCase("MMBtu") && designCapacityUnitDenominator.equalsIgnoreCase("hr"))
                convertedDesignCapacity = designCapacity;
        }
        return convertedDesignCapacity;
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
        if (capCostMultiplier == null
                || capCostExponent == null
                || annCostMultiplier == null
                || annCostExponent == null) 
            hasAllVariables = false;
    }

    public Double getAnnualCost() {
        if (!hasAllVariables
                || designCapacity == null 
                || designCapacity == 0.0) return null;
        return annCostMultiplier * Math.pow(designCapacity, annCostExponent);
    }

    public Double getCapitalCost() {
        if (!hasAllVariables
                || designCapacity == null 
                || designCapacity == 0.0) return null;
        return capCostMultiplier * Math.pow(designCapacity, capCostExponent);
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