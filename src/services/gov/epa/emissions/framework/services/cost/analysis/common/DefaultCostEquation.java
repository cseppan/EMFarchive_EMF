package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.EmfException;

public class DefaultCostEquation implements CostEquation {
    
    private BestMeasureEffRecord bestMeasureEffRecord;
    private double emissionReduction;
    private double discountRate;
    private Double capitalCost;
    private Double capRecFactor;
    private Double capAnnRatio; 
    private Double annulizedCCost;
    private Double annualCost;

    public DefaultCostEquation(double discountRate) {
        this.discountRate = discountRate / 100;
    }

    public void setUp(double emissionReduction, BestMeasureEffRecord bestMeasureEffRecord) throws EmfException {
        this.bestMeasureEffRecord = bestMeasureEffRecord;
        this.emissionReduction = emissionReduction;
        this.annualCost=getAnnualCost();
    }

    public Double getAnnualCost() throws EmfException {
        double tAnnualCost = emissionReduction * bestMeasureEffRecord.adjustedCostPerTon();
        if (tAnnualCost == 0) return null; 
        return tAnnualCost;
    }

    public Double getCapitalCost() {
        capAnnRatio = bestMeasureEffRecord.efficiencyRecord().getCapitalAnnualizedRatio();
        if (capAnnRatio == null || annualCost==null) return null;
        return capAnnRatio * annualCost;
    }  
    
    public Double getOperationMaintenanceCost() {
        annulizedCCost = getAnnualizedCapitalCost();
        if (annulizedCCost == null) return null;
        double omCost = annualCost - annulizedCCost;
        return omCost;
    }
    
    public Double getAnnualizedCapitalCost() { 
        capitalCost = getCapitalCost();
        capRecFactor = getCapRecFactor();
        if (capitalCost == null || capRecFactor == null) return null;
        return capitalCost * capRecFactor;
    }

   
    public Double getCapRecFactor(){
        // Calculate capital recovery factor 
        double equipmentLife = bestMeasureEffRecord.measure().getEquipmentLife();
        if (equipmentLife==0) 
            capRecFactor = bestMeasureEffRecord.efficiencyRecord().getCapRecFactor();
        else 
            capRecFactor = calculateCapRecFactor(discountRate, equipmentLife);
        
        if (capRecFactor != null && capRecFactor != 0) {
            return capRecFactor; 
        }
        return null;
    }
    
    public static Double calculateCapRecFactor(double discountRate, double equipmentLife) {
        if(discountRate==0 || equipmentLife==0) return null;
        return (discountRate * Math.pow((1 + discountRate), equipmentLife)) / (Math.pow((discountRate + 1), equipmentLife) - 1);
    }

    public Double getComputedCPT() {
        
        return annualCost/emissionReduction;
    }
}

