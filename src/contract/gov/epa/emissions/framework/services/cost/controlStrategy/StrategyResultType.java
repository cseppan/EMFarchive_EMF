package gov.epa.emissions.framework.services.cost.controlStrategy;

import java.io.Serializable;

public class StrategyResultType implements Serializable {
    
    private int id;
    
    private String name;
    
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
