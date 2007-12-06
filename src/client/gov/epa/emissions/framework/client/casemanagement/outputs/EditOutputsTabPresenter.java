package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorTabPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;

public interface EditOutputsTabPresenter extends CaseEditorTabPresenter {

    void display() throws EmfException;
    
    CaseOutput[] getCaseOutputs(int caseId, int jobId) throws EmfException;
    
    CaseJob[] getCaseJobs()throws EmfException;
    

}