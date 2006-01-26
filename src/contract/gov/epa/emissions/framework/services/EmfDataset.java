package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.Country;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.ExternalSource;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Lockable;
import gov.epa.emissions.commons.io.Mutex;
import gov.epa.emissions.commons.io.Project;
import gov.epa.emissions.commons.io.Region;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class EmfDataset implements Dataset, Lockable {

    private long datasetid;// unique id needed for hibernate persistence

    private String name;

    private int year;

    private String description;

    private String status;

    private Region region;
    
    private IntendedUse intendedUse;

    private Country country ;

    private String units;

    private String creator;

    private String temporalResolution;

    private Date startDateTime;

    private Date endDateTime;

    private Project project;

    private Date createdDateTime;

    private Date modifiedDateTime;

    private Date accessedDateTime;

    private DatasetType datasetType;

    private List internalSources;

    private List externalSources;

    private List keyValsList;

    private List sectorsList;
    
    private int defaultVersion;

    private Mutex lock;

    public EmfDataset() {
        internalSources = new ArrayList();
        externalSources = new ArrayList();
        keyValsList = new ArrayList();
        sectorsList = new ArrayList();
        lock = new Mutex();
    }

    public int getDefaultVersion() {
        return defaultVersion;
    }

    public void setDefaultVersion(int defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    public Date getAccessedDateTime() {
        return accessedDateTime;
    }

    public void setAccessedDateTime(Date accessedDateTime) {
        this.accessedDateTime = accessedDateTime;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public Date getModifiedDateTime() {
        return modifiedDateTime;
    }

    public void setModifiedDateTime(Date modifiedDateTime) {
        this.modifiedDateTime = modifiedDateTime;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getDatasetTypeName() {
        return datasetType != null ? datasetType.getName() : null;
    }

    public DatasetType getDatasetType() {
        return datasetType;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public void setTemporalResolution(String temporalResolution) {
        this.temporalResolution = temporalResolution;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setStartDateTime(Date time) {
        this.startDateTime = time;
    }

    public void setStopDateTime(Date time) {
        this.endDateTime = time;
    }

    public Region getRegion() {
        return region;
    }

    public int getYear() {
        return year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDatasetType(DatasetType datasetType) {
        this.datasetType = datasetType;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getUnits() {
        return units;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }

    // FIXME: should use TemporalResolution type instead
    public String getTemporalResolution() {
        return temporalResolution;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public Date getStopDateTime() {
        return endDateTime;
    }

    public long getDatasetid() {
        return datasetid;
    }

    public void setDatasetid(long datasetid) {
        this.datasetid = datasetid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof Dataset)) {
            return false;
        }

        Dataset otherDataset = (Dataset) other;

        return (name.equals(otherDataset.getName()));
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public InternalSource[] getInternalSources() {
        return (InternalSource[]) this.internalSources.toArray(new InternalSource[0]);
    }

    public void setInternalSources(InternalSource[] internalSources) {
        this.internalSources.clear();
        this.internalSources.addAll(Arrays.asList(internalSources));
    }

    public void addInternalSource(InternalSource source) {
        this.internalSources.add(source);
    }

    public ExternalSource[] getExternalSources() {
        return (ExternalSource[]) this.externalSources.toArray(new ExternalSource[0]);
    }

    public void setExternalSources(ExternalSource[] externalSources) {
        this.externalSources.clear();
        this.externalSources.addAll(Arrays.asList(externalSources));
    }

    public void addExternalSource(ExternalSource source) {
        this.externalSources.add(source);
    }

    public void addSector(Sector sector) {
        sectorsList.add(sector);
    }

    public Sector[] getSectors() {
        return (Sector[])sectorsList.toArray(new Sector[0]);
    }

    public void setSectors(Sector[] sectors) {
        sectorsList.clear();
        sectorsList.addAll(Arrays.asList(sectors));
    }

    public void addKeyVal(KeyVal keyval) {
        keyValsList.add(keyval);
    }

    public KeyVal[] getKeyVals() {
        return (KeyVal[]) keyValsList.toArray(new KeyVal[0]);
    }

    public void setKeyVals(KeyVal[] keyvals) {
        keyValsList.clear();
        keyValsList.addAll(Arrays.asList(keyvals));
    }

    public void setSummarySource(InternalSource summary) {
        // TODO: implement Summary
    }

    public InternalSource getSummarySource() {
        return null;
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

    public IntendedUse getIntendedUse() {
        return intendedUse;
    }

    public void setIntendedUse(IntendedUse intendedUse) {
        this.intendedUse = intendedUse;
    }

}
