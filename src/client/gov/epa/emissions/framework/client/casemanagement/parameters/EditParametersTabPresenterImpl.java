package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;

import javax.swing.JComponent;

public class EditParametersTabPresenterImpl implements EditParametersTabPresenter {

    private Case caseObj;

    private EditCaseParametersTabView view;

    private EmfSession session;

    public EditParametersTabPresenterImpl(EmfSession session, EditCaseParametersTabView view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
    }

    public void display() {
        view.display(session, caseObj, this);
    }

    public void doSave() {
        view.refresh();
    }

    public void addNewParameterDialog(NewCaseParameterView dialog) {
        dialog.register(this);
        dialog.display(caseObj.getId());
    }

    public void addNewParameter(CaseParameter param) throws EmfException {
        param.setCaseID(caseObj.getId());
        view.addParameter(service().addCaseParameter(param));
        refreshView();
    }

    private CaseService service() {
        return session.caseService();
    }

    private void refreshView() {
        view.refresh();
        // view.notifychanges();
    }

    public void editParameter(CaseParameter param, EditCaseParameterView parameterEditor) throws EmfException {
        EditCaseParameterPresenter editInputPresenter = new EditCaseParameterPresenterImpl(caseObj.getId(), parameterEditor, view, session);
        editInputPresenter.display(param);
    }

    public void addParameterFields(CaseParameter newParameter, JComponent container, ParameterFieldsPanelView parameterFields) throws EmfException {
        ParameterFieldsPanelPresenter parameterFieldsPresenter = new ParameterFieldsPanelPresenter(caseObj.getId(), parameterFields, session);
        parameterFieldsPresenter.display(newParameter, container);
    }

    public CaseParameter[] getCaseParameters(int caseId) throws EmfException {
        return service().getCaseParameters(caseId);
    }

    public void removeParameters(CaseParameter[] params) {
        // NOTE Auto-generated method stub
        
    }

}
