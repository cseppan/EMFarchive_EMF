package gov.epa.emissions.framework.services.cost;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ControlMeasureEquationType implements Serializable {

    private long id;

    private EquationType equationType;
    
    private List controlMeasureEquationTypeVariables;

    public ControlMeasureEquationType() {// persistence/bean
        this.controlMeasureEquationTypeVariables = new ArrayList();
    }

    public ControlMeasureEquationType(EquationType equationType) {
        this();
        this.equationType = equationType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public EquationType getEquationType() {
        return equationType;
    }

    public void setEquationType(EquationType equationType) {
        this.equationType = equationType;
    }

    public ControlMeasureEquationTypeVariable[] getControlMeasureEquationTypeVariables() {
        return (ControlMeasureEquationTypeVariable[]) controlMeasureEquationTypeVariables.toArray(new ControlMeasureEquationTypeVariable[0]);
    }

    public void setControlMeasureEquationTypeVariables(ControlMeasureEquationTypeVariable[] controlMeasureEquationTypeVariables) {
        this.controlMeasureEquationTypeVariables = Arrays.asList(controlMeasureEquationTypeVariables);
    }
}
