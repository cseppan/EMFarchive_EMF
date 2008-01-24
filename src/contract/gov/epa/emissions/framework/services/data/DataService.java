package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;

public interface DataService {
    
    EmfDataset[] getDatasets(DatasetType datasetType) throws EmfException;

    EmfDataset[] getDatasets(int datasetTypeId, String nameContaining) throws EmfException;

    EmfDataset[] getDatasets() throws EmfException;
    
    EmfDataset getDataset(Integer datasetId) throws EmfException;

    String[] getDatasetValues(Integer datasetId) throws EmfException;

    EmfDataset getDataset(String datasetName) throws EmfException;

    EmfDataset obtainLockedDataset(User owner, EmfDataset dataset) throws EmfException;

    EmfDataset releaseLockedDataset(EmfDataset locked) throws EmfException;

    EmfDataset updateDataset(EmfDataset dataset) throws EmfException;
    
    void deleteDatasets(User owner, EmfDataset[] datasets) throws EmfException;
    
    Version obtainedLockOnVersion(User user, int id) throws EmfException;
    
    void updateVersionNReleaseLock(Version locked) throws EmfException;

}