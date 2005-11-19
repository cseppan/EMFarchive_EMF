package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;

public interface ExImServices extends EMFServices {

    void startImport(User user, String folderPath, String fileName, EmfDataset dataset) throws EmfException;

    void startExport(User user, EmfDataset[] datasets, String folder, String purpose) throws EmfException;

    void startExportWithOverwrite(User user, EmfDataset[] datasets, String folder, String purpose) throws EmfException;

    String getImportBaseFolder() throws EmfException;

    String getExportBaseFolder() throws EmfException;
}
