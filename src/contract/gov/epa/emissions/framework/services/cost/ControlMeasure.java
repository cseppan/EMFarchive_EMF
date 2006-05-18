package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.data.ControlMeasureCost;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ControlMeasure implements Lockable, Serializable {

    private int id;

    private String name;

    private String description;

    private int deviceCode, costYear;

    private float equipmentLife;

    private String majorPollutant;

    private User creator;

    private float ruleEffectiveness;

    private float rulePenetration;

    private float annualizedCost;

    private float minUncontrolledEmissions;

    private float maxUncontrolledEmissions;

    private Region region;
    
    private ControlMeasureCost cost;

    private String cmClass;

    private String abbreviation;

    private Date lastModifiedTime;

    private Mutex lock;

    private List sccs;

    public ControlMeasure() {
        this.lock = new Mutex();
        sccs = new ArrayList();
    }

    public ControlMeasure(String name) {
        this();
        this.name = name;
    }

    public float getAnnualizedCost() {
        return annualizedCost;
    }

    public void setAnnualizedCost(float annualizedCost) {
        this.annualizedCost = annualizedCost;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(int deviceCode) {
        this.deviceCode = deviceCode;
    }

    public float getEquipmentLife() {
        return equipmentLife;
    }

    public void setEquipmentLife(float equipmentLife) {
        this.equipmentLife = equipmentLife;
    }

    public String getMajorPollutant() {
        return majorPollutant;
    }

    public void setMajorPollutant(String majorPollutant) {
        this.majorPollutant = majorPollutant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getRuleEffectiveness() {
        return ruleEffectiveness;
    }

    public void setRuleEffectiveness(float ruleEffectiveness) {
        this.ruleEffectiveness = ruleEffectiveness;
    }

    public float getRulePenetration() {
        return rulePenetration;
    }

    public void setRulePenetration(float rulePenetration) {
        this.rulePenetration = rulePenetration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public boolean isLocked(User user) {
        return lock.isLocked(user);
    }

    public boolean isLocked() {
        return lock.isLocked();
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof ControlMeasure)) {
            return false;
        }

        ControlMeasure otherMeasure = (ControlMeasure) other;

        return (id == otherMeasure.getId());
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getCmClass() {
        return cmClass;
    }

    public void setCmClass(String cmClass) {
        this.cmClass = cmClass;
    }

    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public float getMaxUncontrolledEmissions() {
        return maxUncontrolledEmissions;
    }

    public void setMaxUncontrolledEmissions(float maxUncontrolledEmissions) {
        this.maxUncontrolledEmissions = maxUncontrolledEmissions;
    }

    public float getMinUncontrolledEmissions() {
        return minUncontrolledEmissions;
    }

    public void setMinUncontrolledEmissions(float minUncontrolledEmissions) {
        this.minUncontrolledEmissions = minUncontrolledEmissions;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public int getCostYear() {
        return costYear;
    }

    public void setCostYear(int costYear) {
        this.costYear = costYear;
    }

    public String[] getSccs() {
        return (String[]) sccs.toArray(new String[0]);
    }

    public void setSccs(String[] sccs) {
        this.sccs = Arrays.asList(sccs);
    }

    public ControlMeasureCost getCost() {
        return cost;
    }

    public void setCost(ControlMeasureCost cost) {
        this.cost = cost;
    }
}
