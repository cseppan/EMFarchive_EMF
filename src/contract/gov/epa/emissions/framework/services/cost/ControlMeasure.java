package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.Date;

public class ControlMeasure implements Lockable, Serializable {
    
    private int id;
    
    private String name;

    private String description;
    
    private int deviceCode;
    
    private float equipmentLife;
    
    private String majorPollutant;
    
    private User creator;
    
    private float ruleEffectiveness;
    
    private float rulePenetration;
    
    private float annualizedCost;
    
    private Mutex lock;
    
    public ControlMeasure() {
        this.lock = new Mutex();
    }
    
//    public ControlMeasure(String name) {
//        this();
//        this.name = name;
//    }
    
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
}
