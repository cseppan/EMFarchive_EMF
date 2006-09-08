package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.qa.QAService;

public class QAServiceTransport implements QAService {

    private CallFactory callFactory;

    private DataMappings mappings;

    public QAServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        return callFactory.createCall("QA Service");
    }

    public QAStep[] getQASteps(EmfDataset dataset) throws EmfException {
        EmfCall call = call();

        call.setOperation("getQASteps");
        call.addParam("dataset", mappings.dataset());
        call.setReturnType(mappings.qaSteps());
        Object[] params = new Object[] { dataset };

        return (QAStep[]) call.requestResponse(params);
    }

    public void update(QAStep[] steps) throws EmfException {
        EmfCall call = call();

        call.setOperation("update");
        call.addParam("steps", mappings.qaSteps());
        call.setVoidReturnType();

        call.request(new Object[] { steps });
    }

    public QAProgram[] getQAPrograms() throws EmfException {
        EmfCall call = call();

        call.setOperation("getQAPrograms");
        call.setReturnType(mappings.programs());

        return (QAProgram[]) call.requestResponse(new Object[] {});
    }
}
