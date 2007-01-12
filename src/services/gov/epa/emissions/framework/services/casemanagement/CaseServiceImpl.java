package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ExportService;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.ArrayList;
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
            LOG.error("Could not get all Meteorlogical Years", e);
            throw new EmfException("Could not get all Meteorlogical Years");
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
            LOG.error("Could not release lock for Case: " + locked + " by owner: " + locked.getLockOwner(), e);
            throw new EmfException("Could not release lock for Case: " + locked + " by owner: " + locked.getLockOwner());
        }
    }

    public Case updateCase(Case element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            Case released = dao.update(element, session);
            session.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update Case: " + element, e);
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
            LOG.error("Could not get all InputNames", e);
            throw new EmfException("Could not get all InputNames");
        }
    }

    public InputEnvtVar[] getInputEnvtVars() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getInputEnvtVars(session);
            session.close();

            return (InputEnvtVar[]) results.toArray(new InputEnvtVar[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all InputEnvtVars", e);
            throw new EmfException("Could not get all InputEnvtVars");
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
            LOG.error("Could not get all ModelToRuns", e);
            throw new EmfException("Could not get all ModelToRuns");
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
        List list = new ArrayList();

        for (int i = 0; i < inputs.length; i++)
            if (inputs[i].getDataset() != null && !checkExternalDSType(inputs[i].getDatasetType()))
                list.add(inputs[i].getVersion());
        
        return (Version[])list.toArray(new Version[0]);
    }

    private EmfDataset[] getInputDatasets(Case caseToExport) throws EmfException {
        CaseInput[] inputs = getCaseInputs(caseToExport.getId());
        List list = new ArrayList();

        for (int i = 0; i < inputs.length; i++)
            if (inputs[i].getDataset() != null && !checkExternalDSType(inputs[i].getDatasetType()))
                list.add(inputs[i].getDataset());

        return (EmfDataset[])list.toArray(new EmfDataset[0]);
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
            LOG.error("Could not add new InputEnvtVar '" + inputEnvtVar.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new InputEnvtVar '" + inputEnvtVar.getName() + "'");
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
            LOG.error("Could not add new ModelToRun '" + model.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new ModelToRun '" + model.getName() + "'");
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
            LOG.error("Could not add new GridResolution '" + gridResolution.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new GridResolution '" + gridResolution.getName() + "'");
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
            LOG.error("Could not get all SubDirz", e);
            throw new EmfException("Could not get all SubDirs");
        }
    }

    public SubDir addSubDir(SubDir subdir) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(subdir, session);
            return (SubDir) dao.load(SubDir.class, subdir.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new SubDir '" + subdir.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new SubDir '" + subdir.getName() + "'");
        } finally {
            session.close();
        }
    }

    public CaseInput addCaseInput(CaseInput input) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (dao.caseInputExists(input, session))
                throw new EmfException("CaseInput: " + input.getName() + " is already in use.");
            
            dao.add(input, session);
            return (CaseInput) dao.loadCaseInupt(input, session);
        } catch (Exception e) {
            LOG.error("Could not add new CaseInput '" + input.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new CaseInput '" + input.getName() + "'");
        } finally {
            session.close();
        }
    }

    public void updateCaseInput(CaseInput input) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.updateCaseInput(new CaseInput[]{ input }, session);
        } catch (RuntimeException e) {
            LOG.error("Could not update CaseInput: " + input.getName() + ".\n" + e);
            throw new EmfException("Could not update CaseInput: " + input.getName() + ".");
        } finally {
            session.close();
        }
    }

    public void removeCaseInputs(CaseInput[] inputs) throws EmfException {
       Session session = sessionFactory.getSession();
        
        try {
            dao.removeCaseInputs(inputs, session);
        } catch (Exception e) {
            LOG.error("Could not remove CaseInput " + inputs[0].getName() + " etc.\n" + e.getMessage());
            throw new EmfException("Could not remove CaseInput " + inputs[0].getName() + " etc.");
        } finally {
            session.close();
        }
    }

    public CaseInput[] getCaseInputs(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();
        
        try {
            List inputs = dao.getCaseInputs(caseId, session);

            return (CaseInput[]) inputs.toArray(new CaseInput[0]);
        } catch (Exception e) {
            LOG.error("Could not get all CaseInputs with current case (id=" + caseId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all CaseInputs with current case (id=" + caseId + ").\n");
        } finally {
            session.close();
        }
    }

}
