package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.editor.RelatedCasePresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.RelatedCaseView;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

public class ViewableInputsTabPresenterImpl {

    private Case caseObj;

    private ViewableInputsTab view;
    
    private int defaultPageSize = 20;

    private EmfSession session;

    public ViewableInputsTabPresenterImpl(EmfSession session, ViewableInputsTab view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
    }

    public void display() {
        view.display(session, caseObj, this);
    }

    public void addNewInputDialog(NewInputView dialog, CaseInput newInput) {
        dialog.register(this);
        dialog.display(caseObj.getId(), newInput);
    }

    private CaseService service() {
        return session.caseService();
    }

    public void doEditInput(CaseInput input, EditCaseInputView inputEditor) throws EmfException {
        EditInputPresenter editInputPresenter = new EditCaseInputPresenterImpl(caseObj.getId(), inputEditor,
                session);
        editInputPresenter.display(input, caseObj.getModel().getId());
    }
    
    public void doAddInputFields(JComponent container, InputFieldsPanelView inputFields, CaseInput newInput) throws EmfException {
        newInput.setId(view.numberOfRecord());
        
        InputFieldsPanelPresenter inputFieldsPresenter = new InputFieldsPanelPresenter(caseObj.getId(), inputFields,
                session);
        inputFieldsPresenter.display(newInput, container, caseObj.getModel().getId());
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        view.clearMessage();

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }

    public CaseInput[] getCaseInput(int caseId) throws EmfException {
        return service().getCaseInputs(caseId);
    }

    private void doExport(List<CaseInput> caseInputs, boolean overwrite, String purpose) throws EmfException {
        CaseService services = session.caseService();
        Integer[] caseInputIds = new Integer[caseInputs.size()];

        for (int i = 0; i < caseInputIds.length; i++) {
            caseInputIds[i] = new Integer(caseInputs.get(i).getId());
        }

        if (overwrite)
            services.exportCaseInputsWithOverwrite(session.user(), caseInputIds, purpose);

        else
            services.exportCaseInputs(session.user(), caseInputIds, purpose);
    }

    public void exportCaseInputs(List<CaseInput> inputList, String purpose) throws EmfException {
        doExport(inputList, false, purpose);
    }

    public void exportCaseInputsWithOverwrite(List<CaseInput> inputList, String purpose) throws EmfException {
        doExport(inputList, true, purpose);
    }
    
    public Case getCaseObj() {
        return this.caseObj;
    }
    
    public int getPageSize() {
        return this.defaultPageSize;
    }

    public CaseInput[] getCaseInput(int caseId, Sector sector, boolean showAll) throws EmfException {
        return service().getCaseInputs(defaultPageSize, caseId, sector, showAll);
    }
    
    public Sector[] getAllSetcors() {
        List<Sector> all = new ArrayList<Sector>();
        all.add(new Sector("All", "All"));
        all.addAll(Arrays.asList(this.caseObj.getSectors()));

        return all.toArray(new Sector[0]);
    }
    
    public Case[] getCasesByInputDataset(int datasetId) throws EmfException{
        return service().getCasesByInputDataset(datasetId);
    }
    
    public Case[] getCasesByOutputDatasets(int[] datasetIds) throws EmfException{
        return service().getCasesByOutputDatasets(datasetIds);
    }
    
    public void doViewRelated(RelatedCaseView view, Case[] casesByInputDataset, Case[] casesByOutputDataset) {
        RelatedCasePresenter presenter = new RelatedCasePresenter(view, session);
        presenter.doDisplay(casesByInputDataset, casesByOutputDataset);
    }

    
}
