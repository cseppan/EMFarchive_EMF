/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFCommons
 * Package: package gov.epa.emissions.framework.client.transport;
 * File Name: EMFUserAdminTransport.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DatasetTypesServices;
import gov.epa.emissions.framework.services.EMFConstants;

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
 */
public class DatasetTypesServicesTransport implements DatasetTypesServices {
    private static Log log = LogFactory.getLog(DatasetTypesServicesTransport.class);

    private String emfSvcsNamespace = EMFConstants.emfServicesNamespace;

    private String endpoint;

    /**
     * 
     */
    public DatasetTypesServicesTransport() {
        super();
    }

    public DatasetTypesServicesTransport(String endpt) {
        super();
        endpoint = endpt;
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

    public DatasetType[] getDatasetTypes() throws EmfException {
        log.debug("Get all dataset types");

        // Call the ExImServices endpoint and acquire the array of all dataset
        // types
        // defined in the system
        DatasetType[] datasetTypes = null;

        Service service = new Service();
        Call call;

        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName qname1 = new QName(emfSvcsNamespace, "ns1:DatasetType");
            QName qname2 = new QName(emfSvcsNamespace, "ns1:DatasetTypes");
            QName qname3 = new QName(emfSvcsNamespace, "getDatasetTypes");

            call.setOperationName(qname3);

            Class cls1 = gov.epa.emissions.commons.io.DatasetType.class;
            Class cls2 = gov.epa.emissions.commons.io.DatasetType[].class;

            call.registerTypeMapping(cls1, qname1,
                    new org.apache.axis.encoding.ser.BeanSerializerFactory(cls1, qname1),
                    new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls1, qname1));
            call.registerTypeMapping(cls2, qname2,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls2, qname2),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname2));

            call.setReturnType(qname2);

            Object obj = call.invoke(new Object[] {});

            datasetTypes = (DatasetType[]) obj;

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
        return datasetTypes;
    }

    public void insertDatasetType(DatasetType datasetType) throws EmfException {
        log.debug("insert a new dataset type object: " + datasetType.getName());
        Service service = new Service();
        Call call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName qname2 = new QName(emfSvcsNamespace, "ns1:DatasetType");
            QName qname3 = new QName(emfSvcsNamespace, "insertDatasetType");

            call.setOperationName(qname3);

            Class cls2 = gov.epa.emissions.commons.io.DatasetType.class;

            call.registerTypeMapping(cls2, qname2,
                    new org.apache.axis.encoding.ser.BeanSerializerFactory(cls2, qname2),
                    new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls2, qname2));

            call.addParameter("datasettype", qname2, ParameterMode.IN);

            call.setReturnType(org.apache.axis.Constants.XSD_ANY);

            call.invoke(new Object[] { datasetType });

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

        log.debug("insert a new dataset type object: " + datasetType.getName());

    }

    public void updateDatasetType(DatasetType datasetType) throws EmfException {
        log.debug("update a dataset type object: " + datasetType.getName());
        Service service = new Service();
        Call call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName qname2 = new QName(emfSvcsNamespace, "ns1:DatasetType");
            QName qname3 = new QName(emfSvcsNamespace, "updateDatasetType");

            call.setOperationName(qname3);

            Class cls2 = gov.epa.emissions.commons.io.DatasetType.class;

            call.registerTypeMapping(cls2, qname2,
                    new org.apache.axis.encoding.ser.BeanSerializerFactory(cls2, qname2),
                    new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls2, qname2));

            call.addParameter("datasettype", qname2, ParameterMode.IN);

            call.setReturnType(org.apache.axis.Constants.XSD_ANY);

            call.invoke(new Object[] { datasetType });

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

        log.debug("update a dataset type object: " + datasetType.getName());

    }

}// DatasetTypesServicesTransport
