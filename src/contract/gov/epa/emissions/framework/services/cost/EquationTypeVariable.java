package gov.epa.emissions.framework.services.cost;

import java.io.Serializable;

public class EquationTypeVariable implements Serializable {
    private int id;

    private String name;

    private int equationTypeId;
    
    private short fileColPosition;

    public EquationTypeVariable() {
        //
    }

    public EquationTypeVariable(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEquationTypeId() {
        return equationTypeId;
    }

    public void setEquationTypeId(int equationTypeId) {
        this.equationTypeId = equationTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof EquationTypeVariable)) {
            return false;
        }

        EquationTypeVariable other = (EquationTypeVariable) obj;

        return (id == other.getId() || name.equalsIgnoreCase(other.getName()));
    }

    public int hashCode() {
        return name.hashCode();
    }
    
    public String toString() {
        return this.name;
    }

    public void setFileColPosition(short fileColPosition) {
        this.fileColPosition = fileColPosition;
    }

    public short getFileColPosition() {
        return fileColPosition;
    }
}
