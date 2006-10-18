package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlmeasure.ControlMeasureExportService;

public class ControlMeasureExportServiceTransport implements ControlMeasureExportService {

    private CallFactory callFactory;

    private DataMappings mappings;

    public ControlMeasureExportServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        return callFactory.createCall("ControlMeasureExportService");
    }

    public void exportControlMeasures(String folderPath, String prefix, ControlMeasure[] controlMeasures, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("exportControlMeasures");
        call.addParam("folderPath", mappings.string());
        call.addParam("prefix", mappings.string());
        call.addParam("controlMeasures", mappings.controlMeasures());
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { folderPath, prefix, controlMeasures, user });
    }

    public Status[] getExportStatus(User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("getExportStatus");
        call.addParam("user", mappings.user());

        call.setReturnType(mappings.statuses());

        return (Status[]) call.requestResponse(new Object[] { user });
    }
}
