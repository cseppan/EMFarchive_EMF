package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.LoggingService;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.editor.DataViewService;
import gov.epa.emissions.framework.services.exim.ExImService;
import gov.epa.emissions.framework.services.qa.QAService;

public class RemoteServiceLocator implements ServiceLocator {
    private String baseUrl;

    // Note: Each session-based service needs to create it's own Call object
    private EmfCall viewCall;

    private EmfCall editCall;

    public RemoteServiceLocator(String baseUrl) throws Exception {
        this.baseUrl = baseUrl;
        editCall = this.createSessionEnabledCall("DataEditor Service", baseUrl
                + "/gov.epa.emf.services.editor.DataEditorService");
        viewCall = this.createSessionEnabledCall("DataView Service", baseUrl
                + "/gov.epa.emf.services.editor.DataViewService");
    }

    public UserService userService() {
        return new UserServiceTransport(baseUrl + "/gov.epa.emf.services.basic.UserService");
    }

    public ExImService eximService() {
        return new ExImServiceTransport(baseUrl + "/gov.epa.emf.services.exim.ExImService");
    }

    public DataService dataService() {
        return new DataServiceTransport(baseUrl + "/gov.epa.emf.services.data.DataService");
    }

    public LoggingService loggingService() {
        return new LoggingServiceTransport(baseUrl + "/gov.epa.emf.services.basic.LoggingService");
    }

    public QAService qaService() {
        return new QAServiceTransport(baseUrl + "/gov.epa.emf.services.qa.QAService");
    }

    public DataCommonsService dataCommonsService() {
        return new DataCommonsServiceTransport(baseUrl + "/gov.epa.emf.services.data.DataCommonsService");
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
