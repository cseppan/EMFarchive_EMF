package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorService;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ParameterMode;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.client.Call;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataEditorServiceTransport implements DataEditorService {
    private static Log log = LogFactory.getLog(DataEditorServiceTransport.class);

    private Call call = null;

    public DataEditorServiceTransport(String endPoint, Call call) {
        this.call = call;
        try {
            call.setTargetEndpointAddress(new URL(endPoint));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not connect to Data Editor service at " + endPoint);
        }
    }

    public String getName() throws Exception {
        try {
            call.setOperationName("getName");
            call.setReturnType(Constants.XSD_ANY);

            return (String) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get name: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get name: ", e);
        } finally {
            call.removeAllParameters();
        }

        return null;
    }

    public void setName(String name) throws Exception {
        try {
            call.setOperationName("setName");
            call.addParameter("name", org.apache.axis.Constants.XSD_STRING, ParameterMode.IN);
            call.setReturnType(Constants.XSD_ANY);

            call.invoke(new Object[] { name });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get name: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get name: ", e);
        } finally {
            call.removeAllParameters();
        }
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
            DataEditorMappings mappings = new DataEditorMappings();
            mappings.register(call);

            call.setOperationName(mappings.qname("getPage"));
            call.setReturnType(mappings.page());

            mappings.addStringParam(call, "tableName");
            mappings.addIntegerParam(call, "pageNumber");

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
            call.setOperationName("getPageCount");
            call.addParameter("tableName", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
            call.setReturnType(Constants.XSD_INT);

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
            DataEditorMappings mappings = new DataEditorMappings();
            mappings.register(call);

            call.setOperationName(mappings.qname("getPageWithRecord"));
            call.setReturnType(mappings.page());

            call.addParameter("tableName", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
            call.addParameter("recordId", org.apache.axis.Constants.XSD_INTEGER, javax.xml.rpc.ParameterMode.IN);

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
            call.setOperationName("getTotalRecords");
            call.addParameter("tableName", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
            call.setReturnType(Constants.XSD_INT);

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
            call.setOperationName("close");
            call.setReturnType(Constants.XSD_ANY);

            call.invoke(new Object[] {});
            call.removeAllParameters();
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get count: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get count: ", e);
        } finally {
            call.removeAllParameters();
        }
    }

    public Version derive(Version baseVersion) {
        return null;
    }

    public void submit(ChangeSet changeset) {
        // TODO Auto-generated method stub

    }

    public void markFinal() {
        // TODO Auto-generated method stub

    }

    public Version[] getVersions(long datasetId) throws EmfException {
        try {
            DataEditorMappings mappings = new DataEditorMappings();
            mappings.register(call);

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
