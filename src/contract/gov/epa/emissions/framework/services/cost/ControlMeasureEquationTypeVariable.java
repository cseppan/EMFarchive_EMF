package gov.epa.emissions.framework.services.cost;

import java.io.Serializable;

public class ControlMeasureEquationTypeVariable implements Serializable {

    private int id;

    private EquationTypeVariable equationTypeVariable;
    
    private EquationType equationType;

    private double value;

    private long listindex;

    public ControlMeasureEquationTypeVariable() {// persistence/bean
    }

    public ControlMeasureEquationTypeVariable(EquationTypeVariable equationTypeVariable, double value) {
        this.equationTypeVariable = equationTypeVariable;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public EquationTypeVariable getEquationTypeVariable() {
        return equationTypeVariable;
    }

    public void setEquationTypeVariable(EquationTypeVariable equationTypeVariable) {
        this.equationTypeVariable = equationTypeVariable;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public long getListindex() {
        return listindex;
    }

    public void setListindex(long listindex) {
        this.listindex = listindex;
    }

    public void setEquationType(EquationType equationType) {
        this.equationType = equationType;
    }

    public EquationType getEquationType() {
        return equationType;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControlMeasureEquationType)) {
            return false;
        }

        ControlMeasureEquationTypeVariable other = (ControlMeasureEquationTypeVariable) obj;

        return (id == other.getId());
    }

    public int hashCode() {
        return equationType.hashCode();
    }
}
