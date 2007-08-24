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

    public DefaultCostEquation(double discountRate) {
        this.discountRate = discountRate / 100;
    }

    public void setUp(double emissionReduction, BestMeasureEffRecord bestMeasureEffRecord) {
        this.bestMeasureEffRecord = bestMeasureEffRecord;
        this.emissionReduction = emissionReduction;
    }

    public Double getAnnualCost() throws EmfException {
        double tAnnualCost = emissionReduction * bestMeasureEffRecord.adjustedCostPerTon();
        if (tAnnualCost == 0) return null; 
        return tAnnualCost;
    }

    public Double getCapitalCost() throws EmfException {
        capAnnRatio = bestMeasureEffRecord.efficiencyRecord().getCapitalAnnualizedRatio();
        if (capAnnRatio == null) return null;
        return capAnnRatio * getAnnualCost();
    }  
    
    public Double getOperationMaintenanceCost() throws EmfException {
        annulizedCCost = getAnnualizedCapitalCost();
        if (annulizedCCost == null) return null;
        double omCost = getAnnualCost() - annulizedCCost;
        return omCost;
    }
    
    public Double getAnnualizedCapitalCost() throws EmfException { 
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
}

//what the difference of cost per ton and adjusted cost per ton.

//input for new CoST equations
//maxCM.measure().getEquipmentLife() //equipment life
//controlStrategy.getDiscountRate() //interest rate
//maxCM.adjustedCostPerTon() * reducedEmission //annualized cost
//maxCM.adjustedCostPerTon() //Cost Per Ton
//reducedEmission  //Tons reduced
//maxCM.efficiencyRecord().getCapitalAnnualizedRatio()
    
//how to get CapRecFactor
//if (discountRate = 0.07 && maxCM.efficiencyRecord().getCapRecFactor() != null && maxCM.efficiencyRecord().getCapRecFactor() != 0)
//    maxCM.efficiencyRecord().getCapRecFactor() 
//else 
//    calculateCapRecFactor(discountRate, equipmentLife)

//ned to return O&M Costs & Capital Cost