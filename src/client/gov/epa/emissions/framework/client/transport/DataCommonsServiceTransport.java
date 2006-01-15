package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.Country;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.Status;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataCommonsServiceTransport implements DataCommonsService {
    private static Log LOG = LogFactory.getLog(DataCommonsServiceTransport.class);

    private EmfMappings mappings;

    private CallFactory callFactory;

    public DataCommonsServiceTransport(String endPoint) {
        callFactory = new CallFactory(endPoint);
        mappings = new EmfMappings();
    }

    public Country[] getCountries() throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "getCountries");
            mappings.setReturnType(call, mappings.countries());

            return (Country[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to fetch countries", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to fetch countries", e);
        }

        return null;
    }

    public Sector[] getSectors() throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "getSectors");
            mappings.setReturnType(call, mappings.sectors());

            return (Sector[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not fetch Sectors", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not fetch Sectors", e);
        }

        return null;
    }

    public Keyword[] getKeywords() throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "getKeywords");
            mappings.setReturnType(call, mappings.keywords());

            return (Keyword[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to fetch Keywords", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to fetch Keywords", e);
        }

        return null;
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
        String msg=extractMessage(fault.getMessage());
        
        if (fault.getCause()!=null){
            if (fault.getCause().getMessage().equals(EMFConstants.CONNECTION_REFUSED)){
                msg="EMF server not responding";
            }            
        }
        throw new EmfException(msg);
    }


    public Sector obtainLockedSector(User owner, Sector sector) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "obtainLockedSector");
            mappings.addParam(call, "owner", mappings.user());
            mappings.addParam(call, "sector", mappings.sector());
            mappings.setReturnType(call, mappings.sector());

            return (Sector) call.invoke(new Object[] { owner, sector });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not get sector lock: " + sector.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not get sector lock: " + sector.getName(), e);
        }

        return null;
    }

    public Sector updateSector(Sector sector) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "updateSector");
            mappings.addParam(call, "sector", mappings.sector());
            mappings.setReturnType(call, mappings.sector());

            return (Sector) call.invoke(new Object[] { sector });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not update locked Sector: " + sector.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not update locked Sector: " + sector.getName(), e);
        }

        return null;
    }

    public Sector releaseLockedSector(Sector sector) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "releaseLockedSector");
            mappings.addParam(call, "sector", mappings.sector());
            mappings.setReturnType(call, mappings.sector());

            return (Sector) call.invoke(new Object[] { sector });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not release sector lock: " + sector.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not release sector lock: " + sector.getName(), e);
        }

        return null;
    }

    public DatasetType[] getDatasetTypes() throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "getDatasetTypes");
            mappings.setReturnType(call, mappings.datasetTypes());

            return (DatasetType[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not fetch DatasetTypes", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not fetch DatasetTypes", e);
        }

        return null;
    }

    public DatasetType obtainLockedDatasetType(User owner, DatasetType type) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "obtainLockedDatasetType");
            mappings.addParam(call, "owner", mappings.user());
            mappings.addParam(call, "type", mappings.datasetType());
            mappings.setReturnType(call, mappings.datasetType());

            return (DatasetType) call.invoke(new Object[] { owner, type });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not get DatasetType lock: " + type.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not get DatasetType lock: " + type.getName(), e);
        }

        return null;
    }

    public DatasetType updateDatasetType(DatasetType type) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "updateDatasetType");
            mappings.addParam(call, "type", mappings.datasetType());
            mappings.setReturnType(call, mappings.datasetType());

            return (DatasetType) call.invoke(new Object[] { type });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not update DatasetType: " + type.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not update DatasetType: " + type.getName(), e);
        }

        return null;
    }

    public DatasetType releaseLockedDatasetType(User owner, DatasetType type) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "releaseLockedDatasetType");
            mappings.addParam(call, "owner", mappings.user());
            mappings.addParam(call, "type", mappings.datasetType());
            mappings.setReturnType(call, mappings.datasetType());

            return (DatasetType) call.invoke(new Object[] { owner, type });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not release DatasetType lock: " + type.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not release DatasetType lock: " + type.getName(), e);
        }

        return null;
    }

    public Status[] getStatuses(String username) throws EmfException {
        try {
            Call call = callFactory.createCall();
            call.setMaintainSession(true);

            mappings.register(call);
            mappings.setOperation(call, "getStatuses");
            mappings.addStringParam(call, "username");
            mappings.setReturnType(call, mappings.statuses());

            return (Status[]) call.invoke(new Object[] { username });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not get all Status messages for user: " + username, fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not get all Status messages for user: " + username, e);
        }

        return null;
    }
}
