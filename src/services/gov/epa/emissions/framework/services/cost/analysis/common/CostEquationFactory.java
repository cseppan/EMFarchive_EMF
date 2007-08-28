package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasureEquationType;

public class CostEquationFactory {

    private boolean useCostEquations;
    private DefaultCostEquation defaultCostEquations;
    private Type6CostEquation type6CostEquation;
    private Type5CostEquation type5CostEquation;
    private Type4CostEquation type4CostEquation;
    private Type3CostEquation type3CostEquation;
    
    public CostEquationFactory(boolean useCostEquations, double discountRate) {
        this.useCostEquations = useCostEquations;
        this.defaultCostEquations = new DefaultCostEquation(discountRate);
        this.type6CostEquation = new Type6CostEquation(discountRate);
        this.type5CostEquation = new Type5CostEquation(discountRate);
        this.type4CostEquation = new Type4CostEquation(discountRate);
        this.type3CostEquation = new Type3CostEquation(discountRate);
    }

    public CostEquation getCostEquation(double reducedEmission, BestMeasureEffRecord bestMeasureEffRecord, 
            Double minStackFlowRate) throws EmfException {
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
                        type6CostEquation.setUp(reducedEmission, bestMeasureEffRecord, 
                                minStackFlowRate);
                        return type6CostEquation;
                    }
                }
                
                if (equationTypes[0].getEquationType().getName().equals("Type 5")) {
                    //evaluate inputs, if they missing, use the default
                    if (minStackFlowRate != null && minStackFlowRate != 0.0) {
                        type5CostEquation.setUp(reducedEmission, bestMeasureEffRecord, 
                                minStackFlowRate);
                        return type5CostEquation;
                    }
                }
                
                if (equationTypes[0].getEquationType().getName().equals("Type 4")) {
                    //evaluate inputs, if they missing, use the default
                    if (minStackFlowRate != null && minStackFlowRate != 0.0) {
                        type4CostEquation.setUp(reducedEmission, bestMeasureEffRecord, 
                                minStackFlowRate);
                        return type4CostEquation;
                    }
                }
                
                if (equationTypes[0].getEquationType().getName().equals("Type 3")) {
                    //evaluate inputs, if they missing, use the default
                    if (minStackFlowRate != null && minStackFlowRate != 0.0) {
                        type3CostEquation.setUp(reducedEmission, bestMeasureEffRecord, 
                                minStackFlowRate);
                        return type3CostEquation;
                    }
                }
                
                //future equations go here...
            }
      }

      return defaultCostEquations;
    }



}