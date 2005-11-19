package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExImServicesTransport implements ExImServices {
    private static Log LOG = LogFactory.getLog(ExImServicesTransport.class);

    private CallFactory callFactory;

    private ExImServiceMappings mappings;

    public ExImServicesTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new ExImServiceMappings();
    }

    public void startImport(User user, String folderPath, String fileName, EmfDataset dataset) throws EmfException {
        try {
            Call call = callFactory.createCall();
            mappings.register(call);

            mappings.setOperation(call, "startImport");

            mappings.addParam(call, "user", mappings.user());
            mappings.addParam(call, "folderpath", mappings.string());
            mappings.addParam(call, "filename", mappings.string());
            mappings.addParam(call, "dataset", mappings.datasetType());

            mappings.setAnyReturnType(call);

            call.invoke(new Object[] { user, folderPath, fileName, dataset });
        } catch (AxisFault fault) {
            LOG.error("Axis Fault details", fault);
            throw new EmfException(extractMessage(fault.getMessage()));
        } catch (Exception e) {
            LOG.error("Error communicating with WS end point", e);
        }
    }

    public String getImportBaseFolder() throws EmfException {
        try {
            Call call = callFactory.createCall();
            mappings.setOperation(call, "getImportBaseFolder");
            mappings.setStringReturnType(call);

            return (String) call.invoke(new Object[0]);
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not fetch Base Folder for Import", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not fetch Base Folder for Import", e);
        }

        return null;
    }

    public String getExportBaseFolder() throws EmfException {
        try {
            Call call = callFactory.createCall();
            mappings.setOperation(call, "getExportBaseFolder");
            mappings.setStringReturnType(call);

            return (String) call.invoke(new Object[0]);
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not fetch Base Folder for Export", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not fetch Base Folder for Export", e);
        }

        return null;
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
            throwExceptionOnAxisFault("Could not export datasets for user: " + user.getUsername(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not export datasets for user: " + user.getUsername(), e);
        }
    }

    private String extractMessage(String faultReason) {
        return faultReason.substring(faultReason.indexOf("Exception: ") + 11);
    }

    private void throwExceptionDueToServiceErrors(String message, Exception e) throws EmfException {
        LOG.error(message, e);
        throw new EmfException(message, e.getMessage(), e);
    }

    private void throwExceptionOnAxisFault(String message, AxisFault fault) throws EmfException {
        LOG.error(message, fault);
        throw new EmfException(extractMessage(fault.getMessage()));
    }

}
