/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFCommons
 * Package: package gov.epa.emissions.framework.client.transport;
 * File Name: EMFUserAdminTransport.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.User;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

/**
 * @author Conrad F. D'Cruz
 *
 *	This class implements the methods specified in the UserAdmin interface
 *
 */
public class ExImServicesTransport implements ExImServices {

    private static String endpoint = "";

    /**
     * 
     */
    public ExImServicesTransport() {
        super();
    }

    public ExImServicesTransport(String endpt) {
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
//        if (message.equals("Connection refused: connect")){
//            message = "Cannot communicate with EMF Server";
//        }
        
        return message;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.ExImServices#startImport(java.lang.String, java.lang.String, java.lang.String)
     */
    public void startImport(String userName, String fileName, String fileType) throws EmfException {
        Service  service = new Service();
        Call     call;

        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            
            QName qname3 = new QName("urn:ExImServices", "startImport");
            
            call.setOperationName(qname3);
            
            call.addParameter("username",
                    org.apache.axis.Constants.XSD_STRING,
                    javax.xml.rpc.ParameterMode.IN);

            call.addParameter("filename",
                            org.apache.axis.Constants.XSD_STRING,
                            javax.xml.rpc.ParameterMode.IN);
		    call.addParameter("filetype",
                            org.apache.axis.Constants.XSD_STRING,
                            javax.xml.rpc.ParameterMode.IN);
		          
            call.setReturnType(org.apache.axis.Constants.XSD_ANY);
            
            Object obj = call.invoke( new Object[] {userName, fileName, fileType} );
            
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

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.ExImServices#startImport(gov.epa.emissions.framework.commons.User, java.lang.String, gov.epa.emissions.framework.commons.DatasetType)
     */
    public void startImport(User user, String fileName, DatasetType datasetType) throws EmfException {
        Service  service = new Service();
        Call     call;

//        try {
//            call = (Call) service.createCall();
//            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
//            
//            QName qname3 = new QName("urn:ExImServices", "startImport");
//            
//            call.setOperationName(qname3);
//            
//            call.addParameter("username",
//                    org.apache.axis.Constants.XSD_STRING,
//                    javax.xml.rpc.ParameterMode.IN);
//
//            call.addParameter("filename",
//                            org.apache.axis.Constants.XSD_STRING,
//                            javax.xml.rpc.ParameterMode.IN);
//		    call.addParameter("filetype",
//                            org.apache.axis.Constants.XSD_STRING,
//                            javax.xml.rpc.ParameterMode.IN);
//		          
//            call.setReturnType(org.apache.axis.Constants.XSD_ANY);
//            
//            Object obj = call.invoke( new Object[] {userName, fileName, fileType} );
//            
//        } catch (ServiceException e) {
//            System.out.println("Error invoking the service");
//            e.printStackTrace();
//        } catch (MalformedURLException e) {
//            System.out.println("Error in format of URL string");
//            e.printStackTrace();
//        } catch (AxisFault fault){
//            throw new EmfException(extractMessage(fault.getMessage()));           
//        }catch (RemoteException e) {
//            System.out.println("Error communicating with WS end point");
//            e.printStackTrace();
//        }
        
        
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.ExImServices#getDatasetTypes()
     */
    public DatasetType[] getDatasetTypes() {
    	DatasetType[] datasettypes = null;
   
    	// Call the ExImServices endpoint and acquire the array of all dataset types
    	//defined in the system
//        Status[] allStats = null;;
//        
//        Service  service = new Service();
//        Call     call;
//
//        try {
//            call = (Call) service.createCall();
//            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
//            
//            QName qname1 = new QName("urn:gov.epa.emf.StatusServices","ns1:DatasetType");
//            QName qname2 = new QName("urn:gov.epa.emf.StatusServices","ns1:DatasetTypes");
//            QName qname3 = new QName("urn:gov.epa.emf.StatusServices", "getDatasetTypes");
//            
//            call.setOperationName(qname3);
//            
//            Class cls1 = gov.epa.emissions.framework.commons.Status.class;
//            Class cls2 = gov.epa.emissions.framework.commons.Status[].class;
//	          call.registerTypeMapping(cls1, qname1,
//					  new org.apache.axis.encoding.ser.BeanSerializerFactory(cls1, qname1),        
//					  new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls1, qname1));        			  
//		          call.registerTypeMapping(cls2, qname2,
//						  new org.apache.axis.encoding.ser.ArraySerializerFactory(cls2, qname2),        
//						  new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname2));        			  
//		            call.addParameter("uname",
//                            org.apache.axis.Constants.XSD_STRING,
//                            javax.xml.rpc.ParameterMode.IN);
//		          
//            call.setReturnType(qname2);
//            
//            Object obj = call.invoke( new Object[] {userName} );
//                            
//            allStats = (Status[])obj;
//            
//        } catch (ServiceException e) {
//            System.out.println("Error invoking the service");
//            e.printStackTrace();
//        } catch (MalformedURLException e) {
//            System.out.println("Error in format of URL string");
//            e.printStackTrace();
//        } catch (AxisFault fault){
//            throw new EmfException(extractMessage(fault.getMessage()));           
//        }catch (RemoteException e) {
//            System.out.println("Error communicating with WS end point");
//            e.printStackTrace();
//        }
    	return datasettypes;
    }
   

}//EMFDataTransport
