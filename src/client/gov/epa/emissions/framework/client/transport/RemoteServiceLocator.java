/*
 * Created on Aug 11, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.client.transport;
 * File Name: ServiceLocator.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.DatasetTypeService;
import gov.epa.emissions.framework.services.ExImService;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.LoggingService;
import gov.epa.emissions.framework.services.StatusService;
import gov.epa.emissions.framework.services.UserService;

import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: find a better package location for locator classes
public class RemoteServiceLocator implements ServiceLocator {
    private static Log log = LogFactory.getLog(RemoteServiceLocator.class);


    private String baseUrl;
    private Call call = null;

    public RemoteServiceLocator(String baseUrl) throws Exception{
        try {
            this.baseUrl = baseUrl;
            call = this.createCall();
        } catch (ServiceException e) {
            log.error("Failed to create Axis Call object: " + e.getMessage());
            throw new EmfException("Error communicating with the server");
        }
    }

    public UserService getUserService() {
        return new UserServiceTransport(baseUrl + "/gov.epa.emf.services.UserService");
    }

    public StatusService getStatusService() {
        return new StatusServiceTransport(baseUrl + "/gov.epa.emf.services.StatusService");
    }

    public ExImService getExImService() {
        return new ExImServiceTransport(baseUrl + "/gov.epa.emf.services.ExImService");
    }

    public DataService getDataService() {
        return new DataServiceTransport(baseUrl + "/gov.epa.emf.services.DataService");
    }

    public LoggingService getLoggingService() {
        return new LoggingServiceTransport(baseUrl + "/gov.epa.emf.services.LoggingService");
    }

    public DatasetTypeService getDatasetTypesService() {
        return new DatasetTypeServiceTransport(baseUrl + "/gov.epa.emf.services.DatasetTypeService");
    }

    public DataCommonsService getDataCommonsService() {
        return new DataCommonsServiceTransport(baseUrl + "/gov.epa.emf.services.DataCommonsService");
    }

    public DataEditorService getDataEditorService() {
        return new DataEditorServiceTransport(call,baseUrl + "/gov.epa.emf.services.DataEditorService");
    }

    /*
     * Create a singleton reference to the Axis Service Call object
     * This call object will be universal to all client transport objects 
     * and will be passed in to via the transport object's constructor
     * 
     */
    private Call createCall() throws ServiceException {
        Service service = new Service();
        Call call = (Call) service.createCall();
        call.setMaintainSession(true);
        call.setTimeout(new Integer(0));
        
        return call;
    }

}
