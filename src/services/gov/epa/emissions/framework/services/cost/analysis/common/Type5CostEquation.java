package gov.epa.emissions.framework.services.cost.analysis.common;


public class Type5CostEquation implements CostEquation {

    private BestMeasureEffRecord bestMeasureEffRecord;
    private double discountRate;
    private Double minStackFlowRate;
    private double reducedEmission;
    
    
    public Type5CostEquation(double discountRate) {
        this.discountRate = discountRate / 100;
    }

    public void setUp(double reducedEmission, BestMeasureEffRecord bestMeasureEffRecord, Double minStackFlowRate) {
        //define required inputs
        this.bestMeasureEffRecord = bestMeasureEffRecord;
        this.minStackFlowRate = minStackFlowRate;
        this.reducedEmission=reducedEmission;
       
    }

    public Double getAnnualCost() {     
 //       Double capitalCost = getCapitalCost();
        Double annualizedCost=getAnnualizedCapitalCost();
        Double operationMaintenanceCost = getOperationMaintenanceCost();
        if (annualizedCost == null || operationMaintenanceCost == null) return null;
        return annualizedCost + operationMaintenanceCost;
    }

    public Double getCapitalCost() {
        if (minStackFlowRate == null || minStackFlowRate == 0.0) return null;
        return 2882540.0 + (244.74 * minStackFlowRate);
    }  
    
    public Double getOperationMaintenanceCost() {
        if (minStackFlowRate == null || minStackFlowRate == 0.0) return null;
        return 749170.0 + (148.40 * minStackFlowRate);
    }
    
    public Double getAnnualizedCapitalCost() { 
        Double capitalCost = getCapitalCost();
        Double capRecFactor=getCapRecFactor();
        if (capitalCost == null || capRecFactor == null) return null;
        return capitalCost * capRecFactor;
    }

    public Double getComputedCPT() {
        Double totalCost=getAnnualCost();
        if (totalCost==null || reducedEmission == 0.0) return null; 
        return totalCost/reducedEmission;
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

    
}