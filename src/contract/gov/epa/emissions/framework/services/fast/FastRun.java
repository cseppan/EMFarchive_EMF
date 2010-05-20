package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;
import java.util.Date;

public class FastRun implements Lockable, Serializable {

    private int id;
    private String name;
    private String description = "";
    private String abbreviation;
    private Grid grid;
    private String runStatus;
    private User creator;
    private Date lastModifiedDate;
    private Date startDate;
    private Date completionDate;
    private Mutex lock;
    private String copiedFrom;
    private EmfDataset invTableDataset;
    private Integer invTableDatasetVersion;
    private EmfDataset speciesMapppingDataset;
    private Integer speciesMapppingDatasetVersion;
    private EmfDataset transferCoefficientsDataset;
    private Integer transferCoefficientsDatasetVersion;
    private FastRunInventory[] inventories = new FastRunInventory[] {};
    private Sector[] outputSectors = new Sector[] {};

    public FastRun() {
        this.lock = new Mutex();
    }
    
    public FastRun(String name) {
        this();
        this.name = name;
    }

    public FastRun(int id, String name) {
        this(name);
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public String getCopiedFrom() {
        return copiedFrom;
    }

    public void setCopiedFrom(String copiedFrom) {
        this.copiedFrom = copiedFrom;
    }

    public Integer getInvTableDatasetVersion() {
        return invTableDatasetVersion;
    }

    public void setInvTableDatasetVersion(Integer invTableDatasetVersion) {
        this.invTableDatasetVersion = invTableDatasetVersion;
    }

    public EmfDataset getInvTableDataset() {
        return invTableDataset;
    }

    public void setInvTableDataset(EmfDataset invTableDataset) {
        this.invTableDataset = invTableDataset;
    }

    public Integer getSpeciesMapppingDatasetVersion() {
        return speciesMapppingDatasetVersion;
    }

    public void setSpeciesMapppingDatasetVersion(Integer speciesMapppingDatasetVersion) {
        this.speciesMapppingDatasetVersion = speciesMapppingDatasetVersion;
    }

    public EmfDataset getSpeciesMapppingDataset() {
        return speciesMapppingDataset;
    }

    public void setSpeciesMapppingDataset(EmfDataset speciesMapppingDataset) {
        this.speciesMapppingDataset = speciesMapppingDataset;
    }

    public Integer getTransferCoefficientsDatasetVersion() {
        return transferCoefficientsDatasetVersion;
    }

    public void setTransferCoefficientsDatasetVersion(Integer transferCoefficientsDatasetVersion) {
        this.transferCoefficientsDatasetVersion = transferCoefficientsDatasetVersion;
    }

    public EmfDataset getTransferCoefficientsDataset() {
        return transferCoefficientsDataset;
    }

    public void setTransferCoefficientsDataset(EmfDataset transferCoefficientsDataset) {
        this.transferCoefficientsDataset = transferCoefficientsDataset;
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

    public FastRunInventory[] getInventories() {
        return inventories;
    }

    public void setInventories(FastRunInventory[] inventories) {
        this.inventories = inventories;
    }

    public Sector[] getOutputSectors() {
        return outputSectors;
    }

    public void setOutputSectors(Sector[] outputSectors) {
        this.outputSectors = outputSectors;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof FastRun))
            return false;

        final FastRun ss = (FastRun) other;

        return ss.name.equals(name) || ss.id == id;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
