package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Sector;

public interface EditableCaseSummaryTabPresenter extends CaseEditorTabPresenter {
    void addSector(Sector sector);
}