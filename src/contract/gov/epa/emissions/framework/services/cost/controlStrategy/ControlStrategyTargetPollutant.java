package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Pollutant;

import java.io.Serializable;

public class ControlStrategyTargetPollutant implements Serializable {
    
    private int id;

    private Pollutant pollutant;

    private Double maxEmisReduction;

    private Double maxControlEfficiency;

    private Double minCostPerTon;

    private Double minAnnCost;

    private Double replacementControlMinEfficiencyDiff;

    public ControlStrategyTargetPollutant() {
        //
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Pollutant getPollutant() {
        return pollutant;
    }

    public void setPollutant(Pollutant pollutant) {
        this.pollutant = pollutant;
    }

    public void setMaxEmisReduction(Double maxEmisReduction) {
        this.maxEmisReduction = maxEmisReduction;
    }

    public Double getMaxEmisReduction() {
        return maxEmisReduction;
    }

    public void setMaxControlEfficiency(Double maxControlEfficiency) {
        this.maxControlEfficiency = maxControlEfficiency;
    }

    public Double getMaxControlEfficiency() {
        return maxControlEfficiency;
    }

    public void setMinCostPerTon(Double minCostPerTon) {
        this.minCostPerTon = minCostPerTon;
    }

    public Double getMinCostPerTon() {
        return minCostPerTon;
    }

    public void setMinAnnCost(Double minAnnCost) {
        this.minAnnCost = minAnnCost;
    }

    public Double getMinAnnCost() {
        return minAnnCost;
    }

    public void setReplacementControlMinEfficiencyDiff(Double replacementControlMinEfficiencyDiff) {
        this.replacementControlMinEfficiencyDiff = replacementControlMinEfficiencyDiff;
    }

    public Double getReplacementControlMinEfficiencyDiff() {
        return replacementControlMinEfficiencyDiff;
    }
}
