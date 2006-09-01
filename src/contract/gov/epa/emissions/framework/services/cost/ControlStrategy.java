package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ControlStrategy implements Lockable {

    private int id;

    private String name;

    private String description;

    private Region region;

    private Project project;

    private double discountRate;

    private int costYear;

    private int inventoryYear;

    private User creator;

    private Date lastModifiedDate;

    private Date startDate;

    private Date completionDate;

    private DatasetType datasetType;

    private List datasetsList;

    private String targetPollutant;

    private String runStatus;

    private StrategyType strategyType;

    private int datasetVersion;

    private List strategyResults;

    private Mutex lock;

    public ControlStrategy() {
        this.lock = new Mutex();
        this.datasetsList = new ArrayList();
        strategyResults = new ArrayList();
    }

    public ControlStrategy(String name) {
        this();
        this.name = name;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof ControlStrategy))
            return false;

        final ControlStrategy cs = (ControlStrategy) other;

        return cs.name.equals(name) || cs.id == id;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public int getInventoryYear() {
        return inventoryYear;
    }

    public void setInventoryYear(int inventoryYear) {
        this.inventoryYear = inventoryYear;
    }

    public int getCostYear() {
        return costYear;
    }

    public void setCostYear(int costYear) {
        this.costYear = costYear;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public EmfDataset[] getInputDatasets() {
        return (EmfDataset[]) datasetsList.toArray(new EmfDataset[0]);
    }

    public void setInputDatasets(EmfDataset[] datasets) {
        this.datasetsList = Arrays.asList(datasets);
    }

    public DatasetType getDatasetType() {
        return datasetType;
    }

    public void setDatasetType(DatasetType datasetType) {
        this.datasetType = datasetType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getTargetPollutant() {
        return targetPollutant;
    }

    public void setTargetPollutant(String targetPollutant) {
        this.targetPollutant = targetPollutant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Date getLockDate() {
        return lock.getLockDate();
    }

    public void setLockDate(Date lockDate) {
        lock.setLockDate(lockDate);
    }

    public String getLockOwner() {
        return lock.getLockOwner();
    }

    public void setLockOwner(String owner) {
        lock.setLockOwner(owner);
    }

    public boolean isLocked(String owner) {
        return lock.isLocked(owner);
    }

    public boolean isLocked(User owner) {
        return lock.isLocked(owner);
    }

    public boolean isLocked() {
        return lock.isLocked();
    }

    public int getDatasetVersion() {
        return datasetVersion;
    }

    public void setDatasetVersion(int datasetVersion) {
        this.datasetVersion = datasetVersion;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public StrategyType getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(StrategyType strategyType) {
        this.strategyType = strategyType;
    }

    public StrategyResult[] getStrategyResults() {
        return (StrategyResult[]) strategyResults.toArray(new StrategyResult[0]);
    }

    public void setStrategyResults(StrategyResult[] strategyResults) {
        this.strategyResults.clear();
        this.strategyResults.addAll(Arrays.asList(strategyResults));
    }

}
