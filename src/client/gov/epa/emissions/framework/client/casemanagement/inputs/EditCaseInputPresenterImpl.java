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

    public EditCaseInputPresenterImpl(EditCaseInputView view, 
            EditInputsTabView parentView, EmfSession session) {
        this.view = view;
        this.parentView = parentView;
        this.session = session;
    }
    
    public void display(CaseInput input) throws EmfException {
        this.input = input;
        view.observe(this);
        view.display(input);
        view.populateFields();
    }
    
    public void doAddInputFields(JComponent container, 
            InputFieldsPanelView inputFields) throws EmfException {
        inputFieldsPresenter = new InputFieldsPanelPresenter(inputFields, session);
        inputFieldsPresenter.display(input, container);
    }
    
    public void doSave() throws EmfException {
        inputFieldsPresenter.doSave();
        parentView.refresh();
    }

    public void doCheckDuplicate(CaseInput input) throws EmfException {
        parentView.checkDuplicate(input);
    }

}
