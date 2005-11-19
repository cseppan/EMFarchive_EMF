/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFCommons
 * Package: package gov.epa.emissions.framework.client.transport;
 * File Name: EMFUserAdminTransport.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusService;

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
 * This class implements the methods specified in the StatusServices interface
 * 
 */
public class StatusServiceTransport implements StatusService {
    private static Log LOG = LogFactory.getLog(StatusServiceTransport.class);

    private String emfSvcsNamespace = EMFConstants.emfServicesNamespace;

    private static String endpoint = "";

    public StatusServiceTransport(String endpt) {
        endpoint = endpt;
    }

    private String extractMessage(String faultReason) {
        LOG.debug("Utility method extracting Axis fault reason");
        String message = faultReason.substring(faultReason.indexOf("Exception: ") + 11);
        if (message.equals("Connection refused: connect")) {
            message = "Cannot communicate with EMF Server";
        }

        LOG.debug("Utility method extracting Axis fault reason");
        return message;
    }

    public Status[] getMessages(String userName) throws EmfException {
        LOG.debug("Getting all messages for user: " + userName);
        Status[] allStats = null;

        Service service = new Service();
        Call call;

        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName qname1 = new QName(emfSvcsNamespace, "ns1:Status");
            QName qname2 = new QName(emfSvcsNamespace, "ns1:AllStatus");
            QName qname3 = new QName(emfSvcsNamespace, "getMessages");

            call.setOperationName(qname3);

            Class cls1 = gov.epa.emissions.framework.services.Status.class;
            Class cls2 = gov.epa.emissions.framework.services.Status[].class;
            call.registerTypeMapping(cls1, qname1,
                    new org.apache.axis.encoding.ser.BeanSerializerFactory(cls1, qname1),
                    new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls1, qname1));
            call.registerTypeMapping(cls2, qname2,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls2, qname2),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname2));
            call.addParameter("uname", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);

            call.setReturnType(qname2);

            Object obj = call.invoke(new Object[] { userName });

            allStats = (Status[]) obj;

        } catch (ServiceException e) {
            LOG.error("Error invoking the service", e);
        } catch (MalformedURLException e) {
            LOG.error("Error in format of URL string", e);
        } catch (AxisFault fault) {
            LOG.error("Axis fault", fault);
            throw new EmfException(extractMessage(fault.getMessage()));
        } catch (RemoteException e) {
            LOG.error("Error communicating with WS end point", e);
        }

        LOG.debug("Getting all messages for user: " + userName);
        return allStats;
    }

    public Status[] getMessages(String userName, String type) {
        // FIXME: CONRAD Complete this method
        return null;
    }

    public void setStatus(Status status) throws EmfException {
        LOG.debug("Setting status for user:message " + status.getUsername() + " :: " + status.getMessage());
        Service service = new Service();
        Call call;

        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName qname1 = new QName(emfSvcsNamespace, "ns1:Status");
            QName qname3 = new QName(emfSvcsNamespace, "setStatus");

            call.setOperationName(qname3);

            Class cls1 = gov.epa.emissions.framework.services.Status.class;
            call.registerTypeMapping(cls1, qname1,
                    new org.apache.axis.encoding.ser.BeanSerializerFactory(cls1, qname1),
                    new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls1, qname1));
            call.addParameter("status", qname1, ParameterMode.IN);
            call.setReturnType(qname3);

            call.invoke(new Object[] { status });
        } catch (ServiceException e) {
            LOG.error("Error invoking the service", e);
        } catch (MalformedURLException e) {
            LOG.error("Error in format of URL string", e);
        } catch (AxisFault fault) {
            LOG.error("Axis Fault", fault);
            throw new EmfException(extractMessage(fault.getMessage()));
        } catch (RemoteException e) {
            LOG.error("Error communicating with WS end point", e);
        }
        LOG.debug("Setting status for user:message " + status.getUsername() + " :: " + status.getMessage());
    }

}
