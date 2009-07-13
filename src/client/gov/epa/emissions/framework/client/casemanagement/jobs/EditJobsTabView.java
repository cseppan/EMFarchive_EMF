package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenter;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.data.GeoRegion;

public interface EditJobsTabView extends JobsTabView {

    void display(EmfSession session, Case caseObj, EditJobsTabPresenter presenter, CaseEditorPresenter parentPresenter);

    CaseJob[] caseJobs();

    void addJob(CaseJob job);
    
    void refresh();
    
    int numberOfRecord();
    
    void setMessage(String msg);

    void clearMessage();
    
    String getCaseOutputFileDir();
    
    void addSector(Sector sector);
    
    void addGrid(GeoRegion grid);

}
