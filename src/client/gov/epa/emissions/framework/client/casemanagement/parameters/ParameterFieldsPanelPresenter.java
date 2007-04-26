package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.inputs.Programs;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseProgram;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.SubDir;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataEditorService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

public class ParameterFieldsPanelPresenter {

    private EmfSession session;

    private ParameterFieldsPanelView view;

    private CaseParameterNames caseParameterNames;
    
    private CaseParameterEnvtVars caseParameterEnvtVars;
    
    private Programs programs;

    private int caseId;
    
    public static final String ALL_FOR_SECTOR = "All jobs for sector";

    public ParameterFieldsPanelPresenter(int caseId, ParameterFieldsPanelView inputFields, EmfSession session) throws EmfException {
        this.session = session;
        this.view = inputFields;
        this.caseId = caseId;
        this.caseParameterNames = new CaseParameterNames(session, getParameterNames());
        this.caseParameterEnvtVars = new CaseParameterEnvtVars(session, getEnvtVars());
        this.programs = new Programs(session, getPrograms());
    }

    public void display(CaseParameter param, JComponent container) throws EmfException {
        view.observe(this);
        view.display(param, container);
    }

    public CaseParameterNames getCaseParameterNames() {
        return caseParameterNames;
    }

    public CaseParameterEnvtVars getCaseParameterEnvtVars() {
        return this.caseParameterEnvtVars;
    }

    public Programs getCasePrograms() {
        return this.programs;
    }
    
    public ParameterName[] getParameterNames() throws EmfException {
        return caseService().getParameterNames();
    }

    public Sector[] getSectors() throws EmfException {
        List list = new ArrayList();
        list.add(new Sector("All sectors", "All sectors"));
        list.addAll(Arrays.asList(dataCommonsService().getSectors()));

        return (Sector[]) list.toArray(new Sector[0]);
    }

    public CaseProgram[] getPrograms() throws EmfException {
        return caseService().getPrograms();
    }

    public SubDir[] getSubdirs() throws EmfException {
        return caseService().getSubDirs();
    }

    public ParameterEnvVar[] getEnvtVars() throws EmfException {
        return caseService().getParameterEnvVars();
    }

    public CaseJob[] getCaseJobs() throws EmfException {
        List<CaseJob> jobs = new ArrayList<CaseJob>();
        jobs.add(new CaseJob(ALL_FOR_SECTOR));
        jobs.addAll(Arrays.asList(caseService().getCaseJobs(caseId)));
        
        return jobs.toArray(new CaseJob[0]);
    }
    
    public DatasetType[] getDSTypes() throws EmfException {
        return dataCommonsService().getDatasetTypes();
    }

    public EmfDataset[] getDatasets(DatasetType type) throws EmfException {
        if (type == null)
            return new EmfDataset[0];

        return dataService().getDatasets(type);
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
        //view.setFields();
        session.caseService().updateCaseParameter(view.setFields());
    }

    public void doValidateFields() throws EmfException {
        view.setFields(); // FIXME: should do more check here
    }

    public ParameterName getParameterName(Object selected) throws EmfException {
        return caseParameterNames.get(selected);
    }

    public ParameterEnvVar getParameterEnvtVar(Object selected) throws EmfException {
        return caseParameterEnvtVars.get(selected);
    }

    public CaseProgram getCaseProgram(Object selected) throws EmfException {
        return programs.get(selected);
    }

    public CaseJob getJob(int caseJobID) throws EmfException {
        return session.caseService().getCaseJob(caseJobID);
    }

    public int getJobIndex(int caseJobID) throws EmfException {
        CaseJob[] jobs = session.caseService().getCaseJobs(caseId);
        
        for (int i = 0; i < jobs.length; i++)
            if (jobs[i].getId() == caseJobID)
                return i + 1; // because of the default "All jobs" job is not in db
        
        return 0;
    }

    public ValueType[] getValueTypes() throws EmfException {
        return session.caseService().getValueTypes();
    }

}
