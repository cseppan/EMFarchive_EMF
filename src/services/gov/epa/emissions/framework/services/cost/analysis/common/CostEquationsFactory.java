package gov.epa.emissions.framework.services.cost.analysis.common;

public class CostEquationsFactory {

    public CostEquationsFactory() {
        //
    }
//    
//    public CostEquations getCostEquations(double emissionReduction, BestMeasureEffRecord maxCM, double discountRate, boolean useCostEquations) {
//        if (!useCostEquations) {
//            return new DefaultCostEquations(emissionReduction, maxCM);
//        }
// //       return new DefaultCostEquations(maxCM);
//        return null;
//    }

    public CostEquations getCostEquations(double reducedEmission, BestMeasureEffRecord maxCM, double discountRate, boolean useCostEquations) {
        return new DefaultCostEquations(reducedEmission, maxCM, discountRate);
//        if (!useCostEquations) {
//            return new DefaultCostEquations(reducedEmission, maxCM, discountRate);
//        }
//        return null;
    }



}
