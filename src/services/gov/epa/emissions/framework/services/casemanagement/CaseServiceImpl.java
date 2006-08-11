package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ExportService;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

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
            e.printStackTrace();
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

    public CaseInput[] getCaseInputs() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getCaseInputs(session);
            session.close();

            return (CaseInput[]) results.toArray(new CaseInput[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all CaseInputs", e);
            throw new EmfException("Could not get all CaseInputs");
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

    public Program[] getPrograms() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getPrograms(session);
            session.close();

            return (Program[]) results.toArray(new Program[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Programs", e);
            throw new EmfException("Could not get all Programs");
        }
    }

    public void export(User user, String dirName, String purpose, boolean overWrite, Case caseToExport) throws EmfException {
        createExportService();
        exportService.export(user, getInputDatasets(caseToExport), getInputDatasetVersions(caseToExport), dirName, purpose, overWrite);
    }

    private Version[] getInputDatasetVersions(Case caseToExport) {
        CaseInput[] inputs = caseToExport.getCaseInputs();
        Version[] versions = new Version[inputs.length];
        
        for (int i = 0; i < inputs.length; i++)
            versions[i] = inputs[i].getVersion();
        
        return versions;
    }

    private EmfDataset[] getInputDatasets(Case caseToExport) {
        CaseInput[] inputs = caseToExport.getCaseInputs();
        EmfDataset[] datasets = new EmfDataset[inputs.length];
        
        for (int i = 0; i < inputs.length; i++)
            datasets[i] = inputs[i].getDataset();
        
        return datasets;
    }

}
