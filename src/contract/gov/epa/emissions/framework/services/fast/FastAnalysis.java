package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.data.LockableImpl;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;
import java.util.Date;

public class FastAnalysis extends LockableImpl implements Serializable {

    private int id;

    private String name;

    private String description = "";

    private Grid grid;

    private String runStatus;

    private User creator;

    private Date lastModifiedDate;

    private Date startDate;

    private Date completionDate;

    private String copiedFrom;

    private EmfDataset cancerRiskDataset;

    private Integer cancerRiskDatasetVersion;

    private FastAnalysisInputSector[] inputSectors = new FastAnalysisInputSector[] {};

    private Sector[] outputSectors = new Sector[] {};

    public FastAnalysis() {
    }

    public FastAnalysis(String name) {
        this();
        this.name = name;
    }

    public FastAnalysis(int id, String name) {
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

    public Integer getCancerRiskDatasetVersion() {
        return cancerRiskDatasetVersion;
    }

    public void setCancerRiskDatasetVersion(Integer cancerRiskDatasetVersion) {
        this.cancerRiskDatasetVersion = cancerRiskDatasetVersion;
    }

    public EmfDataset getCancerRiskDataset() {
        return cancerRiskDataset;
    }

    public void setCancerRiskDataset(EmfDataset cancerRiskDataset) {
        this.cancerRiskDataset = cancerRiskDataset;
    }

    public FastAnalysisInputSector[] getInputSectors() {
        return inputSectors;
    }

    public void setInputSectors(FastAnalysisInputSector[] inputSectors) {
        this.inputSectors = inputSectors;
    }

    public Sector[] getOutputSectors() {
        return outputSectors;
    }

    public void setOutputSectors(Sector[] outputSectors) {
        this.outputSectors = outputSectors;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof FastAnalysis))
            return false;

        final FastAnalysis ss = (FastAnalysis) other;

        return ss.name.equals(name) || ss.id == id;
    }

    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return this.name;
    }

}
