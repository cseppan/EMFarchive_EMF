package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;

public class ControlStrategyInputDataset implements Serializable {

    private int id;

    private long listindex;

    private EmfDataset inputDataset;

    private int version;

    public ControlStrategyInputDataset() {
        //
    }

    public ControlStrategyInputDataset(EmfDataset inputDataset) {
        this.inputDataset = inputDataset;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getListindex() {
        return listindex;
    }

    public void setListindex(long listindex) {
        this.listindex = listindex;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControlStrategyInputDataset)) {
            return false;
        }

        ControlStrategyInputDataset other = (ControlStrategyInputDataset) obj;

        return ((inputDataset != null ? inputDataset.getId() : 0) 
                == other.getInputDataset().getId());
    }

    public int hashCode() {
        return inputDataset != null ? inputDataset.hashCode() : "".hashCode();
    }

    public EmfDataset getInputDataset() {
        return inputDataset;
    }

    public void setInputDataset(EmfDataset inputDataset) {
        this.inputDataset = inputDataset;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }
}