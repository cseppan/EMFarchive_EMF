package gov.epa.emissions.framework.services.casemanagement.parameters;

import java.io.Serializable;

public class ValueType implements Serializable {

    private int id;
    
    private String name;
    
    public ValueType() {
        //
    }
    
    public ValueType(String name) {
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
        return this.name;
    }
    
    public boolean equals(Object other) {
        if (other == null || !(other instanceof ValueType))
            return false;

        return ((ValueType) other).name.equals(this.name) || ((ValueType)other).id == id;
    }

    public int hashCode() {
        return name.hashCode();
    }
}