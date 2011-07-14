package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

public interface QAService {

    QAStep[] getQASteps(EmfDataset dataset) throws EmfException;
    
    QAStepResult[] getQAStepResults(EmfDataset dataset) throws EmfException; 

    QAProgram[] getQAPrograms() throws EmfException;

    QAStep[] updateWitoutCheckingConstraints(QAStep[] steps) throws EmfException;

    QAStep update(QAStep step) throws EmfException;
    
    boolean getSameAsTemplate(QAStep step) throws EmfException;

    void runQAStep(QAStep step, User user) throws EmfException;

    public void exportQAStep(QAStep step, User user, String dirName) throws EmfException;

    public void exportShapeFileQAStep(QAStep step, User user, String dirName, ProjectionShapeFile projectionShapeFile, Pollutant pollutant) throws EmfException;

    QAStepResult getQAStepResult(QAStep step) throws EmfException;
    
    //String getRunStatus(QAStep step) throws EmfException;

    QAProgram addQAProgram(QAProgram program) throws EmfException;

    ProjectionShapeFile[] getProjectionShapeFiles() throws EmfException;

    void copyQAStepsToDatasets(User user, QAStep[] steps, int[] datasetIds, boolean replace) throws EmfException;

}
