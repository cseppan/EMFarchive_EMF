package gov.epa.emissions.framework.services.cost.data;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ControlMeasureEfficiency implements Serializable, Lockable {
    private String name;
    
    private int id;
    
    private List efficiencyRecords;
    
    private Mutex lock;
    
    public ControlMeasureEfficiency() {
        this.efficiencyRecords = new ArrayList();
        lock = new Mutex();
    }

    public ControlMeasureEfficiency(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EfficiencyRecord[] getEifficiencyRecords() {
        return (EfficiencyRecord[])efficiencyRecords.toArray(new EfficiencyRecord[0]);
    }

    public void setCostRecords(EfficiencyRecord[] efficiencyRecords) {
        this.efficiencyRecords.clear();
        this.efficiencyRecords.addAll(Arrays.asList(efficiencyRecords));
    }
    
    public void addRecord(EfficiencyRecord record) {
        this.efficiencyRecords.add(record);
    }
    
    public String getLockOwner() {
        return lock.getLockOwner();
    }

    public void setLockOwner(String username) {
        lock.setLockOwner(username);
    }

    public Date getLockDate() {
        return lock.getLockDate();
    }

    public void setLockDate(Date lockDate) {
        this.lock.setLockDate(lockDate);
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
    
    public boolean equals(Object other) {
        return (other instanceof ControlMeasureEfficiency && ((ControlMeasureEfficiency) other).id == id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
