package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseProgram;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.InputEnvtVar;
import gov.epa.emissions.framework.services.casemanagement.InputName;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ExImService;

import java.util.Date;

import javax.swing.JComponent;

public class EditInputsTabPresenterImpl implements EditInputsTabPresenter {

    private Case caseObj;

    private EditInputsTabView view;

    private EmfSession session;

    public EditInputsTabPresenterImpl(EmfSession session, EditInputsTabView view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
    }

    public void display() {
        view.display(session, caseObj, this);
    }

    public void doSave() {
        String caseInputDir = view.getCaseInputFileDir();
        caseObj.setInputFileDir(caseInputDir);
        view.refresh();
    }

    public void doAddInput(NewInputView dialog) throws EmfException {
        dialog.register(this);
        dialog.display(caseObj.getId());
        
        if (dialog.shouldCreate()) {
            CaseInput newInput = dialog.input();
            newInput.setCaseID(caseObj.getId());
            CaseInput loaded = service().addCaseInput(newInput);
            view.addInput(loaded);
        }
        
        refreshView();
    }

    private CaseService service() {
        return session.caseService();
    }
    
    private void refreshView() {
        view.refresh();
//        view.notifychanges();
    }
    
    public void removeInputs(CaseInput[] inputs) throws EmfException {
        service().removeCaseInputs(inputs);
    }

    public void doEditInput(CaseInput input, EditCaseInputView inputEditor) throws EmfException {
        EditInputPresenter editInputPresenter = new EditCaseInputPresenterImpl(inputEditor, view, session);
        editInputPresenter.display(input);
    }

    public void doAddInputFields(JComponent container, InputFieldsPanelView inputFields) throws EmfException {
        CaseInput newInput = new CaseInput();
        newInput.setId(view.numberOfRecord());
        newInput.setRequired(true);
        newInput.setShow(true);

        InputFieldsPanelPresenter inputFieldsPresenter = new InputFieldsPanelPresenter(inputFields, session);
        inputFieldsPresenter.display(newInput, container);
    }

    public void doCheckDuplicate(CaseInput input, CaseInput[] existingInputs) throws EmfException {
        for (int i = 0; i < existingInputs.length; i++) {
            if (input.getId() != existingInputs[i].getId())
                if (input.equals(existingInputs[i]))
                    throw new EmfException("The combination of 'Input Name', 'Sector', and 'Program' "
                            + "should be unique.");
        }

        InputName[] names = caseService().getInputNames();
        InputName inputName = input.getInputName();
        for (int i = 0; i < names.length; i++) {
            if (inputName == null)
                throw new EmfException("InputName cannot be null.");

            if (inputName.getId() != names[i].getId())
                if (inputName.equals(names[i]))
                    throw new EmfException("InputName: " + inputName.getName() + " has already existed.");
        }

        CaseProgram[] prgs = caseService().getPrograms();
        CaseProgram prg = input.getProgram();
        for (int i = 0; i < prgs.length; i++)
            if (prg != null && prg.getId() != prgs[i].getId())
                if (prg.equals(prgs[i]))
                    throw new EmfException("Program: " + prg.getName() + " has already existed.");

        InputEnvtVar[] envtVars = caseService().getInputEnvtVars();
        InputEnvtVar envtVar = input.getEnvtVars();
        for (int i = 0; i < envtVars.length; i++)
            if (envtVar != null && envtVar.getId() != envtVars[i].getId())
                if (envtVar.equals(envtVars[i]))
                    throw new EmfException("InputEnvtVar: " + envtVar.getName() + " has already existed.");

    }

    private CaseService caseService() {
        return this.session.caseService();
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) {
        view.clearMessage();

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }

    public void doExportWithOverwrite(EmfDataset[] datasets, Version[] versions, String[] folders, String purpose)
            throws EmfException {
        doExport(datasets, versions, folders, true, purpose);
    }

    public void doExport(EmfDataset[] datasets, Version[] versions, String[] folders, String purpose)
            throws EmfException {
        doExport(datasets, versions, folders, false, purpose);
    }

    private void doExport(EmfDataset[] datasets, Version[] versions, String[] folders, boolean overwrite, String purpose)
            throws EmfException {
        ExImService services = session.eximService();

        for (int i = 0; i < datasets.length; i++) {
            datasets[i].setAccessedDateTime(new Date());

            if (overwrite)
                services.exportDatasetsWithOverwrite(session.user(), new EmfDataset[]{datasets[i]}, new Version[]{versions[i]}, mapToRemote(folders[i]), purpose);
            else
                services.exportDatasets(session.user(), new EmfDataset[]{datasets[i]}, new Version[]{versions[i]}, mapToRemote(folders[i]), purpose);
        }
    }

    private String mapToRemote(String dir) {
        return session.preferences().mapLocalOutputPathToRemote(dir);
    }

    public CaseInput[] getCaseInput(int caseId) throws EmfException {
        return service().getCaseInputs(caseId);
    }

}
