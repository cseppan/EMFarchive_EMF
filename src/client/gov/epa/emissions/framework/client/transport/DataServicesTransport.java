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
import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class DataServicesTransport implements DataServices {
    private static Log log = LogFactory.getLog(DataServicesTransport.class);

    private String emfSvcsNamespace = EMFConstants.emfServicesNamespace;

    private String endpoint = "";

    public DataServicesTransport() {
        super();
    }

    public DataServicesTransport(String endpt) {
        endpoint = endpt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.services.DataServices#getDatasets()
     */
    public EmfDataset[] getDatasets() throws EmfException {
        log.debug("Get all datasets");

        // Call the DataServices endpoint and acquire the array of all dataset
        // types
        // defined in the system
        EmfDataset[] datasets = null;

        Service service = new Service();
        Call call;

        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName qname1 = new QName(emfSvcsNamespace, "ns1:EmfDataset");
            QName qname2 = new QName(emfSvcsNamespace, "ns1:EmfDatasets");
            QName qname3 = new QName(emfSvcsNamespace, "getDatasets");
            QName datasetTypeQName = new QName(emfSvcsNamespace, "ns1:DatasetType");
            QName internalSourceQName = new QName(emfSvcsNamespace, "ns1:InternalSource");
            QName externalSourceQName = new QName(emfSvcsNamespace, "ns1:ExternalSource");
            QName internalSourcesQName = new QName(emfSvcsNamespace, "ns1:InternalSources");
            QName externalSourcesQName = new QName(emfSvcsNamespace, "ns1:ExternalSources");

            call.setOperationName(qname3);

            Class cls1 = EmfDataset.class;
            Class cls2 = EmfDataset[].class;

            call.registerTypeMapping(cls1, qname1, new BeanSerializerFactory(cls1, qname1),
                    new BeanDeserializerFactory(cls1, qname1));
            call.registerTypeMapping(cls2, qname2,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls2, qname2),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname2));
            registerMapping(call, datasetTypeQName, DatasetType.class);

            registerMappingForTable(call);
            registerBeanMapping(call, internalSourceQName, InternalSource.class);
            registerBeanMapping(call, externalSourceQName, ExternalSource.class);
            registerArrayMapping(call, ExternalSource[].class, externalSourcesQName);
            registerArrayMapping(call, InternalSource[].class, internalSourcesQName);

            call.setReturnType(qname2);

            Object obj = call.invoke(new Object[] {});

            datasets = (EmfDataset[]) obj;

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
        log.debug("Get all dataset types");
        return datasets;
    }

    private void registerBeanMapping(Call call, QName beanQName, Class cls) {
        call.registerTypeMapping(cls, beanQName, new BeanSerializerFactory(cls, beanQName),
                new BeanDeserializerFactory(cls, beanQName));

    }

    private void registerMapping(Call call, QName datasetTypeQName, Class clazz) {
        call.registerTypeMapping(clazz, datasetTypeQName,
                new BeanSerializerFactory(DatasetType.class, datasetTypeQName), new BeanDeserializerFactory(clazz,
                        datasetTypeQName));
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.services.DataServices#getDatasets(gov.epa.emissions.framework.services.User)
     */
    public EmfDataset[] getDatasets(User user) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.services.DataServices#insertDataset(gov.epa.emissions.commons.io.Dataset)
     */
    public void insertDataset(EmfDataset aDataset) {
        // log.debug("insert a new dataset type object: " + aDataset.getName());
        // Service service = new Service();
        // Call call;
        // try {
        // call = (Call) service.createCall();
        // call.setTargetEndpointAddress(new java.net.URL(endpoint));
        //
        // QName qname2 = new QName(emfSvcsNamespace, "ns1:EmfDataset");
        // QName qname3 = new QName(emfSvcsNamespace, "insertDataset");
        //
        // call.setOperationName(qname3);
        //
        // call.registerTypeMapping(EmfDataset.class, qname2, new
        // org.apache.axis.encoding.ser.BeanSerializerFactory(
        // EmfDataset.class, qname2), new
        // org.apache.axis.encoding.ser.BeanDeserializerFactory(
        // EmfDataset.class, qname2));
        //
        // registerMappingForTable(call);
        //
        // call.addParameter("dataset", qname2, ParameterMode.IN);
        //
        // call.setReturnType(org.apache.axis.Constants.XSD_ANY);
        // call.invoke(new Object[] { aDataset });
        //
        // } catch (ServiceException e) {
        // log.error("Error invoking the service", e);
        // } catch (MalformedURLException e) {
        // log.error("Error in format of URL string", e);
        // } catch (AxisFault fault) {
        // log.error("Axis Fault details", fault);
        // throw new EmfException(extractMessage(fault.getMessage()));
        // } catch (RemoteException e) {
        // log.error("Error communicating with WS end point", e);
        // }
        //
        // log.debug("insert a new dataset type object: " + aDataset.getName());

    }

    private void registerMappingForTable(Call call) {
        QName tableQName = new QName(emfSvcsNamespace, "ns1:Table");
        call.registerTypeMapping(Table.class, tableQName, new org.apache.axis.encoding.ser.BeanSerializerFactory(
                Table.class, tableQName), new org.apache.axis.encoding.ser.BeanDeserializerFactory(Table.class,
                tableQName));
    }

    private void registerArrayMapping(Call call, Class cls, QName qname) {
        call.registerTypeMapping(cls, qname, new org.apache.axis.encoding.ser.ArraySerializerFactory(cls, qname),
                new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname));
    }

    /**
     * 
     * This utility method extracts the significant message from the Axis Fault
     * 
     * @param faultReason
     * @return
     */
    private String extractMessage(String faultReason) {
        log.debug("Extracting significant message from Axis fault");
        String message = faultReason.substring(faultReason.indexOf("Exception: ") + 11);
        // if (message.equals("Connection refused: connect")){
        // message = "Cannot communicate with EMF Server";
        // }

        log.debug("Extracting significant message from Axis fault");
        return message;
    }

    public void updateDataset(EmfDataset aDset) throws EmfException {
        log.debug("update a dataset object: " + aDset.getName());
        Service service = new Service();
        Call call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName qname2 = new QName(emfSvcsNamespace, "ns1:EmfDataset");
            QName qname3 = new QName(emfSvcsNamespace, "updateDataset");
            QName datasetTypeQName = new QName(emfSvcsNamespace, "ns1:DatasetType");
            QName internalSourceQName = new QName(emfSvcsNamespace, "ns1:InternalSource");
            QName externalSourceQName = new QName(emfSvcsNamespace, "ns1:ExternalSource");
            QName internalSourcesQName = new QName(emfSvcsNamespace, "ns1:InternalSources");
            QName externalSourcesQName = new QName(emfSvcsNamespace, "ns1:ExternalSources");

            call.setOperationName(qname3);

            call.registerTypeMapping(EmfDataset.class, qname2, new org.apache.axis.encoding.ser.BeanSerializerFactory(
                    EmfDataset.class, qname2), new org.apache.axis.encoding.ser.BeanDeserializerFactory(
                    EmfDataset.class, qname2));
            registerMapping(call, datasetTypeQName, DatasetType.class);
            registerMappingForTable(call);
            registerBeanMapping(call, internalSourceQName, gov.epa.emissions.commons.io.InternalSource.class);
            registerBeanMapping(call, externalSourceQName, gov.epa.emissions.commons.io.ExternalSource.class);
            registerArrayMapping(call, gov.epa.emissions.commons.io.ExternalSource[].class, externalSourcesQName);
            registerArrayMapping(call, gov.epa.emissions.commons.io.InternalSource[].class, internalSourcesQName);

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

        log.debug("update a new dataset type object: " + aDset.getName());
    }

    public Country[] getCountries() throws EmfException {
        log.debug("Get all countries");

        // Call the DataServices endpoint and acquire the array of all countries
        // defined in the system
        Country[] countries = null;

        Service service = new Service();
        Call call;

        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName qname1 = new QName(emfSvcsNamespace, "ns1:Country");
            QName qname2 = new QName(emfSvcsNamespace, "ns1:Countries");
            QName qname3 = new QName(emfSvcsNamespace, "getCountries");

            call.setOperationName(qname3);

            Class cls1 = Country.class;
            Class cls2 = Country[].class;

            call.registerTypeMapping(cls1, qname1,
                    new org.apache.axis.encoding.ser.BeanSerializerFactory(cls1, qname1),
                    new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls1, qname1));
            call.registerTypeMapping(cls2, qname2,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls2, qname2),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname2));

            registerMappingForTable(call);

            call.setReturnType(qname2);

            Object obj = call.invoke(new Object[] {});

            countries = (Country[]) obj;

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
        log.debug("Get all countries");
        return countries;
    }

    public Sector[] getSectors() throws EmfException {

        log.debug("Get all sectors");

        // Call the DataServices endpoint and acquire the array of all sectors
        // defined in the system
        Sector[] sectors = null;

        Service service = new Service();
        Call call;

        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName qname1 = new QName(emfSvcsNamespace, "ns1:Sector");
            QName qname2 = new QName(emfSvcsNamespace, "ns1:Sectors");
            QName opName = new QName(emfSvcsNamespace, "getSectors");
            QName qname4 = new QName(emfSvcsNamespace, "ns1:SectorCriteria");
            QName qname5 = new QName(emfSvcsNamespace, "ns1:SectorCriterias");

            call.setOperationName(opName);

            Class cls1 = Sector.class;
            Class cls2 = Sector[].class;
            Class cls4 = SectorCriteria.class;
            Class cls5 = SectorCriteria[].class;

            call.registerTypeMapping(cls1, qname1,
                    new org.apache.axis.encoding.ser.BeanSerializerFactory(cls1, qname1),
                    new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls1, qname1));
            call.registerTypeMapping(cls2, qname2,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls2, qname2),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname2));
            call.registerTypeMapping(cls4, qname4,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls4, qname4),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname4));
            call.registerTypeMapping(cls5, qname5,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls5, qname5),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname5));

            registerMappingForTable(call);

            call.setReturnType(qname2);

            Object obj = call.invoke(new Object[] {});

            sectors = (Sector[]) obj;

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
        log.debug("Get all sectors");
        return sectors;

    }

    public void addCountry(Country country) throws EmfException {
        log.debug("insert a new country object: " + country.getName());
        Service service = new Service();
        Call call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName qname2 = new QName(emfSvcsNamespace, "ns1:Country");
            QName qname3 = new QName(emfSvcsNamespace, "addCountry");

            call.setOperationName(qname3);

            call.registerTypeMapping(Country.class, qname2, new org.apache.axis.encoding.ser.BeanSerializerFactory(
                    Country.class, qname2), new org.apache.axis.encoding.ser.BeanDeserializerFactory(Country.class,
                    qname2));

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

        log.debug("insert a new country type object: " + country.getName());
    }

    public void updateCountry(Country country) throws EmfException {

        log.debug("update a country object: " + country.getName());
        Service service = new Service();
        Call call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName qname2 = new QName(emfSvcsNamespace, "ns1:Country");
            QName qname3 = new QName(emfSvcsNamespace, "updateCountry");

            call.setOperationName(qname3);

            call.registerTypeMapping(Country.class, qname2, new org.apache.axis.encoding.ser.BeanSerializerFactory(
                    Country.class, qname2), new org.apache.axis.encoding.ser.BeanDeserializerFactory(Country.class,
                    qname2));

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

        log.debug("update a country object: " + country.getName());

    }

    public void addSector(Sector sector) throws EmfException {
        log.debug("insert a new sector object: " + sector.getName());
        Service service = new Service();
        Call call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName qname2 = new QName(emfSvcsNamespace, "ns1:Sector");
            QName qname3 = new QName(emfSvcsNamespace, "addSector");
            QName qname4 = new QName(emfSvcsNamespace, "ns1:SectorCriteria");
            QName qname5 = new QName(emfSvcsNamespace, "ns1:SectorCriterias");

            call.setOperationName(qname3);

            Class cls2 = Sector.class;
            Class cls4 = SectorCriteria.class;
            Class cls5 = SectorCriteria[].class;

            call.registerTypeMapping(cls2, qname2,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls2, qname2),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname2));
            call.registerTypeMapping(cls4, qname4,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls4, qname4),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname4));
            call.registerTypeMapping(cls5, qname5,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls5, qname5),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname5));

            call.addParameter("sector", qname2, ParameterMode.IN);

            call.setReturnType(org.apache.axis.Constants.XSD_ANY);
            call.invoke(new Object[] { sector });

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

        log.debug("insert a new sector type object: " + sector.getName());

    }

    public void updateSector(Sector sector) throws EmfException {

        log.debug("update a sector object: " + sector.getName());
        Service service = new Service();
        Call call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName qname2 = new QName(emfSvcsNamespace, "ns1:Sector");
            QName qname3 = new QName(emfSvcsNamespace, "updateSector");
            QName qname4 = new QName(emfSvcsNamespace, "ns1:SectorCriteria");
            QName qname5 = new QName(emfSvcsNamespace, "ns1:SectorCriterias");

            call.setOperationName(qname3);

            Class cls2 = Sector.class;
            Class cls4 = SectorCriteria.class;
            Class cls5 = SectorCriteria[].class;

            call.registerTypeMapping(cls2, qname2,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls2, qname2),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname2));
            call.registerTypeMapping(cls4, qname4,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls4, qname4),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname4));
            call.registerTypeMapping(cls5, qname5,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls5, qname5),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname5));

            call.addParameter("sector", qname2, ParameterMode.IN);

            call.setReturnType(org.apache.axis.Constants.XSD_ANY);
            call.invoke(new Object[] { sector });

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

        log.debug("update a sector object: " + sector.getName());

    }
}
