package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.security.User;
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

    public void runQAStep(QAStep step, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("runQAStep");
        call.addParam("step", mappings.qaStep());
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { step, user });

    }

    public void exportQAStepWithOverwrite(QAStep step, String dirName) throws EmfException {
        export(step, dirName);
    }

    public void exportQAStep(QAStep step, String dirName) throws EmfException {
        export(step, dirName);
    }

    private void export(QAStep step, String dirName) throws EmfException {
        EmfCall call = call();

        call.setOperation("exportQAStep");
        call.addParam("step", mappings.qaStep());
        call.addStringParam("dirName");
        call.setVoidReturnType();

        call.request(new Object[] { step, dirName });
    }

}
