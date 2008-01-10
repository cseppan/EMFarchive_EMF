package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;
import java.util.Date;

public class ControlStrategyResult implements Serializable {
    
    private int id;

    private int controlStrategyId;

    private double totalCost;

    private double totalReduction;

    private String runStatus;

    private Date completionTime;

    private Date startTime;

    private EmfDataset inputDataset;

    private int inputDatasetVersion;

    private Dataset detailedResultDataset;
    
    private Dataset controlledInventoryDataset;
    
    private StrategyResultType strategyResultType;

    private Integer recordCount;

    public ControlStrategyResult() {
        //
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getControlStrategyId() {
        return controlStrategyId;
    }

    public void setControlStrategyId(int id) {
        this.controlStrategyId = id;
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

    public EmfDataset getInputDataset() {
        return inputDataset;
    }

    public void setInputDataset(EmfDataset inputDataset) {
        this.inputDataset = inputDataset;
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

    public Dataset getControlledInventoryDataset() {
        return controlledInventoryDataset;
    }

    public void setControlledInventoryDataset(Dataset controlledInventoryDataset) {
        this.controlledInventoryDataset = controlledInventoryDataset;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    public void setInputDatasetVersion(int inputDatasetVersion) {
        this.inputDatasetVersion = inputDatasetVersion;
    }

    public int getInputDatasetVersion() {
        return inputDatasetVersion;
    }
}
