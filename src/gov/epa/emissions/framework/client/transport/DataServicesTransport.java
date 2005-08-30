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

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.User;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class DataServicesTransport implements DataServices {
    private static Log log = LogFactory.getLog(DataServicesTransport.class);

	private String endpoint = "";

    public DataServicesTransport() {
    	super();
    }

    public DataServicesTransport(String endpt) {
        endpoint=endpt;
        log.info("ENDPOINT: " + endpoint);
    }

	/* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.DataServices#getDatasets()
	 */
    public Dataset[] getDatasets() throws EmfException {
    	log.debug("Get all dataset types");

    	// Call the ExImServices endpoint and acquire the array of all dataset types
    	//defined in the system
        Dataset[] datasets = null;;
        
        Service  service = new Service();
        Call     call;

        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            
            QName qname1 = new QName("urn:gov.epa.emf.services.DataServices","ns1:EmfDataset");
            QName qname2 = new QName("urn:gov.epa.emf.services.DataServices","ns1:EmfDatasets");
            QName qname3 = new QName("urn:gov.epa.emf.services.DataServices","getDatasets");
            
            call.setOperationName(qname3);
            
            Class cls1 = gov.epa.emissions.commons.io.EmfDataset.class;
            Class cls2 = gov.epa.emissions.commons.io.EmfDataset[].class;
	          
            call.registerTypeMapping(cls1, qname1,
					  new org.apache.axis.encoding.ser.BeanSerializerFactory(cls1, qname1),        
					  new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls1, qname1));        			  
		    call.registerTypeMapping(cls2, qname2,
						  new org.apache.axis.encoding.ser.ArraySerializerFactory(cls2, qname2),        
						  new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname2));        			  
		          
            call.setReturnType(qname2);
            
            Object obj = call.invoke( new Object[] {} );
                            
            datasets = (Dataset[])obj;
            
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
    	return datasets;
    }


	/* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.DataServices#getDatasets(gov.epa.emissions.framework.services.User)
	 */
	public Dataset[] getDatasets(User user) throws EmfException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.DataServices#insertDataset(gov.epa.emissions.commons.io.Dataset)
	 */
	public void insertDataset(EmfDataset aDataset) throws EmfException {
    	log.debug("insert a new dataset type object: " + aDataset.getName());
    	Service  service = new Service();
        Call     call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            
            QName qname2 = new QName("urn:gov.epa.emf.services.DataServices","ns1:EmfDataset");
            QName qname3 = new QName("urn:gov.epa.emf.services.DataServices", "insertDataset");
            
            call.setOperationName(qname3);
            
            Class cls2 = gov.epa.emissions.commons.io.EmfDataset.class;
            
	        call.registerTypeMapping(cls2, qname2,
	    			  new org.apache.axis.encoding.ser.BeanSerializerFactory(cls2, qname2),        
	    			  new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls2, qname2));        			  
            
           call.addParameter("dataset", qname2, ParameterMode.IN );
		          
            call.setReturnType(org.apache.axis.Constants.XSD_ANY);
            EmfDataset anEmfDataset = (EmfDataset) aDataset;
            
            call.invoke( new Object[] {anEmfDataset} );
            
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
                
    	log.debug("insert a new dataset type object: " + aDataset.getName());
       

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

}
