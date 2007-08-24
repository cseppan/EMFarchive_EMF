package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EMFService;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface ExImService extends EMFService {

    void importDatasets(User user, String folderPath, String[] fileNames, DatasetType datasetType) throws EmfException;

    String[] getFilenamesFromPattern(String folder, String pattern) throws EmfException;

    void importDataset(User user, String folderPath, String[] filenames, DatasetType datasetType, String datasetName)
            throws EmfException;

    void exportDatasets(User user, EmfDataset[] datasets, Version[] versions, String folder, String purpose)
            throws EmfException;

    void exportDatasetsWithOverwrite(User user, EmfDataset[] datasets, Version[] versions, String folder, String purpose)
            throws EmfException;

    Version getVersion(Dataset dataset, int version) throws EmfException;

    // Added by Conrad 07/12/2007 for new functions to
    // export datasets from the client with only
    //dataset ids

    void exportDatasetids(User user, Integer[] datasetIds, Version[] versions, String folder, String purpose)
            throws EmfException;

    void exportDatasetids(User user, Integer[] datasetIds, String folder, String purpose) throws EmfException;
    
    void exportDatasetidsWithOverwrite(User user, Integer[] datasetIds, Version[] versions, String folder, String purpose)
            throws EmfException;

    void exportDatasetidsWithOverwrite(User user, Integer[] datasetIds, String folder, String purpose) throws EmfException;
    
}
