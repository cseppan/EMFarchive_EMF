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

import gov.epa.emissions.commons.io.Table;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.Country;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Sector;
import gov.epa.emissions.framework.services.User;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
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
        log.info("ENDPOINT: " + endpoint);
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.services.DataServices#getDatasets()
     */
    public EmfDataset[] getDatasets() throws EmfException {
        log.debug("Get all dataset types");

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

            call.setOperationName(qname3);

            Class cls1 = EmfDataset.class;
            Class cls2 = EmfDataset[].class;

            call.registerTypeMapping(cls1, qname1,
                    new org.apache.axis.encoding.ser.BeanSerializerFactory(cls1, qname1),
                    new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls1, qname1));
            call.registerTypeMapping(cls2, qname2,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls2, qname2),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname2));

            registerMappingForTable(call);

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
    public void insertDataset(EmfDataset aDataset) throws EmfException {
        log.debug("insert a new dataset type object: " + aDataset.getName());
        Service service = new Service();
        Call call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName qname2 = new QName(emfSvcsNamespace, "ns1:EmfDataset");
            QName qname3 = new QName(emfSvcsNamespace, "insertDataset");

            call.setOperationName(qname3);

            call.registerTypeMapping(EmfDataset.class, qname2, new org.apache.axis.encoding.ser.BeanSerializerFactory(
                    EmfDataset.class, qname2), new org.apache.axis.encoding.ser.BeanDeserializerFactory(
                    EmfDataset.class, qname2));

            registerMappingForTable(call);

            call.addParameter("dataset", qname2, ParameterMode.IN);

            call.setReturnType(org.apache.axis.Constants.XSD_ANY);
            call.invoke(new Object[] { aDataset });

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

        log.debug("insert a new dataset type object: " + aDataset.getName());

    }

    private void registerMappingForTable(Call call) {
        QName tableQName = new QName(emfSvcsNamespace, "ns1:Table");
        call.registerTypeMapping(Table.class, tableQName, new org.apache.axis.encoding.ser.BeanSerializerFactory(
                Table.class, tableQName), new org.apache.axis.encoding.ser.BeanDeserializerFactory(Table.class,
                tableQName));
    }

    /**
     * 
     * This utility method extracts the significat message from the Axis Fault
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
        log.debug("update a new dataset type object: " + aDset.getName());
        Service service = new Service();
        Call call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName qname2 = new QName(emfSvcsNamespace, "ns1:EmfDataset");
            QName qname3 = new QName(emfSvcsNamespace, "updateDataset");

            call.setOperationName(qname3);

            call.registerTypeMapping(EmfDataset.class, qname2, new org.apache.axis.encoding.ser.BeanSerializerFactory(
                    EmfDataset.class, qname2), new org.apache.axis.encoding.ser.BeanDeserializerFactory(
                    EmfDataset.class, qname2));

            registerMappingForTable(call);

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
            QName qname3 = new QName(emfSvcsNamespace, "getSectors");

            call.setOperationName(qname3);

            Class cls1 = Sector.class;
            Class cls2 = Sector[].class;

            call.registerTypeMapping(cls1, qname1,
                    new org.apache.axis.encoding.ser.BeanSerializerFactory(cls1, qname1),
                    new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls1, qname1));
            call.registerTypeMapping(cls2, qname2,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls2, qname2),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname2));

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

	public void addCountry(String country) throws EmfException {

		//FIXME: REMOVE DUMMY LINES BELOW
		if (false) throw new EmfException("");

	}

	public void addSector(String sector) throws EmfException {

		//FIXME: REMOVE DUMMY LINES BELOW
		if (false) throw new EmfException("");

	}
}
