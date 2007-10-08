package gov.epa.emissions.framework.services.casemanagement.parameters;

import java.io.Serializable;

public class ParameterEnvVar implements Serializable, Comparable {
    
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
    
    public boolean equals(Object other) {
        if (other == null || !(other instanceof ParameterEnvVar))
            return false;

        return (id == ((ParameterEnvVar)other).id) || ((ParameterEnvVar) other).name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }
    
    public int compareTo(Object other) {
        return name.compareToIgnoreCase(((ParameterEnvVar) other).getName());
    }

}
