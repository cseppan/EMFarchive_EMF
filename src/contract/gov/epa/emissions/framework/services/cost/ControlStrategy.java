package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyConstraint;

import java.io.Serializable;
import java.util.Date;

public class ControlStrategy implements Lockable, Serializable {

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

    private ControlStrategyInputDataset[] controlStrategyInputDatasets = new ControlStrategyInputDataset[] {};

    private Pollutant targetPollutant;

    private String runStatus;

    private StrategyType strategyType;

    private String filter;

    private ControlMeasureClass[] controlMeasureClasses = new ControlMeasureClass[] {};

    private ControlStrategyMeasure[] controlMeasures = new ControlStrategyMeasure[] {};

    private String countyFile;

    private Mutex lock;

    private ControlStrategyConstraint constraint;

    private boolean useCostEquations;

    public ControlStrategy() {
        this.lock = new Mutex();
//        this.controlStrategyInputDatasets = new ArrayList();
//        this.controlMeasureClasses = new ArrayList();
//        this.controlMeasures = new ArrayList();
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

    public ControlStrategyInputDataset[] getControlStrategyInputDatasets() {
        return controlStrategyInputDatasets;//(ControlStrategyInputDataset[]) controlStrategyInputDatasets.toArray(new ControlStrategyInputDataset[0]);
    }

    public void setControlStrategyInputDatasets(ControlStrategyInputDataset[] inputDatasets) {
        this.controlStrategyInputDatasets = inputDatasets;//Arrays.asList(inputDatasets);
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

    public Pollutant getTargetPollutant() {
        return targetPollutant;
    }

    public void setTargetPollutant(Pollutant targetPollutant) {
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

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setControlMeasureClasses(ControlMeasureClass[] controlMeasureClasses) {
        this.controlMeasureClasses = controlMeasureClasses;//(controlMeasureClasses != null) ? Arrays.asList(controlMeasureClasses) : new ArrayList();
    }

    public ControlMeasureClass[] getControlMeasureClasses() {
        return controlMeasureClasses;//(ControlMeasureClass[])controlMeasureClasses.toArray(new ControlMeasureClass[0]);
    }

    public void setControlMeasures(ControlStrategyMeasure[] controlMeasures) {
        this.controlMeasures = controlMeasures;// (controlMeasures != null) ? Arrays.asList(controlMeasures) : new ArrayList();
    }

    public ControlStrategyMeasure[] getControlMeasures() {
        return controlMeasures;//(ControlStrategyMeasure[])controlMeasures.toArray(new ControlStrategyMeasure[0]);
    }

    public void setCountyFile(String countyFile) {
        this.countyFile = countyFile;
    }

    public String getCountyFile() {
        return countyFile;
    }

    public void setConstraint(ControlStrategyConstraint constraint) {
        this.constraint = constraint;
    }

    public ControlStrategyConstraint getConstraint() {
        return constraint;
    }

    public String toString() {
        return name;
    }

    public void setUseCostEquations(boolean useCostEquations) {
        this.useCostEquations = useCostEquations;
    }

    public boolean getUseCostEquations() {
        return useCostEquations;
    }
}
