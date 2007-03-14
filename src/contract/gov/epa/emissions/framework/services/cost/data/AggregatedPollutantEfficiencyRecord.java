package gov.epa.emissions.framework.services.cost.data;

import gov.epa.emissions.commons.data.Pollutant;

import java.io.Serializable;

public class AggregatedPollutantEfficiencyRecord implements Serializable {

    private Pollutant pollutant;
    private float maxEfficiency;
    private float minEfficiency;
    private float avgEfficiency;
    private float maxCostPerTon;
    private float minCostPerTon;
    private float avgCostPerTon;
    private float avgRuleEffectiveness;
    private float avgRulePenetration;

    public AggregatedPollutantEfficiencyRecord() {
        //
    }

    public AggregatedPollutantEfficiencyRecord(Pollutant pollutant, float maxEfficiency,
        float minEfficiency, float avgEfficiency,
        float maxCostPerTon, float minCostPerTon,
        float avgCostPerTon, float avgRuleEffectiveness,
        float avgRulePenetration) {
        this.pollutant = pollutant;
        this.maxEfficiency = maxEfficiency;
        this.minEfficiency = minEfficiency;
        this.avgEfficiency = avgEfficiency;
        this.maxCostPerTon = maxCostPerTon;
        this.minCostPerTon = minCostPerTon;
        this.avgCostPerTon = avgCostPerTon;
        this.avgRuleEffectiveness = avgRuleEffectiveness;
        this.avgRulePenetration = avgRulePenetration;
    }
    
    public Pollutant getPollutant() {
        return pollutant;
    }

    public void setPollutant(Pollutant pollutant) {
        this.pollutant = pollutant;
    }

    public float getMaxEfficiency() {
        return maxEfficiency;
    }

    public void setMaxEfficiency(float maxEfficiency) {
        this.maxEfficiency = maxEfficiency;
    }

    public float getMinEfficiency() {
        return minEfficiency;
    }

    public void setMinEfficiency(float minEfficiency) {
        this.minEfficiency = minEfficiency;
    }

    public float getAvgEfficiency() {
        return avgEfficiency;
    }

    public void setAvgEfficiency(float avgEfficiency) {
        this.avgEfficiency = avgEfficiency;
    }

    public float getMaxCostPerTon() {
        return maxCostPerTon;
    }

    public void setMaxCostPerTon(float maxCostPerTon) {
        this.maxCostPerTon = maxCostPerTon;
    }

    public float getMinCostPerTon() {
        return minCostPerTon;
    }

    public void setMinCostPerTon(float minCostPerTon) {
        this.minCostPerTon = minCostPerTon;
    }

    public float getAvgCostPerTon() {
        return avgCostPerTon;
    }

    public void setAvgCostPerTon(float avgCostPerTon) {
        this.avgCostPerTon = avgCostPerTon;
    }

    public float getAvgRuleEffectiveness() {
        return avgRuleEffectiveness;
    }

    public void setAvgRuleEffectiveness(float avgRuleEffectiveness) {
        this.avgRuleEffectiveness = avgRuleEffectiveness;
    }

    public float getAvgRulePenetration() {
        return avgRulePenetration;
    }

    public void setAvgRulePenetration(float avgRulePenetration) {
        this.avgRulePenetration = avgRulePenetration;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof AggregatedPollutantEfficiencyRecord))
            return false;

        return true;
    }

    public int hashCode() {
        return this.hashCode();
    }

}
