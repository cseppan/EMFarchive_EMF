package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImService;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;

public class ExImServiceTransport implements ExImService {

    private CallFactory callFactory;

    private EmfMappings mappings;

    public ExImServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new EmfMappings();
    }

    public void startImport(User user, String folderPath, String fileName, EmfDataset dataset) throws EmfException {
        try {
            Call call = callFactory.createCall();
            mappings.register(call);

            mappings.setOperation(call, "startImport");

            mappings.addParam(call, "user", mappings.user());
            mappings.addParam(call, "folderpath", mappings.string());
            mappings.addParam(call, "filename", mappings.strings());
            mappings.addParam(call, "dataset", mappings.dataset());

            mappings.setAnyReturnType(call);

            call.invoke(new Object[] { user, folderPath, fileName, dataset });
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to Export-Import Service");
        }
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
        try {
            Call call = callFactory.createCall();
            mappings.register(call);

            mappings.setOperation(call, operationName);
            mappings.addParam(call, "user", mappings.user());
            mappings.addParam(call, "datasets", mappings.datasets());
            mappings.addStringParam(call, "foldername");
            mappings.addBooleanParameter(call, "purpose");
            mappings.setAnyReturnType(call);

            call.invoke(new Object[] { user, datasets, folder, purpose });

        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to Export-Import Service");
        }
    }

    public void startMultipleFileImport(User user, String folderPath, String[] fileName, DatasetType datasetType)
            throws EmfException {
        try {
            Call call = callFactory.createCall();
            mappings.register(call);

            mappings.setOperation(call, "startMultipleFileImport");

            mappings.addParam(call, "user", mappings.user());
            mappings.addParam(call, "folderpath", mappings.string());
            mappings.addParam(call, "filename", mappings.strings());
            mappings.addParam(call, "dataset", mappings.datasetType());

            mappings.setAnyReturnType(call);

            call.invoke(new Object[] { user, folderPath, fileName, datasetType });
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to Export-Import Service");
        }
    }
}
