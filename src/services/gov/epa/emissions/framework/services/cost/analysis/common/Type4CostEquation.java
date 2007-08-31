package gov.epa.emissions.framework.services.cost.analysis.common;


public class Type4CostEquation implements CostEquation {

    private BestMeasureEffRecord bestMeasureEffRecord;
    private double discountRate;
    private Double minStackFlowRate;
    private double emissionReduction;
    private Double capRecFactor;
    
    public Type4CostEquation(double discountRate) {
        this.discountRate = discountRate / 100;
    }

    public void setUp(double emissionReduction, BestMeasureEffRecord bestMeasureEffRecord, Double minStackFlowRate) {
        //define required inputs
        this.bestMeasureEffRecord = bestMeasureEffRecord;
        this.minStackFlowRate = minStackFlowRate;
        this.emissionReduction=emissionReduction;
        this.capRecFactor=getCapRecFactor();
    }

    public Double getAnnualCost() {
        Double capitalCost = getCapitalCost();
        Double operationMaintenanceCost = getOperationMaintenanceCost();
        if (capRecFactor == null || capitalCost == null || operationMaintenanceCost == null) return null;
        return capitalCost * capRecFactor + operationMaintenanceCost;
    }

    public Double getCapitalCost() {
        if (minStackFlowRate == null || minStackFlowRate == 0.0) return null;
        return 990000 + (9.836 * minStackFlowRate);
    }  
    
    public Double getOperationMaintenanceCost() {
        if (minStackFlowRate == null || minStackFlowRate == 0.0) return null;
        return 75800 + (12.82 * minStackFlowRate);
    }
    
    public Double getAnnualizedCapitalCost() { 
        Double capitalCost = getCapitalCost();
        if (capitalCost == null || capRecFactor == null) return null;
        return capitalCost * capRecFactor;
    }

//    public Double getCapRecFactor(){
//        // Calculate capital recovery factor 
//        double equipmentLife = bestMeasureEffRecord.measure().getEquipmentLife();
//        Double capRecFactor;
//        if (equipmentLife==0) 
//            capRecFactor = bestMeasureEffRecord.efficiencyRecord().getCapRecFactor();
//        else 
//            capRecFactor = DefaultCostEquation.calculateCapRecFactor(discountRate, equipmentLife);
//        
//        if (capRecFactor != null && capRecFactor != 0) {
//            return capRecFactor; 
//        }
//        return null;
//    }

    public Double getComputedCPT() {
        Double totalCost=getAnnualCost();
        if (totalCost==null || emissionReduction == 0.0) return null; 
        return totalCost/emissionReduction;
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

    // This is for test ----
    public void setUpTest(double reducedEmission, float equipmentLife, Double effRecCapRecFactor, BestMeasureEffRecord bestMeasureEffRecord, Double minStackFlowRate) {
        //define required inputs
        this.bestMeasureEffRecord = bestMeasureEffRecord;
        this.minStackFlowRate = minStackFlowRate;
        this.emissionReduction=reducedEmission;
        this.capRecFactor=getCapRecFactor(equipmentLife, effRecCapRecFactor);
    }
    
}