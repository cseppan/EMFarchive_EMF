package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.EmfException;

public interface CostEquation {

    public Double getAnnualCost() throws EmfException;

    public Double getCapitalCost() ;
    
    public Double getAnnualizedCapitalCost() ;

    public Double getOperationMaintenanceCost() ;
    
    public Double getComputedCPT();

}
