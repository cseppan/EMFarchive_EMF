package gov.epa.emissions.framework.services.cost.controlmeasure;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EMFService;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

public interface ControlMeasureExportService extends EMFService {

    void exportControlMeasures(String folderPath, String prefix, ControlMeasure[] controlMeasures,
            User user) throws EmfException;

    void exportControlMeasuresWithOverwrite(String folderPath, String prefix, ControlMeasure[] controlMeasures,
            User user) throws EmfException;

    Status[] getExportStatus(User user) throws EmfException;

}
