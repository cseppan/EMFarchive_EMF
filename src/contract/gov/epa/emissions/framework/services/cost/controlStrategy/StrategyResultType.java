package gov.epa.emissions.framework.services.cost.controlStrategy;

import java.io.Serializable;

public class StrategyResultType implements Serializable {
    
    private int id;
    
    private String name;
    
    public static final String strategySummaryResult = "Strategy Summary";
    
    public static final String detailedStrategyResult = "Detailed Strategy Result";
    
    public static final String leastCostControlMeasureWorksheetResult = "Least Cost Control Measure Worksheet";
    
    public StrategyResultType(){
        //
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
