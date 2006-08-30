package gov.epa.emissions.framework.services.cost.controlmeasure;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EMFService;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;

public interface ControlMeasureImportService extends EMFService {

    void importControlMeasures(String folderPath, String[] fileNames, User user) throws EmfException;

    Status[] getImportStatus(User user) throws EmfException;

}
