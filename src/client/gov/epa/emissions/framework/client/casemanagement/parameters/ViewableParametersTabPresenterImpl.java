package gov.epa.emissions.framework.client.casemanagement.parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;

import javax.swing.JComponent;

public class ViewableParametersTabPresenterImpl {

    private Case caseObj;

    private ViewableParametersTab view;
    
    private int defaultPageSize = 20;

    private EmfSession session;

    public ViewableParametersTabPresenterImpl(EmfSession session, ViewableParametersTab view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
    }

    public void display() {
        view.display(session, caseObj, this);
    }


    private CaseService service() {
        return session.caseService();
    }
    
    public void editParameter(CaseParameter param, EditCaseParameterView parameterEditor) throws EmfException {
        EditCaseParameterPresenter editInputPresenter = new EditCaseParameterPresenterImpl(caseObj.getId(), parameterEditor, session);
        editInputPresenter.display(param);
    }

    public void addParameterFields(CaseParameter newParameter, JComponent container, ParameterFieldsPanelView parameterFields) throws EmfException {
        ParameterFieldsPanelPresenter parameterFieldsPresenter = new ParameterFieldsPanelPresenter(caseObj.getId(), parameterFields, session);
        parameterFieldsPresenter.display(newParameter, container);
    }

    public CaseParameter[] getCaseParameters(int caseId) throws EmfException {
        return service().getCaseParameters(caseId);
    }
    
    public CaseParameter[] getCaseParameters(int caseId, Sector sector, boolean showAll) throws EmfException {
        return service().getCaseParameters(defaultPageSize, caseId, sector, showAll);
    }

    public Sector[] getAllSetcors() throws EmfException {
        List<Sector> all = new ArrayList<Sector>();
        all.add(new Sector("All", "All"));
        all.addAll(Arrays.asList(session.dataCommonsService().getSectors()));

        return all.toArray(new Sector[0]);
    }
    
    public int getPageSize() {
        return this.defaultPageSize;
    }

    public Case getCaseObj() {
        return this.caseObj;
    }

}
