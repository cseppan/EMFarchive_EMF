package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataViewService;
import gov.epa.emissions.framework.services.EMFConstants;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataViewServiceTransport implements DataViewService {
    private static Log log = LogFactory.getLog(DataViewServiceTransport.class);

    private Call call = null;

    private EmfMappings mappings;

    public DataViewServiceTransport(Call call, String endPoint) {
        this.call = call;
        try {
            call.setTargetEndpointAddress(new URL(endPoint));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not connect to DataView service at " + endPoint);
        }

        mappings = new EmfMappings();
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
        String msg = extractMessage(fault.getMessage());

        if (fault.getCause() != null) {
            if (fault.getCause().getMessage().equals(EMFConstants.CONNECTION_REFUSED)) {
                msg = "EMF server not responding";
            }
        }
        throw new EmfException(msg);
    }

    public Page applyConstraints(DataAccessToken token, String rowFilter, String sortOrder) throws EmfException {
        try {
            mappings.addParam(call, "token", mappings.dataAccessToken());
            mappings.addStringParam(call, "rowFilter");
            mappings.addStringParam(call, "sortOrder");
            
            mappings.setOperation(call, "applyConstraints");
            mappings.setReturnType(call, mappings.page());

            return (Page) call.invoke(new Object[] { token, rowFilter, sortOrder });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to apply constraints: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to apply constraints: ", e);
        } finally {
            call.removeAllParameters();
        }

        return null;
    }

    public Page getPage(DataAccessToken token, int pageNumber) throws EmfException {
        try {
            mappings.addParam(call, "token", mappings.dataAccessToken());
            mappings.addIntegerParam(call, "pageNumber");

            mappings.setOperation(call, "getPage");
            mappings.setReturnType(call, mappings.page());

            return (Page) call.invoke(new Object[] { token, new Integer(pageNumber) });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get page: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get page: ", e);
        } finally {
            call.removeAllParameters();
        }

        return null;
    }

    public int getPageCount(DataAccessToken token) throws EmfException {
        try {
            mappings.setOperation(call, "getPageCount");
            mappings.addParam(call, "token", mappings.dataAccessToken());
            mappings.setIntegerReturnType(call);

            Integer cnt = (Integer) call.invoke(new Object[] { token });

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

    public Page getPageWithRecord(DataAccessToken token, int record) throws EmfException {
        try {
            mappings.setOperation(call, "getPageWithRecord");
            mappings.setReturnType(call, mappings.page());

            mappings.addParam(call, "token", mappings.dataAccessToken());
            mappings.addIntegerParam(call, "record");

            return (Page) call.invoke(new Object[] { token, new Integer(record) });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get page: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get page: ", e);
        } finally {
            call.removeAllParameters();
        }

        return null;
    }

    public int getTotalRecords(DataAccessToken token) throws EmfException {
        try {
            mappings.setOperation(call, "getTotalRecords");
            mappings.addParam(call, "token", mappings.dataAccessToken());
            mappings.setIntegerReturnType(call);

            Integer cnt = (Integer) call.invoke(new Object[] { token });
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

    public DataAccessToken openSession(DataAccessToken token) throws EmfException {
        try {
            mappings.addParam(call, "token", mappings.dataAccessToken());
            mappings.setOperation(call, "openSession");
            mappings.setReturnType(call, mappings.dataAccessToken());

            return (DataAccessToken) call.invoke(new Object[] { token });
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not open session for " + token.key(), e);
        } finally {
            call.removeAllParameters();
        }

        return null;
    }

    public void closeSession(DataAccessToken token) throws EmfException {
        try {
            mappings.addParam(call, "token", mappings.dataAccessToken());
            mappings.setOperation(call, "closeSession");
            mappings.setVoidReturnType(call);

            call.invoke(new Object[] { token });
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not close session for " + token.key(), e);
        } finally {
            call.removeAllParameters();
        }
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
