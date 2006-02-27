package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImService;

public class ExImServiceTransport implements ExImService {

    private CallFactory callFactory;

    private EmfMappings mappings;

    public ExImServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new EmfMappings();
    }

    private EmfCall call() throws EmfException {
        return callFactory.createCall("Export-Import Service");
    }

    public void startExport(User user, EmfDataset[] datasets, String folder, String purpose) throws EmfException {
        doExport("startExport", user, datasets, folder, purpose);
    }

    public void startExportWithOverwrite(User user, EmfDataset[] datasets, String folder, String purpose)
            throws EmfException {
        doExport("startExportWithOverwrite", user, datasets, folder, purpose);
    }

    private void doExport(String operationName, User user, EmfDataset[] datasets, String folder, String purpose)
            throws EmfException {
        EmfCall call = call();

        call.setOperation(operationName);
        call.addParam("user", mappings.user());
        call.addParam("datasets", mappings.datasets());
        call.addStringParam("foldername");
        call.addBooleanParameter("purpose");
        call.setVoidReturnType();

        call.request(new Object[] { user, datasets, folder, purpose });
    }

    public void importDatasetUsingSingleFile(User user, String folderPath, String filename, EmfDataset dataset)
            throws EmfException {
        EmfCall call = call();

        call.setOperation("importDatasetUsingSingleFile");
        call.addParam("user", mappings.user());
        call.addParam("folderpath", mappings.string());
        call.addParam("filename", mappings.string());
        call.addParam("dataset", mappings.dataset());
        call.setVoidReturnType();

        call.request(new Object[] { user, folderPath, filename, dataset });
    }

    public void importDatasetUsingMultipleFiles(User user, String folderPath, String[] filenames, EmfDataset dataset)
            throws EmfException {
        EmfCall call = call();

        call.setOperation("importDatasetUsingMultipleFiles");
        call.addParam("user", mappings.user());
        call.addParam("folderpath", mappings.string());
        call.addParam("filenames", mappings.strings());
        call.addParam("dataset", mappings.dataset());
        call.setVoidReturnType();

        call.request(new Object[] { user, folderPath, filenames, dataset });
    }

    public void importDatasetForEveryFileInPattern(User user, String folderPath, String filePattern,
            DatasetType datasetType) throws EmfException {
        EmfCall call = call();

        call.setOperation("importDatasetForEveryFileInPattern");
        call.addParam("user", mappings.user());
        call.addParam("folderpath", mappings.string());
        call.addParam("filePattern", mappings.string());
        call.addParam("dataset", mappings.datasetType());
        call.setVoidReturnType();

        call.request(new Object[] { user, folderPath, filePattern, datasetType });
    }

    public void importDatasetForEachFile(User user, String folderPath, String[] filenames, DatasetType datasetType)
            throws EmfException {
        EmfCall call = call();

        call.setOperation("importDatasetForEachFile");
        call.addParam("user", mappings.user());
        call.addParam("folderPath", mappings.string());
        call.addParam("filenames", mappings.strings());
        call.addParam("datasetType", mappings.datasetType());
        call.setVoidReturnType();

        call.request(new Object[] { user, folderPath, filenames, datasetType });
    }
}
