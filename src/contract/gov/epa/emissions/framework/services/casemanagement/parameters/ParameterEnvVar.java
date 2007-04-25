package gov.epa.emissions.framework.services.casemanagement.parameters;

import java.io.Serializable;

public class ParameterEnvVar implements Serializable {
    
    private int id;
    
    private String name;
    
    public ParameterEnvVar() {
        //
    }
    
    public ParameterEnvVar(String  name) {
        this.name = name;
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
    
    public String toString() {
        return name;
    }
    

}
