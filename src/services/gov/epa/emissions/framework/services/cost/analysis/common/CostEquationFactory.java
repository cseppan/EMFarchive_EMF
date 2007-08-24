package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.cost.ControlMeasureEquationType;

public class CostEquationFactory {

    private boolean useCostEquations;
    private DefaultCostEquation defaultCostEquations;
    private Type6CostEquation type6CostEquation;
    
    public CostEquationFactory(boolean useCostEquations, double discountRate) {
        this.useCostEquations = useCostEquations;
        this.defaultCostEquations = new DefaultCostEquation(discountRate);
        this.type6CostEquation = new Type6CostEquation(discountRate);
    }

    public CostEquation getCostEquation(double reducedEmission, BestMeasureEffRecord bestMeasureEffRecord, 
            Double minStackFlowRate) {
        //always setup the default cost equation, the other equation types will default to using this approach when the other equation don't work
        //for example, maybe some of the inputs are missing, or maybe some constraint is not met...
        defaultCostEquations.setUp(reducedEmission, bestMeasureEffRecord);

        if (useCostEquations) {
            //see which type of equation to use...
            ControlMeasureEquationType[] equationTypes = bestMeasureEffRecord.measure().getEquationTypes();
            
            //NOTE, we are currently only supporting one equation type, in the future we might need to support multiple 
            //equations (i.e., if Type 6 is the primary but we are missing some inputs we might want to try Type 10, and if
            //we don't have all the inputs for this Type, then we could use default equation approach) 
            if (equationTypes.length > 0) {
                
                //use type 6 equation...
                if (equationTypes[0].getEquationType().getName().equals("Type 6")) {
                    //evaluate inputs, if they missing, use the default
                    if (minStackFlowRate != null && minStackFlowRate != 0.0) {
                        type6CostEquation.setUp(bestMeasureEffRecord, 
                                minStackFlowRate);
                        return type6CostEquation;
                    }
                }
                //future equations go here...
            }
      }

      return defaultCostEquations;
    }



}
