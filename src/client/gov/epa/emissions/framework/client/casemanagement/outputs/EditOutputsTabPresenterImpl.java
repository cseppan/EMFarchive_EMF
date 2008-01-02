package gov.epa.emissions.framework.client.casemanagement.outputs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.data.EmfDataset;

import javax.swing.JComponent;

public class EditOutputsTabPresenterImpl implements EditOutputsTabPresenter {

    private Case caseObj;

    private EditOutputsTabView view;
    
    private EmfSession session;
    
    public EditOutputsTabPresenterImpl(EmfSession session, EditOutputsTabView view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
    }

    public void display() {
        view.observe(this);
        view.display();
    }

    public void doSave() throws EmfException {
        // NOTE Auto-generated method stub
        try{
            view.refresh();
        }catch (Exception e) {
            throw new EmfException("Cannot save output tab");
        }
    }

    public CaseOutput[] getCaseOutputs(int caseId, int jobId) throws EmfException {
        return service().getCaseOutputs(caseId, jobId);
    }

    private CaseService service() {
        return session.caseService();
    }

    public List<CaseJob> getCaseJobs() throws EmfException {
        CaseJob[] caseJobs=service().getCaseJobs(caseObj.getId());
        List<CaseJob> jobs= new ArrayList<CaseJob>();
        jobs.addAll(Arrays.asList(caseJobs));
        Collections.sort(jobs);
        return jobs; 
 //       return service().getCaseJobs(caseObj.getId());
    }
    
    public Case getCaseObj() {
        return this.caseObj;
    }


    public void doRemove(CaseOutput[] outputs, boolean deleteDataset) throws EmfException {
       try{
        service().removeCaseOutputs(session.user(), outputs, deleteDataset);
       }catch (EmfException e) {
           throw new EmfException(e.getMessage());
       }
    }
    
    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) {
        view.clearMessage();

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }
    
    public EmfDataset getDataset(int id) throws EmfException{
        return session.dataService().getDataset(id);
        
    }

    public void editOutput(CaseOutput output, EditCaseOutputView outputEditor) throws EmfException {
        EditOutputPresenter editOutputPresenter = new EditCaseOutputPresenterImpl(caseObj.getId(), outputEditor, view,
                session);
        editOutputPresenter.display(output);
    }
    
    public void addNewOutputDialog(NewOutputView dialog, CaseOutput newOutput) {
        dialog.observe(this);
        dialog.display(caseObj.getId(), newOutput);
    }

    public void addNewOutput(CaseOutput output) throws EmfException {
        output.setCaseId(caseObj.getId());
        view.addOutput(service().addCaseOutput(output));
        view.refresh();
    }
    
    public void doAddOutputFields(JComponent container, OutputFieldsPanelView outputFields, CaseOutput newOutput) throws EmfException {
 //       newOutput.setId(view.numberOfRecord());
        
        OutputFieldsPanelPresenter outputFieldsPresenter = new OutputFieldsPanelPresenter(caseObj.getId(), outputFields,
                session);
        outputFieldsPresenter.display(newOutput, container);
    }
}
