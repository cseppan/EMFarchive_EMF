package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;

public interface DataService {

    EmfDataset[] getDatasets() throws EmfException;

    void updateDataset(EmfDataset dataset) throws EmfException;

}