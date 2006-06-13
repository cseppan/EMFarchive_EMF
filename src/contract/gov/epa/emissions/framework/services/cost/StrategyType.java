package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.DatasetType;

import java.io.Serializable;

public class StrategyType implements Serializable, Comparable {

    private int id;

    private String name;

    private String description;

    private String defaultSortOrder;

    private String strategyClassName;

//    private Mutex lock;

    public StrategyType() {
//        lock = new Mutex();
    }

    public StrategyType(String name) {
        this();
        this.name = name;
    }

    public String getDefaultSortOrder() {
        return defaultSortOrder;
    }

    public void setDefaultSortOrder(String defaultSortOrder) {
        this.defaultSortOrder = defaultSortOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

//    public Mutex getLock() {
//        return lock;
//    }
//
//    public void setLock(Mutex lock) {
//        this.lock = lock;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStrategyClassName() {
        return strategyClassName;
    }

    public void setStrategyClassName(String strategyClassName) {
        this.strategyClassName = strategyClassName;
    }

//    public String getLockOwner() {
//        return lock.getLockOwner();
//    }
//
//    public void setLockOwner(String username) {
//        lock.setLockOwner(username);
//    }
//
//    public Date getLockDate() {
//        return lock.getLockDate();
//    }
//
//    public void setLockDate(Date lockDate) {
//        this.lock.setLockDate(lockDate);
//    }
//
//    public boolean isLocked(String owner) {
//        return lock.isLocked(owner);
//    }
//
//    public boolean isLocked(User owner) {
//        return lock.isLocked(owner);
//    }
//
//    public boolean isLocked() {
//        return lock.isLocked();
//    }

    public int compareTo(Object o) {
        return name.compareTo(((DatasetType) o).getName());
    }
    
    public String toString() {
        return name;
    }
}
