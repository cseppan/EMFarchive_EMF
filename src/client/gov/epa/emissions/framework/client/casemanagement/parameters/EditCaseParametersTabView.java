package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.data.GeoRegion;

public interface EditCaseParametersTabView {

    void display(EmfSession session, Case caseObj, EditParametersTabPresenter presenter);

    CaseParameter[] caseParameters();

    void addParameter(CaseParameter param);
    
    void refresh();
    
    int numberOfRecord();

    void clearMessage();
    
    void addSectorBacktoCase(Sector updatedSector);
    
    void addGridBacktoCase(GeoRegion updatedGrid);
    
    void setMessage(String msg);

}
