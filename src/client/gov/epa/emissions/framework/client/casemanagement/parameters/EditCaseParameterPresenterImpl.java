package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;

import javax.swing.JComponent;

public class EditCaseParameterPresenterImpl implements EditCaseParameterPresenter {

    private EditCaseParameterView view;
    
    private EditCaseParametersTabView parentView;
    
    private EmfSession session;
    
    private ParameterFieldsPanelPresenter parameterFieldsPresenter;
    
    private CaseParameter parameter;
    
    private int caseid;

    public EditCaseParameterPresenterImpl(int caseid, EditCaseParameterView view, 
            EditCaseParametersTabView parentView, EmfSession session) {
        this.view = view;
        this.parentView = parentView;
        this.session = session;
        this.caseid = caseid;
    }
    
    public EditCaseParameterPresenterImpl(int caseid, 
            EditCaseParameterView view, EmfSession session) {
        this.view = view;
        this.session = session;
        this.caseid = caseid;
    }
    
    public void display(CaseParameter param) throws EmfException {
        this.parameter = param;
        view.observe(this);
        view.display(param);
        view.populateFields();
    }
    
    public void doAddInputFields(JComponent container, 
            ParameterFieldsPanelView parameterFields) throws EmfException {
        parameterFieldsPresenter = new ParameterFieldsPanelPresenter(caseid, parameterFields, session);
        parameterFieldsPresenter.display(parameter, container);
    }
    
    public void doSave() throws EmfException {
        parameterFieldsPresenter.doSave();
        parentView.refresh();
    }

}
