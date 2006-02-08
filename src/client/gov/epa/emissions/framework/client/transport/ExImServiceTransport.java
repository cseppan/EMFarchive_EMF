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
        return callFactory.createEmfCall("Export-Import Service");
    }

    public void startImport(User user, String folderPath, String fileName, EmfDataset dataset) throws EmfException {
        EmfCall call = call();

        call.setOperation("startImport");
        call.addParam("user", mappings.user());
        call.addParam("folderpath", mappings.string());
        call.addParam("filename", mappings.strings());
        call.addParam("dataset", mappings.dataset());
        call.setVoidReturnType();

        call.request(new Object[] { user, folderPath, fileName, dataset });
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

    public void startMultipleFileImport(User user, String folderPath, String[] fileName, DatasetType datasetType)
            throws EmfException {
        EmfCall call = call();

        call.setOperation("startMultipleFileImport");
        call.addParam("user", mappings.user());
        call.addParam("folderpath", mappings.string());
        call.addParam("filename", mappings.strings());
        call.addParam("dataset", mappings.datasetType());
        call.setVoidReturnType();

        call.request(new Object[] { user, folderPath, fileName, datasetType });
    }
}
