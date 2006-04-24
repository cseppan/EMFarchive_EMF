package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.casemanagement.Case;

public interface CaseEditorView extends ManagedView {

    void observe(CaseEditorPresenter presenter);

    void display(Case caseObj);

    void notifyLockFailure(Case caseObj);

}