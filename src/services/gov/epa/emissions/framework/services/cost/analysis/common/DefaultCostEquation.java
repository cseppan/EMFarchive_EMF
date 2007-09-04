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

    public void setUp(double emissionReduction, BestMeasureEffRecord bestMeasureEffRecord) {
        this.bestMeasureEffRecord = bestMeasureEffRecord;
        this.emissionReduction = emissionReduction;
 //       this.annualCost=getAnnualCost();
 //       this.capRecFactor=getCapRecFactor();
    }

    public Double getAnnualCost() throws EmfException {
        double tAnnualCost = emissionReduction * bestMeasureEffRecord.adjustedCostPerTon();
        if (tAnnualCost == 0) return null; 
        
        return tAnnualCost;
    }

    public Double getCapitalCost(){
        try {
            annualCost=getAnnualCost();
        } catch (EmfException e) {
            e.printStackTrace();
        }
        capAnnRatio = bestMeasureEffRecord.efficiencyRecord().getCapitalAnnualizedRatio();
        if (capAnnRatio == null || annualCost==null) return null;
        
        return capAnnRatio * annualCost;
    }  
    
    public Double getOperationMaintenanceCost() {
        annulizedCCost = getAnnualizedCapitalCost();
        
        if (annulizedCCost == null) return null;
        double omCost = annualCost - annulizedCCost;
        if (omCost==0) return null; 
        return omCost;
    }
    
    public Double getAnnualizedCapitalCost(){ 
        capRecFactor=getCapRecFactor();
        capitalCost = getCapitalCost();
        if (capitalCost == null || capRecFactor == null) return null;
        return capitalCost * capRecFactor;
    }

   
    public Double getCapRecFactor(){
        // Calculate capital recovery factor
        return getCapRecFactor(bestMeasureEffRecord.measure().getEquipmentLife(), 
                bestMeasureEffRecord.efficiencyRecord().getCapRecFactor());
    }
    
    public Double getCapRecFactor(float equipmentLife, Double effRecCapRecFactor){
        // Calculate capital recovery factor 
        Double capRecFactor = effRecCapRecFactor;
        if (equipmentLife!=0) 
             capRecFactor = DefaultCostEquation.calculateCapRecFactor(discountRate, equipmentLife);
        
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
        if (annualCost==null ||annualCost==0.0 || emissionReduction==0.0 ) return null;
        return annualCost/emissionReduction;
    }
    
}

