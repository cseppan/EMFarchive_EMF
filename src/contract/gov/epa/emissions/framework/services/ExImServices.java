package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;

public interface ExImServices extends EMFServices {

    public void startImport(User user, String folderPath, String fileName, EmfDataset dataset) throws EmfException;

    // FIXME: have two separate explicit interfaces for overwrite(default) and
    // no-overwrite
    public void startExport(User user, EmfDataset[] datasets, String folder, boolean overwrite, String purpose)
            throws EmfException;

    public String getImportBaseFolder() throws EmfException;

    public String getExportBaseFolder() throws EmfException;
}
