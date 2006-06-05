package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.security.User;
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

    private int analysisYear;

    private User creator;

    private Date lastModifiedDate;
    
    private Date startDate;

    private Date completionDate;
    
    private DatasetType datasetType;

    private List datasetsList;

    private String majorPollutant;

    private String runStatus;

    private double majorPollutantControlEfficiency;

    private int analysisType;
    
    private int datasetVersion;

    private Mutex lock;

    public ControlStrategy() {
        this.lock = new Mutex();
        this.datasetsList = new ArrayList();
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

    public int getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(int analysisType) {
        this.analysisType = analysisType;
    }

    public int getAnalysisYear() {
        return analysisYear;
    }

    public void setAnalysisYear(int analysisYear) {
        this.analysisYear = analysisYear;
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

    public EmfDataset[] getDatasets() {
        return (EmfDataset[]) datasetsList.toArray(new EmfDataset[0]);
    }

    public void setDatasets(EmfDataset[] datasets) {
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

    public String getMajorPollutant() {
        return majorPollutant;
    }

    public void setMajorPollutant(String majorPollutant) {
        this.majorPollutant = majorPollutant;
    }

    public double getMajorPollutantControlEfficiency() {
        return majorPollutantControlEfficiency;
    }

    public void setMajorPollutantControlEfficiency(double majorPollutantControlEfficiency) {
        this.majorPollutantControlEfficiency = majorPollutantControlEfficiency;
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

}
