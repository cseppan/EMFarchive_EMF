package gov.epa.emissions.framework.services.cost;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ControlMeasureEquationType implements Serializable {

    private int id;

    private EquationType equationType;
    
    private ControlMeasure controlMeasure;
    
    private List equationTypeVariables;

    public ControlMeasureEquationType() {// persistence/bean
        this.equationTypeVariables = new ArrayList();
    }

    public ControlMeasureEquationType(EquationType equationType) {
        this();
        this.equationType = equationType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public EquationType getEquationType() {
        return equationType;
    }

    public void setEquationType(EquationType equationType) {
        this.equationType = equationType;
    }

    public ControlMeasureEquationTypeVariable[] getEquationTypeVariables() {
        return (ControlMeasureEquationTypeVariable[]) equationTypeVariables.toArray(new ControlMeasureEquationTypeVariable[0]);
    }

    public void setEquationTypeVariables(ControlMeasureEquationTypeVariable[] controlMeasureEquationTypeVariables) {
        this.equationTypeVariables = Arrays.asList(controlMeasureEquationTypeVariables);
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControlMeasureEquationType)) {
            return false;
        }

        ControlMeasureEquationType other = (ControlMeasureEquationType) obj;

        return (id == other.getId());
    }

    public int hashCode() {
        return equationType.hashCode();
    }

    public void setControlMeasure(ControlMeasure controlMeasure) {
        this.controlMeasure = controlMeasure;
    }

    public ControlMeasure getControlMeasure() {
        return controlMeasure;
    }
}
