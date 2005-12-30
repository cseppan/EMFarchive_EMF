package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.Country;
import gov.epa.emissions.framework.services.DataCommonsService;

import javax.xml.rpc.ParameterMode;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
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

    public void addCountry(Country country) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            call.setOperationName(mappings.qname("addCountry"));
            call.addParameter("country", mappings.country(), ParameterMode.IN);
            call.setReturnType(Constants.XSD_ANY);

            call.invoke(new Object[] { country });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to add country: " + country.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to add country: " + country.getName(), e);
        }
    }

    public void updateCountry(Country country) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            call.setOperationName(mappings.qname("updateCountry"));
            call.addParameter("country", mappings.country(), ParameterMode.IN);
            call.setReturnType(Constants.XSD_ANY);

            call.invoke(new Object[] { country });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to update country: " + country.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to update country: " + country.getName(), e);
        }
    }

    public Country[] getCountries() throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            call.setOperationName(mappings.qname("getCountries"));
            call.setReturnType(mappings.countries());

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
            call.setOperationName(mappings.qname("getSectors"));
            call.setReturnType(mappings.sectors());

            return (Sector[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not fetch Sectors", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not fetch Sectors", e);
        }

        return null;
    }

    public void updateSector(Sector sector) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            call.setOperationName(mappings.qname("updateSector"));
            call.addParameter("sector", mappings.sector(), ParameterMode.IN);
            call.setReturnType(Constants.XSD_ANY);

            call.invoke(new Object[] { sector });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not update Sector: " + sector.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not update Sector: " + sector.getName(), e);
        }
    }

    public void addSector(Sector sector) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);

            call.setOperationName(mappings.qname("addSector"));
            call.addParameter("sector", mappings.sector(), ParameterMode.IN);
            call.setReturnType(Constants.XSD_ANY);

            call.invoke(new Object[] { sector });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not add Sector: " + sector.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not add Sector: " + sector.getName(), e);
        }
    }

    public Keyword[] getKeywords() throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            call.setOperationName(mappings.qname("getKeywords"));
            call.setReturnType(mappings.keywords());

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
        throw new EmfException(extractMessage(fault.getMessage()));
    }

    public Sector getSectorLock(User user, Sector sector) throws EmfException {
        Sector modifiedSector = null;
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            call.setOperationName(mappings.qname("getSectorLock"));
            call.addParameter("user", mappings.user(), ParameterMode.IN);
            call.addParameter("sector", mappings.sector(), ParameterMode.IN);
            call.setReturnType(mappings.sector());

            modifiedSector = (Sector) call.invoke(new Object[] { user, sector });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not get sector lock: " + sector.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not get sector lock: " + sector.getName(), e);
        }
        return modifiedSector;
    }

    public Sector updateSector(User user, Sector sector) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            call.setOperationName(mappings.qname("updateSector"));
            call.addParameter("user", mappings.user(), ParameterMode.IN);
            call.addParameter("sector", mappings.sector(), ParameterMode.IN);
            call.setReturnType(mappings.sector());

            return (Sector) call.invoke(new Object[] { user, sector });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not update locked Sector: " + sector.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not update locked Sector: " + sector.getName(), e);
        }

        return null;
    }

    public Sector releaseSectorLock(User user, Sector sector) throws EmfException {
        Sector modifiedSector = null;
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            call.setOperationName(mappings.qname("releaseSectorLock"));
            call.addParameter("user", mappings.user(), ParameterMode.IN);
            call.addParameter("sector", mappings.sector(), ParameterMode.IN);
            call.setReturnType(mappings.sector());

            modifiedSector = (Sector) call.invoke(new Object[] { user, sector });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not release sector lock: " + sector.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not release sector lock: " + sector.getName(), e);
        }
        return modifiedSector;
    }

    public DatasetType[] getDatasetTypes() throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            call.setOperationName(mappings.qname("getDatasetTypes"));
            call.setReturnType(mappings.sectors());

            return (DatasetType[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not fetch DatasetTypes", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not fetch DatasetTypes", e);
        }

        return null;
    }

    public DatasetType getDatasetTypeLock(User user, DatasetType type) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            call.setOperationName(mappings.qname("getDatasetTypeLock"));
            mappings.addParam(call, "user", mappings.user());
            mappings.addParam(call, "type", mappings.datasetType());
            mappings.setReturnType(call, mappings.datasetType());

            return (DatasetType) call.invoke(new Object[] { user, type });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not get DatasetType lock: " + type.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not get DatasetType lock: " + type.getName(), e);
        }

        return null;
    }

    public DatasetType updateDatasetType(User user, DatasetType type) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            call.setOperationName(mappings.qname("updateDatasetType"));
            mappings.addParam(call, "user", mappings.user());
            mappings.addParam(call, "type", mappings.datasetType());
            mappings.setReturnType(call, mappings.datasetType());

            return (DatasetType) call.invoke(new Object[] { user, type });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not update DatasetType: " + type.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not update DatasetType: " + type.getName(), e);
        }

        return null;
    }

    public DatasetType releaseDatasetTypeLock(User user, DatasetType type) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            call.setOperationName(mappings.qname("releaseDatasetTypeLock"));
            mappings.addParam(call, "user", mappings.user());
            mappings.addParam(call, "type", mappings.datasetType());
            mappings.setReturnType(call, mappings.datasetType());

            return (DatasetType) call.invoke(new Object[] { user, type });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not release DatasetType lock: " + type.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not release DatasetType lock: " + type.getName(), e);
        }

        return null;
    }
}
