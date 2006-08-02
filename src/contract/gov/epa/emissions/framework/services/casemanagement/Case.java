package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    
    private String inputFileDir;

    private String outputFileDir;

    private Project project;

    private Mutex lock;

    private Region modelingRegion;

    private Region controlRegion;

    private String runStatus;

    private Date lastModifiedDate;

    private User lastModifiedBy;
    
    private boolean caseTemplate;
    
    private GridResolution gridResolution;
    
    private int numMetLayers;

    private int numEmissionsLayers;
    
    private int baseYear;
    
    private int futureYear;
    
    private Date startDate;
    
    private Date endDate;
    
    private List sectors;
    
    private boolean isFinal;
    
    private String templateUsed;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public Case() {
        lock = new Mutex();
        this.sectors = new ArrayList();
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
        final Case caze = (Case) other;
        return caze.name.equals(name) || caze.id == id;
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

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
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

    public int getBaseYear() {
        return baseYear;
    }

    public void setBaseYear(int baseYear) {
        this.baseYear = baseYear;
    }

    public boolean isCaseTemplate() {
        return caseTemplate;
    }

    public void setCaseTemplate(boolean caseTemplate) {
        this.caseTemplate = caseTemplate;
    }

    public Region getControlRegion() {
        return controlRegion;
    }

    public void setControlRegion(Region controlRegion) {
        this.controlRegion = controlRegion;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getFutureYear() {
        return futureYear;
    }

    public void setFutureYear(int futureYear) {
        this.futureYear = futureYear;
    }

    public GridResolution getGridResolution() {
        return gridResolution;
    }

    public void setGridResolution(GridResolution gridResolution) {
        this.gridResolution = gridResolution;
    }

    public boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Mutex getLock() {
        return lock;
    }

    public void setLock(Mutex lock) {
        this.lock = lock;
    }

    public Region getModelingRegion() {
        return modelingRegion;
    }

    public void setModelingRegion(Region modelingRegion) {
        this.modelingRegion = modelingRegion;
    }

    public int getNumEmissionsLayers() {
        return numEmissionsLayers;
    }

    public void setNumEmissionsLayers(int numEmissionsLayers) {
        this.numEmissionsLayers = numEmissionsLayers;
    }

    public int getNumMetLayers() {
        return numMetLayers;
    }

    public void setNumMetLayers(int numMetLayers) {
        this.numMetLayers = numMetLayers;
    }

    public Sector[] getSectors() {
        return (Sector[])sectors.toArray(new Sector[0]);
    }

    public void setSectors(Sector[] sectors) {
        this.sectors = Arrays.asList(sectors);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getTemplateUsed() {
        return templateUsed;
    }

    public void setTemplateUsed(String templateUsed) {
        this.templateUsed = templateUsed;
    }

    public String getInputFileDir() {
        return inputFileDir;
    }

    public void setInputFileDir(String inputFileDir) {
        this.inputFileDir = inputFileDir;
    }

    public String getOutputFileDir() {
        return outputFileDir;
    }

    public void setOutputFileDir(String outputFileDir) {
        this.outputFileDir = outputFileDir;
    }

}
