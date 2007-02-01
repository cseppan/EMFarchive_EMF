package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ExportService;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class CaseServiceImpl implements CaseService {
    private static Log LOG = LogFactory.getLog(CaseServiceImpl.class);

    private CaseDAO dao;

    private PooledExecutor threadPool;

    private HibernateSessionFactory sessionFactory;

    private EmfDbServer dbServer;

    private ExportService exportService;

    public CaseServiceImpl() {
        this(HibernateSessionFactory.get());
    }

    public CaseServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.dao = new CaseDAO();
        this.threadPool = createThreadPool();
    }

    protected void finalize() throws Throwable {
        threadPool.shutdownAfterProcessingCurrentlyQueuedTasks();
        threadPool.awaitTerminationAfterShutdown();
        super.finalize();
    }

    private PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3);// terminate after 3 (unused) minutes

        return threadPool;
    }

    private void createExportService() throws EmfException {
        try {
            this.dbServer = new EmfDbServer();
            this.exportService = new ExportService(dbServer, threadPool, sessionFactory);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public Case[] getCases() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List cases = dao.getCases(session);
            session.close();

            return (Case[]) cases.toArray(new Case[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Cases", e);
            throw new EmfException("Could not get all Cases");
        }
    }
    
    private Case getCase(int caseId) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Case caseObj = dao.getCase(caseId, session);
            session.close();
            
            return caseObj;
        } catch (RuntimeException e) {
            LOG.error("Could not get all Cases", e);
            throw new EmfException("Could not get all Cases");
        }
    }

    public Abbreviation[] getAbbreviations() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List abbreviations = dao.getAbbreviations(session);
            session.close();

            return (Abbreviation[]) abbreviations.toArray(new Abbreviation[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Abbreviations", e);
            throw new EmfException("Could not get all Abbreviations");
        }
    }

    public AirQualityModel[] getAirQualityModels() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List airQualityModels = dao.getAirQualityModels(session);
            session.close();

            return (AirQualityModel[]) airQualityModels.toArray(new AirQualityModel[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Air Quality Models", e);
            throw new EmfException("Could not get all Air Quality Models");
        }
    }

    public CaseCategory[] getCaseCategories() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getCaseCategories(session);
            session.close();

            return (CaseCategory[]) results.toArray(new CaseCategory[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Case Categories", e);
            throw new EmfException("Could not get all Case Categories");
        }
    }

    public EmissionsYear[] getEmissionsYears() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getEmissionsYears(session);
            session.close();

            return (EmissionsYear[]) results.toArray(new EmissionsYear[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Emissions Years", e);
            throw new EmfException("Could not get all Emissions Years");
        }
    }

    public Grid[] getGrids() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getGrids(session);
            session.close();

            return (Grid[]) results.toArray(new Grid[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Grids", e);
            throw new EmfException("Could not get all Grids");
        }
    }

    public GridResolution[] getGridResolutions() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getGridResolutions(session);
            session.close();

            return (GridResolution[]) results.toArray(new GridResolution[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Grid Resolutions", e);
            throw new EmfException("Could not get all Grid Resolutions");
        }
    }

    public MeteorlogicalYear[] getMeteorlogicalYears() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getMeteorlogicalYears(session);
            session.close();

            return (MeteorlogicalYear[]) results.toArray(new MeteorlogicalYear[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Meteorological Years", e);
            throw new EmfException("Could not get all Meteorological Years");
        }
    }

    public Speciation[] getSpeciations() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getSpeciations(session);
            session.close();

            return (Speciation[]) results.toArray(new Speciation[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Speciations", e);
            throw new EmfException("Could not get all Speciations");
        }
    }

    public void addCase(Case element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.add(element, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add Case: " + element, e);
            throw new EmfException("Could not add Case: " + element);
        }
    }

    public void removeCase(Case element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List inputs = dao.getCaseInputs(element.getId(), session);
            dao.removeCaseInputs((CaseInput[])inputs.toArray(new CaseInput[0]), session);
            dao.remove(element, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not remove Case: " + element, e);
            throw new EmfException("Could not remove Case: " + element);
        }
    }

    public Case obtainLocked(User owner, Case element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Case locked = dao.obtainLocked(owner, element, session);
            session.close();

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for Case: " + element + " by owner: " + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for Case: " + element + " by owner: " + owner.getUsername());
        }
    }

    public Case releaseLocked(Case locked) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Case released = dao.releaseLocked(locked, session);
            session.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not release lock by "+locked.getLockOwner(), e);
            throw new EmfException("Could not release lock by " + locked.getLockOwner+" for Case: " + locked);
        }
    }

    public Case updateCase(Case element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            Case released = dao.update(element, session);
            session.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update Case", e);
            throw new EmfException("Could not update Case: " + element);
        }
    }

    public InputName[] getInputNames() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getInputNames(session);
            session.close();

            return (InputName[]) results.toArray(new InputName[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Input Names", e);
            throw new EmfException("Could not get all Input Names");
        }
    }

    public InputEnvtVar[] getInputEnvtVars() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getInputEnvtVars(session);
            session.close();

            return (InputEnvtVar[]) results.toArray(new InputEnvtVar[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Input Environment Variables", e);
            throw new EmfException("Could not get all Input Environment Variables");
        }
    }

    public CaseProgram[] getPrograms() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getPrograms(session);
            session.close();

            return (CaseProgram[]) results.toArray(new CaseProgram[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Programs", e);
            throw new EmfException("Could not get all Programs");
        }
    }
    
    public ModelToRun[] getModelToRuns() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getModelToRuns(session);
            session.close();

            return (ModelToRun[]) results.toArray(new ModelToRun[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Models To Run", e);
            throw new EmfException("Could not get all Models To Run");
        }
    }

    public void export(User user, String dirName, String purpose, boolean overWrite, Case caseToExport)
            throws EmfException {
        createExportService();
        EmfDataset[] datasets = getInputDatasets(caseToExport);
        Version[] versions = getInputDatasetVersions(caseToExport);
        SubDir[] subdirs = getSubdirs(caseToExport);
        
        if (datasets.length == 0)
            return;

        for (int i = 0; i < datasets.length; i++) {
            String subdir = (subdirs[i] == null) ? "":subdirs[i].getName();
            String exportDir = dirName + System.getProperty("file.separator") + subdir;
            File dir = new File(exportDir);
            if (!dir.exists())
                dir.mkdirs();

            exportService.export(user, new EmfDataset[] { datasets[i] }, new Version[] { versions[i] }, exportDir,
                    purpose, overWrite);
        }
    }

    private Version[] getInputDatasetVersions(Case caseToExport) throws EmfException {
        CaseInput[] inputs = getCaseInputs(caseToExport.getId());
        List<Version> list = new ArrayList<Version>();

        for (int i = 0; i < inputs.length; i++)
            if (inputs[i].getDataset() != null && !checkExternalDSType(inputs[i].getDatasetType()))
                list.add(inputs[i].getVersion());
        
        return list.toArray(new Version[0]);
    }

    private EmfDataset[] getInputDatasets(Case caseToExport) throws EmfException {
        CaseInput[] inputs = getCaseInputs(caseToExport.getId());
        List<EmfDataset> list = new ArrayList<EmfDataset>();

        for (int i = 0; i < inputs.length; i++)
            if (inputs[i].getDataset() != null && !checkExternalDSType(inputs[i].getDatasetType()))
                list.add(inputs[i].getDataset());

        return list.toArray(new EmfDataset[0]);
    }
    
    private boolean checkExternalDSType(DatasetType type) {
        String name = type.getName();
        
        return name.indexOf("External") >= 0;
    }

    private SubDir[] getSubdirs(Case caseToExport) throws EmfException {
        CaseInput[] inputs = getCaseInputs(caseToExport.getId());
        SubDir[] subdirs = new SubDir[inputs.length];

        for (int i = 0; i < inputs.length; i++)
            subdirs[i] = inputs[i].getSubdirObj();

        return subdirs;
    }

    public InputName addCaseInputName(InputName name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(name, session);
            return (InputName) dao.load(InputName.class, name.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new case input name '" + name.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new case input name '" + name.getName() + "'");
        } finally {
            session.close();
        }
    }

    public CaseProgram addProgram(CaseProgram program) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(program, session);
            return (CaseProgram) dao.load(CaseProgram.class, program.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new program '" + program.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new program '" + program.getName() + "'");
        } finally {
            session.close();
        }
    }

    public InputEnvtVar addInputEnvtVar(InputEnvtVar inputEnvtVar) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(inputEnvtVar, session);
            return (InputEnvtVar) dao.load(InputEnvtVar.class, inputEnvtVar.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new input environment variable '" + inputEnvtVar.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new input environment variable '" + inputEnvtVar.getName() + "'");
        } finally {
            session.close();
        }
    }

    public ModelToRun addModelToRun(ModelToRun model) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(model, session);
            return (ModelToRun) dao.load(ModelToRun.class, model.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new model to run '" + model.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new model to run '" + model.getName() + "'");
        } finally {
            session.close();
        }
    }

    public GridResolution addGridResolution(GridResolution gridResolution) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(gridResolution, session);
            return (GridResolution) dao.load(GridResolution.class, gridResolution.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new Grid Resolution '" + gridResolution.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new Grid Resolution '" + gridResolution.getName() + "'");
        } finally {
            session.close();
        }
    }

    public SubDir[] getSubDirs() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getSubDirs(session);
            session.close();

            return (SubDir[]) results.toArray(new SubDir[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all subdirectories", e);
            throw new EmfException("Could not get all subdirectories");
        }
    }

    public SubDir addSubDir(SubDir subdir) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(subdir, session);
            return (SubDir) dao.load(SubDir.class, subdir.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new subdirectory '" + subdir.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new subdirectory '" + subdir.getName() + "'");
        } finally {
            session.close();
        }
    }

    public CaseInput addCaseInput(CaseInput input) throws EmfException {
        Session session = sessionFactory.getSession();
        
        if (dao.caseInputExists(input, session))
            throw new EmfException("The combination of 'Input Name', 'Sector', and 'Program' "
                    + "should be unique.");
        
        try {
            dao.add(input, session);
            return (CaseInput) dao.loadCaseInupt(input, session);
        } catch (Exception e) {
            LOG.error("Could not add new case input '" + input.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new case input '" + input.getName() + "'");
        } finally {
            session.close();
        }
    }

    public void updateCaseInput(CaseInput input) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            CaseInput loaded = (CaseInput) dao.loadCaseInupt(input, session);
            
            if (loaded != null && loaded.getId() != input.getId())
                throw new EmfException("Case input uniqueness check failed ("+
                        loaded.getId()+","+input.getId()+")");
            
            session.clear();
            dao.updateCaseInput(input, session);
        } catch (RuntimeException e) {
            LOG.error("Could not update case input: " + input.getName() + ".\n" + e);
            throw new EmfException("Could not update case input: " + input.getName() + ".");
        } finally {
            session.close();
        }
    }

    public void removeCaseInputs(CaseInput[] inputs) throws EmfException {
       Session session = sessionFactory.getSession();
        
        try {
            dao.removeCaseInputs(inputs, session);
        } catch (Exception e) {
            LOG.error("Could not remove case input " + inputs[0].getName() + " etc.\n" + e.getMessage());
            throw new EmfException("Could not remove case input " + inputs[0].getName() + " etc.");
        } finally {
            session.close();
        }
    }

    public CaseInput[] getCaseInputs(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();
        
        try {
            List<CaseInput> inputs = dao.getCaseInputs(caseId, session);

            return inputs.toArray(new CaseInput[0]);
        } catch (Exception e) {
            LOG.error("Could not get all inputs for case (id=" + caseId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all inputs for case (id=" + caseId + ").\n");
        } finally {
            session.close();
        }
    }

    public Case[] copyCaseObject(int[] toCopy) throws EmfException {
        List<Case> copiedList = new ArrayList<Case>();
        
        for (int i = 0; i < toCopy.length; i++) {
            Case caseToCopy = getCase(toCopy[i]);
            try {
                copiedList.add(copySingleCaseObj(caseToCopy));
            } catch (Exception e) {
                LOG.error("Could not copy case " + caseToCopy.getName() + ".", e);
                throw new EmfException("Could not copy case " + caseToCopy.getName() + ". " + e.getMessage());
            }
        }
        
        return copiedList.toArray(new Case[0]);
    }

    private Case copySingleCaseObj(Case toCopy) throws Exception {
        Case copied = (Case)DeepCopy.copy(toCopy);
        copied.setName("Copy of " + toCopy.getName() + " " + new Date().getTime());
        Case loaded = addCopiedCase(copied);
        copyCaseInputs(toCopy.getId(), loaded.getId());
        
        return loaded;
    }
    
    private void copyCaseInputs(int origCaseId, int copiedCaseId) throws Exception {
        CaseInput[] tocopy = getCaseInputs(origCaseId);
        
        for(int i = 0; i < tocopy.length; i++)
            copySingleInput(tocopy[i], copiedCaseId);
    }

    private CaseInput copySingleInput(CaseInput input, int copiedCaseId) throws Exception {
        CaseInput copied = (CaseInput)DeepCopy.copy(input);
        copied.setCaseID(copiedCaseId);
        
        return addCaseInput(copied);
    }

    private Case addCopiedCase(Case element) throws EmfException {
        Session session = sessionFactory.getSession();
        
        try {
            dao.add(element, session);
            return (Case)dao.load(Case.class, element.getName(), session);
        } catch (RuntimeException e) {
            LOG.error("Could not add case " + element, e);
            throw new EmfException("Could not add case " + element);
        }  finally {
            session.close();
        }
    }

}
