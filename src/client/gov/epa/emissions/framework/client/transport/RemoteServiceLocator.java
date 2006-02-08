package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.DataViewService;
import gov.epa.emissions.framework.services.ExImService;
import gov.epa.emissions.framework.services.LoggingService;
import gov.epa.emissions.framework.services.UserService;

public class RemoteServiceLocator implements ServiceLocator {
    private String baseUrl;

    // Note: Each session-based service needs to create it's own Call object
    private EmfCall viewCall;

    private EmfCall editCall;

    public RemoteServiceLocator(String baseUrl) throws Exception {
        this.baseUrl = baseUrl;
        editCall = this.createSessionEnabledCall("DataEditor Service", baseUrl
                + "/gov.epa.emf.services.DataEditorService");
        viewCall = this.createSessionEnabledCall("DataView Service", baseUrl + "/gov.epa.emf.services.DataViewService");
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
        return new DataViewServiceTransport(viewCall);
    }

    /*
     * Create a singleton reference to the Axis Service Call object This call object will be universal to all client
     * transport objects and will be passed in to via the transport object's constructor
     * 
     */
    private EmfCall createSessionEnabledCall(String service, String url) throws EmfException {
        CallFactory callFactory = new CallFactory(url);
        return callFactory.createSessionEnabledCall(service);
    }

}
