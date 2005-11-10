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
import gov.epa.emissions.framework.services.DataEditorServices;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.DatasetTypesServices;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.InterDataServices;
import gov.epa.emissions.framework.services.LoggingServices;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.UserServices;

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

    public UserServices getUserServices() {
        return new UserServicesTransport(baseUrl + "/gov.epa.emf.services.UserServices");
    }

    public StatusServices getStatusServices() {
        return new StatusServicesTransport(baseUrl + "/gov.epa.emf.services.StatusServices");
    }

    public ExImServices getExImServices() {
        return new ExImServicesTransport(baseUrl + "/gov.epa.emf.services.ExImServices");
    }

    public DataServices getDataServices() {
        return new DataServicesTransport(baseUrl + "/gov.epa.emf.services.DataServices");
    }

    public LoggingServices getLoggingServices() {
        return new LoggingServicesTransport(baseUrl + "/gov.epa.emf.services.LoggingServices");
    }

    public DatasetTypesServices getDatasetTypesServices() {
        return new DatasetTypesServicesTransport(baseUrl + "/gov.epa.emf.services.DatasetTypesServices");
    }

    public InterDataServices getInterDataServices() {
        return new InterDataServicesTransport(baseUrl + "/gov.epa.emf.services.InterDataServices");
    }

    public DataEditorServices getDataEditorServices() {
        return new DataEditorServicesTransport(baseUrl + "/gov.epa.emf.services.DataEditorServices",call);
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
