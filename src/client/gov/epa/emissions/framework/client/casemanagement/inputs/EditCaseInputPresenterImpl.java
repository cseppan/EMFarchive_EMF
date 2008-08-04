package gov.epa.emissions.framework.client.casemanagement.inputs;

import javax.swing.JComponent;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;

public class EditCaseInputPresenterImpl implements EditInputPresenter {

    private EditCaseInputView view;
    
    private EditInputsTabView parentView;
    
    private EmfSession session;
    
    private InputFieldsPanelPresenter inputFieldsPresenter;
    
    private CaseInput input;
    
    private int caseid, modelToRunId;

    public EditCaseInputPresenterImpl(int caseid, EditCaseInputView view, 
            EditInputsTabView parentView, EmfSession session) {
        this.view = view;
        this.parentView = parentView;
        this.session = session;
        this.caseid = caseid;
    }
    
    public EditCaseInputPresenterImpl(int caseid, EditCaseInputView view, 
           EmfSession session) {
        this.view = view;
        this.session = session;
        this.caseid = caseid;
    }
    
    public void display(CaseInput input, int modelToRunId) throws EmfException {
        this.modelToRunId = modelToRunId;
        this.input = input;
        view.observe(this);
        view.display(input);
        view.populateFields();
    }
    
    public void doAddInputFields(JComponent container, 
            InputFieldsPanelView inputFields) throws EmfException {
        inputFieldsPresenter = new InputFieldsPanelPresenter(caseid, inputFields, session);
        inputFieldsPresenter.display(input, container, modelToRunId);
    }
    
    public void doSave() throws EmfException {
        inputFieldsPresenter.doSave();
        parentView.refresh();
    }

    public EmfSession getSession() {
        return session;
    }
    
    

}
