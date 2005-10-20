/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFCommons
 * Package: package gov.epa.emissions.framework.client.transport;
 * File Name: EMFUserAdminTransport.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.TestStuff;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Conrad F. D'Cruz
 * 
 * This class implements the methods specified in the UserAdmin interface
 * 
 */
public class TestServiceTransport {
    private static Log log = LogFactory.getLog(TestServiceTransport.class);

    private String emfSvcsNamespace = EMFConstants.emfServicesNamespace;

    private static String endpoint = "";

    /**
     * 
     */
    public TestServiceTransport() {
        super();
    }

    public TestServiceTransport(String endpt) {
        super();
        endpoint = endpt;
        log.debug("Transport instantiated with endpoint: " + endpt);
    }

    /**
     * 
     * This utility method extracts the significant message from the Axis Fault
     * 
     * @param faultReason
     * @return
     */
    private String extractMessage(String faultReason) {
        log.debug("extracting the faultReason message and parsing");
        String message = faultReason.substring(faultReason.indexOf("Exception: ") + 11);
        if (message.equals("Connection refused: connect")) {
            message = "Cannot communicate with EMF Server";
        }
        log.debug("extracting the faultReason message and parsing. Return message is " + message);
        return message;
    }

	public List getStuff() {
        log.debug("Get stuff");
        TestStuff[] stuff = null;
        List listStuff = new ArrayList();
        
        Service service = new Service();
        Call call;

        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName testStuffQname = new QName(emfSvcsNamespace, "ns1:TestStuff");
            QName operQname = new QName(emfSvcsNamespace, "getStuff");

            call.setOperationName(operQname);

            Class testStuffClass = gov.epa.emissions.framework.services.TestStuff.class;

            call.registerTypeMapping(testStuffClass, testStuffQname,
                    new org.apache.axis.encoding.ser.BeanSerializerFactory(testStuffClass, testStuffQname),
                    new org.apache.axis.encoding.ser.BeanDeserializerFactory(testStuffClass, testStuffQname));
            call.setReturnType(operQname);

            Object obj = call.invoke(new Object[] {});

            stuff = (TestStuff[]) obj;

            listStuff = java.util.Arrays.asList(stuff);
            
        } catch (ServiceException e) {
            log.error("Error invoking the service", e);
        } catch (MalformedURLException e) {
            log.error("Error in format of URL string", e);
            e.printStackTrace();
        } catch (RemoteException e) {
            log.error("Error communicating with WS end point", e);
            e.printStackTrace();
        }
        log.debug("Get users");
        return listStuff;
	}


}
