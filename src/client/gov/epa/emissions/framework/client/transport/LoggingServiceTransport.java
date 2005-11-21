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
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.LoggingService;

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
public class LoggingServiceTransport implements LoggingService {
    private static Log log = LogFactory.getLog(LoggingServiceTransport.class);

    private String emfSvcsNamespace = EMFConstants.emfServicesNamespace;

    private static String endpoint = "";

    public LoggingServiceTransport(String endpt) {
        endpoint = endpt;
    }

    private String extractMessage(String faultReason) {
        log.debug("Utility method extracting Axis fault reason");
        String message = faultReason.substring(faultReason.indexOf("Exception: ") + 11);
        if (message.equals("Connection refused: connect")) {
            message = "Cannot communicate with EMF Server";
        }

        log.debug("Utility method extracting Axis fault reason");
        return message;
    }

    // FIXME: THIS IS NOT NEEDED ON THE FRONTEND
    // LEAVE IN FOR NOW UNTIL REFACTOR
    public void setAccessLog(AccessLog accesslog) throws EmfException {
        // TODO Auto-generated method stub
        if (false)
            throw new EmfException("BOGUS");
    }

    public AccessLog[] getAccessLogs(long datasetid) throws EmfException {
        log.debug("Getting all access logs for dataset: " + datasetid);
        AccessLog[] allLogs = null;

        Service service = new Service();
        Call call;

        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName qname1 = new QName(emfSvcsNamespace, "ns1:AccessLog");
            QName qname2 = new QName(emfSvcsNamespace, "ns1:AllAccessLogs");
            QName qname3 = new QName(emfSvcsNamespace, "getAccessLogs");

            call.setOperationName(qname3);

            Class cls1 = gov.epa.emissions.framework.services.AccessLog.class;
            Class cls2 = gov.epa.emissions.framework.services.AccessLog[].class;
            call.registerTypeMapping(cls1, qname1,
                    new org.apache.axis.encoding.ser.BeanSerializerFactory(cls1, qname1),
                    new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls1, qname1));
            call.registerTypeMapping(cls2, qname2,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls2, qname2),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname2));
            call.addParameter("datasetid", org.apache.axis.Constants.XSD_LONG, ParameterMode.IN);

            call.setReturnType(qname2);

            Object obj = call.invoke(new Object[] { new Long(datasetid) });

            allLogs = (AccessLog[]) obj;

        } catch (ServiceException e) {
            log.error("Error invoking the service", e);
        } catch (MalformedURLException e) {
            log.error("Error in format of URL string", e);
        } catch (AxisFault fault) {
            log.error("Axis fault", fault);
            throw new EmfException(extractMessage(fault.getMessage()));
        } catch (RemoteException e) {
            log.error("Error communicating with WS end point", e);
        }

        log.debug("Getting all access logs for dataset: " + datasetid + " total= " + allLogs.length);
        return allLogs;
    }

}
