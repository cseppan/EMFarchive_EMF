package gov.epa.emissions.framework.services.sms;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;
import java.util.Date;

public class SectorScenario implements Lockable, Serializable {

    private int id;
    private String name;
    private String description = "";
    private String abbreviation;
    private String runStatus;
    private Boolean shouldDoubleCount;
    private Boolean annotateInventoryWithEECS;
    private Short autoRunQASteps;
    private Short annotatingEecsOption;
    private User creator;
    private Date lastModifiedDate;
    private Date startDate;
    private Date completionDate;
    private Mutex lock;
    private String copiedFrom;
    private String exportDirectory;
    private Integer eecsVersion;
    private EmfDataset eecsMapppingDataset;
    private Integer eecsMapppingDatasetVersion;
    private EmfDataset sectorMapppingDataset;
    private Integer sectorMapppingDatasetVersion;
    private SectorScenarioInventory[] inventories = new SectorScenarioInventory[] {};
    private String[] sectors = new String[] {};

    public SectorScenario() {
        this.lock = new Mutex();
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

    public Boolean getShouldDoubleCount() {
        return shouldDoubleCount;
    }

    public void setShouldDoubleCount(Boolean shouldDoubleCount) {
        this.shouldDoubleCount = shouldDoubleCount;
    }

    public Boolean getAnnotateInventoryWithEECS() {
        return annotateInventoryWithEECS;
    }

    public void setAnnotateInventoryWithEECS(Boolean annotateInventoryWithEECS) {
        this.annotateInventoryWithEECS = annotateInventoryWithEECS;
    }

    public Short getAutoRunQASteps() {
        return autoRunQASteps;
    }

    public void setAutoRunQASteps(Short autoRunQASteps) {
        this.autoRunQASteps = autoRunQASteps;
    }

    public Short getAnnotatingEecsOption() {
        return annotatingEecsOption;
    }

    public void setAnnotatingEecsOption(Short annotatingEecsOption) {
        this.annotatingEecsOption = annotatingEecsOption;
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

    public String getExportDirectory() {
        return exportDirectory;
    }

    public void setExportDirectory(String exportDirectory) {
        this.exportDirectory = exportDirectory;
    }

    public Integer getEecsVersion() {
        return eecsVersion;
    }

    public void setEecsVersion(Integer eecsVersion) {
        this.eecsVersion = eecsVersion;
    }

    public EmfDataset getEecsMapppingDataset() {
        return eecsMapppingDataset;
    }

    public void setEecsMapppingDataset(EmfDataset eecsMapppingDataset) {
        this.eecsMapppingDataset = eecsMapppingDataset;
    }

    public Integer getEecsMapppingDatasetVersion() {
        return eecsMapppingDatasetVersion;
    }

    public void setEecsMapppingDatasetVersion(Integer eecsMapppingDatasetVersion) {
        this.eecsMapppingDatasetVersion = eecsMapppingDatasetVersion;
    }

    public EmfDataset getSectorMapppingDataset() {
        return sectorMapppingDataset;
    }

    public void setSectorMapppingDataset(EmfDataset sectorMapppingDataset) {
        this.sectorMapppingDataset = sectorMapppingDataset;
    }

    public Integer getSectorMapppingDatasetVersion() {
        return sectorMapppingDatasetVersion;
    }

    public void setSectorMapppingDatasetVersion(Integer sectorMapppingDatasetVersion) {
        this.sectorMapppingDatasetVersion = sectorMapppingDatasetVersion;
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

    public SectorScenarioInventory[] getInventories() {
        return inventories;
    }

    public void setInventories(SectorScenarioInventory[] inventories) {
        this.inventories = inventories;
    }

    public String[] getSectors() {
        return sectors;
    }

    public void setInventories(String[] sectors) {
        this.sectors = sectors;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof SectorScenario))
            return false;

        final SectorScenario ss = (SectorScenario) other;

        return ss.name.equals(name) || ss.id == id;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
