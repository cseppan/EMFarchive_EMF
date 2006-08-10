package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ExImService;

public class ExImServiceTransport implements ExImService {

    private DataMappings mappings;

    private EmfCall call;

    public ExImServiceTransport(EmfCall call) {
        this.call = call;
        mappings = new DataMappings();
    }

    public void exportDatasets(User user, EmfDataset[] datasets, Version[] versions, String folder, String purpose) throws EmfException {
        doExport("exportDatasets", user, datasets, versions, folder, purpose);
    }

    public void exportDatasetsWithOverwrite(User user, EmfDataset[] datasets, Version[] versions, String folder, String purpose)
            throws EmfException {
        doExport("exportDatasetsWithOverwrite", user, datasets, versions, folder, purpose);
    }

    private void doExport(String operationName, User user, EmfDataset[] datasets, Version[] versions, String folder, String purpose)
            throws EmfException {
        call.setOperation(operationName);
        call.addParam("user", mappings.user());
        call.addParam("datasets", mappings.datasets());
        call.addParam("versions", mappings.versions());
        call.addStringParam("folder");
        call.addBooleanParameter("purpose");
        call.setVoidReturnType();

        call.request(new Object[] { user, datasets, versions, folder, purpose });
    }

    public void importDataset(User user, String folderPath, String[] fileNames, DatasetType datasetType,
            String datasetName) throws EmfException {
        call.setOperation("importDataset");
        call.addParam("user", mappings.user());
        call.addParam("folderPath", mappings.string());
        call.addParam("fileNames", mappings.strings());
        call.addParam("datasetType", mappings.datasetType());
        call.addParam("datasetName", mappings.string());
        call.setVoidReturnType();

        call.request(new Object[] { user, folderPath, fileNames, datasetType, datasetName });
    }

    public void importDatasets(User user, String folderPath, String[] fileNames, DatasetType datasetType)
            throws EmfException {
        call.setOperation("importDatasets");
        call.addParam("user", mappings.user());
        call.addParam("folderPath", mappings.string());
        call.addParam("fileNames", mappings.strings());
        call.addParam("datasetType", mappings.datasetType());
        call.setVoidReturnType();

        call.request(new Object[] { user, folderPath, fileNames, datasetType });
    }

    public String[] getFilenamesFromPattern(String folder, String pattern) throws EmfException {
        call.setOperation("getFilenamesFromPattern");
        call.addParam("folder", mappings.string());
        call.addParam("pattern", mappings.string());
        call.setReturnType(mappings.strings());

        return (String[]) call.requestResponse(new Object[] { folder, pattern });
    }

    public Version getVersion(Dataset dataset, int version) throws EmfException {
        call.setOperation("getVersion");
        call.addIntegerParam("dataset");
        call.addIntegerParam("version");
        call.setReturnType(mappings.version());

        return (Version) call.requestResponse(new Object[] { dataset, new Integer(version) });
    }

}
