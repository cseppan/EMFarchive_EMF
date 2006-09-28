package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

public interface QAService {

    QAStep[] getQASteps(EmfDataset dataset) throws EmfException;

    QAProgram[] getQAPrograms() throws EmfException;

    void updateWitoutCheckingConstraints(QAStep[] steps) throws EmfException;

    void update(QAStep step) throws EmfException;

    void runQAStep(QAStep step, User user) throws EmfException;

    public void exportQAStep(QAStep step, User user, String dirName) throws EmfException;

    QAStepResult getQAStepResult(QAStep step) throws EmfException;

}
