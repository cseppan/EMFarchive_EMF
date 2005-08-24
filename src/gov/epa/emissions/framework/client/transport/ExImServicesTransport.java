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
import gov.epa.emissions.framework.services.ExImServices;
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
 *	This class implements the methods specified in the UserAdmin interface
 *
 */
public class ExImServicesTransport implements ExImServices {
    private static Log log = LogFactory.getLog(ExImServicesTransport.class);

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
    	log.debug("Extracting significant message from Axis fault");
    	String message = faultReason.substring(faultReason.indexOf("Exception: ") + 11);
//        if (message.equals("Connection refused: connect")){
//            message = "Cannot communicate with EMF Server";
//        }
        
    	log.debug("Extracting significant message from Axis fault");
        return message;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.ExImServices#startImport(gov.epa.emissions.framework.commons.User, java.lang.String, gov.epa.emissions.framework.commons.DatasetType)
     */
    public void startImport(User user, String fileName, DatasetType datasetType) throws EmfException {
    	log.debug("Begin import file for user:filename:datasettype:: " + user.getUserName() + " :: " + fileName + " :: " + datasetType.getName());
    	Service  service = new Service();
        Call     call;

        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            
            QName qname1 = new QName("urn:gov.epa.emf.services.ExImServices","ns1:User");
            QName qname2 = new QName("urn:gov.epa.emf.services.ExImServices","ns1:DatasetType");
            QName qname3 = new QName("urn:gov.epa.emf.services.ExImServices", "startImport");
            
            call.setOperationName(qname3);
            
            Class cls1 = gov.epa.emissions.framework.services.User.class;
            Class cls2 = gov.epa.emissions.commons.io.DatasetType.class;
            
	        call.registerTypeMapping(cls1, qname1,
	    			  new org.apache.axis.encoding.ser.BeanSerializerFactory(cls1, qname1),        
	    			  new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls1, qname1));        			  
	        call.registerTypeMapping(cls2, qname2,
	    			  new org.apache.axis.encoding.ser.BeanSerializerFactory(cls2, qname2),        
	    			  new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls2, qname2));        			  
            
            call.addParameter("user", qname1, ParameterMode.IN );

            call.addParameter("filename",
                            org.apache.axis.Constants.XSD_STRING,
                            javax.xml.rpc.ParameterMode.IN);

            call.addParameter("datasettype", qname2, ParameterMode.IN );
		          
            call.setReturnType(org.apache.axis.Constants.XSD_ANY);
            
            call.invoke( new Object[] {user, fileName, datasetType} );
            
        } catch (ServiceException e) {
            log.error("Error invoking the service",e);
        } catch (MalformedURLException e) {
            System.out.println();
            log.error("Error in format of URL string",e);
        } catch (AxisFault fault){
            log.error("Axis Fault details",fault);
            throw new EmfException(extractMessage(fault.getMessage()));           
        }catch (RemoteException e) {
            log.error("Error communicating with WS end point",e);
        }
        
    	log.debug("Begin import file for user:filename:datasettype:: " + user.getUserName() + " :: " + fileName + " :: " + datasetType.getName());
        
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.ExImServices#getDatasetTypes()
     */
    public DatasetType[] getDatasetTypes() throws EmfException {
    	log.debug("Get all dataset types");

    	// Call the ExImServices endpoint and acquire the array of all dataset types
    	//defined in the system
        DatasetType[] datasetTypes = null;;
        
        Service  service = new Service();
        Call     call;

        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            
            QName qname1 = new QName("urn:gov.epa.emf.services.ExImServices","ns1:DatasetType");
            QName qname2 = new QName("urn:gov.epa.emf.services.ExImServices","ns1:DatasetTypes");
            QName qname3 = new QName("urn:gov.epa.emf.services.ExImServices","getDatasetTypes");
            
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
            
            Object obj = call.invoke( new Object[] {} );
                            
            datasetTypes = (DatasetType[])obj;
            
        } catch (ServiceException e) {
            log.error("Error invoking the service",e);
        } catch (MalformedURLException e) {
            log.error("Error in format of URL string",e);
        } catch (AxisFault fault){
        	log.error("Axis fault details",fault);
            throw new EmfException(extractMessage(fault.getMessage()));           
        }catch (RemoteException e) {
            log.error("Error communicating with WS end point",e);
        }
    	log.debug("Get all dataset types");
    	return datasetTypes;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.services.ExImServices#insertDatasetType(gov.epa.emissions.framework.services.DatasetType)
     */
    public void insertDatasetType(DatasetType aDstn) throws EmfException {
    	log.debug("insert a new dataset type object: " + aDstn.getName());
    	Service  service = new Service();
        Call     call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            
            QName qname2 = new QName("urn:gov.epa.emf.services.ExImServices","ns1:DatasetType");
            QName qname3 = new QName("urn:gov.epa.emf.services.ExImServices", "insertDatasetType");
            
            call.setOperationName(qname3);
            
            Class cls2 = gov.epa.emissions.commons.io.DatasetType.class;
            
	        call.registerTypeMapping(cls2, qname2,
	    			  new org.apache.axis.encoding.ser.BeanSerializerFactory(cls2, qname2),        
	    			  new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls2, qname2));        			  
            
           call.addParameter("datasettype", qname2, ParameterMode.IN );
		          
            call.setReturnType(org.apache.axis.Constants.XSD_ANY);
            
            call.invoke( new Object[] {aDstn} );
            
        } catch (ServiceException e) {
            log.error("Error invoking the service",e);
        } catch (MalformedURLException e) {
            log.error("Error in format of URL string",e);
        } catch (AxisFault fault){
        	log.error("Axis Fault details",fault);
        	throw new EmfException(extractMessage(fault.getMessage()));           
        }catch (RemoteException e) {
            log.error("Error communicating with WS end point",e);
        }
                
    	log.debug("insert a new dataset type object: " + aDstn.getName());
       
    }
   

}//EMFDataTransport
