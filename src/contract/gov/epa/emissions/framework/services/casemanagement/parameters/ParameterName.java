package gov.epa.emissions.framework.services.casemanagement.parameters;

import java.io.Serializable;


public class ParameterName implements Serializable {

    private int id;
    
    private String name;
    
    public ParameterName() {
        //
    }
    
    public ParameterName(String name) {
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
    
    public boolean equals(Object other) {
        if (other == null || !(other instanceof ParameterName))
            return false;

        return (id == ((ParameterName)other).id) || ((ParameterName) other).name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }
}
