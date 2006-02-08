package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.DataViewService;
import gov.epa.emissions.framework.services.ExImService;
import gov.epa.emissions.framework.services.LoggingService;
import gov.epa.emissions.framework.services.UserService;

import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RemoteServiceLocator implements ServiceLocator {
    private static Log log = LogFactory.getLog(RemoteServiceLocator.class);

    private String baseUrl;

    // Note: Each session-based service needs to create it's own Call object
    private Call viewCall = null;

    private EmfCall editCall;

    public RemoteServiceLocator(String baseUrl) throws Exception {
        try {
            this.baseUrl = baseUrl;
            viewCall = this.createCall();
            editCall = this.createEmfCall("DataEditor Service", baseUrl + "/gov.epa.emf.services.DataEditorService");
        } catch (ServiceException e) {
            log.error("Failed to create Axis Call object: " + e.getMessage());
            throw new EmfException("Error communicating with the server");
        }
    }

    public UserService userService() {
        return new UserServiceTransport(baseUrl + "/gov.epa.emf.services.UserService");
    }

    public ExImService eximService() {
        return new ExImServiceTransport(baseUrl + "/gov.epa.emf.services.ExImService");
    }

    public DataService dataService() {
        return new DataServiceTransport(baseUrl + "/gov.epa.emf.services.DataService");
    }

    public LoggingService loggingService() {
        return new LoggingServiceTransport(baseUrl + "/gov.epa.emf.services.LoggingService");
    }

    public DataCommonsService dataCommonsService() {
        return new DataCommonsServiceTransport(baseUrl + "/gov.epa.emf.services.DataCommonsService");
    }

    public DataEditorService dataEditorService() {
        return new DataEditorServiceTransport(editCall);
    }

    public DataViewService dataViewService() {
        return new DataViewServiceTransport(viewCall, baseUrl + "/gov.epa.emf.services.DataViewService");
    }

    /*
     * Create a singleton reference to the Axis Service Call object This call object will be universal to all client
     * transport objects and will be passed in to via the transport object's constructor
     * 
     */
    private Call createCall() throws ServiceException {
        Service service = new Service();
        Call call = (Call) service.createCall();
        call.setMaintainSession(true);
        call.setTimeout(new Integer(0));// never times out

        return call;
    }

    private EmfCall createEmfCall(String service, String url) throws EmfException {
        CallFactory callFactory = new CallFactory(url);
        EmfCall call = callFactory.createSessionEnabledCall(service);

        return call;
    }

}
