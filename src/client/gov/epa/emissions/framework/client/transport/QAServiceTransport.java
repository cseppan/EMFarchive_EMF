package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.qa.QAService;

public class QAServiceTransport implements QAService {

    private CallFactory callFactory;

    private DataMappings mappings;
    
    private EmfCall call;

    public QAServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        if (call == null)
            call = callFactory.createSessionEnabledCall("QA Service");
        
        return call;
    }

    public synchronized QAStep[] getQASteps(EmfDataset dataset) throws EmfException {
        EmfCall call = call();

        call.setOperation("getQASteps");
        call.addParam("dataset", mappings.dataset());
        call.setReturnType(mappings.qaSteps());
        Object[] params = new Object[] { dataset };

        return (QAStep[]) call.requestResponse(params);
    }

    public synchronized void updateWitoutCheckingConstraints(QAStep[] steps) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateWitoutCheckingConstraints");
        call.addParam("steps", mappings.qaSteps());
        call.setVoidReturnType();

        call.request(new Object[] { steps });
    }

    public synchronized QAProgram[] getQAPrograms() throws EmfException {
        EmfCall call = call();

        call.setOperation("getQAPrograms");
        call.setReturnType(mappings.programs());

        return (QAProgram[]) call.requestResponse(new Object[] {});
    }

    public synchronized void runQAStep(QAStep step, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("runQAStep");
        call.addParam("step", mappings.qaStep());
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { step, user });

    }

    public synchronized void exportQAStep(QAStep step, User user, String dirName) throws EmfException {
        EmfCall call = call();

        call.setOperation("exportQAStep");
        call.addParam("step", mappings.qaStep());
        call.addParam("user", mappings.user());
        call.addStringParam("dirName");
        call.setVoidReturnType();

        call.request(new Object[] { step, user, dirName });
    }

    public synchronized void exportShapeFileQAStep(QAStep step, User user, String dirName, ProjectionShapeFile projectionShapeFile, Pollutant pollutant) throws EmfException {
        EmfCall call = call();

        call.setOperation("exportShapeFileQAStep");
        call.addParam("step", mappings.qaStep());
        call.addParam("user", mappings.user());
        call.addStringParam("dirName");
        call.addParam("projectionShapeFile", mappings.projectionShapeFile());
        call.addParam("pollutant", mappings.pollutant());
        call.setVoidReturnType();

        call.request(new Object[] { step, user, dirName, projectionShapeFile, pollutant });
    }

    public synchronized QAStepResult getQAStepResult(QAStep step) throws EmfException {
        EmfCall call = call();

        call.setOperation("getQAStepResult");
        call.addParam("step", mappings.qaStep());
        call.setReturnType(mappings.qaStepResult());

        return (QAStepResult) call.requestResponse(new Object[] { step });

    }

    public synchronized void update(QAStep step) throws EmfException {
        EmfCall call = call();

        call.setOperation("update");
        call.addParam("steps", mappings.qaStep());
        call.setVoidReturnType();

        call.request(new Object[] { step });

    }

    public synchronized QAProgram addQAProgram(QAProgram program) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("addQAProgram");
        call.addParam("program", mappings.program());
        call.setReturnType(mappings.program());
        
        return (QAProgram) call.requestResponse(new Object[] { program });
    }

    public ProjectionShapeFile[] getProjectionShapeFiles() throws EmfException {
        EmfCall call = call();

        call.setOperation("getProjectionShapeFiles");
        call.setReturnType(mappings.projectionShapeFiles());

        return (ProjectionShapeFile[]) call.requestResponse(new Object[] {});
    }

}
