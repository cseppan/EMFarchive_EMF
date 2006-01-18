package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.DataViewService;
import gov.epa.emissions.framework.services.ExImService;
import gov.epa.emissions.framework.services.LoggingService;
import gov.epa.emissions.framework.services.UserService;

public interface ServiceLocator {
    UserService userService();

    ExImService eximService();

    DataService dataService();

    LoggingService loggingService();

    DataCommonsService dataCommonsService();

    DataEditorService dataEditorService();

    DataViewService dataViewService();

}
