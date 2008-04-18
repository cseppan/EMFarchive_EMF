package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public void addNewParameterDialog(NewCaseParameterView dialog, CaseParameter newParam) {
        dialog.register(this);
        dialog.display(newParam.getCaseID(), newParam);
    }

    public void addNewParameter(CaseParameter param) throws EmfException {
        CaseParameter loaded = service().addCaseParameter(session.user(), param);

        if (param.getCaseID() == caseObj.getId()) {
            view.addParameter(loaded);
            refreshView();
        }
    }

    private CaseService service() {
        return session.caseService();
    }

    private void refreshView() {
        view.refresh();
        // view.notifychanges();
    }

    public void editParameter(CaseParameter param, EditCaseParameterView parameterEditor) throws EmfException {
        EditCaseParameterPresenter editInputPresenter = new EditCaseParameterPresenterImpl(caseObj.getId(),
                parameterEditor, view, session);
        editInputPresenter.display(param);
    }

    public void copyParameter(int caseID, NewCaseParameterDialog dialog, CaseParameter param) throws Exception {
        CaseParameter newParam = (CaseParameter) DeepCopy.copy(param);
        newParam.setCaseID(caseID);
        addNewParameterDialog(dialog, newParam);
    }

    public void addParameterFields(CaseParameter newParameter, JComponent container,
            ParameterFieldsPanelView parameterFields) throws EmfException {
        ParameterFieldsPanelPresenter parameterFieldsPresenter = new ParameterFieldsPanelPresenter(caseObj.getId(),
                parameterFields, session);
        parameterFieldsPresenter.display(newParameter, container);
    }

    public CaseParameter[] getCaseParameters(int caseId, Sector sector, boolean showAll) throws EmfException {
        if (sector == null)
            return new CaseParameter[0];

        if (sector.compareTo(new Sector("All", "All")) == 0)
            sector = null; // to trigger select all on the server side

        return service().getCaseParameters(caseId, sector, showAll);
    }

    public void removeParameters(CaseParameter[] params) throws EmfException {
        service().removeCaseParameters(params);
    }

    public Sector[] getAllSetcors() throws EmfException {
        List<Sector> all = new ArrayList<Sector>();
        all.add(new Sector("All", "All"));
        all.addAll(Arrays.asList(session.dataCommonsService().getSectors()));

        return all.toArray(new Sector[0]);
    }

    public Case getCaseObj() {
        return this.caseObj;
    }

    public void checkIfLockedByCurrentUser() throws EmfException {
        Case reloaded = service().reloadCase(caseObj.getId());

        if (!reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current case object expired. User " + reloaded.getLockOwner()
                    + " has it now.");
    }
    
    public Object[] getAllCaseNameIDs() throws EmfException {
        return service().getAllCaseNameIDs();
    }

}
