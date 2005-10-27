/*
 * Creation on Aug 29, 2005
 * Eclipse Project Name: EMF
 * File Name: DataServicesTransport.java
 * Author: Conrad F. D'Cruz
 */
/**
 * 
 */

package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.ExternalSource;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.io.SectorCriteria;
import gov.epa.emissions.commons.io.Table;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.Country;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.User;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataServicesTransport implements DataServices {
    private static Log log = LogFactory.getLog(DataServicesTransport.class);

    private String endpoint;

    public DataServicesTransport(String endpt) {
        endpoint = endpt;
    }

    public EmfDataset[] getDatasets() throws EmfException {
        try {
            Call call = call();

            registerDatasetMappings(call);
            call.setOperationName(qname("getDatasets"));
            QName datasetsQName = qname("ns1:EmfDatasets");
            call.setReturnType(datasetsQName);

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
            Call call = call();

            registerDatasetMappings(call);
            call.setOperationName(qname("updateDataset"));
            call.addParameter("dataset", qname("ns1:EmfDataset"), ParameterMode.IN);
            call.setReturnType(org.apache.axis.Constants.XSD_ANY);

            call.invoke(new Object[] { dataset });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to update Dataset: " + dataset.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to update Dataset: " + dataset.getName(), e);
        }
    }

    public void addCountry(Country country) throws EmfException {

        try {
            Call call = call();

            registerCountryMappings(call);
            call.setOperationName(qname("addCountry"));
            call.addParameter("country", qname("ns1:Country"), ParameterMode.IN);
            call.setReturnType(org.apache.axis.Constants.XSD_ANY);

            call.invoke(new Object[] { country });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to add country: " + country.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to add country: " + country.getName(), e);
        }
    }

    public void updateCountry(Country country) throws EmfException {
        try {
            Call call = call();

            registerCountryMappings(call);
            call.setOperationName(qname("updateCountry"));
            call.addParameter("country", qname("ns1:Country"), ParameterMode.IN);
            call.setReturnType(org.apache.axis.Constants.XSD_ANY);

            call.invoke(new Object[] { country });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to update country: " + country.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to update country: " + country.getName(), e);
        }
    }

    public Country[] getCountries() throws EmfException {
        try {
            Call call = call();

            registerCountryMappings(call);
            call.setOperationName(qname("getCountries"));
            call.setReturnType(qname("ns1:Countries"));

            return (Country[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to fetch countries", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to fetch countries", e);
        }

        return null;
    }

    private void registerCountryMappings(Call call) {
        registerBeanMapping(call, Country.class, qname("ns1:Country"));
        registerArrayMapping(call, Country[].class, qname("ns1:Countries"));
        registerMappingForTable(call);
    }

    public Sector[] getSectors() throws EmfException {
        try {
            Call call = call();

            registerSectorMappings(call);
            call.setOperationName(qname("getSectors"));
            call.setReturnType(qname("ns1:Sectors"));

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
            Call call = call();

            registerSectorMappings(call);
            call.setOperationName(qname("updateSector"));
            call.addParameter("sector", qname("ns1:Sector"), ParameterMode.IN);
            call.setReturnType(org.apache.axis.Constants.XSD_ANY);

            call.invoke(new Object[] { sector });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not update Sector: " + sector.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not update Sector: " + sector.getName(), e);
        }
    }

    public void addSector(Sector sector) throws EmfException {
        try {
            Call call = call();

            registerSectorMappings(call);
            call.setOperationName(qname("addSector"));
            call.addParameter("sector", qname("ns1:Sector"), ParameterMode.IN);
            call.setReturnType(org.apache.axis.Constants.XSD_ANY);

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

    private QName qname(String name) {
        return new QName(EMFConstants.emfServicesNamespace, name);
    }

    private void registerDatasetMappings(Call call) {
        registerBeanMapping(call, EmfDataset.class, qname("ns1:EmfDataset"));
        registerArrayMapping(call, EmfDataset[].class, qname("ns1:EmfDatasets"));
        registerBeanMapping(call, DatasetType.class, qname("ns1:DatasetType"));

        registerMappingForTable(call);
        registerBeanMapping(call, InternalSource.class, qname("ns1:InternalSource"));
        registerBeanMapping(call, ExternalSource.class, qname("ns1:ExternalSource"));
        registerArrayMapping(call, ExternalSource[].class, qname("ns1:ExternalSources"));
        registerArrayMapping(call, InternalSource[].class, qname("ns1:InternalSources"));
    }

    private void registerSectorMappings(Call call) {
        registerBeanMapping(call, Sector.class, qname("ns1:Sector"));
        registerArrayMapping(call, Sector[].class, qname("ns1:Sectors"));
        registerBeanMapping(call, SectorCriteria.class, qname("ns1:SectorCriteria"));
        registerArrayMapping(call, SectorCriteria[].class, qname("ns1:SectorCriterias"));
        registerMappingForTable(call);
    }

    private Call call() throws ServiceException, MalformedURLException {
        Service service = new Service();
        Call call = (Call) service.createCall();
        call.setTargetEndpointAddress(new URL(endpoint));

        return call;
    }

    private void registerBeanMapping(Call call, Class clazz, QName beanQName) {
        call.registerTypeMapping(clazz, beanQName, new BeanSerializerFactory(clazz, beanQName),
                new BeanDeserializerFactory(clazz, beanQName));

    }

    private void registerMappingForTable(Call call) {
        QName tableQName = qname("ns1:Table");
        registerBeanMapping(call, Table.class, tableQName);
    }

    private void registerArrayMapping(Call call, Class cls, QName qname) {
        call.registerTypeMapping(cls, qname, new ArraySerializerFactory(cls, qname),
                new ArrayDeserializerFactory(qname));
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
