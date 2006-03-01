package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;

public interface ExImService extends EMFService {

    void importDatasets(User user, String folderPath, String[] fileNames, DatasetType datasetType) throws EmfException;

    void importDataset(User user, String folderPath, String[] filenames, DatasetType datasetType, String datasetName) throws EmfException;

    void startExport(User user, EmfDataset[] datasets, String folder, String purpose) throws EmfException;

    void startExportWithOverwrite(User user, EmfDataset[] datasets, String folder, String purpose) throws EmfException;

}
