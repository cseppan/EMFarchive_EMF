package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.DoubleValue;

public interface DataService {

    EmfDataset[] getDatasets(DatasetType datasetType) throws EmfException;

    EmfDataset[] getDatasetsWithFilter(int datasetTypeId, String nameContains) throws EmfException;

    EmfDataset[] getDatasets(int datasetTypeId, String nameContaining) throws EmfException;

    EmfDataset[] getDatasets(String nameContains) throws EmfException;

    EmfDataset getDataset(Integer datasetId) throws EmfException;

    String[] getDatasetValues(Integer datasetId) throws EmfException;
    
    void addExternalSources(String folder, String[] files, int datasetId) throws EmfException;
    
    void updateExternalSources(int datasetId, String newDir) throws EmfException;
    
    ExternalSource[] getExternalSources(int datasetId, int limit) throws EmfException;
    
    boolean isExternal (int datasetId) throws EmfException;

    EmfDataset getDataset(String datasetName) throws EmfException;

    EmfDataset obtainLockedDataset(User owner, EmfDataset dataset) throws EmfException;

    EmfDataset releaseLockedDataset(User user, EmfDataset locked) throws EmfException;

    EmfDataset updateDataset(EmfDataset dataset) throws EmfException;

    void deleteDatasets(User owner, EmfDataset[] datasets) throws EmfException;

    Version obtainedLockOnVersion(User user, int id) throws EmfException;

    void updateVersionNReleaseLock(Version locked) throws EmfException;

    void purgeDeletedDatasets(User user) throws EmfException;

    int getNumOfDeletedDatasets(User user) throws EmfException;
    
    int getNumOfDatasets(int datasetTypeId, String nameContains) throws EmfException;
    
    int getNumOfDatasets(String nameContains) throws EmfException;

    String getTableAsString(String qualifiedTableName) throws EmfException;

    long getTableRecordCount(String qualifiedTableName) throws EmfException;

    void appendData(User user, int srcDSid, int srcDSVersion, String filter, int targetDSid, int targetDSVersion,
            DoubleValue targetStartLineNumber) throws EmfException;

    void checkIfDeletable(User user, int datasetID) throws EmfException;
    
    boolean checkTableDefinitions(int srcDSid, int targetDSid) throws EmfException;
    
    void replaceColValues(String table, String colName, String find, String replaceWith, Version version, String rowFilter) throws EmfException;

    void copyDataset(int datasetId, Version version, User user) throws EmfException;
}