package gov.epa.emissions.framework.client.data.region;

import gov.epa.emissions.framework.services.EmfException;

public interface NewRegionPresenter {
    
    void display() throws EmfException;
    
    void save() throws EmfException;
    
}
