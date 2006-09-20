package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public interface QAService {

    QAStep[] getQASteps(EmfDataset dataset) throws EmfException;

    QAProgram[] getQAPrograms() throws EmfException;

    void update(QAStep[] steps) throws EmfException;

    void runQAStep(QAStep step, User user) throws EmfException;

    public void exportQAStepWithOverwrite(QAStep step, String dirName) throws EmfException;

    public void exportQAStep(QAStep step, String dirName) throws EmfException;

}
