package gov.epa.emissions.framework.services.cost.data;

import gov.epa.emissions.commons.data.Pollutant;

import java.io.Serializable;

public class SumEffRec implements Serializable {

    private Pollutant pollutant;
    private float maxCE;
    private float minCE;
    private float avgCE;
    private float maxCPT;
    private float minCPT;
    private float avgCPT;
    private float avgRE;
    private float avgRP;

    public SumEffRec() {
        //
    }

    public SumEffRec(Pollutant pollutant, float maxEfficiency,
        float minEfficiency, float avgEfficiency,
        float maxCostPerTon, float minCostPerTon,
        float avgCostPerTon, float avgRuleEffectiveness,
        float avgRulePenetration) {
        this.pollutant = pollutant;
        this.maxCE = maxEfficiency;
        this.minCE = minEfficiency;
        this.avgCE = avgEfficiency;
        this.maxCPT = maxCostPerTon;
        this.minCPT = minCostPerTon;
        this.avgCPT = avgCostPerTon;
        this.avgRE = avgRuleEffectiveness;
        this.avgRP = avgRulePenetration;
    }
    
    public Pollutant getPollutant() {
        return pollutant;
    }

    public void setPollutant(Pollutant pollutant) {
        this.pollutant = pollutant;
    }

    public float getMaxCE() {
        return maxCE;
    }

    public void setMaxCE(float maxEfficiency) {
        this.maxCE = maxEfficiency;
    }

    public float getMinCE() {
        return minCE;
    }

    public void setMinCE(float minEfficiency) {
        this.minCE = minEfficiency;
    }

    public float getAvgCE() {
        return avgCE;
    }

    public void setAvgCE(float avgEfficiency) {
        this.avgCE = avgEfficiency;
    }

    public float getMaxCPT() {
        return maxCPT;
    }

    public void setMaxCPT(float maxCostPerTon) {
        this.maxCPT = maxCostPerTon;
    }

    public float getMinCPT() {
        return minCPT;
    }

    public void setMinCPT(float minCostPerTon) {
        this.minCPT = minCostPerTon;
    }

    public float getAvgCPT() {
        return avgCPT;
    }

    public void setAvgCPT(float avgCostPerTon) {
        this.avgCPT = avgCostPerTon;
    }

    public float getAvgRE() {
        return avgRE;
    }

    public void setAvgRE(float avgRuleEffectiveness) {
        this.avgRE = avgRuleEffectiveness;
    }

    public float getAvgRP() {
        return avgRP;
    }

    public void setAvgRP(float avgRulePenetration) {
        this.avgRP = avgRulePenetration;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof SumEffRec))
            return false;

        return true;
    }

    public int hashCode() {
        return this.hashCode();
    }

}
