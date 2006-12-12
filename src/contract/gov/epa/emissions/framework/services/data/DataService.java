package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;

public interface DataService {
    
    EmfDataset[] getDatasets(DatasetType datasetType) throws EmfException;

    EmfDataset[] getDatasets() throws EmfException;

    EmfDataset obtainLockedDataset(User owner, EmfDataset dataset) throws EmfException;

    EmfDataset releaseLockedDataset(EmfDataset locked) throws EmfException;

    EmfDataset updateDataset(EmfDataset dataset) throws EmfException;
    
    void deleteDatasets(User owner, EmfDataset[] datasets) throws EmfException;

}