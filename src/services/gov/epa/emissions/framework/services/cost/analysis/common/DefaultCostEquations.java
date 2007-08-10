package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.EmfException;




public class DefaultCostEquations implements CostEquations {
    
    private BestMeasureEffRecord maxCM;
//   private ResultSet resultSet; 
    private double emissionReduction;
    //private double costPerTon;
    private double discountRate;
    Double capitalCost;
    Double capRecFactor;
    Double capAnnRatio; 
    Double annulizedCCost;
 
    
    public DefaultCostEquations(double emissionReduction, BestMeasureEffRecord maxCM, double discountRate) {
        this.maxCM=maxCM;
 //       this.resultSet=resultSet;
        this.emissionReduction=emissionReduction;
        this.discountRate=discountRate;
    }

 
    public Double getAnnualCost() throws EmfException {
        double tAnnualCost=emissionReduction * maxCM.adjustedCostPerTon();
        if (tAnnualCost==0) return null; 
        return tAnnualCost;
    }

    public Double getCapitalCost() throws EmfException {
        capAnnRatio=maxCM.efficiencyRecord().getCapitalAnnualizedRatio();
        if (capAnnRatio == null) return null;
        return (capAnnRatio)*getAnnualCost();
    }  
    
    public Double getOperationMaintenanceCost() throws EmfException {
        annulizedCCost=getAnnualizedCapitalCost();
        if (annulizedCCost == null) return null;
        double omCost=getAnnualCost()-annulizedCCost;
        return omCost;
    }
    
    public Double getAnnualizedCapitalCost() throws EmfException { 
        capitalCost=getCapitalCost();
        capRecFactor=getCapRecFactor();
        if (capitalCost == null || capRecFactor == null) return null;
        return capitalCost * capRecFactor;
 //       maxCM.measure().getAnnualizedCost();
        
    }

   
    public Double getCapRecFactor(){
        capRecFactor=maxCM.efficiencyRecord().getCapRecFactor();
        if (capRecFactor != 0 ) {
            return capRecFactor; 
        }
        return calculateCapRecFactor(discountRate, maxCM.measure().getEquipmentLife());
    }
    private Double calculateCapRecFactor(double discountRate, double equipmentLife){
        if(discountRate==0 || equipmentLife==0) return null;
        return (discountRate*Math.pow((1+discountRate),equipmentLife))/(Math.pow((discountRate+1),equipmentLife)-1);
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