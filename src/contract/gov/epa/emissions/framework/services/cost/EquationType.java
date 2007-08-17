package gov.epa.emissions.framework.services.cost;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EquationType implements Serializable {
    private int id;

    private String name;

    private String description = "";
    
    private List equationTypeVariables;

    public EquationType() {
        this.equationTypeVariables = new ArrayList();
    }

    public EquationType(String name) {
        this();
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
    
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof EquationType)) {
            return false;
        }

        EquationType other = (EquationType) obj;

        return (id == other.getId() || name.equalsIgnoreCase(other.getName()));
    }

    public int hashCode() {
        return name.hashCode();
    }
    
    public String toString() {
        return this.name;
    }

    public EquationTypeVariable[] getEquationTypeVariables() {
        return (EquationTypeVariable[]) equationTypeVariables.toArray(new EquationTypeVariable[0]);
    }

    public void setEquationTypeVariables(EquationTypeVariable[] equationTypeVariables) {
        this.equationTypeVariables = Arrays.asList(equationTypeVariables);
    }
}
