package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.data.LockableImpl;

import java.io.Serializable;

@SuppressWarnings("serial")
public class FastRunInput extends LockableImpl implements Serializable {

    private String name;

    private int id;

    private String orlDataset;

    private String ancillaryORLDataset;

    private String derivedORLPointDataset;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrlDataset() {
        return orlDataset;
    }

    public void setOrlDataset(String orlDataset) {
        this.orlDataset = orlDataset;
    }

    public String getAncillaryORLDataset() {
        return ancillaryORLDataset;
    }

    public void setAncillaryORLDataset(String ancillaryORLDataset) {
        this.ancillaryORLDataset = ancillaryORLDataset;
    }

    public String getDerivedORLPointDataset() {
        return derivedORLPointDataset;
    }

    public void setDerivedORLPointDataset(String derivedORLPointDataset) {
        this.derivedORLPointDataset = derivedORLPointDataset;
    }
    
    @Override
    public boolean equals(Object o) {

        boolean equals = false;
        if (o instanceof FastRunInput) {

            FastRunInput that = (FastRunInput) o;
            equals = this.id == that.id;
        }

        return equals;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public FastRunInput clone() {
        
        FastRunInput clone = new FastRunInput();
        clone.orlDataset = this.orlDataset;
        clone.ancillaryORLDataset = this.ancillaryORLDataset;
        clone.derivedORLPointDataset = this.derivedORLPointDataset;
        clone.name = this.name;
        
        return clone;
    }
}
