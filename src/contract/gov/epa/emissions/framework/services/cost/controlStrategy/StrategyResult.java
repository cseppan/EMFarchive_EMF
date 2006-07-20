package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;

import java.io.Serializable;
import java.util.Date;

public class StrategyResult implements Serializable {

    private int id;

    private int inputDatasetId;

    private double totalCost;

    private double totalReduction;

    private String runStatus;

    private Date completionTime;

    private Date startTime;

    private Dataset detailedResultDataset;
    
    private StrategyResultType strategyResultType;

    public StrategyResult() {
        //
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public StrategyResultType getStrategyResultType() {
        return strategyResultType;
    }

    public void setStrategyResultType(StrategyResultType resultType) {
        this.strategyResultType = resultType;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public int getInputDatasetId() {
        return inputDatasetId;
    }

    public void setInputDatasetId(int datasetId) {
        this.inputDatasetId = datasetId;
    }

    public double getTotalReduction() {
        return totalReduction;
    }

    public void setTotalReduction(double totalReduction) {
        this.totalReduction = totalReduction;
    }

    public Date getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(Date completionTime) {
        this.completionTime = completionTime;
    }

    public Dataset getDetailedResultDataset() {
        return detailedResultDataset;
    }

    public void setDetailedResultDataset(Dataset detailedResultDataset) {
        this.detailedResultDataset = detailedResultDataset;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

}
