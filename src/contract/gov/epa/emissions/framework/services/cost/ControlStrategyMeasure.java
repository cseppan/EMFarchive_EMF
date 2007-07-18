package gov.epa.emissions.framework.services.cost;

import java.io.Serializable;

public class ControlStrategyMeasure implements Serializable {

    private int id;

    private long listindex;

    private LightControlMeasure controlMeasure;

    private Double ruleEffectiveness;

    private Double rulePenetration;

    private Double applyOrder;

    public ControlStrategyMeasure() {
        //
    }

    public ControlStrategyMeasure(LightControlMeasure controlMeasure) {
        this.controlMeasure = controlMeasure;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getListindex() {
        return listindex;
    }

    public void setListindex(long listindex) {
        this.listindex = listindex;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControlStrategyMeasure)) {
            return false;
        }

        ControlStrategyMeasure other = (ControlStrategyMeasure) obj;

        return ((controlMeasure != null ? controlMeasure.getId() : 0) 
                == other.getControlMeasure().getId());
    }

    public int hashCode() {
        return controlMeasure != null ? controlMeasure.hashCode() : "".hashCode();
    }

    public LightControlMeasure getControlMeasure() {
        return controlMeasure;
    }

    public void setControlMeasure(LightControlMeasure controlMeasure) {
        this.controlMeasure = controlMeasure;
    }
    
    public void setRuleEffectiveness(Double ruleEffectiveness) {
        this.ruleEffectiveness = ruleEffectiveness;
    }

    public Double getRuleEffectiveness() {
        return ruleEffectiveness;
    }

    public void setRulePenetration(Double rulePenetration) {
        this.rulePenetration = rulePenetration;
    }

    public Double getRulePenetration() {
        return rulePenetration;
    }

    public void setApplyOrder(Double applyOrder) {
        this.applyOrder = applyOrder;
    }

    public Double getApplyOrder() {
        return applyOrder;
    }
}