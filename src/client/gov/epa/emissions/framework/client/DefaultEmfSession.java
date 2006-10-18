package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
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

public class DefaultEmfSession implements EmfSession {

    private User user;

    private ServiceLocator serviceLocator;

    private String mostRecentExportFolder;

    private UserPreference preferences;

    public DefaultEmfSession(User user, ServiceLocator locator) throws EmfException {
        serviceLocator = locator;
        this.preferences = new DefaultUserPreferences();
        this.user = user;
    }

    public UserPreference preferences() {
        return preferences;
    }

    public ServiceLocator serviceLocator() {
        return serviceLocator;
    }

    public User user() {
        return user;
    }

    public ExImService eximService() {
        return serviceLocator.eximService();
    }

    public DataService dataService() {
        return serviceLocator.dataService();
    }

    public String getMostRecentExportFolder() {
        return mostRecentExportFolder;
    }

    public void setMostRecentExportFolder(String mostRecentExportFolder) {
        this.mostRecentExportFolder = mostRecentExportFolder;
    }

    public UserService userService() {
        return serviceLocator.userService();
    }

    public LoggingService loggingService() {
        return serviceLocator.loggingService();
    }

    public DataCommonsService dataCommonsService() {
        return serviceLocator.dataCommonsService();
    }

    public DataViewService dataViewService() {
        return serviceLocator.dataViewService();
    }

    public DataEditorService dataEditorService() {
        return serviceLocator.dataEditorService();
    }

    public QAService qaService() {
        return serviceLocator.qaService();
    }

    public CaseService caseService() {
        return serviceLocator.caseService();
    }

    public ControlMeasureService controlMeasureService() {
        return serviceLocator.controlMeasureService();
    }

    public ControlStrategyService controlStrategyService() {
        return serviceLocator.controlStrategyService();
    }

    public ControlMeasureImportService controlMeasureImportService() {
        return serviceLocator.controlMeasureImportService();
    }

    public ControlMeasureExportService controlMeasureExportService() {
        return serviceLocator.controlMeasureExportService();
    }
}
