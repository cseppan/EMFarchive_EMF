package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.cost.controlmeasure.ControlMeasureImportService;

public class ControlMeasureImportServiceTransport implements ControlMeasureImportService {

    private CallFactory callFactory;

    private DataMappings mappings;

    public ControlMeasureImportServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        return callFactory.createCall("ControlMeasureService");
    }

    public void importControlMeasures(String folderPath, String[] fileNames, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("importControlMeasures");
        call.addParam("folderPath", mappings.string());
        call.addParam("fileNames", mappings.strings());
        call.addParam("user", mappings.user());

        call.setReturnType(mappings.controlMeasures());

        call.request(new Object[] { folderPath, fileNames, user });
    }

    public Status[] getImportStatus(User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("getImportStatus");
        call.addParam("user", mappings.user());

        call.setReturnType(mappings.statuses());

        return (Status[]) call.requestResponse(new Object[] { user });
    }

    public void removeImportStatuses(User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeImportStatuses");
        call.addParam("user", mappings.user());

        call.setVoidReturnType();

        call.requestResponse(new Object[] { user });
    }
}
