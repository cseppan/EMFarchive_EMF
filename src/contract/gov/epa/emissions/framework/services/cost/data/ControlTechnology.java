package gov.epa.emissions.framework.services.cost.data;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.Date;

public class ControlTechnology implements Serializable, Lockable {
    private int id;

    private String name;

    private String description;

    private Mutex lock;

    public ControlTechnology() {
        this.lock = new Mutex();
    }

    public ControlTechnology(String description, String name) {
        this();
        this.description = description;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean equals(Object other) {
        return (other instanceof ControlTechnology) && (((ControlTechnology) other).id == id);
    }

    public int hashCode() {
        return id;
    }
    
    public String toString(){
        return name;
    }
}
