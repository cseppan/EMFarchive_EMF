package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;

public interface ExImService extends EMFService {

    void importDatasetUsingSingleFile(User user, String folderPath, String fileName, EmfDataset dataset)
            throws EmfException;

    void importDatasetUsingMultipleFiles(User user, String folderPath, String[] fileNames, EmfDataset dataset)
            throws EmfException;

    void importDatasetForEveryFileInPattern(User user, String folderPath, String filePattern, DatasetType datasetType)
            throws EmfException;

    void importDatasetForEachFile(User user, String folderPath, String[] fileName, DatasetType datasetType)
            throws EmfException;

    void startExport(User user, EmfDataset[] datasets, String folder, String purpose) throws EmfException;

    void startExportWithOverwrite(User user, EmfDataset[] datasets, String folder, String purpose) throws EmfException;

}
