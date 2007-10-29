package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.LoggingService;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.controlmeasure.ControlMeasureExportService;
import gov.epa.emissions.framework.services.cost.controlmeasure.ControlMeasureImportService;
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

    private EmfCall eximCall;
    
    private CaseService caseService;
    
    private QAService qaService;
    
    private UserService userService;
    
    private LoggingService loggingService;
    
    private DataCommonsService dataCommonsService;
    
    private ControlMeasureService controlMeasureService;
    
    private ControlStrategyService controlStrategyService;
    
    private ControlMeasureImportService controlMeasureImportService;
    
    private ControlMeasureExportService controlMeasureExportService;
    
    public RemoteServiceLocator(String baseUrl) throws Exception {
        this.baseUrl = baseUrl;
        editCall = this.createSessionEnabledCall("DataEditor Service", baseUrl
                + "/gov.epa.emf.services.editor.DataEditorService");
        viewCall = this.createSessionEnabledCall("DataView Service", baseUrl
                + "/gov.epa.emf.services.editor.DataViewService");
        eximCall = this.createSessionEnabledCall("ExIm Service", baseUrl + "/gov.epa.emf.services.exim.ExImService");
    }

    public UserService userService() {
        if (userService == null)
            userService = new UserServiceTransport(baseUrl + "/gov.epa.emf.services.basic.UserService");
        
        return userService;
    }

    public ExImService eximService() {
        return new ExImServiceTransport(eximCall);
    }

    public DataService dataService() {
        return new DataServiceTransport(baseUrl + "/gov.epa.emf.services.data.DataService");
    }

    public LoggingService loggingService() {
        if (loggingService == null)
            loggingService = new LoggingServiceTransport(baseUrl + "/gov.epa.emf.services.basic.LoggingService");
        
        return loggingService;
    }

    public QAService qaService() {
        if (qaService == null)
            qaService = new QAServiceTransport(baseUrl + "/gov.epa.emf.services.qa.QAService");
        
        return qaService;
    }

    public DataCommonsService dataCommonsService() {
        if (dataCommonsService == null)
            dataCommonsService = new DataCommonsServiceTransport(baseUrl + "/gov.epa.emf.services.data.DataCommonsService");
        
        return dataCommonsService;
    }

    public DataEditorService dataEditorService() {
        return new DataEditorServiceTransport(editCall);
    }

    public DataViewService dataViewService() {
        return new DataViewServiceTransport(viewCall);
    }

    public CaseService caseService() {
        if (caseService == null)
            caseService = new CaseServiceTransport(baseUrl + "/gov.epa.emissions.framework.services.casemanagement.CaseService");
        
        return caseService;
    }
    
    public ControlMeasureService controlMeasureService() {
        if (controlMeasureService == null)
            controlMeasureService = new ControlMeasureServiceTransport(baseUrl + "/gov.epa.emf.services.cost.ControlMeasureService");
        
        return controlMeasureService;
    }
    
    public ControlStrategyService controlStrategyService() {
        if (controlStrategyService == null)
            controlStrategyService = new ControlStrategyServiceTransport(baseUrl + "/gov.epa.emissions.framework.services.cost.ControlStrategyService");
        
        return controlStrategyService;
    }
    
    public ControlMeasureImportService controlMeasureImportService() {
        if (controlMeasureImportService == null)
            controlMeasureImportService = new ControlMeasureImportServiceTransport(baseUrl + "/gov.epa.emf.services.cost.controlmeasure.ControlMeasureImportService");
        
        return controlMeasureImportService;
    }

    public ControlMeasureExportService controlMeasureExportService() {
        if (controlMeasureExportService == null)
            controlMeasureExportService = new ControlMeasureExportServiceTransport(baseUrl + "/gov.epa.emf.services.cost.controlmeasure.ControlMeasureExportService");
        
        return controlMeasureExportService;
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
