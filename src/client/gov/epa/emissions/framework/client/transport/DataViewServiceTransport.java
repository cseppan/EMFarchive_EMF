package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataViewService;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;

public class DataViewServiceTransport implements DataViewService {
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

    public Page applyConstraints(DataAccessToken token, String rowFilter, String sortOrder) throws EmfException {
        try {
            mappings.addParam(call, "token", mappings.dataAccessToken());
            mappings.addStringParam(call, "rowFilter");
            mappings.addStringParam(call, "sortOrder");

            mappings.setOperation(call, "applyConstraints");
            mappings.setReturnType(call, mappings.page());

            return (Page) call.invoke(new Object[] { token, rowFilter, sortOrder });
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataView Service");
        } finally {
            call.removeAllParameters();
        }
    }

    public Page getPage(DataAccessToken token, int pageNumber) throws EmfException {
        try {
            mappings.addParam(call, "token", mappings.dataAccessToken());
            mappings.addIntegerParam(call, "pageNumber");

            mappings.setOperation(call, "getPage");
            mappings.setReturnType(call, mappings.page());

            return (Page) call.invoke(new Object[] { token, new Integer(pageNumber) });
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataView Service");
        } finally {
            call.removeAllParameters();
        }
    }

    public int getPageCount(DataAccessToken token) throws EmfException {
        try {
            mappings.setOperation(call, "getPageCount");
            mappings.addParam(call, "token", mappings.dataAccessToken());
            mappings.setIntegerReturnType(call);

            Integer cnt = (Integer) call.invoke(new Object[] { token });

            return cnt.intValue();
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataView Service");
        } finally {
            call.removeAllParameters();
        }
    }

    public Page getPageWithRecord(DataAccessToken token, int record) throws EmfException {
        try {
            mappings.setOperation(call, "getPageWithRecord");
            mappings.setReturnType(call, mappings.page());

            mappings.addParam(call, "token", mappings.dataAccessToken());
            mappings.addIntegerParam(call, "record");

            return (Page) call.invoke(new Object[] { token, new Integer(record) });
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataView Service");
        } finally {
            call.removeAllParameters();
        }
    }

    public int getTotalRecords(DataAccessToken token) throws EmfException {
        try {
            mappings.setOperation(call, "getTotalRecords");
            mappings.addParam(call, "token", mappings.dataAccessToken());
            mappings.setIntegerReturnType(call);

            Integer cnt = (Integer) call.invoke(new Object[] { token });
            return cnt.intValue();
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataView Service");
        } finally {
            call.removeAllParameters();
        }
    }

    public DataAccessToken openSession(DataAccessToken token) throws EmfException {
        try {
            mappings.addParam(call, "token", mappings.dataAccessToken());
            mappings.setOperation(call, "openSession");
            mappings.setReturnType(call, mappings.dataAccessToken());

            return (DataAccessToken) call.invoke(new Object[] { token });
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataView Service");
        } finally {
            call.removeAllParameters();
        }
    }

    public void closeSession(DataAccessToken token) throws EmfException {
        try {
            mappings.addParam(call, "token", mappings.dataAccessToken());
            mappings.setOperation(call, "closeSession");
            mappings.setVoidReturnType(call);

            call.invoke(new Object[] { token });
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataView Service");
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
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataView Service");
        } finally {
            call.removeAllParameters();
        }
    }

}
