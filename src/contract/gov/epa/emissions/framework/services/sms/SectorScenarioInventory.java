package gov.epa.emissions.framework.services.sms;

import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;

public class SectorScenarioInventory implements Serializable {

    private EmfDataset dataset;

    private int version;

    public SectorScenarioInventory() {
        //
    }

    public SectorScenarioInventory(EmfDataset dataset) {
        this.dataset = dataset;
    }

    public SectorScenarioInventory(EmfDataset dataset, int version) {
        this.dataset = dataset;
        this.version = version;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SectorScenarioInventory)) {
            return false;
        }

        SectorScenarioInventory other = (SectorScenarioInventory) obj;

        return (dataset.getId() == other.getDataset().getId()) 
            && (version == other.getVersion());
    }

    public int hashCode() {
        return dataset != null ? dataset.hashCode() : "".hashCode();
    }

    public EmfDataset getDataset() {
        return dataset;
    }

    public void setDataset(EmfDataset dataset) {
        this.dataset = dataset;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }
}