package gov.epa.emissions.framework.services.cost.data;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ControlMeasureCost implements Serializable, Lockable {

    private String name;
    
    private int id;
    
    private List costRecords;
    
    private Mutex lock;
    
    public ControlMeasureCost() {
        this.costRecords = new ArrayList();
        lock = new Mutex();
    }

    public ControlMeasureCost(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CostRecord[] getCostRecords() {
        return (CostRecord[])costRecords.toArray(new CostRecord[0]);
    }

    public void setCostRecords(CostRecord[] costRecords) {
        this.costRecords.clear();
        this.costRecords.addAll(Arrays.asList(costRecords));
    }
    
    public void addRecord(CostRecord record) {
        this.costRecords.add(record);
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
    
//    public int compareTo(Object o) {
//        return name.compareTo(((ControlMeasureCost) o).getName());
//    }
//    
//    public int hashCode() {
//        return name.hashCode();
//    }

    public boolean equals(Object other) {
        return (other instanceof ControlMeasureCost && ((ControlMeasureCost) other).id == id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
