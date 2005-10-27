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
import java.rmi.RemoteException;

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

            QName qname1 = qname("ns1:EmfDataset");
            QName qname2 = qname("ns1:EmfDatasets");
            QName qname3 = qname("getDatasets");
            QName datasetTypeQName = qname("ns1:DatasetType");
            QName internalSourceQName = qname("ns1:InternalSource");
            QName externalSourceQName = qname("ns1:ExternalSource");
            QName internalSourcesQName = qname("ns1:InternalSources");
            QName externalSourcesQName = qname("ns1:ExternalSources");

            call.setOperationName(qname3);

            Class cls1 = EmfDataset.class;
            Class cls2 = EmfDataset[].class;

            call.registerTypeMapping(cls1, qname1, new BeanSerializerFactory(cls1, qname1),
                    new BeanDeserializerFactory(cls1, qname1));
            call.registerTypeMapping(cls2, qname2, new ArraySerializerFactory(cls2, qname2),
                    new ArrayDeserializerFactory(qname2));
            registerBeanMapping(call, DatasetType.class, datasetTypeQName);

            registerMappingForTable(call);
            registerBeanMapping(call, InternalSource.class, internalSourceQName);
            registerBeanMapping(call, ExternalSource.class, externalSourceQName);
            registerArrayMapping(call, ExternalSource[].class, externalSourcesQName);
            registerArrayMapping(call, InternalSource[].class, internalSourcesQName);

            call.setReturnType(qname2);

            Object obj = call.invoke(new Object[] {});

            return (EmfDataset[]) obj;

        } catch (AxisFault fault) {
            log.error("Axis Fault details", fault);
            throw new EmfException(extractMessage(fault.getMessage()));
        } catch (Exception e) {
            log.error("Failed to fetch Datasets", e);
            throw new EmfException("Failed to fetch Datasets", e.getMessage(), e);
        }
    }

    public EmfDataset[] getDatasets(User user) {
        return null;
    }

    public void insertDataset(EmfDataset aDataset) {
    }

    /**
     * 
     * This utility method extracts the significant message from the Axis Fault
     * 
     * @param faultReason
     * @return
     */
    private String extractMessage(String faultReason) {
        return faultReason.substring(faultReason.indexOf("Exception: ") + 11);
    }

    public void updateDataset(EmfDataset aDset) throws EmfException {
        try {
            Call call = call();

            QName qname2 = qname("ns1:EmfDataset");
            QName qname3 = qname("updateDataset");
            QName datasetTypeQName = qname("ns1:DatasetType");
            QName internalSourceQName = qname("ns1:InternalSource");
            QName externalSourceQName = qname("ns1:ExternalSource");
            QName internalSourcesQName = qname("ns1:InternalSources");
            QName externalSourcesQName = qname("ns1:ExternalSources");

            call.setOperationName(qname3);

            call.registerTypeMapping(EmfDataset.class, qname2, new BeanSerializerFactory(EmfDataset.class, qname2),
                    new BeanDeserializerFactory(EmfDataset.class, qname2));
            registerBeanMapping(call, DatasetType.class, datasetTypeQName);
            registerMappingForTable(call);
            registerBeanMapping(call, InternalSource.class, internalSourceQName);
            registerBeanMapping(call, ExternalSource.class, externalSourceQName);
            registerArrayMapping(call, ExternalSource[].class, externalSourcesQName);
            registerArrayMapping(call, InternalSource[].class, internalSourcesQName);

            call.addParameter("dataset", qname2, ParameterMode.IN);

            call.setReturnType(org.apache.axis.Constants.XSD_ANY);
            call.invoke(new Object[] { aDset });

        } catch (ServiceException e) {
            log.error("Error invoking the service", e);
        } catch (MalformedURLException e) {
            log.error("Error in format of URL string", e);
        } catch (AxisFault fault) {
            log.error("Axis Fault details", fault);
            throw new EmfException(extractMessage(fault.getMessage()));
        } catch (RemoteException e) {
            log.error("Error communicating with WS end point", e);
        }
    }

    public void addCountry(Country country) throws EmfException {
        try {
            Call call = call();

            QName qname2 = qname("ns1:Country");
            QName qname3 = qname("addCountry");

            call.setOperationName(qname3);

            call.registerTypeMapping(Country.class, qname2, new BeanSerializerFactory(Country.class, qname2),
                    new BeanDeserializerFactory(Country.class, qname2));

            call.addParameter("country", qname2, ParameterMode.IN);

            call.setReturnType(org.apache.axis.Constants.XSD_ANY);
            call.invoke(new Object[] { country });

        } catch (ServiceException e) {
            log.error("Error invoking the service", e);
        } catch (MalformedURLException e) {
            log.error("Error in format of URL string", e);
        } catch (AxisFault fault) {
            log.error("Axis Fault details", fault);
            throw new EmfException(extractMessage(fault.getMessage()));
        } catch (RemoteException e) {
            log.error("Error communicating with WS end point", e);
        }
    }

    public void updateCountry(Country country) throws EmfException {
        try {
            Call call = call();

            QName qname2 = qname("ns1:Country");
            QName qname3 = qname("updateCountry");

            call.setOperationName(qname3);

            call.registerTypeMapping(Country.class, qname2, new BeanSerializerFactory(Country.class, qname2),
                    new BeanDeserializerFactory(Country.class, qname2));

            call.addParameter("country", qname2, ParameterMode.IN);

            call.setReturnType(org.apache.axis.Constants.XSD_ANY);
            call.invoke(new Object[] { country });

        } catch (ServiceException e) {
            log.error("Error invoking the service", e);
        } catch (MalformedURLException e) {
            log.error("Error in format of URL string", e);
        } catch (AxisFault fault) {
            log.error("Axis Fault details", fault);
            throw new EmfException(extractMessage(fault.getMessage()));
        } catch (RemoteException e) {
            log.error("Error communicating with WS end point", e);
        }
    }

    public Country[] getCountries() throws EmfException {
        try {
            Call call = call();

            QName qname1 = qname("ns1:Country");
            QName qname2 = qname("ns1:Countries");
            QName qname3 = qname("getCountries");

            call.setOperationName(qname3);

            Class cls1 = Country.class;
            Class cls2 = Country[].class;

            call.registerTypeMapping(cls1, qname1, new BeanSerializerFactory(cls1, qname1),
                    new BeanDeserializerFactory(cls1, qname1));
            call.registerTypeMapping(cls2, qname2, new ArraySerializerFactory(cls2, qname2),
                    new ArrayDeserializerFactory(qname2));

            registerMappingForTable(call);

            call.setReturnType(qname2);

            return (Country[]) call.invoke(new Object[] {});
        } catch (ServiceException e) {
            log.error("Error invoking the service", e);
        } catch (MalformedURLException e) {
            log.error("Error in format of URL string", e);
        } catch (AxisFault fault) {
            log.error("Axis fault details", fault);
            throw new EmfException(extractMessage(fault.getMessage()));
        } catch (RemoteException e) {
            log.error("Error communicating with WS end point", e);
        }

        return null;
    }

    public Sector[] getSectors() throws EmfException {
        try {
            Call call = call();

            registerSectorMappings(call);
            call.setOperationName(qname("getSectors"));
            call.setReturnType(qname("ns1:Sectors"));

            return (Sector[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            log.error("Could not fetch Sectors", fault);
            throw new EmfException(extractMessage(fault.getMessage()));
        } catch (Exception e) {
            log.error("Could not fetch Sectors", e);
            throw new EmfException("Could not fetch Sectors", e.getMessage(), e);
        }
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
            log.error("Could not update Sector: " + sector.getName(), fault);
            throw new EmfException(extractMessage(fault.getMessage()));
        } catch (Exception e) {
            log.error("Could not update Sector: " + sector.getName(), e);
            throw new EmfException("Could not update Sector: " + sector.getName(), e.getMessage(), e);
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
            log.error("Could not add Sector: " + sector.getName(), fault);
            throw new EmfException(extractMessage(fault.getMessage()));
        } catch (Exception e) {
            log.error("Could not add Sector: " + sector.getName(), e);
            throw new EmfException("Could not add Sector: " + sector.getName(), e.getMessage(), e);
        }
    }

    private QName qname(String name) {
        return new QName(EMFConstants.emfServicesNamespace, name);
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
        call.registerTypeMapping(Table.class, tableQName, new BeanSerializerFactory(Table.class, tableQName),
                new BeanDeserializerFactory(Table.class, tableQName));
    }

    private void registerArrayMapping(Call call, Class cls, QName qname) {
        call.registerTypeMapping(cls, qname, new ArraySerializerFactory(cls, qname),
                new ArrayDeserializerFactory(qname));
    }

}
