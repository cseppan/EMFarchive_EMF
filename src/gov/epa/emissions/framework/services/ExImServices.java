package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;

public interface ExImServices extends EMFServices {

    public void startImport(User user, String fileName, EmfDataset dataset, DatasetType datasetType) throws EmfException;

    public void startExport(User user, Dataset dataset, String fileName) throws EmfException;
    public DatasetType[] getDatasetTypes() throws EmfException;

    //FIXME: Move this to (Meta)DataServices ??
    public void insertDatasetType(DatasetType datasetType) throws EmfException;

}
