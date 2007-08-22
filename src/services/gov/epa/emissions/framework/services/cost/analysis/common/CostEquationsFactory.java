package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.cost.ControlMeasureEquationType;

public class CostEquationsFactory {

    private DefaultCostEquations defaultCostEquations;
    private Type6CostEquation type6CostEquation;
    
    public CostEquationsFactory(boolean useCostEquations, double discountRate) {
        this.defaultCostEquations = new DefaultCostEquations(discountRate);
        this.type6CostEquation = new Type6CostEquation(discountRate);
    }
//    
//    public CostEquations getCostEquations(double emissionReduction, BestMeasureEffRecord maxCM, double discountRate, boolean useCostEquations) {
//        if (!useCostEquations) {
//            return new DefaultCostEquations(emissionReduction, maxCM);
//        }
// //       return new DefaultCostEquations(maxCM);
//        return null;
//    }

    public CostEquations getCostEquations(double reducedEmission, BestMeasureEffRecord bestMeasureEffRecord) {
        //see which type of equation to use...
        ControlMeasureEquationType[] equationTypes = bestMeasureEffRecord.measure().getEquationTypes();
        if (equationTypes.length > 0) {
            if (equationTypes[0].getEquationType().getName().equals("Type 6")) {
                type6CostEquation.setUp(reducedEmission, bestMeasureEffRecord);
                return type6CostEquation;
            }
        }

        defaultCostEquations.setUp(reducedEmission, bestMeasureEffRecord);
        return defaultCostEquations;
//        if (!useCostEquations) {
//            return new DefaultCostEquations(reducedEmission, maxCM, discountRate);
//        }
//        return null;
    }



}
