package gov.epa.emissions.framework.services.cost;

import java.io.Serializable;

public class ControlMeasureEquationTypeVariable implements Serializable {

    private long id;

    private EquationTypeVariable equationTypeVariable;

    private double value;

    private long listindex;

    public ControlMeasureEquationTypeVariable() {// persistence/bean
    }

    public ControlMeasureEquationTypeVariable(EquationTypeVariable equationTypeVariable, double value) {
        this.equationTypeVariable = equationTypeVariable;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
}
