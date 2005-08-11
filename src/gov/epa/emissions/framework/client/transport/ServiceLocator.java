/*
 * Created on Aug 11, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.client.transport;
 * File Name: ServiceLocator.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.commons.EMFService;
import gov.epa.emissions.framework.commons.EMFConstants;


/**
 * @author Conrad F. D'Cruz
 *
 */
public class ServiceLocator {
    // singleton's private instance 
    private static ServiceLocator me;

    static {
      me = new ServiceLocator();
    }
      
    private ServiceLocator() {}

    // returns the Service Locator instance 
    static public ServiceLocator getInstance() { 
      return me;
    }

    public UserServicesTransport getUsersService(int deployServer){
        String prefix=null;
        switch (deployServer){
	        case EMFConstants.HOST_NAME_BEN_ID: prefix=EMFConstants.HOST_NAME_BEN;
	        case EMFConstants.HOST_NAME_LOCAL_ID: prefix=EMFConstants.HOST_NAME_LOCAL;
	        case EMFConstants.HOST_NAME_PROD_ID: prefix=EMFConstants.HOST_NAME_PROD;
        }
        return new UserServicesTransport(prefix + EMFConstants.usersEndpoint);
    }

    public StatusServicesTransport getStatusService(int deployServer){
        String prefix=null;
        switch (deployServer){
	        case EMFConstants.HOST_NAME_BEN_ID: prefix=EMFConstants.HOST_NAME_BEN;
	        case EMFConstants.HOST_NAME_LOCAL_ID: prefix=EMFConstants.HOST_NAME_LOCAL;
	        case EMFConstants.HOST_NAME_PROD_ID: prefix=EMFConstants.HOST_NAME_PROD;
        }
        return new StatusServicesTransport(prefix + EMFConstants.statusEndpoint);
    }

    public ExImServicesTransport getEximService(int deployServer){
        String prefix=null;
        switch (deployServer){
	        case EMFConstants.HOST_NAME_BEN_ID: prefix=EMFConstants.HOST_NAME_BEN;
	        case EMFConstants.HOST_NAME_LOCAL_ID: prefix=EMFConstants.HOST_NAME_LOCAL;
	        case EMFConstants.HOST_NAME_PROD_ID: prefix=EMFConstants.HOST_NAME_PROD;
        }
        return new ExImServicesTransport(prefix + EMFConstants.eximEndpoint);
    }
    
}//ServiceLocator
