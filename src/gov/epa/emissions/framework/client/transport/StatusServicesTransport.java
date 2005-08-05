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
import gov.epa.emissions.framework.commons.StatusServices;
import gov.epa.emissions.framework.commons.Status;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

/**
 * @author Conrad F. D'Cruz
 *
 *	This class implements the methods specified in the StatusServices interface
 *
 */
public class StatusServicesTransport implements StatusServices {

    private static String endpoint = "";

    /**
     * 
     */
    public StatusServicesTransport() {
        super();
    }

    public StatusServicesTransport(String endpt) {
        super();
        endpoint=endpt;
    }
    
    
    /**
     * 
     * This utility method extracts the significat message from the Axis Fault
     * @param faultReason
     * @return
     */
    private String extractMessage(String faultReason) {
        String message = faultReason.substring(faultReason.indexOf("Exception: ") + 11);
        if (message.equals("Connection refused: connect")){
            message = "Cannot communicate with EMF Server";
        }
        
        return message;
    }
   
    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.EMFStatus#getMessages(java.lang.String)
     */
    public Status[] getMessages(String userName) throws EmfException {
        Status[] allStats = null;;
        
        Service  service = new Service();
        Call     call;

        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            
            QName qname1 = new QName("urn:gov.epa.emf.StatusServices","ns1:Status");
            QName qname2 = new QName("urn:gov.epa.emf.StatusServices","ns1:AllStatus");
            QName qname3 = new QName("urn:gov.epa.emf.StatusServices", "getMessages");
            
            call.setOperationName(qname3);
            
            Class cls1 = gov.epa.emissions.framework.commons.Status.class;
            Class cls2 = gov.epa.emissions.framework.commons.Status[].class;
	          call.registerTypeMapping(cls1, qname1,
					  new org.apache.axis.encoding.ser.BeanSerializerFactory(cls1, qname1),        
					  new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls1, qname1));        			  
		          call.registerTypeMapping(cls2, qname2,
						  new org.apache.axis.encoding.ser.ArraySerializerFactory(cls2, qname2),        
						  new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname2));        			  
		            call.addParameter("uname",
                            org.apache.axis.Constants.XSD_STRING,
                            javax.xml.rpc.ParameterMode.IN);
		          
            call.setReturnType(qname2);
            
            Object obj = call.invoke( new Object[] {userName} );
                            
            allStats = (Status[])obj;
            
        } catch (ServiceException e) {
            System.out.println("Error invoking the service");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.out.println("Error in format of URL string");
            e.printStackTrace();
        } catch (AxisFault fault){
            throw new EmfException(extractMessage(fault.getMessage()));           
        }catch (RemoteException e) {
            System.out.println("Error communicating with WS end point");
            e.printStackTrace();
        }

        return allStats;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.EMFStatus#getMessages(java.lang.String, java.lang.String)
     */
    public Status[] getMessages(String userName, String type) throws EmfException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.EMFStatus#setStatus(gov.epa.emissions.framework.commons.Status)
     */
    public void setStatus(Status status) throws EmfException {
        Service  service = new Service();
        Call     call;
        
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            
            QName qname1 = new QName("urn:gov.epa.emf.StatusServices","ns1:Status");
            QName qname3 = new QName("urn:gov.epa.emf.StatusServices", "setStatus");
            
            call.setOperationName(qname3);
            
            Class cls1 = gov.epa.emissions.framework.commons.Status.class;
	        call.registerTypeMapping(cls1, qname1,
					  new org.apache.axis.encoding.ser.BeanSerializerFactory(cls1, qname1),        
					  new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls1, qname1));        			  
		    call.addParameter( "status", qname1, ParameterMode.IN );
            call.setReturnType(qname3);
            
            Object obj = call.invoke( new Object[] {status} );
        } catch (ServiceException e) {
            System.out.println("Error invoking the service");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.out.println("Error in format of URL string");
            e.printStackTrace();
        } catch (AxisFault fault){
            throw new EmfException(extractMessage(fault.getMessage()));           
        }catch (RemoteException e) {
            System.out.println("Error communicating with WS end point");
            e.printStackTrace();
        }
        
    }

}
