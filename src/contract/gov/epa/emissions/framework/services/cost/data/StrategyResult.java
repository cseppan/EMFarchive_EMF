package gov.epa.emissions.framework.services.cost.data;

import java.io.Serializable;

public class StrategyResult implements Serializable {
    
    private int id;
    
    private int datasetId;
    
    private int sourceId;
    
    private int controlMeasureID;
    
    private String controlMeasureAbbr;
    
    private double cost;
    
    private double costPerTon;
    
    private double redEmissions;
    
    private String pollutant;
    
    private String controlStrategy;
    
    private String scc;
    
    public StrategyResult() {
        //
    }

    public String getControlStrategy() {
        return controlStrategy;
    }

    public void setControlStrategy(String controlStrategy) {
        this.controlStrategy = controlStrategy;
    }

    public double getCostPerTon() {
        return costPerTon;
    }

    public void setCostPerTon(double costPerTon) {
        this.costPerTon = costPerTon;
    }

    public String getPollutant() {
        return pollutant;
    }

    public void setPollutant(String pollutant) {
        this.pollutant = pollutant;
    }

    public double getRedEmissions() {
        return redEmissions;
    }

    public void setRedEmissions(double redEmissions) {
        this.redEmissions = redEmissions;
    }

    public String getScc() {
        return scc;
    }

    public void setScc(String scc) {
        this.scc = scc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getControlMeasureAbbr() {
        return controlMeasureAbbr;
    }

    public void setControlMeasureAbbr(String controlMeasureAbbr) {
        this.controlMeasureAbbr = controlMeasureAbbr;
    }

    public int getControlMeasureID() {
        return controlMeasureID;
    }

    public void setControlMeasureID(int controlMeasureID) {
        this.controlMeasureID = controlMeasureID;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }
}
