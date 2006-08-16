package gov.epa.emissions.framework.client.casemanagement.inputs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.InputEnvtVar;
import gov.epa.emissions.framework.services.casemanagement.InputName;
import gov.epa.emissions.framework.services.casemanagement.Program;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataEditorService;

public class InputFieldsPanelPresenter {
    private EmfSession session;
    
    private InputFieldsPanelView view;
    
    public InputFieldsPanelPresenter(InputFieldsPanelView inputFields, EmfSession session) {
        this.session = session;
        this.view = inputFields;
    }
    
    public void display(CaseInput input, JComponent container) throws EmfException {
        view.observe(this);
        view.display(input, container);
        view.populateFields();
    }
    
    public InputName[] getInputNames() throws EmfException {
        return caseService().getInputNames();
    }

    public Sector[] getSectors() throws EmfException {
        List list = new ArrayList();
        list.add(new Sector("All sectors", "All sectors"));
        list.addAll(Arrays.asList(dataCommonsService().getSectors()));
        
        return (Sector[])list.toArray(new Sector[0]);
    }
    
    public Program[] getPrograms() throws EmfException {
        return caseService().getPrograms();
    }

    public InputEnvtVar[] getEnvtVars() throws EmfException {
        return caseService().getInputEnvtVars();
    }

    public DatasetType[] getDSTypes() throws EmfException {
        return dataCommonsService().getDatasetTypes();
    }
    
    public EmfDataset[] getDatasets(DatasetType type) throws EmfException {
        if (type == null) 
            return new EmfDataset[0];
        
        EmfDataset[] datasets = dataService().getDatasets();
        List list = new ArrayList();
        int typeId = type.getId();
        
        for (int i = 0; i < datasets.length; i++) {
            if (datasets[i].getDatasetType().getId() == typeId)
                list.add(datasets[i]);
        }
        
        return (EmfDataset[]) list.toArray(new EmfDataset[0]);
    }
    
    public Version[] getVersions(EmfDataset dataset) throws EmfException {
        if (dataset == null) {
            return new Version[0];
        }
        
        return dataEditorServive().getVersions(dataset.getId());
    }
    
    private CaseService caseService() {
        return session.caseService();
    }

    private DataCommonsService dataCommonsService() {
        return session.dataCommonsService();
    }

    private DataService dataService() {
        return session.dataService();
    }
    
    private DataEditorService dataEditorServive() {
        return session.dataEditorService();
    }
    
    public void doSave() throws EmfException {
        view.setFields();
    }
    
}
