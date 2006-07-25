package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ControlMeasure implements Lockable, Serializable {

    private int id;

    private String name;

    private String description;

    private int deviceCode;
    
    private int costYear;

    private float equipmentLife;

    private String majorPollutant;

    private User creator;

    private float annualizedCost;

    private String cmClass;

    private String abbreviation;

    private String controlTechnology;
    
    private String sourceGroup;

    private String dataSouce;

    private Date dateReviewed;

    private Date lastModifiedTime;

    private Mutex lock;

    private List sccs;
    
    private List efficiencyRecords;
    
    private List sectors;

    public ControlMeasure() {
        this.lock = new Mutex();
        this.sccs = new ArrayList();
        this.efficiencyRecords = new ArrayList();
        this.sectors = new ArrayList();
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

    public int getCostYear() {
        return costYear;
    }

    public void setCostYear(int costYear) {
        this.costYear = costYear;
    }

    public EfficiencyRecord[] getEfficiencyRecords() {
        return (EfficiencyRecord[]) efficiencyRecords.toArray(new EfficiencyRecord[0]);
    }

    public void setEfficiencyRecords(EfficiencyRecord[] efficiencyRecords) {
        this.efficiencyRecords = Arrays.asList(efficiencyRecords);
    }

    public String getControlTechnology() {
        return controlTechnology;
    }

    public void setControlTechnology(String controlTechnology) {
        this.controlTechnology = controlTechnology;
    }

    public String getDataSouce() {
        return dataSouce;
    }

    public void setDataSouce(String dataSouce) {
        this.dataSouce = dataSouce;
    }

    public Date getDateReviewed() {
        return dateReviewed;
    }

    public void setDateReviewed(Date dateReviewed) {
        this.dateReviewed = dateReviewed;
    }

    public String getSourceGroup() {
        return sourceGroup;
    }

    public void setSourceGroup(String sourceGroup) {
        this.sourceGroup = sourceGroup;
    }

    public Scc[] getSccs() {
        return (Scc[])sccs.toArray(new Scc[0]);
    }

    public void setSccs(Scc[] sccs) {
        this.sccs = Arrays.asList(sccs);
    }

    public Sector[] getSectors() {
        return (Sector[])sectors.toArray(new Sector[0]);
    }

    public void setSectors(Sector[] sectors) {
        this.sectors = Arrays.asList(sectors);
    }

}
