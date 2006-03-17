package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.basic.LoggingService;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.editor.DataViewService;
import gov.epa.emissions.framework.services.exim.ExImService;
import gov.epa.emissions.framework.services.qa.QAService;

public interface EmfSession {

    ServiceLocator serviceLocator();

    User user();

    ExImService eximService();

    DataService dataService();

    String getMostRecentExportFolder();

    void setMostRecentExportFolder(String mostRecentExportFolder);

    UserService userService();

    LoggingService loggingService();

    DataCommonsService dataCommonsService();

    DataViewService dataViewService();

    DataEditorService dataEditorService();

    UserPreference preferences();

    QAService qaService();
}