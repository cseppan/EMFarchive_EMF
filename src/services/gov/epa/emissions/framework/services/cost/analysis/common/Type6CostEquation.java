package gov.epa.emissions.framework.services.cost.analysis.common;


public class Type6CostEquation implements CostEquation {

    private BestMeasureEffRecord bestMeasureEffRecord;
    private double discountRate;
    private Double minStackFlowRate;
    
    public Type6CostEquation(double discountRate) {
        this.discountRate = discountRate / 100;
    }

    public void setUp(BestMeasureEffRecord bestMeasureEffRecord, Double minStackFlowRate) {
        //define required inputs
        this.bestMeasureEffRecord = bestMeasureEffRecord;
        this.minStackFlowRate = minStackFlowRate;
    }

    public Double getAnnualCost() {
        Double capRecFactor = getCapRecFactor();
        Double capitalCost = getCapitalCost();
        Double operationMaintenanceCost = getOperationMaintenanceCost();
        if (capRecFactor == null || capitalCost == null || operationMaintenanceCost == null) return null;
        return capitalCost * capRecFactor + operationMaintenanceCost;
    }

    public Double getCapitalCost() {
        if (minStackFlowRate == null || minStackFlowRate == 0.0) return null;
        return 3449803.0 + (135.86 * minStackFlowRate);
    }  
    
    public Double getOperationMaintenanceCost() {
        if (minStackFlowRate == null || minStackFlowRate == 0.0) return null;
        return 797667.0 + (58.84 * minStackFlowRate);
    }
    
    public Double getAnnualizedCapitalCost() { 
        Double capRecFactor = getCapRecFactor();
        Double capitalCost = getCapitalCost();
        if (capitalCost == null || capRecFactor == null) return null;
        return capitalCost * capRecFactor;
    }

    public Double getCapRecFactor(){
        // Calculate capital recovery factor 
        double equipmentLife = bestMeasureEffRecord.measure().getEquipmentLife();
        Double capRecFactor;
        if (equipmentLife==0) 
            capRecFactor = bestMeasureEffRecord.efficiencyRecord().getCapRecFactor();
        else 
            capRecFactor = DefaultCostEquation.calculateCapRecFactor(discountRate, equipmentLife);
        
        if (capRecFactor != null && capRecFactor != 0) {
            return capRecFactor; 
        }
        return null;
    }
}