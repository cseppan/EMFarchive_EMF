package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;

public class CaseInput implements Serializable, Comparable {

    private int id;

    private String name;
    
    private InputName inputName;
    
    private Sector sector;
    
    private Program program;
    
    private InputEnvtVar envtVars;
    
    private EmfDataset dataset;
    
    private Version version;
    
    private DatasetType datasetType;
    
    private boolean required;
    
    private boolean show;
    
    private String subdir;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public CaseInput() {
        super();
    }

    public CaseInput(String name) {
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
        if (other == null || !(other instanceof CaseInput))
            return false;

        return ((CaseInput) other).name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(Object other) {
        return name.compareTo(((CaseInput) other).getName());
    }

    public EmfDataset getDataset() {
        return dataset;
    }

    public void setDataset(EmfDataset dataset) {
        this.dataset = dataset;
    }

    public DatasetType getDatasetType() {
        return datasetType;
    }

    public void setDatasetType(DatasetType datasetType) {
        this.datasetType = datasetType;
    }

    public InputEnvtVar getEnvtVars() {
        return envtVars;
    }

    public void setEnvtVars(InputEnvtVar envtVars) {
        this.envtVars = envtVars;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public String getSubdir() {
        return subdir;
    }

    public void setSubdir(String subdir) {
        this.subdir = subdir;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public InputName getInputName() {
        return inputName;
    }

    public void setInputName(InputName inputName) {
        this.inputName = inputName;
    }

}
