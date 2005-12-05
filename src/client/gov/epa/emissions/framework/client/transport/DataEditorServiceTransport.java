package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorService;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataEditorServiceTransport implements DataEditorService {
    private static Log log = LogFactory.getLog(DataEditorServiceTransport.class);

    private Call call = null;

    private DataEditorMappings mappings;

    public DataEditorServiceTransport(Call call, String endPoint) {
        this.call = call;
        try {
            call.setTargetEndpointAddress(new URL(endPoint));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not connect to Data Editor service at " + endPoint);
        }

        mappings = new DataEditorMappings();
        mappings.register(call);
    }

    private String extractMessage(String faultReason) {
        return faultReason.substring(faultReason.indexOf("Exception: ") + 11);
    }

    private void throwExceptionDueToServiceErrors(String message, Exception e) throws EmfException {
        log.error(message, e);
        throw new EmfException(message, e.getMessage(), e);
    }

    private void throwExceptionOnAxisFault(String message, AxisFault fault) throws EmfException {
        log.error(message, fault);
        throw new EmfException(extractMessage(fault.getMessage()));
    }

    public Page getPage(String tableName, int pageNumber) throws EmfException {
        try {
            mappings.addStringParam(call, "tableName");
            mappings.addIntegerParam(call, "pageNumber");

            mappings.setOperation(call, "getPage");
            mappings.setReturnType(call, mappings.page());

            return (Page) call.invoke(new Object[] { tableName, new Integer(pageNumber) });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get page: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get page: ", e);
        } finally {
            call.removeAllParameters();
        }

        return null;
    }

    public int getPageCount(String tableName) throws EmfException {
        try {
            mappings.setOperation(call, "getPageCount");
            mappings.addStringParam(call, "tableName");
            mappings.setIntegerReturnType(call);

            Integer cnt = (Integer) call.invoke(new Object[] { tableName });

            return cnt.intValue();
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get count: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get count: ", e);
        } finally {
            call.removeAllParameters();
        }

        return -1;
    }

    public Page getPageWithRecord(String tableName, int recordId) throws EmfException {
        Page page = null;

        try {
            mappings.setOperation(call, "getPageWithRecord");
            mappings.setReturnType(call, mappings.page());

            mappings.addStringParam(call, "tableName");
            mappings.addIntegerParam(call, "recordId");

            page = (Page) call.invoke(new Object[] { tableName, new Integer(recordId) });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get page: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get page: ", e);
        } finally {
            call.removeAllParameters();
        }

        return page;
    }

    public int getTotalRecords(String tableName) throws EmfException {
        try {
            mappings.setOperation(call, "getTotalRecords");
            mappings.addStringParam(call, "tableName");
            mappings.setIntegerReturnType(call);

            Integer cnt = (Integer) call.invoke(new Object[] { tableName });
            return cnt.intValue();
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get count: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get count: ", e);
        } finally {
            call.removeAllParameters();
        }

        return -1;
    }

    public void close() throws EmfException {
        try {
            mappings.setOperation(call, "close");
            mappings.setAnyReturnType(call);

            call.invoke(new Object[0]);
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get count: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get count: ", e);
        } finally {
            call.removeAllParameters();
        }
    }

    public Version derive(Version baseVersion) throws EmfException {
        try {
            mappings.addParam(call, "baseVersion", mappings.version());
            mappings.setOperation(call, "derive");
            mappings.setReturnType(call, mappings.version());

            return (Version) call.invoke(new Object[] { baseVersion });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get derive Version from base Version: " + baseVersion.getVersion()
                    + " for Dataset: " + baseVersion.getDatasetId(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get derive Version from base Version: "
                    + baseVersion.getVersion() + " for Dataset: " + baseVersion.getDatasetId(), e);
        } finally {
            call.removeAllParameters();
        }

        return null;
    }

    public void submit(ChangeSet changeset) {
        // TODO Auto-generated method stub

    }

    public Version markFinal(Version derived) throws EmfException {
        try {
            mappings.addParam(call, "derived", mappings.version());
            mappings.setOperation(call, "markFinal");
            mappings.setReturnType(call, mappings.version());

            return (Version) call.invoke(new Object[] { derived });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to mark a derived Version: " + derived.getVersion() + " as Final", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to mark a derived Version: " + derived.getVersion() + " as Final",
                    e);
        } finally {
            call.removeAllParameters();
        }

        return null;
    }

    public Version[] getVersions(long datasetId) throws EmfException {
        try {
            mappings.addLongParam(call, "datasetId");
            mappings.setOperation(call, "getVersions");
            mappings.setReturnType(call, mappings.versions());

            return (Version[]) call.invoke(new Object[] { new Long(datasetId) });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get versions for dataset: " + datasetId, fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get versions for dataset: " + datasetId, e);
        } finally {
            call.removeAllParameters();
        }

        return null;
    }

}
