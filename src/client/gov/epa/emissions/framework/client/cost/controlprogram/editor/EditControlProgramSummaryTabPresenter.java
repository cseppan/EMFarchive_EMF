package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlprogram.ControlProgramManagerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class EditControlProgramSummaryTabPresenter  implements EditControlProgramTabPresenter {
    private EditControlProgramSummaryTab view;
    
    private EmfSession session;
    
    private ControlProgram controlProgram;

//    private ControlProgramManagerPresenter controlProgramManagerPresenter;

    public EditControlProgramSummaryTabPresenter(EditControlProgramSummaryTab view, 
            ControlProgram controlProgram, EmfSession session, 
            ControlProgramManagerPresenter controlProgramManagerPresenter) {
        this.controlProgram = controlProgram;
        this.session = session;
        this.view = view;
//        this.controlProgramManagerPresenter = controlProgramManagerPresenter;
    }
    
    public void doDisplay() throws EmfException  {
        view.observe(this);
        view.display(this.controlProgram);
    }

    public void doSave(ControlProgram controlProgram) {
        view.save(controlProgram);
    }

    public void doChangeControlProgramType(ControlProgramType controlProgramType) {
        // NOTE Auto-generated method stub
    }

    public DatasetType[] getDatasetTypes() throws EmfException {
        return session.dataCommonsService().getDatasetTypes();
     }

     public EmfDataset[] getDatasets(DatasetType type) throws EmfException
 {
         if (type == null)
             return new EmfDataset[0];

         return session.dataService().getDatasets(type);
     }

     public Version[] getVersions(EmfDataset dataset) throws EmfException 
     {
         if (dataset == null) {
             return new Version[0];
         }
         return session.dataEditorService().getVersions(dataset.getId());
     }
}
