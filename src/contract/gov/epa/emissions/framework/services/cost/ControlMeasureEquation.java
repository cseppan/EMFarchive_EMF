package gov.epa.emissions.framework.services.cost;

import java.io.Serializable;

public class ControlMeasureEquation implements Serializable {

    private int id;

    private EquationType equationType;

    private EquationTypeVariable equationTypeVariable;
    
    private Double value;

    public ControlMeasureEquation() {// persistence/bean
    }

    public ControlMeasureEquation(EquationType equationType, EquationTypeVariable equationTypeVariable, 
            Double value) {
        this(equationType);
        this.equationTypeVariable = equationTypeVariable;
        this.value = value;
    }

    public ControlMeasureEquation(EquationType equationType) {
        this();
        this.equationType = equationType;
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

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public void setEquationType(EquationType equationType) {
        this.equationType = equationType;
    }

    public EquationType getEquationType() {
        return equationType;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControlMeasureEquation)) {
            return false;
        }

        ControlMeasureEquation other = (ControlMeasureEquation) obj;

        return (
                id == other.getId() 
                && (equationType != null && other.getEquationType()!= null && equationType.getId() == other.getEquationType().getId())
                && ((equationTypeVariable == null) || (equationTypeVariable != null && other.getEquationTypeVariable()!= null && equationTypeVariable.getId() == other.getEquationTypeVariable().getId()))
                );
    }

    public int hashCode() {
        return equationType.hashCode();
    }
}
