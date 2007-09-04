package gov.epa.emissions.framework.services.cost.analysis.common;


public class Type3CostEquation implements CostEquation {

    private BestMeasureEffRecord bestMeasureEffRecord;
    
    private double emissionReduction; 
    private double discountRate;
    private Double minStackFlowRate;
    private Double capRecFactor;
    
    private static final double capitalCostFactor=192;
    private static final double gasFlowRateFactor=.486;
    private static final double retrofitFactor=1.1;
    
    public Type3CostEquation(double discountRate) {
        this.discountRate = discountRate / 100;
    }

    public void setUp(double emissionReduction, BestMeasureEffRecord bestMeasureEffRecord, Double minStackFlowRate) {
        //define required inputs
        this.bestMeasureEffRecord = bestMeasureEffRecord;
        this.minStackFlowRate = minStackFlowRate;
        this.emissionReduction=emissionReduction;
        this.capRecFactor=getCapRecFactor();
        getFactors();
    }
    
    public void getFactors(){
        //
    }

    public Double getAnnualCost() {
        Double capitalCost = getCapitalCost();
        Double operationMaintenanceCost = getOperationMaintenanceCost();
        if (capRecFactor == null || capitalCost == null || operationMaintenanceCost == null) return null;
        return capitalCost * capRecFactor + operationMaintenanceCost;
    }

    public Double getCapitalCost() {
        if (minStackFlowRate == null || minStackFlowRate == 0.0) return null;
        if (minStackFlowRate <1028000 )
            return Math.pow(1028000/minStackFlowRate, 0.6)*capitalCostFactor*gasFlowRateFactor*retrofitFactor*minStackFlowRate;
        return capitalCostFactor*gasFlowRateFactor*retrofitFactor*minStackFlowRate;
    }  
    
    public Double getOperationMaintenanceCost() {
        if (minStackFlowRate == null || minStackFlowRate == 0.0) return null;
        return (3.35+(0.00729*8736))* minStackFlowRate*0.9383;
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
    

}