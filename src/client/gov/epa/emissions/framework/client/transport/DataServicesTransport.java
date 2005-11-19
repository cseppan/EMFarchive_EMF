package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.Country;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.User;

import javax.xml.rpc.ParameterMode;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.client.Call;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataServicesTransport implements DataServices {
    private static Log log = LogFactory.getLog(DataServicesTransport.class);

    private CallFactory callFactory;

    public DataServicesTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
    }

    public EmfDataset[] getDatasets() throws EmfException {
        try {
            Call call = callFactory.createCall();

            DatasetMappings mappings = new DatasetMappings();
            mappings.register(call);
            call.setOperationName(mappings.qname("getDatasets"));
            call.setReturnType(mappings.datasets());

            return (EmfDataset[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to fetch Datasets", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to fetch Datasets", e);
        }

        return null;
    }

    public EmfDataset[] getDatasets(User user) {
        return null;
    }

    public void insertDataset(EmfDataset aDataset) {
    }

    public void updateDataset(EmfDataset dataset) throws EmfException {
        try {
            Call call = callFactory.createCall();

            DatasetMappings mappings = new DatasetMappings();
            mappings.register(call);
            call.setOperationName(mappings.qname("updateDataset"));
            call.addParameter("dataset", mappings.dataset(), ParameterMode.IN);
            call.setReturnType(Constants.XSD_ANY);

            call.invoke(new Object[] { dataset });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to update Dataset: " + dataset.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to update Dataset: " + dataset.getName(), e);
        }
    }

    public void addCountry(Country country) throws EmfException {

        try {
            Call call = callFactory.createCall();

            CountryMappings mappings = new CountryMappings();
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

            CountryMappings mappings = new CountryMappings();
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

            CountryMappings mappings = new CountryMappings();
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

            SectorMappings mappings = new SectorMappings();
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

            SectorMappings mappings = new SectorMappings();
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

            SectorMappings mappings = new SectorMappings();
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
}
