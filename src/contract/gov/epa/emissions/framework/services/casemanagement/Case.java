package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.security.User;

import java.util.Date;

public class Case implements Comparable, Lockable {

    private int id;

    private String name;

    private Abbreviation abbreviation;

    private AirQualityModel airQualityModel;

    private CaseCategory caseCategory;

    private EmissionsYear emissionsYear;

    private Grid grid;

    private MeteorlogicalYear meteorlogicalYear;

    private Speciation speciation;

    private String description;

    private User creator;

    private Project project;

    private Mutex lock;

    private Region region;

    private String runStatus;

    private Date lastModifiedDate;

    private String copiedFrom;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public Case() {
        lock = new Mutex();
    }

    public Case(String name) {
        this();
        this.name = name;
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

    public boolean equals(Object other) {
        if (other == null || !(other instanceof Case))
            return false;

        return ((Case) other).name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(Object other) {
        return name.compareTo(((Case) other).getName());
    }

    public void setAbbreviation(Abbreviation abbreviation) {
        this.abbreviation = abbreviation;
    }

    public Abbreviation getAbbreviation() {
        return abbreviation;
    }

    public void setAirQualityModel(AirQualityModel airQualityModel) {
        this.airQualityModel = airQualityModel;
    }

    public AirQualityModel getAirQualityModel() {
        return airQualityModel;
    }

    public void setCaseCategory(CaseCategory caseCategory) {
        this.caseCategory = caseCategory;
    }

    public CaseCategory getCaseCategory() {
        return caseCategory;
    }

    public void setEmissionsYear(EmissionsYear emissionsYear) {
        this.emissionsYear = emissionsYear;
    }

    public EmissionsYear getEmissionsYear() {
        return emissionsYear;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public Grid getGrid() {
        return grid;
    }

    public void setMeteorlogicalYear(MeteorlogicalYear meteorlogicalYear) {
        this.meteorlogicalYear = meteorlogicalYear;
    }

    public MeteorlogicalYear getMeteorlogicalYear() {
        return meteorlogicalYear;
    }

    public void setSpeciation(Speciation speciation) {
        this.speciation = speciation;
    }

    public Speciation getSpeciation() {
        return speciation;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public User getCreator() {
        return creator;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public Region getRegion() {
        return region;
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

    public void setRegion(Region region) {
        this.region = region;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setCopiedFrom(String copiedFrom) {
        this.copiedFrom = copiedFrom;
    }

    public String getCopiedFrom() {
        return copiedFrom;
    }

}
