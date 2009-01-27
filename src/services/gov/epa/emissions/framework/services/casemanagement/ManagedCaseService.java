package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.io.generic.GenericExporterToString;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.commons.util.CustomStringTools;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfProperty;
import gov.epa.emissions.framework.services.basic.RemoteCommand;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJobKey;
import gov.epa.emissions.framework.services.casemanagement.jobs.DependentJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.casemanagement.outputs.QueueCaseOutput;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ManagedExportService;
import gov.epa.emissions.framework.services.exim.ManagedImportService;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.CaseJobSubmitter;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.TaskManagerFactory;
import gov.epa.emissions.framework.tasks.TaskSubmitter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class ManagedCaseService {
    private static Log log = LogFactory.getLog(ManagedCaseService.class);

    private static int svcCount = 0;

    private String svcLabel = null;

    public synchronized String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }
        log.info(svcLabel);

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    private CaseDAO dao;

    private HibernateSessionFactory sessionFactory = null;

    private DbServerFactory dbFactory;

    private User user;

    private ManagedImportService importService;

    private ManagedExportService exportService;

    private String eolString = System.getProperty("line.separator");

    // these run fields are used to create the run script
    private String runShell = "#!/bin/csh"; // shell to run under

    private String runSet = "setenv"; // how to set a variable

    private String runEq = " "; // equate a variable (could be a space)

    private String runTerminator = ""; // line terminator

    private String runComment = "##"; // line comment

    private String runSuffix = ".csh"; // job run file suffix

    // all sectors and all jobs id in the case inputs tb
    private final Sector ALL_SECTORS = null;

    private final int ALL_JOB_ID = 0;

    // protected Session session = null;

    // private Session getSession() {
    // if (session == null) {
    // session = sessionFactory.getSession();
    // }
    // return session;
    // }

    public ManagedCaseService(DbServerFactory dbFactory, HibernateSessionFactory sessionFactory) {
        this.dbFactory = dbFactory;
        this.sessionFactory = sessionFactory;
        this.dao = new CaseDAO(sessionFactory);

        if (DebugLevels.DEBUG_9)
            System.out.println("In ManagedCaseService constructor: Is the session Factory null? "
                    + (sessionFactory == null));

        myTag();

        if (DebugLevels.DEBUG_1)
            System.out.println(">>>> " + myTag());

        log.info("ManagedCaseService");
        // log.info("exportTaskSubmitter: " + caseJobSubmitter);
        log.info("Session factory null? " + (sessionFactory == null));
        log.info("User null? : " + (user == null));
    }

    /**
     * Generate the unique job key
     * 
     */
    private synchronized String createJobKey(int jobId) {
        return jobId + "_" + new Date().getTime();
    }

    private synchronized ManagedExportService getExportService() {
        log.info("ManagedCaseService::getExportService");

        if (exportService == null) {
            try {
                exportService = new ManagedExportService(dbFactory, sessionFactory);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return exportService;
    }

    private synchronized ManagedImportService getImportService() throws EmfException {
        log.info("ManagedCaseService::getImportService");

        if (importService == null) {
            try {
                importService = new ManagedImportService(dbFactory, sessionFactory);
            } catch (Exception e) {
                e.printStackTrace();
                throw new EmfException(e.getMessage());
            }
        }

        return importService;
    }

    // ***************************************************************************

    public synchronized Case[] getCases() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List cases = dao.getCases(session);

            return (Case[]) cases.toArray(new Case[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Cases", e);
            throw new EmfException("Could not get all Cases");
        } finally {
            session.close();
        }
    }

    public synchronized Case getCase(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Case caseObj = dao.getCase(caseId, session);
            return caseObj;
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Cases", e);
            throw new EmfException("Could not get all Cases");
        } finally {
            session.close();
        }
    }

    public synchronized Case getCaseFromName(String name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Case caseObj = dao.getCaseFromName(name, session);
            return caseObj;
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get case", e);
            throw new EmfException("Could not get case");
        } finally {
            session.close();
        }
    }

    private Case getCase(int caseId, Session session) throws EmfException {
        try {
            Case caseObj = dao.getCase(caseId, session);
            return caseObj;
        } catch (RuntimeException e) {
            log.error("Could not get case from case id: " + caseId + ".", e);
            throw new EmfException("Could not get case from case id: " + caseId + ".");
        }
    }

    public synchronized Case[] getCases(CaseCategory category) {
        return dao.getCases(category);
    }

    public synchronized Version[] getLaterVersions(EmfDataset dataset, Version version) throws EmfException {
        Versions versions = new Versions();
        Session session = sessionFactory.getSession();

        try {
            int id = dataset.getId();
            Version[] vers = versions.getLaterVersions(id, version, session);

            if (DebugLevels.DEBUG_14 && vers.length > 0)
                System.out.println("There are " + vers.length + " later versions for dataset: " + dataset.getName());

            return vers;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get versions for dataset: " + dataset.getName() + ".\n" + e.getMessage());
            throw new EmfException("Could not get versions for dataset: " + dataset.getName() + ".\n");
        } finally {
            session.close();
        }
    }

    public synchronized CaseJob getCaseJob(int jobId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return dao.getCaseJob(jobId, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get job for job id " + jobId + ".\n" + e.getMessage());
            throw new EmfException("Could not get job for job id " + jobId + ".\n");
        } finally {
            session.close();
        }
    }

    private CaseJob getCaseJob(int jobId, Session session) throws EmfException {
        try {
            return dao.getCaseJob(jobId, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get job for job id " + jobId + ".\n" + e.getMessage());
            throw new EmfException("Could not get job for job id " + jobId + ".\n");
        }
    }

    public synchronized Sector[] getSectorsUsedbyJobs(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List<Sector> sectors = dao.getSectorsUsedbyJobs(caseId, session);
            return sectors.toArray(new Sector[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get sectors for case id " + caseId + ".\n" + e.getMessage());
            throw new EmfException("Could not get sectors for case id " + caseId + ".\n");
        }
    }

    public synchronized Abbreviation[] getAbbreviations() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List abbreviations = dao.getAbbreviations(session);

            return (Abbreviation[]) abbreviations.toArray(new Abbreviation[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Abbreviations", e);
            throw new EmfException("Could not get all Abbreviations");
        } finally {
            session.close();
        }
    }

    public synchronized Abbreviation addAbbreviation(Abbreviation abbr) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.add(abbr, session);
            return (Abbreviation) dao.load(Abbreviation.class, abbr.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Cannot add case abbreviation " + abbr.getName(), e);
            throw new EmfException("Cannot add case abbreviation " + abbr.getName() + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized AirQualityModel[] getAirQualityModels() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List airQualityModels = dao.getAirQualityModels(session);
            return (AirQualityModel[]) airQualityModels.toArray(new AirQualityModel[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Air Quality Models", e);
            throw new EmfException("Could not get all Air Quality Models");
        } finally {
            session.close();
        }
    }

    public synchronized CaseCategory[] getCaseCategories() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getCaseCategories(session);
            return (CaseCategory[]) results.toArray(new CaseCategory[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Case Categories", e);
            throw new EmfException("Could not get all Case Categories");
        } finally {
            session.close();
        }
    }

    public synchronized CaseCategory addCaseCategory(CaseCategory element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(element, session);
            return (CaseCategory) dao.load(CaseCategory.class, element.getName(), session);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not add CaseCategory: " + element, e);
            throw new EmfException("Could not add CaseCategory: " + element);
        } finally {
            session.close();
        }
    }

    public synchronized EmissionsYear[] getEmissionsYears() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getEmissionsYears(session);
            return (EmissionsYear[]) results.toArray(new EmissionsYear[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Emissions Years", e);
            throw new EmfException("Could not get all Emissions Years");
        } finally {
            session.close();
        }
    }

    public synchronized Grid[] getGrids() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getGrids(session);
            return (Grid[]) results.toArray(new Grid[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Grids", e);
            throw new EmfException("Could not get all Grids");
        } finally {
            session.close();
        }
    }

    public synchronized GridResolution[] getGridResolutions() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getGridResolutions(session);
            return (GridResolution[]) results.toArray(new GridResolution[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Grid Resolutions", e);
            throw new EmfException("Could not get all Grid Resolutions");
        } finally {
            session.close();
        }
    }

    public synchronized MeteorlogicalYear[] getMeteorlogicalYears() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getMeteorlogicalYears(session);
            return (MeteorlogicalYear[]) results.toArray(new MeteorlogicalYear[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Meteorological Years", e);
            throw new EmfException("Could not get all Meteorological Years");
        } finally {
            session.close();
        }
    }

    public synchronized Speciation[] getSpeciations() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getSpeciations(session);
            return (Speciation[]) results.toArray(new Speciation[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Speciations", e);
            throw new EmfException("Could not get all Speciations");
        } finally {
            session.close();
        }
    }

    public synchronized Case addCase(User user, Case element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(element, session);
            Case loaded = (Case) dao.load(Case.class, element.getName(), session);
            Case locked = dao.obtainLocked(user, loaded, session);
            locked.setAbbreviation(new Abbreviation(loaded.getId() + ""));
            return dao.update(locked, session);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not add Case: " + element, e);
            throw new EmfException("Could not add Case: " + element);
        } finally {
            session.close();
        }
    }

    public synchronized String checkParentCase(Case caseObj) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.checkParentChildRelationship(caseObj, session);

            List<?> list1 = session.createQuery(
                    "SELECT obj.caseId FROM CaseJob as obj WHERE obj.parentCaseId = " + caseObj.getId()).list();
            List<?> list2 = session.createQuery(
                    "SELECT obj.caseID FROM CaseInput as obj WHERE obj.parentCaseId = " + caseObj.getId()).list();
            List<?> list3 = session.createQuery(
                    "SELECT obj.caseID FROM CaseParameter as obj WHERE obj.parentCaseId = " + caseObj.getId()).list();

            if (list1 != null && list1.size() > 0) {
                int childId = Integer.parseInt(list1.get(0).toString());

                if (childId != caseObj.getId()) {
                    Case childCase = dao.getCase(childId, session);
                    return "\"" + caseObj.getName() + "\" is a parent case for at least one job from case \""
                            + childCase.getName() + "\".";
                }
            }

            if (list2 != null && list2.size() > 0) {
                int childId = Integer.parseInt(list2.get(0).toString());

                if (childId != caseObj.getId()) {
                    Case childCase = dao.getCase(childId, session);
                    return "\"" + caseObj.getName() + "\" is a parent case for at least one input from case \""
                            + childCase.getName() + "\".";
                }
            }

            if (list3 != null && list3.size() > 0) {
                int childId = Integer.parseInt(list3.get(0).toString());

                if (childId != caseObj.getId()) {
                    Case childCase = dao.getCase(childId, session);
                    return "\"" + caseObj.getName() + "\" is a parent case for at least one parameter from case \""
                            + childCase.getName() + "\".";
                }
            }

            return "";
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Checking case " + caseObj.getName() + ": " + e.getMessage());
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized void removeCase(Case caseObj) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            if (DebugLevels.DEBUG_18)
                log.warn("Started removing case: " + new Date());

            setStatus(caseObj.getLastModifiedBy(), "Started removing case " + caseObj.getName() + ".", "Remove Case");

            List<CaseJob> jobs = dao.getCaseJobs(caseObj.getId(), session);
            checkJobsStatuses(jobs, caseObj);

            if (DebugLevels.DEBUG_18)
                log.warn("Removing case: finished checking job statuses: " + new Date());

            List<CaseInput> inputs = dao.getCaseInputs(caseObj.getId(), session);
            dao.removeCaseInputs(inputs.toArray(new CaseInput[0]), session);

            if (DebugLevels.DEBUG_18)
                log.warn("Removing case: finished removing case inputs: " + new Date());

            PersistedWaitTask[] persistedJobs = getPersistedJobs(jobs, session);
            QueueCaseOutput[] outputQs = getQedOutputs(jobs, session);

            if (outputQs.length > 0)
                throw new EmfException("Selected case: " + caseObj.getName() + " has " + outputQs.length
                        + " pending outputs to register.");

            CaseJobKey[] keys = getJobsKeys(jobs, session);
            JobMessage[] msgs = getJobsMessages(jobs, session);
            CaseOutput[] outputs = getJobsOutputs(jobs, session);

            try {
                dao.removeObjects(persistedJobs, session);
            } catch (RuntimeException e) {
                throw new EmfException("Cannot remove persisted jobs from db table.");
            }

            if (DebugLevels.DEBUG_18)
                log.warn("Removing case: finished removing persisted jobs: " + new Date());

            try {
                dao.removeObjects(keys, session);
            } catch (RuntimeException e) {
                throw new EmfException("Cannot remove job keys from db table.");
            }

            if (DebugLevels.DEBUG_18)
                log.warn("Removing case: finished removing job keys from job-key table: " + new Date());

            try {
                dao.removeObjects(msgs, session);
            } catch (RuntimeException e) {
                throw new EmfException("Cannot remove job messages from db table.");
            }

            if (DebugLevels.DEBUG_18)
                log.warn("Removing case: finished removing job messages: " + new Date());

            try {
                dao.removeObjects(outputs, session);
            } catch (RuntimeException e) {
                throw new EmfException("Cannot remove case outputs from db table.");
            }

            if (DebugLevels.DEBUG_18)
                log.warn("Removing case: finished removing outputs: " + new Date());

            try {
                CaseJob[] toRemove = jobs.toArray(new CaseJob[0]);

                // NOTE: cannot remove jobs without first reset the job dependencies
                for (CaseJob job : toRemove) {
                    job.setDependentJobs(null);
                    dao.updateCaseJob(job);
                }

                dao.removeCaseJobs(toRemove, session);
            } catch (RuntimeException e) {
                throw new EmfException("Cannot remove jobs from db table.");
            }

            if (DebugLevels.DEBUG_18)
                log.warn("Removing case: finished removing jobs: " + new Date());

            List<CaseParameter> parameters = dao.getCaseParameters(caseObj.getId(), session);
            try {
                dao.removeCaseParameters(parameters.toArray(new CaseParameter[0]), session);
            } catch (RuntimeException e) {
                throw new EmfException("Cannot remove case parameters from db table.");
            }

            if (DebugLevels.DEBUG_18)
                log.warn("Removing case: finished removing case parameters: " + new Date());

            try {
                dao.removeChildCase(caseObj.getId(), session);
            } catch (Exception e) {
                throw new EmfException("Cannot remove case: " + e.getMessage());
            }

            if (DebugLevels.DEBUG_18)
                log.warn("Removing case: finished removing items from parent-sensitivity cases table: " + new Date());

            try {
                dao.remove(caseObj, session);
            } catch (RuntimeException e) {
                throw new EmfException("Cannot remove case objects: " + caseObj.getName() + " from db table.");
            }

            if (DebugLevels.DEBUG_18)
                log.warn("Removing case: finished removing case objects: " + new Date());

            try {
                dao.removeObject(caseObj.getAbbreviation(), session);
            } catch (RuntimeException e) {
                throw new EmfException("Cannot remove case abbreviation: " + caseObj.getAbbreviation().getName()
                        + " from db table.");
            }

            setStatus(caseObj.getLastModifiedBy(), "Finished removing case " + caseObj.getName() + ".", "Remove Case");

            if (DebugLevels.DEBUG_18)
                log.warn("Removing case: finished removing case abbreviation: " + new Date());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not remove Case: " + caseObj, e);
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    private PersistedWaitTask[] getPersistedJobs(List<CaseJob> jobs, Session session2) {
        List<PersistedWaitTask> persistedJobs = new ArrayList<PersistedWaitTask>();

        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            persistedJobs.addAll(dao.getPersistedWaitTasks(job.getCaseId(), job.getId(), session2));
        }

        return persistedJobs.toArray(new PersistedWaitTask[0]);
    }

    private QueueCaseOutput[] getQedOutputs(List<CaseJob> jobs, Session session) {
        List<QueueCaseOutput> outputs = new ArrayList<QueueCaseOutput>();

        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            outputs.addAll(dao.getQueueCaseOutputs(job.getCaseId(), job.getId(), session));
        }

        return outputs.toArray(new QueueCaseOutput[0]);
    }

    private CaseJobKey[] getJobsKeys(List<CaseJob> jobs, Session session2) {
        List<CaseJobKey> keys = new ArrayList<CaseJobKey>();

        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            keys.addAll(dao.getCaseJobKey(job.getId(), session2));
        }

        return keys.toArray(new CaseJobKey[0]);
    }

    private void checkJobsStatuses(List<CaseJob> jobs, Case caseObj) throws EmfException {
        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            JobRunStatus status = job.getRunstatus();
            boolean active = false;

            if (status != null && status.getName().toUpperCase().equals("RUNNING"))
                active = true;

            if (status != null && status.getName().toUpperCase().equals("SUBMITTED"))
                active = true;

            if (status != null && status.getName().toUpperCase().equals("EXPORTING"))
                active = true;

            if (status != null && status.getName().toUpperCase().equals("WAITING"))
                active = true;

            if (active)
                throw new EmfException("Job: " + job.getName() + " in case: " + caseObj.getName()
                        + " has an active status.");
        }

    }

    private CaseOutput[] getJobsOutputs(List<CaseJob> jobs, Session session) {
        List<CaseOutput> allOutputs = new ArrayList<CaseOutput>();

        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            allOutputs.addAll(dao.getCaseOutputs(job.getCaseId(), job.getId(), session));
        }

        return allOutputs.toArray(new CaseOutput[0]);
    }

    private JobMessage[] getJobsMessages(List<CaseJob> jobs, Session session) {
        List<JobMessage> allMsgs = new ArrayList<JobMessage>();

        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            allMsgs.addAll(dao.getJobMessages(job.getCaseId(), job.getId(), session));
        }

        return allMsgs.toArray(new JobMessage[0]);
    }

    private synchronized void setStatus(User user, String message, String type) {
        Status status = new Status();
        status.setUsername(user.getUsername());
        status.setType(type);
        status.setMessage(message);
        status.setTimestamp(new Date());
        StatusDAO statusDao = new StatusDAO(sessionFactory);
        statusDao.add(status);
    }

    public synchronized Case obtainLocked(User owner, Case element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Case locked = dao.obtainLocked(owner, element, session);
            return locked;
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not obtain lock for Case: " + element + " by owner: " + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for Case: " + element + " by owner: " + owner.getUsername());
        } finally {
            session.close();
        }
    }

    public synchronized Case releaseLocked(User owner, Case locked) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Case released = dao.releaseLocked(owner, locked, session);
            return released;
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not release lock by " + locked.getLockOwner(), e);
            throw new EmfException("Could not release lock by " + locked.getLockOwner() + " for Case: " + locked);
        } finally {
            session.close();
        }
    }

    public synchronized Case updateCase(Case element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {

            Case released = dao.update(element, session);
            return released;
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not update Case", e);
            throw new EmfException("Could not update Case: " + element + "; " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized CaseInput getCaseInput(int inputId) throws EmfException {

        Session session = sessionFactory.getSession();
        try {

            CaseInput input = dao.getCaseInput(inputId, session);
            return input;
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Input Names", e);
            throw new EmfException("Could not get all Input Names");
        } finally {
            session.close();
        }

    }

    public synchronized InputName[] getInputNames() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getInputNames(session);
            return (InputName[]) results.toArray(new InputName[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Input Names", e);
            throw new EmfException("Could not get all Input Names");
        } finally {
            session.close();
        }
    }

    public synchronized InputEnvtVar[] getInputEnvtVars() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getInputEnvtVars(session);
            return (InputEnvtVar[]) results.toArray(new InputEnvtVar[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Input Environment Variables", e);
            throw new EmfException("Could not get all Input Environment Variables");
        } finally {
            session.close();
        }
    }

    public synchronized CaseProgram[] getPrograms() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getPrograms(session);
            return (CaseProgram[]) results.toArray(new CaseProgram[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Programs", e);
            throw new EmfException("Could not get all Programs");
        } finally {
            session.close();
        }
    }

    public synchronized ModelToRun[] getModelToRuns() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getModelToRuns(session);
            return (ModelToRun[]) results.toArray(new ModelToRun[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Models To Run", e);
            throw new EmfException("Could not get all Models To Run");
        } finally {
            session.close();
        }
    }

    private Version[] getInputDatasetVersions(int caseId) throws EmfException {
        CaseInput[] inputs = getCaseInputs(caseId);
        List<Version> list = new ArrayList<Version>();

        for (int i = 0; i < inputs.length; i++)
            if (inputs[i].getDataset() != null && !checkExternalDSType(inputs[i].getDatasetType()))
                list.add(inputs[i].getVersion());

        return list.toArray(new Version[0]);
    }

    private EmfDataset[] getInputDatasets(int caseId) throws EmfException {
        CaseInput[] inputs = getCaseInputs(caseId);
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

    private SubDir[] getSubdirs(int caseId) throws EmfException {
        CaseInput[] inputs = getCaseInputs(caseId);
        SubDir[] subdirs = new SubDir[inputs.length];

        for (int i = 0; i < inputs.length; i++)
            subdirs[i] = inputs[i].getSubdirObj();

        return subdirs;
    }

    public synchronized InputName addCaseInputName(InputName name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(name, session);
            Criterion crit1 = Restrictions.eq("name", name.getName());
            Criterion crit2 = Restrictions.eq("modelToRunId", new Integer(name.getModelToRunId()));

            return (InputName) dao.load(InputName.class, new Criterion[] { crit1, crit2 }, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new case input name '" + name.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new case input name '" + name.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized CaseProgram addProgram(CaseProgram program) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(program, session);
            Criterion crit1 = Restrictions.eq("name", program.getName());
            Criterion crit2 = Restrictions.eq("modelToRunId", new Integer(program.getModelToRunId()));

            return (CaseProgram) dao.load(CaseProgram.class, new Criterion[] { crit1, crit2 }, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new program '" + program.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new program '" + program.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized InputEnvtVar addInputEnvtVar(InputEnvtVar inputEnvtVar) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(inputEnvtVar, session);
            Criterion crit1 = Restrictions.eq("name", inputEnvtVar.getName());
            Criterion crit2 = Restrictions.eq("modelToRunId", new Integer(inputEnvtVar.getModelToRunId()));
            return (InputEnvtVar) dao.load(InputEnvtVar.class, new Criterion[] { crit1, crit2 }, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new input environment variable '" + inputEnvtVar.getName() + "'\n"
                    + e.getMessage());
            throw new EmfException("Could not add new input environment variable '" + inputEnvtVar.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized ModelToRun addModelToRun(ModelToRun model) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ModelToRun temp = (ModelToRun) dao.load(ModelToRun.class, model.getName(), session);

            if (temp != null)
                return temp;

            dao.add(model, session);

            return (ModelToRun) dao.load(ModelToRun.class, model.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new model to run '" + model.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new model to run '" + model.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized GridResolution addGridResolution(GridResolution gridResolution) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            GridResolution temp = (GridResolution) dao.load(GridResolution.class, gridResolution.getName(), session);

            if (temp != null)
                return temp;

            dao.add(gridResolution, session);
            return (GridResolution) dao.load(GridResolution.class, gridResolution.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new Grid Resolution '" + gridResolution.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new Grid Resolution '" + gridResolution.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized SubDir[] getSubDirs() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getSubDirs(session);
            return (SubDir[]) results.toArray(new SubDir[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all subdirectories", e);
            throw new EmfException("Could not get all subdirectories");
        } finally {
            session.close();
        }
    }

    public synchronized SubDir addSubDir(SubDir subdir) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Criterion crit1 = Restrictions.eq("name", subdir.getName());
            Criterion crit2 = Restrictions.eq("modelToRunId", new Integer(subdir.getModelToRunId()));
            SubDir existed = (SubDir) dao.load(SubDir.class, new Criterion[] { crit1, crit2 }, session);

            if (existed != null)
                return existed;

            dao.add(subdir, session);

            return (SubDir) dao.load(SubDir.class, new Criterion[] { crit1, crit2 }, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new subdirectory '" + subdir.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new subdirectory '" + subdir.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized void addCaseInputs(User user, int caseId, CaseInput[] inputs) throws EmfException {
        Session session = sessionFactory.getSession();
        List<Sector> sectors = new ArrayList<Sector>();

        try {
            for (int i = 0; i < inputs.length; i++) {
                inputs[i].setCaseID(caseId);
                CaseJob job = dao.getCaseJob(inputs[i].getCaseJobID());

                if (job != null) {
                    CaseJob targetJob = dao.getCaseJob(caseId, job, session);

                    if (targetJob == null)
                        throw new EmfException("Required case job: " + job.getName() + " doesn't exist in target case.");

                    inputs[i].setCaseJobID(targetJob.getId());
                }

                if (dao.caseInputExists(inputs[i], session))
                    throw new EmfException("Case input: " + inputs[i].getName() + " already exists in target case.");
            }

            for (CaseInput input : inputs) {
                dao.add(input, session);
                Sector sector = input.getSector();

                if (sector != null && !sectors.contains(sector))
                    sectors.add(input.getSector());
            }
        } catch (Exception e) {
            log.error("Could not add new case input '" + inputs[0].getName() + "' etc.\n", e);
            throw new EmfException(e.getMessage());
        } finally {
            // NOTE: if any inputs get copied, we need to update target case sector list
            try {
                updateSectorsList(user, caseId, session, sectors);
            } catch (Exception e) {
                throw new EmfException(e.getMessage() + " " + inputs.length + " inputs copied.");
            } finally {
                session.close();
            }
        }
    }

    private synchronized void addCaseInputs(User user, int caseId, CaseInput[] inputs, String jobPrefix)
            throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            for (int i = 0; i < inputs.length; i++) {
                inputs[i].setCaseID(caseId);
                CaseJob job = dao.getCaseJob(inputs[i].getCaseJobID());

                if (job != null) {
                    CaseJob targetJob = dao.getCaseJob(caseId, jobPrefix + job.getName(), session);

                    if (targetJob == null)
                        throw new EmfException("Required case job: " + jobPrefix + job.getName()
                                + " doesn't exist in target case.");

                    inputs[i].setCaseJobID(targetJob.getId());
                }

                if (dao.caseInputExists(inputs[i], session))
                    throw new EmfException("Case input: " + inputs[i].getName() + " already exists in target case.");
            }

            for (CaseInput input : inputs) {
                dao.add(input, session);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new case input '" + inputs[0].getName() + "' etc.\n" + e.getMessage());
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized CaseInput addCaseInput(User user, CaseInput input, boolean copyingCase) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            if (!copyingCase)
                checkNExtendCaseLock(user, getCase(input.getCaseID(), session));
        } catch (EmfException e) {
            throw e;
        }

        try {
            if (dao.caseInputExists(input, session))
                throw new EmfException(
                        "The combination of 'Input Name', 'Sector', 'Program', and 'Job' should be unique.");

            dao.add(input, session);
            return (CaseInput) dao.loadCaseInput(input, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new case input '" + input.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new case input '" + input.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized void updateCaseInput(User user, CaseInput input) throws EmfException {
        Session localSession = sessionFactory.getSession();

        try {
            checkNExtendCaseLock(user, getCase(input.getCaseID(), localSession));
        } catch (EmfException e) {
            throw e;
        }

        try {
            CaseInput loaded = (CaseInput) dao.loadCaseInput(input, localSession);

            if (loaded != null && loaded.getId() != input.getId())
                throw new EmfException("Case input uniqueness check failed (" + loaded.getId() + "," + input.getId()
                        + ")");

            // Clear the cached information. To update a case
            // FIXME: Verify the session.clear()
            localSession.clear();
            dao.updateCaseInput(input, localSession);
            // setStatus(user, "Saved input " + input.getName() + " to database.", "Save Input");
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not update case input: " + input.getName() + ".\n" + e);
            throw new EmfException("Could not update case input: " + input.getName() + ".");
        } finally {
            localSession.close();
        }
    }

    public synchronized void removeCaseInputs(CaseInput[] inputs) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.removeCaseInputs(inputs, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not remove case input " + inputs[0].getName() + " etc.\n" + e.getMessage());
            throw new EmfException("Could not remove case input " + inputs[0].getName() + " etc.");
        } finally {
            session.close();
        }
    }

    public synchronized CaseInput[] getCaseInputs(int caseId, int[] jobIds) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<CaseInput> inputs = dao.getCaseInputsByJobIds(caseId, jobIds, session);

            return inputs.toArray(new CaseInput[0]);
        } catch (Exception e) {
            throw new EmfException("Error retrieving case inputs: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized CaseInput[] getCaseInputs(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<CaseInput> inputs = dao.getCaseInputs(caseId, session);

            Collections.sort(inputs, new Comparator<CaseInput>() {
                public int compare(CaseInput o1, CaseInput o2) {
                    int sectorId1 = (o1.getSector() == null) ? 0 : o1.getSector().getId();
                    int sectorId2 = (o2.getSector() == null) ? 0 : o2.getSector().getId();

                    int envId1 = (o1.getEnvtVars() == null) ? 0 : o1.getEnvtVars().getId();
                    int envId2 = (o2.getEnvtVars() == null) ? 0 : o2.getEnvtVars().getId();

                    int jobId1 = o1.getCaseJobID();
                    int jobId2 = o2.getCaseJobID();

                    if (sectorId1 > sectorId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 > envId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 == envId2 && jobId1 > jobId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 == envId2 && jobId1 == jobId2)
                        return 0;

                    return -1;
                }
            });

            return inputs.toArray(new CaseInput[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all inputs for case (id=" + caseId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all inputs for case (id=" + caseId + ").\n");
        } finally {
            session.close();
        }
    }

    public synchronized CaseInput[] getCaseInputs(int pageSize, int caseId, Sector sector, boolean showAll)
            throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<CaseInput> inputs = dao.getCaseInputs(pageSize, caseId, sector, showAll, session);

            Collections.sort(inputs, new Comparator<CaseInput>() {
                public int compare(CaseInput o1, CaseInput o2) {
                    int sectorId1 = (o1.getSector() == null) ? 0 : o1.getSector().getId();
                    int sectorId2 = (o2.getSector() == null) ? 0 : o2.getSector().getId();

                    int envId1 = (o1.getEnvtVars() == null) ? 0 : o1.getEnvtVars().getId();
                    int envId2 = (o2.getEnvtVars() == null) ? 0 : o2.getEnvtVars().getId();

                    int jobId1 = o1.getCaseJobID();
                    int jobId2 = o2.getCaseJobID();

                    if (sectorId1 > sectorId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 > envId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 == envId2 && jobId1 > jobId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 == envId2 && jobId1 == jobId2)
                        return 0;

                    return -1;
                }
            });
            return inputs.toArray(new CaseInput[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all inputs for case (id=" + caseId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all inputs for case (id=" + caseId + ").\n");
        } finally {
            session.close();
        }
    }

    /**
     * Gets all the inputs for this job, selects based on: case ID, job ID, and sector
     */
    private List<CaseInput> getJobInputs(int caseId, int jobId, Sector sector, Session session) throws EmfException {
        List<CaseInput> outInputs = new ArrayList<CaseInput>();
        EmfDataset cipDataset = null;
        String badCipName = null;

        // select the inputs based on 3 criteria
        try {
            List<CaseInput> inputs = dao.getJobInputs(caseId, jobId, sector, session);
            if (DebugLevels.DEBUG_9)
                System.out.println("Are inputs null?" + (inputs == null));
            Iterator<CaseInput> iter = inputs.iterator();

            while (iter.hasNext()) {

                CaseInput cip = iter.next();
                badCipName = cip.getName();
                cipDataset = cip.getDataset();

                if (cipDataset == null) {

                    if (cip.isRequired()) {
                        if (DebugLevels.DEBUG_9)
                            System.out.println("CIP DATASET IS NULL AND IS REQD FOR CIP INPUT " + cip.getName());
                        badCipName = cip.getName();
                        // emf exception
                        throw new EmfException("Required dataset not set for CaseInput= " + badCipName);
                    }
                } else {
                    outInputs.add(cip);
                }
            }

            return outInputs;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all inputs for case (id=" + caseId + "), job (id=" + jobId + ").\n"
                    + e.getMessage());
            // throw new EmfException("Required dataset not set for Case Input name = " + badCipName);
            throw new EmfException(e.getMessage());
        }
    }

    private List<CaseInput> getAllJobInputs(CaseJob job, Session session) throws EmfException {
        /**
         * Gets all the inputs for a specific job
         */
        int caseId = job.getCaseId();
        int jobId = job.getId();

        /*
         * Need to get the inputs for 4 different scenarios: All sectors, all jobs Specific sector, all jobs All
         * sectors, specific job specific sector, specific job
         */
        List<CaseInput> inputsAA = null; // inputs for all sectors and all jobs
        List<CaseInput> inputsSA = null; // inputs for specific sector and all jobs
        List<CaseInput> inputsAJ = null; // inputs for all sectors specific jobs
        List<CaseInput> inputsSJ = null; // inputs for specific sectors specific jobs
        List<CaseInput> inputsAll = new ArrayList<CaseInput>(); // all inputs
        try {

            // Get case inputs (the datasets associated w/ the case)
            // All sectors, all jobs
            inputsAA = this.getJobInputs(caseId, this.ALL_JOB_ID, this.ALL_SECTORS, session);

            // Sector specific, all jobs
            Sector sector = job.getSector();
            if (sector != this.ALL_SECTORS) {
                inputsSA = this.getJobInputs(caseId, this.ALL_JOB_ID, sector, session);
            }

            // All sectors, job specific
            inputsAJ = this.getJobInputs(caseId, jobId, this.ALL_SECTORS, session);

            // Specific sector and specific job
            if (sector != this.ALL_SECTORS) {
                inputsSJ = this.getJobInputs(caseId, jobId, sector, session);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all inputs for case (id=" + caseId + "), job (id=" + jobId + ").\n"
                    + e.getMessage());
            // throw new EmfException("Could not get all inputs for case (id=" + caseId + "), job (id=" + jobId +
            // ").\n");
            throw new EmfException(e.getMessage());
        }

        // append all the job inputs to the inputsAll list
        if ((inputsAA != null) && (inputsAA.size() > 0)) {
            if (DebugLevels.DEBUG_0)
                System.out.println("Number of AA inputs = " + inputsAA.size());
            inputsAll.addAll(inputsAA);
        }
        if ((inputsSA != null) && (inputsSA.size() > 0)) {
            if (DebugLevels.DEBUG_0)
                System.out.println("Number of SA inputs = " + inputsSA.size());
            inputsAll.addAll(inputsSA);
        }
        if ((inputsAJ != null) && (inputsAJ.size() > 0)) {
            if (DebugLevels.DEBUG_0)
                System.out.println("Number of AJ inputs = " + inputsAJ.size());
            inputsAll.addAll(inputsAJ);
        }
        if ((inputsSJ != null) && (inputsSJ.size() > 0)) {
            if (DebugLevels.DEBUG_0)
                System.out.println("Number of SJ inputs = " + inputsSJ.size());
            inputsAll.addAll(inputsSJ);
        }
        if (DebugLevels.DEBUG_0)
            System.out.println("Total number of inputs = " + inputsAll.size());

        return (inputsAll);
    }

    public synchronized Case[] copyCaseObject(int[] toCopy, User user) throws EmfException {
        List<Case> copiedList = new ArrayList<Case>();

        for (int i = 0; i < toCopy.length; i++) {
            Case caseToCopy = getCase(toCopy[i]);
            try {
                copiedList.add(copySingleCaseObj(caseToCopy, user));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Could not copy case " + caseToCopy.getName() + ".", e);
                throw new EmfException("Could not copy case " + caseToCopy.getName() + ". " + e.getMessage());
            }
        }

        return copiedList.toArray(new Case[0]);
    }

    private synchronized void copyCaseInputs(User user, int origCaseId, int copiedCaseId) throws Exception {
        CaseInput[] tocopy = getCaseInputs(origCaseId);

        for (int i = 0; i < tocopy.length; i++)
            copySingleInput(user, tocopy[i], copiedCaseId);
    }

    private synchronized CaseInput copySingleInput(User user, CaseInput input, int copiedCaseId) throws Exception {
        CaseInput copied = (CaseInput) DeepCopy.copy(input);
        copied.setCaseID(copiedCaseId);

        Session session = sessionFactory.getSession();
        try {
            CaseJob job = dao.getCaseJob(input.getCaseJobID(), session);

            if (job != null) {
                CaseJob copiedJob = dao.getCaseJob(copiedCaseId, job, session);
                copied.setCaseJobID(copiedJob.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        } finally {

            session.close();
        }

        return addCaseInput(user, copied, true);
    }

    private synchronized Case addCopiedCase(Case element, User user) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.add(element, session);
            Case loaded = (Case) dao.load(Case.class, element.getName(), session);
            Case locked = dao.obtainLocked(user, loaded, session);
            locked.setAbbreviation(new Abbreviation(loaded.getId() + ""));
            return dao.update(locked, session);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not add case " + element, e);
            throw new EmfException("Could not add case " + element);
        } finally {
            session.close();
        }
    }

    private synchronized void copyCaseJobs(int toCopyCaseId, int copiedCaseId, User user) throws Exception {
        CaseJob[] tocopy = getCaseJobs(toCopyCaseId);
        CaseJob[] copiedJobs = new CaseJob[tocopy.length];

        for (int i = 0; i < tocopy.length; i++)
            copiedJobs[i] = copySingleJob(tocopy[i], copiedCaseId, user);

        CaseJob[] depencyUpdated = resetDependentJobIds(copiedJobs, tocopy);

        try {
            for (CaseJob job : depencyUpdated)
                dao.updateCaseJob(job);
        } catch (Exception e) {
            log.error("Cannot update copied jobs with their dependent jobs.", e);
        }
    }

    private synchronized CaseJob copySingleJob(CaseJob job, int copiedCaseId, User user) throws Exception {
        job.setRunCompletionDate(new Date());
        job.setRunStartDate(new Date());
        CaseJob copied = (CaseJob) DeepCopy.copy(job);
        copied.setCaseId(copiedCaseId);
        copied.setJobkey(null); // jobkey supposedly generated when it is run
        copied.setRunstatus(null);
        copied.setRunLog(null);
        copied.setRunStartDate(null);
        copied.setRunCompletionDate(null);
        copied.setRunJobUser(null); // no running user at this time
        copied.setUser(user); // the user who makes the copy should be the owner of the copy

        return addCaseJob(user, copied, true);
    }

    private synchronized CaseJob[] resetDependentJobIds(CaseJob[] copiedJobs, CaseJob[] toCopyJobs) {
        HashMap<String, String> origJobMap = new HashMap<String, String>();
        HashMap<String, String> copiedJobMap = new HashMap<String, String>();

        int size = copiedJobs.length;

        for (int i = 0; i < size; i++) {
            origJobMap.put(toCopyJobs[i].getId() + "", toCopyJobs[i].getName());
            copiedJobMap.put(copiedJobs[i].getName(), copiedJobs[i].getId() + "");
        }

        for (int j = 0; j < size; j++) {
            DependentJob[] depJobs = copiedJobs[j].getDependentJobs();
            ArrayList jobsToKeep = new ArrayList(); // AME: added per Qun

            if (depJobs != null && depJobs.length > 0) {
                for (int k = 0; k < depJobs.length; k++) {
                    String jobName = origJobMap.get(depJobs[k].getJobId() + "");
                    String jobId = copiedJobMap.get(jobName);
                    int id = 0;

                    try {
                        id = Integer.parseInt(jobId);
                        depJobs[k].setJobId(id);
                        jobsToKeep.add(depJobs[k]);
                    } catch (Exception e) {
                        // NOTE: discard the dependency if the job depended on doesn't exist.
                    }

                }
            }
            DependentJob[] remainingJobs = new DependentJob[jobsToKeep.size()];
            jobsToKeep.toArray(remainingJobs);
            copiedJobs[j].setDependentJobs(remainingJobs);
        }

        return copiedJobs;
    }

    public synchronized ParameterName[] getParameterNames() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<ParameterName> type = dao.getParameterNames(session);

            return type.toArray(new ParameterName[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all parameter names.\n" + e.getMessage());
            throw new EmfException("Could not get all parameter names.\n");
        } finally {
            session.close();
        }
    }

    public synchronized void addCaseParameters(User user, int caseId, CaseParameter[] params) throws EmfException {
        Session session = sessionFactory.getSession();
        List<Sector> sectors = new ArrayList<Sector>();

        try {
            for (int i = 0; i < params.length; i++) {
                params[i].setCaseID(caseId);

                CaseJob job = dao.getCaseJob(params[i].getJobId());

                if (job != null) {
                    CaseJob targetJob = dao.getCaseJob(caseId, job, session);

                    if (targetJob == null)
                        throw new EmfException("Required case job: " + job.getName() + " doesn't exist in target case.");

                    params[i].setJobId(targetJob.getId());
                }

                if (dao.caseParameterExists(params[i], session))
                    throw new EmfException("Case parameter: " + params[i].getName() + " already exists in target case.");
            }

            for (CaseParameter param : params) {
                dao.addParameter(param, session);
                Sector sector = param.getSector();

                if (sector != null && !sectors.contains(sector))
                    sectors.add(param.getSector());
            }
        } catch (Exception e) {
            log.error("Could not add new case parameter '" + params[0].getName() + "' etc.\n", e);
            throw new EmfException(e.getMessage());
        } finally {
            // NOTE: if there is any parameter get copied, need to add new sectors to the copied case
            // sectors list
            try {
                updateSectorsList(user, caseId, session, sectors);
            } catch (Exception e) {
                throw new EmfException(e.getMessage() + " " + params.length + " parameters copied.");
            } finally {
                session.close();
            }
        }
    }

    private void updateSectorsList(User user, int caseId, Session session, List<Sector> sectors) throws EmfException {
        if (sectors.size() > 0) {
            boolean lockObtained = false;
            Case target = dao.getCase(caseId, session);

            if (target.isLocked() && !target.isLocked(user))
                throw new EmfException("Cannot obtain lock on target case to update sectors list.");

            Case locked = target;

            if (!target.isLocked()) {
                locked = dao.obtainLocked(user, target, session);
                lockObtained = true;
            }

            Sector[] sectrs = locked.getSectors();
            List<Sector> existed = new ArrayList<Sector>();
            existed.addAll(Arrays.asList(sectrs));

            for (int i = 0; i < sectors.size(); i++)
                if (!existed.contains(sectors.get(i)))
                    existed.add(sectors.get(i));

            locked.setSectors(existed.toArray(new Sector[0]));

            if (lockObtained) {
                dao.update(locked, session);
                return;
            }

            dao.updateWithLock(locked, session);
        }
    }

    private synchronized void addCaseParameters(User user, int caseId, CaseParameter[] params, String jobPrefix)
            throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            for (int i = 0; i < params.length; i++) {
                params[i].setCaseID(caseId);

                CaseJob job = dao.getCaseJob(params[i].getJobId());

                if (job != null) {
                    CaseJob targetJob = dao.getCaseJob(caseId, jobPrefix + job.getName(), session);

                    if (targetJob == null)
                        throw new EmfException("Required case job: " + jobPrefix + job.getName()
                                + " doesn't exist in target case.");

                    params[i].setJobId(targetJob.getId());
                }

                if (dao.caseParameterExists(params[i], session))
                    throw new EmfException("Case parameter: " + params[i].getName() + " already exists in target case.");
            }

            for (CaseParameter param : params)
                dao.addParameter(param, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new case parameter '" + params[0].getName() + "' etc.\n" + e.getMessage());
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized CaseParameter addCaseParameter(User user, CaseParameter param, boolean copyingCase)
            throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            if (!copyingCase)
                checkNExtendCaseLock(user, getCase(param.getCaseID(), session));
        } catch (EmfException e) {
            throw e;
        }

        try {
            if (dao.caseParameterExists(param, session))
                throw new EmfException(
                        "The combination of 'Parameter Name', 'Sector', 'Program', and 'Job' should be unique.");

            dao.addParameter(param, session);
            return dao.loadCaseParameter(param, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new case parameter '" + param.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new case parameter '" + param.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized void removeCaseParameters(CaseParameter[] params) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.removeCaseParameters(params, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not remove case parameter " + params[0].getName() + " etc.\n" + e.getMessage());
            throw new EmfException("Could not remove case parameter " + params[0].getName() + " etc.");
        } finally {
            session.close();
        }
    }

    public CaseParameter getCaseParameter(int caseId, ParameterEnvVar var) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return dao.getCaseParameter(caseId, var, session);
        } catch (Exception e) {
            log.error("Could not get parameter for case (id=" + caseId + ") and environment variable: " + var.getName()
                    + ".\n", e);
            throw new EmfException("Could not get parameter for case (id=" + caseId + ") and environment variable: "
                    + var.getName() + ".\n");
        } finally {
            session.close();
        }
    }

    public synchronized CaseParameter[] getCaseParameters(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<CaseParameter> params = dao.getCaseParameters(caseId, session);

            Collections.sort(params, new Comparator<CaseParameter>() {
                public int compare(CaseParameter o1, CaseParameter o2) {
                    int sectorId1 = (o1.getSector() == null) ? 0 : o1.getSector().getId();
                    int sectorId2 = (o2.getSector() == null) ? 0 : o2.getSector().getId();

                    int envId1 = (o1.getEnvVar() == null) ? 0 : o1.getEnvVar().getId();
                    int envId2 = (o2.getEnvVar() == null) ? 0 : o2.getEnvVar().getId();

                    int jobId1 = o1.getJobId();
                    int jobId2 = o2.getJobId();

                    if (sectorId1 > sectorId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 > envId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 == envId2 && jobId1 > jobId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 == envId2 && jobId1 == jobId2)
                        return 0;

                    return -1;
                }
            });

            return params.toArray(new CaseParameter[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all parameters for case (id=" + caseId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all parameters for case (id=" + caseId + ").\n");
        } finally {
            session.close();
        }
    }

    public synchronized CaseParameter[] getCaseParameters(int caseId, int[] jobIds) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<CaseParameter> params = dao.getCaseParametersByJobIds(caseId, jobIds, session);

            return params.toArray(new CaseParameter[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all parameters for case (id=" + caseId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all parameters for case (id=" + caseId + ").\n");
        } finally {
            session.close();
        }
    }

    public synchronized CaseParameter[] getCaseParameters(int pageSize, int caseId, Sector sector, boolean showAll)
            throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<CaseParameter> params = dao.getCaseParameters(pageSize, caseId, sector, showAll, session);

            Collections.sort(params, new Comparator<CaseParameter>() {
                public int compare(CaseParameter o1, CaseParameter o2) {
                    int sectorId1 = (o1.getSector() == null) ? 0 : o1.getSector().getId();
                    int sectorId2 = (o2.getSector() == null) ? 0 : o2.getSector().getId();

                    int envId1 = (o1.getEnvVar() == null) ? 0 : o1.getEnvVar().getId();
                    int envId2 = (o2.getEnvVar() == null) ? 0 : o2.getEnvVar().getId();

                    int jobId1 = o1.getJobId();
                    int jobId2 = o2.getJobId();

                    if (sectorId1 > sectorId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 > envId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 == envId2 && jobId1 > jobId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 == envId2 && jobId1 == jobId2)
                        return 0;

                    return -1;
                }
            });

            return params.toArray(new CaseParameter[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all parameters for case (id=" + caseId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all parameters for case (id=" + caseId + ").\n");
        } finally {
            session.close();
        }
    }

    public synchronized Executable addExecutable(Executable exe) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (!dao.exeutableExists(session, exe))
                dao.add(exe, session);

            return (Executable) dao.load(Executable.class, exe.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new executable '" + exe.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new executable '" + exe.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized ParameterName addParameterName(ParameterName name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.addParameterName(name, session);
            Criterion crit1 = Restrictions.eq("name", name.getName());
            Criterion crit2 = Restrictions.eq("modelToRunId", new Integer(name.getModelToRunId()));
            return (ParameterName) dao.load(ParameterName.class, new Criterion[] { crit1, crit2 }, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new parameter name '" + name.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new parameter name '" + name.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized ValueType[] getValueTypes() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<ValueType> type = dao.getValueTypes(session);

            return type.toArray(new ValueType[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all value types.\n" + e.getMessage());
            throw new EmfException("Could not get all value types.\n");
        } finally {
            session.close();
        }
    }

    public synchronized ValueType addValueType(ValueType type) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.addValueType(type, session);
            return (ValueType) dao.load(ValueType.class, type.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new value type '" + type.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new value type '" + type.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized ParameterEnvVar[] getParameterEnvVars() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<ParameterEnvVar> envvar = dao.getParameterEnvVars(session);

            return envvar.toArray(new ParameterEnvVar[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all parameter env variables.\n" + e.getMessage());
            throw new EmfException("Could not get all parameter env variables.\n");
        } finally {
            session.close();
        }
    }

    public synchronized ParameterEnvVar addParameterEnvVar(ParameterEnvVar envVar) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(envVar, session);
            Criterion crit1 = Restrictions.eq("name", envVar.getName());
            Criterion crit2 = Restrictions.eq("modelToRunId", new Integer(envVar.getModelToRunId()));
            return (ParameterEnvVar) dao.load(ParameterEnvVar.class, new Criterion[] { crit1, crit2 }, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new parameter env variable '" + envVar.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new parameter env variable '" + envVar.getName() + "'");
        } finally {
            session.close();
        }
    }

    private synchronized void copyCaseParameters(User user, int toCopyCaseId, int copiedCaseId) throws Exception {
        CaseParameter[] tocopy = getCaseParameters(toCopyCaseId);

        for (int i = 0; i < tocopy.length; i++)
            copySingleParameter(user, tocopy[i], copiedCaseId);
    }

    private synchronized CaseParameter copySingleParameter(User user, CaseParameter parameter, int copiedCaseId)
            throws Exception {
        CaseParameter copied = (CaseParameter) DeepCopy.copy(parameter);
        copied.setCaseID(copiedCaseId);

        Session session = sessionFactory.getSession();
        try {
            CaseJob job = dao.getCaseJob(parameter.getJobId(), session);

            if (job != null) {
                CaseJob copiedJob = dao.getCaseJob(copiedCaseId, job, session);
                copied.setJobId(copiedJob.getId());
            }
        } finally {
            session.close();
        }

        return addCaseParameter(user, copied, true);
    }

    public synchronized void addCaseJobs(User user, int caseId, CaseJob[] jobs) throws EmfException {
        Session session = sessionFactory.getSession();
        List<Sector> sectors = new ArrayList<Sector>();

        try {
            for (CaseJob job : jobs)
                if (dao.getCaseJob(caseId, job, session) != null)
                    throw new EmfException("Case job: " + job.getName() + " already exists in the target case.");

            for (CaseJob job : jobs) {
                job.setCaseId(caseId);
                job.setJobkey(null); // jobkey supposedly generated when it is run
                job.setRunLog(null);
                job.setRunStartDate(null);
                job.setRunCompletionDate(null);
                job.setRunstatus(dao.getJobRunStatuse("Not Started"));
                dao.add(job, session);
                Sector sector = job.getSector();

                if (sector != null && !sectors.contains(sector))
                    sectors.add(job.getSector());
            }
        } catch (EmfException e) {
            log.error("Could not add new case jobs '" + jobs[0].getName() + "' etc.\n" + e.getMessage());
            throw new EmfException(e.getMessage());
        } finally {
            // NOTE: if any jobs get copied, we need to update target case sector list
            try {
                updateSectorsList(user, caseId, session, sectors);
            } catch (Exception e) {
                throw new EmfException(e.getMessage() + " " + jobs.length + " jobs copied.");
            } finally {
                session.close();
            }
        }
    }

    public synchronized void addCaseJobs4Sensitivity(User user, int caseId, CaseJob[] jobs) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            for (CaseJob job : jobs)
                if (dao.getCaseJob(caseId, job, session) != null)
                    throw new EmfException("Case job: " + job.getName() + " already exists in the target case.");

            for (CaseJob job : jobs) {
                job.setCaseId(caseId);
                job.setJobkey(null); // jobkey supposedly generated when it is run
                job.setRunLog(null);
                job.setRunStartDate(null);
                job.setRunCompletionDate(null);
                job.setRunstatus(dao.getJobRunStatuse("Not Started"));
                dao.add(job, session);
            }
        } catch (EmfException e) {
            log.error("Could not add new case jobs '" + jobs[0].getName() + "' etc.\n" + e.getMessage());
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized CaseJob addCaseJob(User user, CaseJob job, boolean copyingCase) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            if (!copyingCase) // if not copying case, automatically extend the lock on case object
                checkNExtendCaseLock(user, getCase(job.getCaseId(), session));
        } catch (EmfException e) {
            throw e;
        }

        try {
            if (job.getRunstatus() == null)
                job.setRunstatus(dao.getJobRunStatuse("Not Started"));

            dao.add(job, session);
            return (CaseJob) dao.loadCaseJobByName(job);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new case job '" + job.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new case job '" + job.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized CaseJob[] getCaseJobs(int caseId) throws EmfException {

        Session session = sessionFactory.getSession();

        try {
            List<CaseJob> jobs = dao.getCaseJobs(caseId, session);

            if (jobs == null || jobs.size() == 0)
                return new CaseJob[0];

            Collections.sort(jobs);
            return jobs.toArray(new CaseJob[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all jobs for case (id=" + caseId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all jobs for case (id=" + caseId + ").\n");
        } finally {
            session.close();
        }
    }

    public synchronized JobRunStatus[] getJobRunStatuses() throws EmfException {

        Session session = sessionFactory.getSession();

        try {
            List<JobRunStatus> runstatuses = dao.getJobRunStatuses(session);

            return runstatuses.toArray(new JobRunStatus[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all job run statuses.\n" + e.getMessage());
            throw new EmfException("Could not get all job run statuses.\n");
        } finally {
            session.close();
        }
    }

    public synchronized Executable[] getExecutables(int casejobId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<Executable> runstatuses = dao.getExecutables(session);

            return runstatuses.toArray(new Executable[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all executables.\n" + e.getMessage());
            throw new EmfException("Could not get all executables.\n");
        } finally {
            session.close();
        }
    }

    private void resetRelatedJobsField(CaseJob[] jobs) throws EmfException {
        int jobslen = jobs.length;

        if (jobslen == 0)
            return;

        Session session = sessionFactory.getSession();

        try {
            int caseId = jobs[0].getCaseId();
            CaseInput[] inputs = dao.getCaseInputs(caseId, session).toArray(new CaseInput[0]);
            CaseParameter[] params = dao.getCaseParameters(caseId, session).toArray(new CaseParameter[0]);

            for (int i = 0; i < jobslen; i++) {
                for (int j = 0; j < inputs.length; j++)
                    if (inputs[j].getCaseJobID() == jobs[i].getId())
                        inputs[j].setCaseJobID(0);

                for (int k = 0; k < params.length; k++)
                    if (params[k].getJobId() == jobs[i].getId())
                        params[k].setJobId(0);

                jobs[i].setDependentJobs(null);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not reset case job.\n" + e.getMessage());
            throw new EmfException("Could not reset case job ");
        } finally {
            session.close();
        }

    }

    public synchronized void removeCaseJobs(CaseJob[] jobs) throws EmfException {
        Session session = sessionFactory.getSession();

        checkJobsStatuses(Arrays.asList(jobs), dao.getCase(jobs[0].getCaseId(), session));
        checkTaskPersistItems(jobs, session);
        checkJobOutputItems(jobs, session);
        checkJobHistoryItems(jobs, session);
        resetRelatedJobsField(jobs);
        deleteCaseJobKeyObjects(jobs, session);

        try {
            dao.checkJobDependency(jobs, session);
        } catch (EmfException e) {
            throw new EmfException("Cannot remove " + e.getMessage());
        }

        try {
            session.clear();
            dao.removeCaseJobs(jobs, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not remove case job " + jobs[0].getName() + " etc.\n" + e.getMessage());
            throw new EmfException("Could not remove selected job(s): " + e.getLocalizedMessage());
        } finally {
            session.close();
        }
    }

    private void checkJobOutputItems(CaseJob[] jobs, Session session) throws EmfException {
        String query = "SELECT obj.name from " + CaseOutput.class.getSimpleName() + " obj WHERE obj.jobId = "
                + getAndOrClause(jobs, "obj.jobId");
        List<?> list = session.createQuery(query).list();

        if (list != null && list.size() > 0)
            throw new EmfException("Please remove case outputs for the selected jobs.");
    }

    private void checkJobHistoryItems(CaseJob[] jobs, Session session) throws EmfException {
        String query = "SELECT obj.message from " + JobMessage.class.getSimpleName() + " obj WHERE obj.jobId = "
                + getAndOrClause(jobs, "obj.jobId");
        List<?> list = session.createQuery(query).list();

        if (list != null && list.size() > 0)
            throw new EmfException("Please remove job messages for the selected jobs.");
    }

    private void checkTaskPersistItems(CaseJob[] jobs, Session session) throws EmfException {
        String query = "SELECT obj.id from " + PersistedWaitTask.class.getSimpleName() + " obj WHERE obj.jobId = "
                + getAndOrClause(jobs, "obj.jobId");
        List<?> list = session.createQuery(query).list();

        if (list != null && list.size() > 0)
            throw new EmfException("Cannot delete job(s) -- selected job(s) is still active.");
    }

    private void deleteCaseJobKeyObjects(CaseJob[] jobs, Session session) throws EmfException {
        int updatedItems = 0;

        try {
            Transaction tx = session.beginTransaction();

            String query = "DELETE " + CaseJobKey.class.getSimpleName() + " obj WHERE obj.jobId = "
                    + getAndOrClause(jobs, "obj.jobId");

            if (DebugLevels.DEBUG_16)
                System.out.println("hql delete string: " + query);

            updatedItems = session.createQuery(query).executeUpdate();
            tx.commit();

            if (DebugLevels.DEBUG_16)
                System.out.println(updatedItems + " items updated.");
        } catch (HibernateException e) {
            throw new EmfException(e.getMessage());
        } finally {
            if (DebugLevels.DEBUG_16)
                log.warn(updatedItems + " items updated from " + CaseJobKey.class.getName() + " table.");
        }
    }

    private String getAndOrClause(CaseJob[] jobs, String attrName) {
        StringBuffer sb = new StringBuffer();
        int numIDs = jobs.length;

        if (numIDs == 1)
            return "" + jobs[0].getId();

        for (int i = 0; i < numIDs - 1; i++)
            sb.append(jobs[i].getId() + " OR " + attrName + " = ");

        sb.append(jobs[numIDs - 1].getId());

        return sb.toString();
    }

    private synchronized Case copySingleCaseObj(Case toCopy, User user) throws Exception {
        Case copied = (Case) DeepCopy.copy(toCopy);
        copied.setName(getUniqueNewName("Copy of " + toCopy.getName()));
        copied.setTemplateUsed(toCopy.getName());
        copied.setAbbreviation(null);
        copied.setLastModifiedBy(user);
        copied.setLastModifiedDate(new Date());
        Case loaded = addCopiedCase(copied, user);
        copyCaseJobs(toCopy.getId(), loaded.getId(), user); // copy job first for references in input and parameter
        copyCaseInputs(user, toCopy.getId(), loaded.getId());
        copyCaseParameters(user, toCopy.getId(), loaded.getId());

        Session session = sessionFactory.getSession();

        try {

            // NOTE: Verify why locked?
            // NOTE: it could be being edited by other user, but you still want to copy it
            if (loaded.isLocked())
                dao.forceReleaseLocked(loaded, session);
        } finally {
            session.close();
        }

        return loaded;
    }

    private String getUniqueNewName(String name) {
        Session session = sessionFactory.getSession();
        List<String> names = new ArrayList<String>();

        try {
            List<Case> allCases = dao.getCases(session);

            for (Iterator<Case> iter = allCases.iterator(); iter.hasNext();) {
                Case caseObj = iter.next();
                if (caseObj.getName().startsWith(name)) {
                    names.add(caseObj.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all cases.\n" + e.getMessage());
        } finally {
            session.close();
        }

        if (names.size() == 0)
            return name;

        return name + " " + getSequence(name, names);
    }

    private int getSequence(String stub, List<String> names) {
        int sequence = names.size() + 1;
        String integer = "";

        try {
            for (Iterator<String> iter = names.iterator(); iter.hasNext();) {
                integer = iter.next().substring(stub.length()).trim();

                if (!integer.isEmpty()) {
                    int temp = Integer.parseInt(integer);

                    if (temp == sequence)
                        ++sequence;
                    else if (temp > sequence)
                        sequence = temp + 1;
                }
            }

            return sequence;
        } catch (Exception e) {
            // NOTE: Assume one case won't be copied 10000 times.
            // This is farely safe assuming the random number do not duplicate.
            return Math.abs(new Random().nextInt()) % 10000;
        }
    }

    public synchronized void exportInputsForCase(User user, String dirName, String purpose, boolean overWrite,
            int caseId) throws EmfException {
        EmfDataset[] datasets = getInputDatasets(caseId);
        Version[] versions = getInputDatasetVersions(caseId);
        SubDir[] subdirs = getSubdirs(caseId);

        if (datasets.length == 0)
            return;

        File inputsDir = new File(dirName);

        if (!inputsDir.exists()) {
            inputsDir.mkdirs();
            inputsDir.setWritable(true, false);
        }

        for (int i = 0; i < datasets.length; i++) {
            String subdir = (subdirs[i] == null) ? "" : subdirs[i].getName();
            String exportDir = dirName + System.getProperty("file.separator") + subdir;
            File dir = new File(exportDir);

            if (!dir.exists()) {
                dir.mkdirs();
                setDirsWritable(new File(dirName), dir);
            }

            getExportService().exportForClient(user, new EmfDataset[] { datasets[i] }, new Version[] { versions[i] },
                    exportDir, purpose, overWrite);
        }
    }

    private void setDirsWritable(File base, File dir) {
        while (dir != null) {
            try {
                dir.setWritable(true, false);
            } catch (Exception e) {
                return;
            }

            dir = dir.getParentFile();

            if (dir.compareTo(base) == 0)
                return;
        }
    }

    public synchronized void updateCaseParameter(User user, CaseParameter parameter) throws EmfException {
        Session localSession = sessionFactory.getSession();

        try {
            checkNExtendCaseLock(user, getCase(parameter.getCaseID(), localSession));
        } catch (EmfException e) {
            throw e;
        }

        try {
            CaseParameter loaded = dao.loadCaseParameter(parameter, localSession);

            if (loaded != null && loaded.getId() != parameter.getId())
                throw new EmfException("Case parameter uniqueness check failed (" + loaded.getId() + ","
                        + parameter.getId() + ")");

            // FIXME: why session.clear()?
            localSession.clear();
            dao.updateCaseParameter(parameter, localSession);
            // setStatus(user, "Saved parameter " + parameter.getName() + " to database.", "Save Parameter");
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not update case parameter: " + parameter.getName() + ".\n" + e);
            throw new EmfException("Could not update case parameter: " + parameter.getName() + ".");
        } finally {
            localSession.close();
        }
    }

    /**
     * SubmitJobs(...) is called form the CaseServiceImpl.
     * 
     * 
     */
    public synchronized String submitJobs(Integer[] jobIds, int caseId, User user) throws EmfException {
        if (DebugLevels.DEBUG_0)
            System.out.println("ManagedCaseService::submitJobs size: " + jobIds.length + " for caseId= " + caseId);

        // create a new caseJobSubmitter for each client call in a session
        TaskSubmitter caseJobSubmitter = new CaseJobSubmitter(sessionFactory);

        Hashtable<String, CaseJob> caseJobsTable = new Hashtable<String, CaseJob>();
        ManagedExportService expSvc = null;

        CaseJobTask[] caseJobsTasksInSubmission = null;
        ArrayList<CaseJobTask> caseJobsTasksList = new ArrayList<CaseJobTask>();

        Case jobCase = this.getCase(caseId);

        if (DebugLevels.DEBUG_15) {
            // logNumDBConn("beginning of job submitter");
        }

        Session session = sessionFactory.getSession();

        try {
            String caseJobExportSubmitterId = null;
            String caseJobSubmitterId = caseJobSubmitter.getSubmitterId();

            // Test input directory is not empty

            if ((jobCase.getInputFileDir() == null) || (jobCase.getInputFileDir().equals(""))) {
                throw new EmfException("Input directory must be set to run job(s).");
            }
            if (DebugLevels.DEBUG_0)
                System.out.println("Is CaseJobSubmitterId null? " + (caseJobSubmitterId == null));
            // FIXME: Does this need to be done in a new DAO method???
            // Get the CaseJobs for each jobId
            // create a CaseJobTask per CaseJob.
            // save the CaseJobTask in an array and the CaseJob in the hashTable with
            // the casejjobtask unique id as the key to find the casejob
            for (Integer jobId : jobIds) {
                int jid = jobId.intValue();

                if (DebugLevels.DEBUG_15) {
                    // logNumDBConn("beginning of job submitter loop (jobID: " + jid + ")");
                }

                String jobKey = null;

                if (DebugLevels.DEBUG_0)
                    System.out.println("The jobId= " + jid);

                CaseJob caseJob = this.getCaseJob(jid, session);

                // NOTE: This is where the jobkey is generated and set in the CaseJob
                jobKey = this.createJobKey(jid);

                // set the job key in the case job
                caseJob.setJobkey(jobKey);

                // set the run user for the case job
                caseJob.setRunJobUser(user);

                // reset job run log
                caseJob.setRunLog("");

                // NOTE: does this affect job run???
                this.updateCaseJob(user, caseJob);

                // FIXME: Is this still needed?????
                // caseJob.setRunStartDate(new Date());
                if (DebugLevels.DEBUG_15) {
                    logNumDBConn("beginning of job task creation (jobID: " + jid + ")");
                }

                CaseJobTask cjt = new CaseJobTask(jid, caseId, user);
                cjt.setJobkey(jobKey);
                cjt.setNumDepends(caseJob.getDependentJobs().length);

                // get or create the reference to the Managed Export Service for this casejobtask
                expSvc = this.getExportService();

                if (DebugLevels.DEBUG_6)
                    System.out.println("set the casejobsubmitter id = " + caseJobSubmitterId);

                cjt.setSubmitterId(caseJobSubmitterId);

                if (DebugLevels.DEBUG_9)
                    System.out.println("before getJobFileName");

                String jobFileName = this.getJobFileName(caseJob, session);

                if (DebugLevels.DEBUG_6)
                    System.out.println("setJobFileContent");

                if (DebugLevels.DEBUG_9)
                    System.out.println("before setJobFileContent");

                cjt.setJobFileContent(this.createJobFileContent(caseJob, user, jobFileName, expSvc, session));

                if (DebugLevels.DEBUG_15) {
                    logNumDBConn("after creation of job file (jobID: " + jid + ")");
                }

                cjt.setJobFile(jobFileName);
                String jobLogFile = this.getLog(jobFileName);
                cjt.setLogFile(jobLogFile);
                cjt.setJobName(caseJob.getName());
                if (DebugLevels.DEBUG_6)
                    System.out.println("set Host Name");
                cjt.setHostName(caseJob.getHost().getName());
                if (DebugLevels.DEBUG_6)
                    System.out.println("getQueOptions");

                String queueOptions = caseJob.getQueOptions();

                // replace joblog in queue options
                queueOptions = queueOptions.replace("$EMF_JOBLOG", jobLogFile);
                // If other parameters are in the queue options, translate them to their
                // string values in the queue
                try {
                    queueOptions = dao.replaceEnvVarsCase(queueOptions, " ", jobCase, jobId);
                } catch (Exception e) {
                    throw new EmfException("Job (" + cjt.getJobName() + "): " + e.getMessage());
                }
                if (DebugLevels.DEBUG_6)
                    System.out.println("Queue options: " + queueOptions);
                cjt.setQueueOptions(queueOptions);

                if (DebugLevels.DEBUG_6)
                    System.out.println("Completed setting the CaseJobTask");

                // Now add the CaseJobTask to the caseJobsTasksList
                caseJobsTasksList.add(cjt);
                // Add the caseJob to the Hashtable caseJobsTable with the cjt taskid as the key
                caseJobsTable.put(cjt.getTaskId(), caseJob);

            }// for jobIds

            // convert the caseJobsTasksList to an array caseJobsTasksInSubmission
            caseJobsTasksInSubmission = caseJobsTasksList.toArray(new CaseJobTask[0]);

            // Now sort the Array using the built in comparator
            Arrays.sort(caseJobsTasksInSubmission);

            for (CaseJobTask cjt : caseJobsTasksInSubmission) {

                if (DebugLevels.DEBUG_15) {
                    logNumDBConn("beginning of job task loop (jobID: " + cjt.getJobId() + ")");
                }

                // get the caseJob out of the hashtable
                CaseJob caseJob = caseJobsTable.get(cjt.getTaskId());

                if (DebugLevels.DEBUG_0)
                    System.out.println("Is the caseJob for this jobId null? " + (caseJob == null));

                // now get the Case (called jobCase since case is a reserved word in Java) using
                // the caseId sent in from the GUI
                // Case jobCase = this.getCase(caseId, session);
                cjt.setCaseName(jobCase.getName());

                String purpose = "Used by job: " + caseJob.getName() + " of Case: " + jobCase.getName();
                if (DebugLevels.DEBUG_6)
                    System.out.println("Purpose= " + purpose);

                if (DebugLevels.DEBUG_0)
                    System.out.println("caseId= " + caseId + " Is the Case for this job null? " + (jobCase == null));

                List<CaseInput> inputs = getAllJobInputs(caseJob, session);

                if (DebugLevels.DEBUG_6)
                    System.out.println("Number of inputs for this job: " + inputs.size());

                if (DebugLevels.DEBUG_15) {
                    logNumDBConn("beginning of adding job task (jobID: " + cjt.getJobId() + ")");
                }

                // send the casejobtask to the CJTM priority queue and then to wait queue
                TaskManagerFactory.getCaseJobTaskManager(sessionFactory).addTask(cjt);

                if (DebugLevels.DEBUG_15) {
                    logNumDBConn("before submitting to export (jobID: " + cjt.getJobId() + ")");
                }
                // pass the inputs to the exportService which uses an exportJobSubmitter to work with exportTaskManager
                if (!this.doNotExportJobs(session))
                    caseJobExportSubmitterId = expSvc.exportForJob(user, inputs, cjt.getTaskId(), purpose, caseJob,
                            jobCase);
                else
                    log.warn("ManagedCaseService: case jobs related datasets are not exported.");

                if (DebugLevels.DEBUG_15) {
                    logNumDBConn("after submitted to export (jobID: " + cjt.getJobId() + ")");
                }

                String runStatusExporting = "Exporting";

                caseJob.setRunstatus(getJobRunStatus(runStatusExporting, session));
                caseJob.setRunStartDate(new Date());

                // Now update the casejob in the database
                updateJob(caseJob, session);

                if (DebugLevels.DEBUG_6)
                    System.out.println("Added caseJobTask to collection");

                if (DebugLevels.DEBUG_0)
                    System.out.println("Case Job Export Submitter Id for case job:" + caseJobExportSubmitterId);

            }// for cjt

            if (DebugLevels.DEBUG_0)
                System.out.println("Case Job Submitter Id for case job:" + caseJobSubmitterId);

            return caseJobSubmitterId;
        } catch (EmfException ex) {
            ex.printStackTrace();
            throw new EmfException(ex.getMessage());
        } finally {
            if (DebugLevels.DEBUG_15) {
                logNumDBConn("finished job submission");
            }

            if (session != null && session.isConnected())
                session.close();
        }
    }

    private JobRunStatus getJobRunStatus(String runStatus, Session session) throws EmfException {
        JobRunStatus jrStat = null;

        try {
            jrStat = dao.getJobRunStatuse(runStatus);

            return jrStat;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get job run status.\n" + e.getMessage());
            throw new EmfException("Could not get job run status.\n");
        }
    }

    private synchronized void updateJob(CaseJob caseJob, Session session) throws EmfException {
        try {
            dao.updateCaseJob(caseJob, session);
            dao.updateCaseJobKey(caseJob.getId(), caseJob.getJobkey(), session);
            session.flush();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public synchronized void updateCaseJob(User user, CaseJob job) throws EmfException {
        try {
            CaseJob loaded = (CaseJob) dao.loadCaseJobByName(job);
            if (user == null)
                throw new EmfException("Running Case Job requires a valid user");

            if (loaded != null && loaded.getId() != job.getId())
                throw new EmfException("Case job uniqueness check failed (" + loaded.getId() + "," + job.getId() + ")");

            dao.updateCaseJob(job);

            // This is a manual update of the waiting tasks in CaseJobTask manager
            // If CaseJobTaskManager is not null. check the dependencies in the WaitTable
            TaskManagerFactory.getCaseJobTaskManager(sessionFactory).callBackFromJobRunServer();

        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not update case job: " + job.getName() + ".\n" + e);
            throw new EmfException("Could not update case job: " + job.getName() + ".");
        }
    }

    public synchronized void updateCaseJobStatus(CaseJob job) throws EmfException {
        try {
            dao.updateCaseJob(job);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not update case job: " + job.getName() + ".\n" + e);
            throw new EmfException("Could not update case job: " + job.getName() + ".");
        }
    }

    public synchronized void saveCaseJobFromClient(User user, CaseJob job) throws EmfException {
        Session localSession = sessionFactory.getSession();

        try {
            checkNExtendCaseLock(user, getCase(job.getCaseId(), localSession));
        } catch (EmfException e) {
            throw e;
        } finally {
            localSession.close();
        }

        try {
            CaseJob loaded = (CaseJob) dao.loadCaseJobByName(job);

            if (loaded != null && loaded.getId() != job.getId())
                throw new EmfException("Case job uniqueness check failed (" + loaded.getId() + "," + job.getId() + ")");

            // maintain current running user from server
            // do NOT update the running user from the client
            job.setRunJobUser(dao.getCaseJob(job.getId()).getRunJobUser());

            dao.updateCaseJob(job);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not update case job: " + job.getName() + ".\n", e);
            throw new EmfException("Could not update case job: " + job.getName() + ".");
        }
    }

    public synchronized Host[] getHosts() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<Host> hosts = dao.getHosts(session);

            return hosts.toArray(new Host[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all hosts.\n" + e.getMessage());
            throw new EmfException("Could not get all hosts.\n");
        } finally {
            session.close();
        }
    }

    public synchronized Host addHost(Host host) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(host, session);
            return (Host) dao.load(Host.class, host.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new host '" + host.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new host '" + host.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized void exportCaseInputs(User user, Integer[] caseInputIds, String purpose) throws EmfException {

        for (Integer caseInputId : caseInputIds) {
            CaseInput caseInput = this.getCaseInput(caseInputId.intValue());
            Case caseObj = this.getCase(caseInput.getCaseID());

            EmfDataset ds = caseInput.getDataset();
            Version version = caseInput.getVersion();
            SubDir subdirObj = caseInput.getSubdirObj();

            String subDir = "";

            if (subdirObj != null) {
                if (subdirObj.getName() != null) {
                    subDir = subdirObj.getName();
                }
            }

            String delimeter = System.getProperty("file.separator");
            String exportDir = caseObj.getInputFileDir() + delimeter + subDir;
            String exportDirExpanded = dao.replaceEnvVarsCase(exportDir, delimeter, caseObj, caseInput.getCaseJobID());
            getExportService().exportForClient(user, new EmfDataset[] { ds }, new Version[] { version },
                    exportDirExpanded, purpose, false);

        }

    }

    public synchronized void exportCaseInputsWithOverwrite(User user, Integer[] caseInputIds, String purpose)
            throws EmfException {
        for (Integer caseInputId : caseInputIds) {
            CaseInput caseInput = this.getCaseInput(caseInputId.intValue());
            Case caseObj = this.getCase(caseInput.getCaseID());

            EmfDataset ds = caseInput.getDataset();
            Version version = caseInput.getVersion();
            SubDir subdirObj = caseInput.getSubdirObj();
            String subDir = "";

            if (subdirObj != null) {
                if (subdirObj.getName() != null) {
                    subDir = subdirObj.getName();
                }
            }

            String delimeter = System.getProperty("file.separator");
            String exportDir = caseObj.getInputFileDir() + delimeter + subDir;
            String exportDirExpanded = dao.replaceEnvVarsCase(exportDir, delimeter, caseObj, caseInput.getCaseJobID());

            getExportService().exportForClient(user, new EmfDataset[] { ds }, new Version[] { version },
                    exportDirExpanded, purpose, true);
        }

    }

    public synchronized void export(User user, String dirName, String purpose, boolean overWrite, int caseId)
            throws EmfException {
        if (DebugLevels.DEBUG_0)
            System.out.println("ManagedCaseService::export for caseId: " + caseId);

        EmfDataset[] datasets = getInputDatasets(caseId);
        Version[] versions = getInputDatasetVersions(caseId);
        SubDir[] subdirs = getSubdirs(caseId);

        if (datasets.length == 0)
            return;

        File inputsDir = new File(dirName);

        if (!inputsDir.exists()) {
            inputsDir.mkdirs();
            inputsDir.setWritable(true, false);
        }

        for (int i = 0; i < datasets.length; i++) {
            String subdir = (subdirs[i] == null) ? "" : subdirs[i].getName();

            String exportDir = dirName + System.getProperty("file.separator") + subdir;
            File dir = new File(exportDir);

            if (!dir.exists()) {
                dir.mkdirs();
                setDirsWritable(new File(dirName), dir);
            }

            getExportService().exportForClient(user, new EmfDataset[] { datasets[i] }, new Version[] { versions[i] },
                    exportDir, purpose, overWrite);

        }
    }

    private CaseParameter[] getJobParameters(int caseId, int jobId, Sector sector, Session session) throws EmfException {
        /**
         * Gets all the parameters for this job, selects based on: case ID, job ID, and sector
         */
        // select the inputs based on 3 criteria
        try {
            List<CaseParameter> parameters = dao.getJobParameters(caseId, jobId, sector, session);
            // return an array of all type CaseParameter
            Collections.sort(parameters);
            return parameters.toArray(new CaseParameter[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all parameters for case (id=" + caseId + "), job (id=" + jobId + ").\n"
                    + e.getMessage());
            throw new EmfException("Could not get all parameters for case (id=" + caseId + "), job (id=" + jobId
                    + ").\n");
        }
    }

    private synchronized List<CaseInput> excludeInputsEnv(List<CaseInput> inputs, String envname) {
        /**
         * Excludes input elements from the inputs list based on environmental variable name.
         * 
         * NOTE: the # of elements of inputs is modified in the calling routine also, if an input has no environmental
         * variable, it will be treated as having the name ""
         */
        List<CaseInput> exclInputs = new ArrayList();
        String inputEnvName = "";

        // loop of inputs (using an iterator) and test for this env name
        Iterator<CaseInput> iter = inputs.iterator();
        while (iter.hasNext()) {
            CaseInput input = iter.next();

            inputEnvName = (input.getEnvtVars() == null) ? "" : input.getEnvtVars().getName();
            // input has an environmental variable w/ this name
            if (inputEnvName.equals(envname)) {
                // add the input to exclude
                exclInputs.add(input);
            }
        }

        // Now remove the excluded elements from inputs
        Iterator<CaseInput> iter2 = exclInputs.iterator();
        while (iter2.hasNext()) {
            CaseInput exclInput = iter2.next();
            // remove this element from the input list
            inputs.remove(exclInput);
        }

        // return the exclude list
        return exclInputs;
    }

    private String setenvInput(CaseInput input, Case caseObj, CaseJob job, ManagedExportService expSvc)
            throws EmfException {
        /**
         * Creates a line of the run job file. Sets the env variable to the value input file.
         * 
         * For eg. If the env variable is GRIDDESC, and the shell type is csh, this will return "setenv GRIDDESC
         * /home/azubrow/smoke/ge_dat/griddesc_12Apr2007_vo.txt"
         * 
         * Will replace any environmental variables in the input director with their value, i.e. will expand the path.
         * 
         * Note: this could be bash or tcsh format, could easily modify for other languages, for example python, perl,
         * etc.
         */

        EmfDataset dataset = input.getDataset();
        InputEnvtVar envvar = input.getEnvtVars();
        SubDir subdir = input.getSubdirObj();
        String fullPath = null;
        String setenvLine = null;
        String delimeter = System.getProperty("file.separator");

        // check if dataset is null, if so exception
        if (dataset == null) {
            throw new EmfException("Input (" + input.getName() + ") must have a dataset");
        }

        // check for external dataset
        if (dataset.isExternal()) {
            // set the full path to the first external file in the dataset
            ExternalSource[] externalDatasets = dataset.getExternalSources();
            if (externalDatasets.length == 0) {
                throw new EmfException("Input (" + input.getName() + ") must have at least 1 external dataset");
            }
            fullPath = externalDatasets[0].getDatasource();
        } else {
            // internal dataset
            // Create a full path to the input file
            fullPath = expSvc.getCleanDatasetName(input.getDataset(), input.getVersion());
            if ((subdir != null) && !(subdir.toString()).equals("")) {
                fullPath = caseObj.getInputFileDir() + delimeter + input.getSubdirObj() + delimeter + fullPath;
            } else {
                fullPath = caseObj.getInputFileDir() + delimeter + fullPath;
            }
        }
        if (envvar == null) {
            // if no environmental variable, just created a commented
            // line w/ input name = fullPath
            setenvLine = this.runComment + " " + input.getName() + " = " + fullPath + eolString;
        } else {
            setenvLine = shellSetenv(envvar.getName(), fullPath);
        }

        // Expand input director, ie. remove env variables
        try {
            setenvLine = dao.replaceEnvVarsCase(setenvLine, delimeter, caseObj, job.getId());
            return setenvLine;
        } catch (Exception e) {
            throw new EmfException("Input folder: " + e.getMessage());
        }
    }

    private String shellSetenv(String envvariable, String envvalue) {
        /**
         * Simply creates a setenv line from an environmental variable and a value.
         * 
         * For eg. If the env variable is IOAPI_GRIDNAME, and the shell type is csh, this will return "setenv GRIDDESC
         * US36_148x112"
         * 
         * Note: this could be bash or tcsh format, could easily modify for other languages, for example python, perl,
         * etc.
         */
        String setenvLine;

        // add quotes to value, if they are not already there
        if (envvalue.indexOf('"') >= 0)
            setenvLine = this.runSet + " " + envvariable + this.runEq + envvalue + this.runTerminator;
        else
            setenvLine = this.runSet + " " + envvariable + this.runEq + addQuotes(envvalue) + this.runTerminator;

        return setenvLine + eolString;

    }

    /**
     * Creates a line of the run job file. Sets the env variable to the value of the parameter.
     * 
     * For eg. If the env variable is IOAPI_GRIDNAME, and the shell type is csh, this will return
     * 
     * "setenv GRIDDESC US36_148x112"
     * 
     * Note: this could be bash or tcsh format, could easily modify for other languages, for example python, perl, etc.
     */
    private String setenvParameter(CaseParameter parameter) throws EmfException {

        if (false)
            throw new EmfException();
        String setenvLine = null;
        if (parameter.getEnvVar() == null) {
            // no environmental variable, therefore create commented line
            // parameter name = value
            setenvLine = this.runComment + " " + parameter.getName() + " = " + parameter.getValue() + eolString;
        } else {
            setenvLine = shellSetenv(parameter.getEnvVar().getName(), parameter.getValue());

        }
        return setenvLine;
    }

    public synchronized String createJobFileContent(CaseJob job, User user, String jobFileName,
            ManagedExportService expSvc, Session session) throws EmfException {
        // String jobContent="";
        String jobFileHeader = "";

        StringBuffer sbuf = new StringBuffer();

        /**
         * Creates the content string for a job run file w/ all necessary inputs and parameters set.
         * 
         * Input: job - the Case Job user - the user
         * 
         * Output: String - the job content
         */

        // Some objects needed for accessing data
        Case caseObj = this.getCase(job.getCaseId(), session);
        int caseId = job.getCaseId();
        int jobId = job.getId();

        CaseInput headerInput = null; // input for the EMF Job header

        List<CaseInput> inputsAA = null; // inputs for all sectors and all jobs
        List<CaseInput> inputsSA = null; // inputs for specific sector and all jobs
        List<CaseInput> inputsAJ = null; // inputs for all sectors specific jobs
        List<CaseInput> inputsSJ = null; // inputs for specific sectors specific jobs

        // Make sure no spaces or strange characters in the job name
        String jobName = job.getName().replace(" ", "_");
        jobName = replaceNonDigitNonLetterChars(jobName);
        /*
         * Get the inputs in the following order: all sectors, all jobs sector specific, all jobs all sectors, job
         * specific sector specific, job specific
         * 
         * Need to search for inputs now, b/c want to see if there is an EMF_HEADER
         */

        // Create an export service to get names of the datasets as inputs to Smoke script
        // ExportService exports = new ExportService(dbServerlocal, this.threadPool, this.sessionFactory);
        // Get case inputs (the datasets associated w/ the case)
        // All sectors, all jobs
        inputsAA = this.getJobInputs(caseId, this.ALL_JOB_ID, this.ALL_SECTORS, session);

        // Exclude any inputs w/ environmental variable EMF_JOBHEADER
        List<CaseInput> exclInputs = this.excludeInputsEnv(inputsAA, "EMF_JOBHEADER");

        if (exclInputs.size() != 0) {
            headerInput = exclInputs.get(0); // get the first element as header
        }

        // Sector specific, all jobs
        Sector sector = job.getSector();
        if (sector != this.ALL_SECTORS) {
            inputsSA = this.getJobInputs(caseId, this.ALL_JOB_ID, sector, session);

            // Exclude any inputs w/ environmental variable EMF_JOBHEADER
            exclInputs = this.excludeInputsEnv(inputsSA, "EMF_JOBHEADER");
            if (exclInputs.size() != 0) {
                headerInput = exclInputs.get(0); // get the first element as header
            }
        }

        // All sectors, job specific
        inputsAJ = this.getJobInputs(caseId, jobId, this.ALL_SECTORS, session);

        // Exclude any inputs w/ environmental variable EMF_JOBHEADER
        exclInputs = this.excludeInputsEnv(inputsAJ, "EMF_JOBHEADER");
        if (exclInputs.size() != 0) {
            headerInput = exclInputs.get(0); // get the first element as header
        }

        // Specific sector and specific job
        if (sector != this.ALL_SECTORS) {
            inputsSJ = this.getJobInputs(caseId, jobId, sector, session);

            // Exclude any inputs w/ environmental variable EMF_JOBHEADER
            exclInputs = this.excludeInputsEnv(inputsSJ, "EMF_JOBHEADER");
            if (exclInputs.size() != 0) {
                headerInput = exclInputs.get(0); // get the first element as header
            }
        }

        // Get header String:
        if (headerInput != null) {
            // Header string starts the job file content string

            try {
                // get the string of the EMF_JOBHEADER from the input
                jobFileHeader = getJobFileHeader(headerInput);
                sbuf.append(jobFileHeader);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Could not write EMF header to job script file, " + e.getMessage());
                throw new EmfException("Could not write EMF header to job script file");
            }

        } else {
            // Start the job file content string and append the end of line characters for this OS
            sbuf.append(this.runShell + this.eolString);
        }

        // print job name to file
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Job run file for job: " + jobName + eolString);

        /*
         * Define some EMF specific variables
         */
        sbuf.append(eolString);

        sbuf.append(this.runComment + " EMF specific variables" + eolString);
        sbuf.append(shellSetenv("EMF_JOBID", String.valueOf(jobId)));
        sbuf.append(shellSetenv("EMF_JOBNAME", jobName));
        sbuf.append(shellSetenv("EMF_USER", user.getUsername()));

        // name of this job script and script directory

        // Expand output director, ie. remove env variables
        String delimeter = System.getProperty("file.separator");
        String outputFileDir = caseObj.getOutputFileDir();
        if (outputFileDir == null || (outputFileDir.length() == 0))
            throw new EmfException("The Output Job Scripts folder has not been specified");
        try {
            String outputDirExpanded = dao.replaceEnvVarsCase(outputFileDir, delimeter, caseObj, job.getId());
            sbuf.append(shellSetenv("EMF_SCRIPTDIR", outputDirExpanded));
        } catch (Exception e) {
            throw new EmfException("Output folder: " + e.getMessage());
        }
        sbuf.append(shellSetenv("EMF_SCRIPTNAME", jobFileName));

        // Generate and get a unique job key, add it to the job,
        // update the db, and write it to the script
        String jobKey = createJobKey(jobId);
        job.setJobkey(jobKey);
        updateCaseJob(user, job);
        sbuf.append(shellSetenv("EMF_JOBKEY", job.getJobkey()));

        // Print the inputs to the file

        /*
         * loop over inputs and write Env variables and input (full name and path) to job run file, print comments
         */
        // All sectors and all jobs
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Inputs -- for all sectors and all jobs" + eolString);
        if (inputsAA != null) {
            for (CaseInput input : inputsAA) {
                sbuf.append(setenvInput(input, caseObj, job, expSvc));
            }
        }

        // Sector specific and all jobs
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Inputs -- sector (" + sector + ") and all jobs" + eolString);
        if (inputsSA != null) {
            for (CaseInput input : inputsSA) {
                sbuf.append(setenvInput(input, caseObj, job, expSvc));
            }
        }
        // All sectors and specific job
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Inputs -- for all sectors and job: " + job + eolString);
        if (inputsAJ != null) {
            for (CaseInput input : inputsAJ) {
                sbuf.append(setenvInput(input, caseObj, job, expSvc));
            }
        }
        // Sector and Job specific
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Inputs -- sector (" + sector + ") and job: " + job + eolString);
        if (inputsSJ != null) {
            for (CaseInput input : inputsSJ) {
                sbuf.append(setenvInput(input, caseObj, job, expSvc));
            }
        }
        /*
         * Get the parameters for this job in following order: from summary tab, all sectors, all jobs sector specific,
         * all jobs all sectors, job specific sector specific, job specific
         */

        // Parameters from the summary tab
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Parameters -- from Case summary " + eolString);
        if (caseObj.getAbbreviation() != null) {
            sbuf.append(shellSetenv("CASE", caseObj.getAbbreviation().getName()));
        }
        // Need to have quotes around model name b/c could be more than one word, include version if available
        if (caseObj.getModel() != null) {
            String modelName = caseObj.getModel().getName();
            if (caseObj.getModelVersion() != null) {
                modelName += caseObj.getModelVersion();
            }
            sbuf.append(shellSetenv("MODEL_LABEL", modelName));
        }
        if (caseObj.getGrid() != null) {
            sbuf.append(shellSetenv("IOAPI_GRIDNAME_1", caseObj.getGrid().getName()));
        }
        if (caseObj.getGridResolution() != null) {
            sbuf.append(shellSetenv("EMF_GRID", caseObj.getGridResolution().getName()));
        }
        if (caseObj.getAirQualityModel() != null) {
            sbuf.append(shellSetenv("EMF_AQM", caseObj.getAirQualityModel().getName()));
        }
        if (caseObj.getSpeciation() != null) {
            sbuf.append(shellSetenv("EMF_SPC", caseObj.getSpeciation().getName()));
        }
        if (caseObj.getEmissionsYear() != null) {
            sbuf.append(shellSetenv("BASE_YEAR", caseObj.getEmissionsYear().getName())); // Should base year ==
            // emissions year ????
        }
        // sbuf.append(shellSetenv("BASE_YEAR", String.valueOf(caseObj.getBaseYear())));
        if (caseObj.getFutureYear() != 0) { // CHECK: should it be included if == 0 ???
            sbuf.append(shellSetenv("FUTURE_YEAR", String.valueOf(caseObj.getFutureYear())));
        }
        // Need to have quotes around start and end date b/c could be more than one word 'DD/MM/YYYY HH:MM'
        if (caseObj.getStartDate() != null) {
            String startString = caseObj.getStartDate().toString();
            sbuf.append(shellSetenv("EPI_STDATE_TIME", startString));
        }
        if (caseObj.getEndDate() != null) {
            String endString = caseObj.getEndDate().toString();
            sbuf.append(shellSetenv("EPI_ENDATE_TIME", endString));
        }
        // Parent case
        String parentName = caseObj.getTemplateUsed();
        if (parentName != null && parentName != "") {
            try {
                Case parentCase = dao.getCaseFromName(parentName, session);
                sbuf.append(shellSetenv("PARENT_CASE", parentCase.getAbbreviation().getName()));
            } catch (Exception e) {
                log.error("Parent case (" + parentName + ") does not exist. Will not set PARENT_CASE parameter");
                log.error(e.getMessage());
            }
        }

        // Add Parameters from job tab
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Parameters -- from job tab" + eolString);
        if (job.getSector() != null) {
            sbuf.append(shellSetenv("SECTOR", job.getSector().getName()));
        }
        if (job.getJobGroup() != null && !job.getJobGroup().isEmpty()) {
            sbuf.append(shellSetenv("JOB_GROUP", job.getJobGroup()));
        }

        // All sectors, all jobs
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Parameters -- all sectors, all jobs " + eolString);
        CaseParameter[] parameters = this.getJobParameters(caseId, this.ALL_JOB_ID, this.ALL_SECTORS, session);
        if (parameters != null) {
            for (CaseParameter param : parameters) {
                sbuf.append(setenvParameter(param));
            }

        }

        // Specific sector, all jobs
        if (sector != this.ALL_SECTORS) {
            sbuf.append(eolString);
            sbuf.append(this.runComment + " Parameters -- sectors (" + sector + "), all jobs " + eolString);
            parameters = this.getJobParameters(caseId, this.ALL_JOB_ID, sector, session);
            if (parameters != null) {
                for (CaseParameter param : parameters) {
                    sbuf.append(setenvParameter(param));
                }
            }
        }
        // All sectors, specific job
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Parameters -- all sectors, job: " + job + eolString);
        parameters = this.getJobParameters(caseId, jobId, this.ALL_SECTORS, session);
        if (parameters != null) {
            for (CaseParameter param : parameters) {
                sbuf.append(setenvParameter(param));
            }
        }
        // Specific sector, specific job
        if (sector != this.ALL_SECTORS) {
            sbuf.append(eolString);
            sbuf.append(this.runComment + " Parameters -- sectors (" + sector + "), job: " + job + eolString);
            parameters = this.getJobParameters(caseId, jobId, sector, session);
            if (parameters != null) {
                for (CaseParameter param : parameters) {
                    sbuf.append(setenvParameter(param));
                }
            }
        }
        // Getting the executable object from the job
        Executable execVal = job.getExecutable();

        // path to executable
        String execPath = job.getPath();

        // executable string
        String execName = execVal.getName();

        // executable full name and arguments
        String execFull = execPath + System.getProperty("file.separator") + execName;
        String execFullArgs = execFull + " " + job.getArgs() + eolString;
        // print executable
        sbuf.append(eolString);
        sbuf.append(this.runComment + " job executable" + eolString);
        sbuf.append("$EMF_CLIENT -k $EMF_JOBKEY -x " + execFull + " -m \"Running top level script for job: " + jobName
                + "\"" + eolString);
        sbuf.append(execFullArgs);

        // add a test of the status and send info through the
        // command client -- should generalize so not csh specific
        sbuf.append("if ( $status != 0 ) then" + eolString);
        sbuf.append("\t $EMF_CLIENT -k $EMF_JOBKEY -s 'Failed' -m \"ERROR running Job: $EMF_JOBNAME\" -t 'e' "
                + eolString);
        sbuf.append("\t exit(1)" + eolString);
        sbuf.append("else" + eolString);
        sbuf.append("\t $EMF_CLIENT -k $EMF_JOBKEY -s 'Completed' -m \"Completed job: $EMF_JOBNAME\"" + eolString);
        sbuf.append("endif" + eolString);

        // Send back the contents of jobContent string for job file
        return sbuf.toString();

    }// /end of createJobFileContent()

    private String addQuotes(String evName) {
        return '"' + evName + '"';
    }

    private String getJobFileHeader(CaseInput headerInput) throws EmfException {
        /**
         * gets the EMF JOBHEADER as a string
         */

        // get some info from the header input
        EmfDataset dataset = headerInput.getDataset();
        Version version = headerInput.getVersion();

        // create an exporter to get the string
        DbServer dbServer = this.dbFactory.getDbServer();
        GenericExporterToString exporter = new GenericExporterToString(dataset, dbServer, dbServer.getSqlDataTypes(),
                new VersionedDataFormatFactory(version, dataset), null);

        // Get the string from the exporter
        try {
            exporter.export(null);
            String fileHeader = exporter.getOutputString();
            return fileHeader;

        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            try {
                if (dbServer != null && dbServer.isConnected())
                    dbServer.disconnect();
            } catch (Exception e) {
                throw new EmfException("ManagedCaseService: error closing db server. " + e.getMessage());
            }
        }

    }

    private String getJobFileName(CaseJob job, Session session) throws EmfException {
        /*
         * Creates the File name that corresponds to the job script file name.
         * 
         * Format: <jobname>_<case_abbrev>_<datestamp>.csh
         */
        String dateStamp = CustomDateFormat.format_YYYYMMDDHHMMSS(new Date());
        String jobName = job.getName().replace(" ", "_");
        Case caseObj = this.getCase(job.getCaseId(), session);

        // Get case abbreviation, if no case abbreviation construct one from id
        String defaultAbbrev = "case" + job.getCaseId();
        String caseAbbrev = (caseObj.getAbbreviation() == null) ? defaultAbbrev : caseObj.getAbbreviation().getName();

        // Expand output director, ie. remove env variables
        String delimeter = System.getProperty("file.separator");
        String outputFileDir = caseObj.getOutputFileDir();
        try {
            String outputDirExpanded = dao.replaceEnvVarsCase(outputFileDir, delimeter, caseObj, job.getId());

            // Test output directory to place job script
            if ((outputDirExpanded == null) || (outputDirExpanded.equals(""))) {
                throw new EmfException(
                        "The output job scripts directory must be set on the Jobs tab prior to running any jobs");
            }

            String fileName = replaceNonDigitNonLetterChars(jobName + "_" + caseAbbrev + "_" + dateStamp
                    + this.runSuffix);
            fileName = outputDirExpanded + delimeter + fileName;
            return fileName;
        } catch (Exception e) {
            throw new EmfException("Output folder: " + e.getMessage());
        }
    }

    private String getLog(String jobFileName) throws EmfException {
        /*
         * From the job script name, it creates the name of corresponding log file. It also checks that an appropriate
         * log directory exists.
         */

        File file = new File(jobFileName);
        String fileShort = file.getName(); // file w/o path
        // Create a log file name by replacing the suffix of the job
        // of the job file w/ .log
        String logFileName = fileShort.replaceFirst(this.runSuffix, ".log");

        // Check if logs dir (jobpath/logs) exists, if not create
        File logDir = new File(file.getParent() + System.getProperty("file.separator") + "logs");

        if (!(logDir.isDirectory())) {
            // Need to create the directory
            if (!(logDir.mkdirs())) {
                throw new EmfException("Error creating job log directory: " + logDir);
            }

            // Make directory writable by everyone
            if (!logDir.setWritable(true, false)) {
                throw new EmfException("Error changing job log directory's write permissions: " + logDir);
            }
        }
        logDir.getParentFile().setWritable(true, false);

        // Create the logFile full name
        logFileName = logDir + System.getProperty("file.separator") + logFileName;

        return logFileName;

    }

    // for command line client
    public synchronized int recordJobMessage(JobMessage message, String jobKey) throws EmfException {
        try {
            CaseJob job = getJobFromKey(jobKey);

            User user = job.getUser();
            message.setCaseId(job.getCaseId());
            message.setJobId(job.getId());
            String status = message.getStatus();
            String jobStatus = job.getRunstatus().getName();
            String lastMsg = message.getMessage();

            if (lastMsg != null && !lastMsg.trim().isEmpty())
                job.setRunLog(lastMsg);

            if (!status.isEmpty() && !jobStatus.equalsIgnoreCase(status)) {
                job.setRunstatus(dao.getJobRunStatuse(status));

                if (!(status.equalsIgnoreCase("Running"))) {
                    // status is Completed or Failed - set completion date
                    job.setRunCompletionDate(new Date());

                } else {
                    // status is running - set running date
                    job.setRunStartDate(new Date());
                }

            }

            dao.updateCaseJob(job);

            if (!user.getUsername().equalsIgnoreCase(message.getRemoteUser()))
                throw new EmfException(
                        "Error recording job messages: Remote user doesn't match the user who runs the job.");

            dao.add(message);

            if (!status.isEmpty() && !jobStatus.equalsIgnoreCase(status)) {
                if (!(status.equalsIgnoreCase("Running"))) {
                    // Notify CaseJobTaskManager that the job status has changed to Completed or Failed
                    TaskManagerFactory.getCaseJobTaskManager(sessionFactory).callBackFromJobRunServer();
                }

            }

            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new EmfException("Error recording job messages: " + e.getMessage());
        }
    }

    private CaseJob getJobFromKey(String jobKey) throws EmfException {
        CaseJob job = dao.getCaseJob(jobKey);

        if (job == null)
            throw new EmfException("Error recording job messages: No jobs found associated with job key: " + jobKey);
        return job;
    }

    public synchronized JobMessage[] getJobMessages(int caseId, int jobId) throws EmfException {
        Session session = sessionFactory.getSession();
        List<JobMessage> msgs = null;

        try {
            if (jobId == 0)
                msgs = dao.getJobMessages(caseId, session);
            else
                msgs = dao.getJobMessages(caseId, jobId, session);

            return msgs.toArray(new JobMessage[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized String[] getAllValidJobs(int jobId, int caseId) throws EmfException {
        try {
            return dao.getAllValidJobs(jobId, caseId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all valid jobs for job (id=" + jobId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all valid jobs for job (id=" + jobId + ").\n");
        }
    }

    public synchronized String[] getDependentJobs(int jobId) throws EmfException {
        try {
            return dao.getDependentJobs(jobId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all dependent jobs for job (id=" + jobId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all dependent jobs for job (id=" + jobId + ").\n");
        }
    }

    public synchronized int[] getJobIds(int caseId, String[] jobNames) throws EmfException {
        try {
            return dao.getJobIds(caseId, jobNames);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all job ids for job (" + jobNames[0] + ", etc.).\n" + e.getMessage());
            throw new EmfException("Could not get all job ids for job (" + jobNames[0] + ", etc.).\n");
        }
    }

    public synchronized void finalize() throws Throwable {
        // this.session = null;
        super.finalize();
    }

    public synchronized String restoreTaskManagers() throws EmfException {
        if (DebugLevels.DEBUG_9)
            System.out.println("ManagedCaseService::restoreTaskManagers");

        String mesg;
        List distinctUserIds = null;

        try {
            ArrayList userIds = (ArrayList) dao.getDistinctUsersOfPersistedWaitTasks();
            mesg = "Total number of persisted wait tasks retored: " + userIds.size();
            distinctUserIds = getDistinctUserIds(userIds);

            Iterator iter = distinctUserIds.iterator();
            while (iter.hasNext()) {

                // get the user id
                int uid = ((IntegerHolder) iter.next()).getUserId();

                // acquire all the CaseJobTasks for this uid
                // List allCJTsForUser = getCaseJobTasksForUser(uid);

                resubmitPersistedTasksForUser(uid);

            }

            return mesg;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EmfException("System problems: Database access failed");
        }

    }

    private synchronized void resubmitPersistedTasksForUser(int uid) throws EmfException {
        if (DebugLevels.DEBUG_9)
            System.out.println("Start ManagedCaseService::resubmitPersistedTasksForUser uid= " + uid);
        Integer[] jobIds = null;
        int caseId = -9;
        User user = getUser(uid);

        if (DebugLevels.DEBUG_9)
            System.out.println("Incoming userid= " + uid + " acquired userName= " + user.getName());

        // Get All persisted wait jobs for this user
        List allPersistedTasks = getPersistedTasksForUser(uid);
        if (allPersistedTasks == null) {
            if (DebugLevels.DEBUG_9)
                System.out.println("allPersistedTasks is null WHY?");

        } else {
            if (DebugLevels.DEBUG_9)
                System.out.println("Size of list returned from Persist wait table= " + allPersistedTasks.size());
            jobIds = new Integer[allPersistedTasks.size()];

            for (int i = 0; i < allPersistedTasks.size(); i++) {
                PersistedWaitTask pwTask = (PersistedWaitTask) allPersistedTasks.get(i);
                if (caseId == -9) {
                    caseId = pwTask.getCaseId();
                }
                jobIds[i] = new Integer(pwTask.getJobId());

                // Task has been acquired so delete from persisted wait task table
                dao.removePersistedTasks(pwTask);
            }

            if (DebugLevels.DEBUG_9)
                System.out.println("After the loop jobId array of ints size= " + jobIds.length);
            if (DebugLevels.DEBUG_9)
                System.out.println("After the loop CaseId= " + caseId);

        }

        if (allGood(user, jobIds, caseId)) {
            if (DebugLevels.DEBUG_9)
                System.out.println("ManagedCaseService::resubmitPersistedTasksForUser Everything is good so resubmit");
            this.submitJobs(jobIds, caseId, user);
        } else {
            throw new EmfException("Failed to restore persisted wait tasks for user= " + user.getName());
        }

        if (DebugLevels.DEBUG_9)
            System.out.println("End ManagedCaseService::resubmitPersistedTasksForUser uid= " + uid);
    }

    private boolean allGood(User user, Integer[] jobIds, int caseId) {
        boolean allGewd = false;

        if ((caseId != -9) && (user != null) && (jobIds != null) && (jobIds.length > 0)) {
            allGewd = true;
        }
        if (DebugLevels.DEBUG_9)
            System.out.println("END ManagedCaseService::allGood status= " + allGewd);

        return allGewd;
    }

    private User getUser(int uid) throws EmfException {
        Session session = this.sessionFactory.getSession();
        User user = null;
        try {
            UserDAO userDAO = new UserDAO();
            user = userDAO.get(uid, session);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EmfException("System Problems: Database access error");

        } finally {
            session.clear();
            session.close();
        }

        return user;
    }

    private List getPersistedTasksForUser(int uid) throws EmfException {
        Session session = this.sessionFactory.getSession();
        List allPersistTasks = null;

        try {
            allPersistTasks = dao.getPersistedWaitTasksByUser(uid);
            String statMsg;

            if (allPersistTasks != null) {
                statMsg = allPersistTasks.size() + " elements List returned of persisted wait tasks for user= " + uid;
            } else {
                statMsg = "Empty list of elements for user= " + uid;

            }

            if (DebugLevels.DEBUG_9)
                System.out.println(statMsg);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EmfException("System Problems: Database access error");

        } finally {
            session.clear();
            session.close();
        }

        return allPersistTasks;
    }

    private ArrayList getDistinctUserIds(List userIds) {
        ArrayList distUid = new ArrayList();

        Iterator iter = userIds.iterator();
        while (iter.hasNext()) {
            IntegerHolder intHold = (IntegerHolder) iter.next();
            intHold.setId(0);
            if (!distUid.contains(intHold)) {
                distUid.add(intHold);
            }
        }

        return distUid;
    }

    public synchronized String printStatusCaseJobTaskManager() throws EmfException {
        return TaskManagerFactory.getCaseJobTaskManager(sessionFactory).getStatusOfWaitAndRunTable();
    }

    public synchronized String validateJobs(Integer[] jobIDs) throws EmfException {
        if (DebugLevels.DEBUG_14)
            System.out.println("Start validating jobs on server side. " + new Date());

        String ls = System.getProperty("line.separator");
        Session session = sessionFactory.getSession();

        List<CaseInput> allInputs = new ArrayList<CaseInput>();

        try {
            for (Integer id : jobIDs) {
                CaseJob job = this.getCaseJob(id.intValue(), session);
                allInputs.addAll(this.getAllJobInputs(job, session));
            }
        } catch (RuntimeException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        } finally {
            session.close();
        }

        if (DebugLevels.DEBUG_14)
            System.out.println("Finished validating jobs on server side. " + new Date());

        TreeSet<CaseInput> set = new TreeSet<CaseInput>(allInputs);
        List<CaseInput> uniqueInputs = new ArrayList<CaseInput>(set);

        String finalVersionMsg = listNonFinalInputs(uniqueInputs);
        String laterVersionMsg = listInputsMsg(uniqueInputs);
        String returnMsg = "";

        if (finalVersionMsg == null || finalVersionMsg.isEmpty())
            returnMsg = laterVersionMsg;
        else
            returnMsg = border(100, "*") + ls + finalVersionMsg + ls + border(100, "*") + ls + laterVersionMsg
                    + border(100, "*");

        return returnMsg;
    }

    private String listNonFinalInputs(List<CaseInput> inputs) {
        String inputsList = "";
        String ls = System.getProperty("line.separator");

        if (DebugLevels.DEBUG_14)
            System.out.println("Start listing non-final inputs. " + new Date());

        for (Iterator<CaseInput> iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = iter.next();
            String dataset = input.getDataset().getName();
            Version version = input.getVersion();

            if (!version.isFinalVersion())
                inputsList += "Input: " + input.getName() + ";  Dataset: " + dataset + ls;
        }

        if (DebugLevels.DEBUG_14)
            System.out.println("Finished listing non-final inputs. " + new Date());

        if (inputsList.isEmpty())
            return inputsList;

        return "The selected jobs have non-final dataset versions:" + ls + ls + inputsList;
    }

    private String listInputsMsg(List<CaseInput> inputs) throws EmfException {
        StringBuffer inputsList = new StringBuffer("");

        // NOTE: if change headers msg here, please also change the client side
        String header = "Inputs using datasets that have later versions available: ";
        String header2 = "No new versions exist for selected inputs.";
        boolean laterVersionExists = false;

        for (Iterator<CaseInput> iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = iter.next();
            String inputName = input.getName();
            EmfDataset dataset = input.getDataset();

            if (dataset == null)
                inputsList.append("Input: " + inputName + " has no dataset\n");
            else {
                String datasetName = input.getDataset().getName();
                Version version = input.getVersion();

                if (version == null) {
                    inputsList.append("Input: " + inputName + " hasn't set dataset version\n");
                    continue;
                }

                Version[] laterVersions = getLaterVersions(dataset, version);

                if (laterVersions.length > 0)
                    inputsList.append("Input name: " + inputName + ";    " + "Dataset name: " + datasetName + "\n");

                for (Version ver : laterVersions) {
                    inputsList.append("    Version " + ver.getVersion() + ":   " + ver.getName() + ", "
                            + (dataset.getCreator() == null ? "" : dataset.getCreator()) + ", "
                            + (ver.isFinalVersion() ? "Final" : "Non-final") + "\n");
                    laterVersionExists = true;
                }
            }
        }

        return (laterVersionExists) ? header + "\n\n" + inputsList.toString() : header2 + "\n\n"
                + inputsList.toString();
    }

    private String border(int num, String symbl) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < num; i++)
            sb.append(symbl);

        return sb.toString();
    }

    public synchronized CaseOutput[] getCaseOutputs(int caseId, int jobId) throws EmfException {
        Session session = sessionFactory.getSession();
        List<CaseOutput> outputs = null;

        try {
            if (jobId == 0)
                outputs = dao.getCaseOutputs(caseId, session);
            else
                outputs = dao.getCaseOutputs(caseId, jobId, session);

            return outputs.toArray(new CaseOutput[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized void registerOutputs(CaseOutput[] outputs, String[] jobKeys) throws EmfException {
        try {
            CaseJob job = null;

            for (int i = 0; i < outputs.length; i++) {
                job = getJobFromKey(jobKeys[i]);
                outputs[i].setCaseId(job.getCaseId());
                outputs[i].setJobId(job.getId());
            }

            getImportService().importDatasetsForCaseOutput(job.getUser(), outputs);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().startsWith("Error registering output"))
                throw new EmfException(e.getMessage());
            throw new EmfException("Error registering output: " + e.getMessage());
        }
    }

    public synchronized void removeCaseOutputs(User user, CaseOutput[] outputs, boolean deleteDataset)
            throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.removeCaseOutputs(user, outputs, deleteDataset, session);
        } catch (Exception e) {
            e.printStackTrace();
            // log.error("Could not remove case output " + outputs[0].getName() + " etc.\n" + e.getMessage());
            // throw new EmfException("Could not remove case output " + outputs[0].getName() + " etc.");
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized void updateCaseOutput(User user, CaseOutput output) throws EmfException {
        Session localSession = sessionFactory.getSession();

        try {
            checkNExtendCaseLock(user, getCase(output.getCaseId(), localSession));
        } catch (EmfException e) {
            throw e;
        }

        try {
            CaseOutput loaded = (CaseOutput) dao.loadCaseOutput(output, localSession);

            if (loaded != null && loaded.getId() != output.getId())
                throw new EmfException("Case output uniqueness check failed (" + loaded.getId() + "," + output.getId()
                        + ")");

            // Clear the cached information. To update a case
            // FIXME: Verify the session.clear()
            localSession.clear();
            dao.updateCaseOutput(output, localSession);
            // setStatus(user, "Saved input " + input.getName() + " to database.", "Save Input");
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not update case output: " + output.getName() + ".\n" + e);
            throw new EmfException("Could not update case output: " + output.getName() + ".");
        } finally {
            localSession.close();
        }

    }

    public synchronized void removeMessages(User user, JobMessage[] msgs) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.removeJobMessages(msgs, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not remove case output " + msgs[0].getMessage() + " etc.\n" + e.getMessage());
            throw new EmfException("Could not remove case messages " + msgs[0].getMessage());
        } finally {
            session.close();
        }

    }

    public synchronized CaseOutput addCaseOutput(User user, CaseOutput output) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            checkNExtendCaseLock(user, getCase(output.getCaseId(), session));
        } catch (EmfException e) {
            throw e;
        }

        try {
            if (dao.caseOutputExists(output, session))
                throw new EmfException("The combination of 'Output Name'and 'Job' should be unique.");

            dao.add(output, session);
            return (CaseOutput) dao.loadCaseOutput(output, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new case output '" + output.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new case output '" + output.getName() + "'");
        } finally {
            session.close();
        }
    }

    private synchronized void checkNExtendCaseLock(User user, Case currentCase) throws EmfException {
        Case locked = obtainLocked(user, currentCase);

        if (!locked.isLocked(user))
            throw new EmfException("Lock on the current case object expired. User " + locked.getLockOwner()
                    + " has it now.");
    }

    private boolean doNotExportJobs(Session session) {
        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("DO_NOT_EXPORT_JOBS", session);
            String value = property.getValue();
            return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes");
        } catch (Exception e) {
            return false;// Default value for maxpool and poolsize
        }
    }

    private void logNumDBConn(String prefix) throws EmfException {
        String logNumDBConnCmd = "ps aux | grep postgres | wc -l";
        InputStream inStream = RemoteCommand.executeLocal(logNumDBConnCmd);

        RemoteCommand.logStdout("Logged DB connections: " + prefix, inStream);
    }

    public synchronized AirQualityModel addAirQualityModel(AirQualityModel airQModel) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            AirQualityModel temp = (AirQualityModel) dao.load(AirQualityModel.class, airQModel.getName(), session);

            if (temp != null)
                return temp;
            dao.add(airQModel, session);
            return (AirQualityModel) dao.load(AirQualityModel.class, airQModel.getName(), session);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new AirQualityModel '" + airQModel.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new AirQualityModel '" + airQModel.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized EmissionsYear addEmissionYear(EmissionsYear emissYear) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(emissYear, session);
            return (EmissionsYear) dao.load(EmissionsYear.class, emissYear.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new EmissionsYear '" + emissYear.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new EmissionsYear '" + emissYear.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized Grid addGrid(Grid grid) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Grid temp = (Grid) dao.load(Grid.class, grid.getName(), session);

            if (temp != null)
                return temp;

            dao.add(grid, session);
            return (Grid) dao.load(Grid.class, grid.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new Grid '" + grid.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new Grid '" + grid.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized MeteorlogicalYear addMeteorologicalYear(MeteorlogicalYear metYear) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(metYear, session);
            return (MeteorlogicalYear) dao.load(MeteorlogicalYear.class, metYear.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new MeteorlogicalYear '" + metYear.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new MeteorlogicalYear '" + metYear.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized Speciation addSpeciation(Speciation speciation) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Speciation temp = (Speciation) dao.load(Speciation.class, speciation.getName(), session);

            if (temp != null)
                return temp;

            dao.add(speciation, session);
            return (Speciation) dao.load(Speciation.class, speciation.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new speciation '" + speciation.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new speciation '" + speciation.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized String getJobStatusMessage(int caseId) {
        Session session = sessionFactory.getSession();

        int failedCount = 0;
        int waitingCount = 0;
        int runningCount = 0;

        try {
            CaseJob[] jobs = getCaseJobs(caseId);

            if (jobs.length == 0)
                return "";

            for (CaseJob job : jobs) {
                JobRunStatus status = job.getRunstatus();

                if (status == null)
                    continue;

                if (status.getName().equalsIgnoreCase("Failed"))
                    failedCount++;

                if (status.getName().equalsIgnoreCase("Waiting"))
                    waitingCount++;

                if (status.getName().equalsIgnoreCase("Running"))
                    runningCount++;
            }

            if (failedCount == 0 && waitingCount == 0 && runningCount == 0)
                return "";

            return "Current case has " + runningCount + " running, " + waitingCount + " waiting, and " + failedCount
                    + " failed jobs.";
        } catch (Exception e) {
            return "";
        } finally {
            session.close();
        }
    }

    private String replaceNonDigitNonLetterChars(String name) {
        String filename = name.trim();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < filename.length(); i++) {
            if (filename.charAt(i) == '.')
                sb.append(".");
            else if (!Character.isLetterOrDigit(filename.charAt(i)))
                sb.append("_");
            else
                sb.append(filename.charAt(i));
        }

        return sb.toString();
    }

    public synchronized String[] getAllCaseNameIDs() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getAllCaseNameIDs(session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not retrieve all case names and ids.", e);
            throw new EmfException("Could not retrieve all case names and ids. " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized Case addSensitivity2Case(User user, int parentCaseId, int templateCaseId, int[] jobIds,
            String jobGroup, Case sensitivityCase) throws EmfException {
        Session session = sessionFactory.getSession();
        Case lockedSC = null;
        Case lockedPC = null;
        Case lockedTC = null;
        String jobPrefix = (jobGroup == null || jobGroup.trim().isEmpty()) ? "" : jobGroup + " ";

        try {
            lockedSC = dao.obtainLocked(user, sensitivityCase, session);
            int targetId = sensitivityCase.getId();

            Case parent = dao.getCase(parentCaseId, session);
            lockedPC = dao.obtainLocked(user, parent, session);

            Case template = dao.getCase(templateCaseId, session);
            lockedTC = dao.obtainLocked(user, template, session);

            if (lockedSC == null)
                throw new EmfException("Cannot obtain lock for adding sensitivity to happen: User "
                        + sensitivityCase.getLockOwner() + " has the lock for case '" + sensitivityCase.getName() + "'");

            if (lockedPC == null)
                throw new EmfException("Cannot obtain lock for adding sensitivity to happen: User "
                        + parent.getLockOwner() + " has the lock for case '" + parent.getName() + "'");

            if (lockedTC == null)
                throw new EmfException("Cannot obtain lock for adding sensitivity to happen: User "
                        + template.getLockOwner() + " has the lock for case '" + template.getName() + "'");

            List<CaseJob> existingJobs = dao.getCaseJobs(targetId, session);
            List<CaseInput> existingInputs = dao.getCaseInputs(targetId, session);
            List<CaseParameter> existingParams = dao.getCaseParameters(targetId, session);
            CaseJob[] jobs2copy = getJobs2Copy(jobIds);

            checkJobsDuplication(existingJobs, jobs2copy, jobPrefix);

            CaseJob[] jobs = cloneCaseJobs(lockedSC.getId(), lockedTC.getId(), jobGroup, jobPrefix, jobs2copy, user);
            CaseInput[] inputs = cloneCaseInputs(parentCaseId, lockedSC.getId(), getValidCaseInputs4SensitivityCase(
                    template.getId(), jobIds, jobs2copy, session), session);
            CaseParameter[] params = cloneCaseParameters(parentCaseId, lockedSC.getId(),
                    getValidCaseParameters4SensitivityCase(template.getId(), jobIds, jobs2copy, session), session);

            addCaseJobs4Sensitivity(user, targetId, jobs);
            addCaseInputs(user, targetId, removeRedundantInputs(inputs, existingInputs, jobPrefix), jobPrefix);
            addCaseParameters(user, targetId, removeRedundantParams(params, existingParams, jobPrefix), jobPrefix);

            // NOTE: add sectors to sensitivity case from the selected jobs
            existingJobs.addAll(Arrays.asList(jobs));
            lockedSC.setSectors(getSectorsFromJobs(existingJobs.toArray(new CaseJob[0])));

            updateCase(lockedSC);

            return lockedSC;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add sensitivity to sensitivity case.", e);
            throw new EmfException(e.getMessage());
        } finally {
            try {
                if (lockedSC != null)
                    dao.releaseLocked(user, lockedSC, session);

                session.clear();
                if (lockedPC != null)
                    dao.releaseLocked(user, lockedPC, session);

                session.clear();
                if (lockedTC != null)
                    dao.releaseLocked(user, lockedTC, session);

                session.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkJobsDuplication(List<CaseJob> existingJobs, CaseJob[] jobs2copy, String jobPrefix)
            throws EmfException {
        for (Iterator<CaseJob> iter = existingJobs.iterator(); iter.hasNext();) {
            String jobName = iter.next().getName();

            for (CaseJob job : jobs2copy)
                if (jobName.equalsIgnoreCase(jobPrefix + job.getName()))
                    throw new EmfException("Job (" + job.getName() + ") plus job prefix (" + jobPrefix
                            + ") already exists.");
        }
    }

    private CaseInput[] removeRedundantInputs(CaseInput[] inputs, List<CaseInput> existingInputs, String jobPrefix)
            throws EmfException {
        List<CaseInput> uniqueInputs = new ArrayList<CaseInput>();
        uniqueInputs.addAll(Arrays.asList(inputs));
        Session session = this.sessionFactory.getSession();
        CaseInput[] existingInputsArray = existingInputs.toArray(new CaseInput[0]);
        CaseJob[] selectedJobs = new CaseJob[inputs.length];
        CaseJob[] existingJobs = new CaseJob[existingInputs.size()];

        try {
            for (int i = 0; i < inputs.length; i++)
                selectedJobs[i] = dao.getCaseJob(inputs[i].getCaseJobID(), session);

            for (int j = 0; j < existingInputs.size(); j++)
                existingJobs[j] = dao.getCaseJob(existingInputsArray[j].getCaseJobID(), session);
        } catch (Exception e) {
            throw new EmfException("Cannot get jobs for comparisons in removing dedundant case inputs. "
                    + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }

        for (int m = 0; m < inputs.length; m++) {
            for (int n = 0; n < existingInputsArray.length; n++) {
                String inputJobName = "";
                String nextJobName = "";

                if (selectedJobs[m] != null)
                    inputJobName = jobPrefix + selectedJobs[m].getName();

                if (existingJobs[n] != null)
                    nextJobName = existingJobs[n].getName();

                Sector inputSector = inputs[m].getSector();
                Sector nextSector = existingInputsArray[n].getSector();
                boolean sectorExists = (inputSector == null && nextSector == null)
                        || (inputSector != null && inputSector.equals(nextSector));

                CaseProgram inputProgram = inputs[m].getProgram();
                CaseProgram nextProgram = existingInputsArray[n].getProgram();
                boolean programExists = (inputProgram == null && nextProgram == null)
                        || (inputProgram != null && inputProgram.equals(nextProgram));

                if (inputs[m].getInputName().equals(existingInputsArray[n].getInputName()) && sectorExists
                        && programExists && inputJobName.equals(nextJobName))
                    uniqueInputs.remove(inputs[m]);
            }
        }

        return uniqueInputs.toArray(new CaseInput[0]);
    }

    private CaseParameter[] removeRedundantParams(CaseParameter[] params, List<CaseParameter> existingParams,
            String jobPrefix) throws EmfException {
        List<CaseParameter> uniqueParams = new ArrayList<CaseParameter>();
        uniqueParams.addAll(Arrays.asList(params));
        Session session = this.sessionFactory.getSession();
        CaseParameter[] existingParamsArray = existingParams.toArray(new CaseParameter[0]);
        CaseJob[] selectedJobs = new CaseJob[params.length];
        CaseJob[] existingJobs = new CaseJob[existingParams.size()];

        try {
            for (int i = 0; i < params.length; i++)
                selectedJobs[i] = dao.getCaseJob(params[i].getJobId(), session);

            for (int j = 0; j < existingParams.size(); j++)
                existingJobs[j] = dao.getCaseJob(existingParamsArray[j].getJobId(), session);
        } catch (Exception e) {
            throw new EmfException("Cannot get jobs for comparisons in removing dedundant case parameters. "
                    + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }

        for (int m = 0; m < params.length; m++) {
            for (int n = 0; n < existingParams.size(); n++) {
                String paramJobName = "";
                String nextJobName = "";

                if (selectedJobs[m] != null)
                    paramJobName = jobPrefix + selectedJobs[m].getName();

                if (existingJobs[n] != null)
                    nextJobName = existingJobs[n].getName();

                Sector paramSector = params[m].getSector();
                Sector nextSector = existingParamsArray[n].getSector();
                boolean sectorExists = (paramSector == null && nextSector == null)
                        || (paramSector != null && paramSector.equals(nextSector));

                CaseProgram paramProgram = params[m].getProgram();
                CaseProgram nextProgram = existingParamsArray[n].getProgram();
                boolean programExists = (paramProgram == null && nextProgram == null)
                        || (paramProgram != null && paramProgram.equals(nextProgram));

                if (params[m].getParameterName().equals(existingParamsArray[n].getParameterName()) && sectorExists
                        && programExists && (paramJobName.equals(nextJobName)))
                    uniqueParams.remove(params[m]);
            }
        }

        return uniqueParams.toArray(new CaseParameter[0]);
    }

    public synchronized Case mergeCases(User user, int parentCaseId, int templateCaseId, int[] jobIds, String jobGroup,
            Case sensitivityCase) throws EmfException {
        Session session = sessionFactory.getSession();
        Case lockedSC = null;
        Case lockedPC = null;
        Case lockedTC = null;
        String jobPrefix = (jobGroup == null || jobGroup.trim().isEmpty()) ? "" : jobGroup + " ";

        try {
            Abbreviation abbr = sensitivityCase.getAbbreviation();

            if (abbr != null) {
                try {
                    dao.add(abbr, session);
                } catch (RuntimeException e) {
                    throw new EmfException("Please check if the specified abbreviation already exists.");
                }
            }

            dao.add(sensitivityCase, session);
            Case loaded = (Case) dao.load(Case.class, sensitivityCase.getName(), session);
            lockedSC = dao.obtainLocked(user, loaded, session);
            int targetId = loaded.getId();

            Case parent = dao.getCase(parentCaseId, session);
            lockedPC = dao.obtainLocked(user, parent, session);
            Case template = dao.getCase(templateCaseId, session);
            lockedTC = dao.obtainLocked(user, template, session);

            if (lockedSC == null)
                throw new EmfException("Cannot obtain lock for merging case to happen: User " + loaded.getLockOwner()
                        + " has the lock for case '" + loaded.getName() + "'");

            if (lockedPC == null)
                throw new EmfException("Cannot obtain lock for merging case to happen: User " + parent.getLockOwner()
                        + " has the lock for case '" + parent.getName() + "'");

            if (lockedTC == null)
                throw new EmfException("Cannot obtain lock for merging case to happen: User " + template.getLockOwner()
                        + " has the lock for case '" + template.getName() + "'");

            CaseJob[] jobs2copy = getJobs2Copy(jobIds);
            CaseJob[] jobs = cloneCaseJobs(lockedSC.getId(), lockedTC.getId(), jobGroup, jobPrefix, jobs2copy, user);
            CaseInput[] inputs = cloneCaseInputs(parentCaseId, lockedSC.getId(), getValidCaseInputs4SensitivityCase(
                    template.getId(), jobIds, jobs2copy, session), session);
            CaseParameter[] params = cloneCaseParameters(parentCaseId, lockedSC.getId(),
                    getValidCaseParameters4SensitivityCase(template.getId(), jobIds, jobs2copy, session), session);

            addCaseJobs4Sensitivity(user, targetId, jobs);
            addCaseInputs(user, targetId, inputs, jobPrefix);
            addCaseParameters(user, targetId, params, jobPrefix);
            copySummaryInfo(lockedPC, lockedSC);

            // NOTE: copy input/output folder from template case
            lockedSC.setInputFileDir(lockedTC.getInputFileDir());
            lockedSC.setOutputFileDir(lockedTC.getOutputFileDir());

            // NOTE: set sectors from the selected jobs
            lockedSC.setSectors(getSectorsFromJobs(jobs));

            if (abbr == null)
                lockedSC.setAbbreviation(new Abbreviation(lockedSC.getId() + ""));

            updateCase(lockedSC);
            dao.add(new CasesSens(parentCaseId, lockedSC.getId()));

            return lockedSC;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not merge case.", e);
            throw new EmfException("Could not merge case. " + e.getMessage());
        } finally {
            try {
                if (lockedSC != null)
                    dao.releaseLocked(user, lockedSC, session);

                session.clear();
                if (lockedPC != null)
                    dao.releaseLocked(user, lockedPC, session);

                session.clear();
                if (lockedTC != null)
                    dao.releaseLocked(user, lockedTC, session);

                session.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Sector[] getSectorsFromJobs(CaseJob[] jobs) {
        List<Sector> sectors = new ArrayList<Sector>();

        for (CaseJob job : jobs) {
            Sector sctr = job.getSector();

            if (sctr != null)
                sectors.add(sctr);
        }

        TreeSet<Sector> set = new TreeSet<Sector>(sectors);
        List<Sector> uniqueSectors = new ArrayList<Sector>(set);

        return uniqueSectors.toArray(new Sector[0]);
    }

    private void copySummaryInfo(Case parent, Case sensitivityCase) {
        sensitivityCase.setAirQualityModel(parent.getAirQualityModel());
        sensitivityCase.setBaseYear(parent.getBaseYear());
        sensitivityCase.setControlRegion(parent.getControlRegion());
        sensitivityCase.setDescription("Sensitivity on " + parent.getName() + ": " + parent.getDescription());
        sensitivityCase.setEmissionsYear(parent.getEmissionsYear());
        sensitivityCase.setFutureYear(parent.getFutureYear());
        sensitivityCase.setGrid(parent.getGrid());
        sensitivityCase.setGridDescription(parent.getGridDescription());
        sensitivityCase.setGridResolution(parent.getGridResolution());
        sensitivityCase.setMeteorlogicalYear(parent.getMeteorlogicalYear());
        sensitivityCase.setModel(parent.getModel());
        sensitivityCase.setModelVersion(parent.getModelVersion());
        sensitivityCase.setModelingRegion(parent.getModelingRegion());
        sensitivityCase.setProject(parent.getProject());
        sensitivityCase.setSpeciation(parent.getSpeciation());
        sensitivityCase.setStartDate(parent.getStartDate());
        sensitivityCase.setEndDate(parent.getEndDate());
        sensitivityCase.setTemplateUsed(parent.getName());
        sensitivityCase.setNumEmissionsLayers(parent.getNumEmissionsLayers());
        sensitivityCase.setNumMetLayers(parent.getNumMetLayers());
    }

    private CaseJob[] getJobs2Copy(int[] jobIds) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            CaseJob[] jobs = new CaseJob[jobIds.length];

            for (int i = 0; i < jobs.length; i++)
                jobs[i] = dao.getCaseJob(jobIds[i], session);

            return jobs;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all jobs.", e);
            throw new EmfException("Could not get all jobs. " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private CaseJob[] cloneCaseJobs(int targetCaseId, int parentCaseId, String jobGroup, String jobPrefix,
            CaseJob[] objects, User user) throws Exception {
        List<CaseJob> copied = new ArrayList<CaseJob>();

        for (int i = 0; i < objects.length; i++) {
            CaseJob job = (CaseJob) DeepCopy.copy(objects[i]);
            job.setName(jobPrefix + job.getName());
            job.setCaseId(targetCaseId);
            job.setParentCaseId(parentCaseId);
            job.setJobGroup(jobGroup);
            job.setUser(user);
            job.setRunJobUser(null);
            copied.add(job);
        }

        return copied.toArray(new CaseJob[0]);
    }

    private CaseInput[] cloneCaseInputs(int parentCaseId, int targetCaseId, CaseInput[] inputs, Session session)
            throws Exception {
        List<CaseInput> inputList2Target = new ArrayList<CaseInput>();

        for (int i = 0; i < inputs.length; i++) {
            CaseInput tempInput = (CaseInput) DeepCopy.copy(inputs[i]);
            CaseInput inputFromParent = getParentCaseInputs4SensitivityCase(parentCaseId, inputs[i], session);
            boolean modifiedFromParent = false;

            if (inputFromParent != null) {
                tempInput.setParentCaseId(parentCaseId);

                if (tempInput.getDataset() == null) {
                    tempInput.setDataset(inputFromParent.getDataset());
                    tempInput.setVersion(inputFromParent.getVersion());
                    tempInput.setDatasetType(inputFromParent.getDatasetType());
                    modifiedFromParent = true;
                }

                if (tempInput.getEnvtVars() == null) {
                    tempInput.setEnvtVars(inputFromParent.getEnvtVars());
                    modifiedFromParent = true;
                }

                if (tempInput.getSubdirObj() == null) {
                    tempInput.setSubdirObj(inputFromParent.getSubdirObj());
                    modifiedFromParent = true;
                }

                if (modifiedFromParent)
                    tempInput.setLastModifiedDate(inputFromParent.getLastModifiedDate());
            } else {
                tempInput.setParentCaseId(inputs[i].getCaseID());
            }

            tempInput.setCaseID(targetCaseId);
            inputList2Target.add(tempInput);
        }

        /*
         * NOTE: If two inputs are very similar in the template, they potentially could be identical after they are
         * updated from the parent. This makes sure that we do not try to add 2 identical inputs. GOTCHA: If we need
         * both inputs, maybe we need to check for this and copy the original inputs without updating from parent.
         */
        // TreeSet<CaseInput> set = new TreeSet<CaseInput>(inputList2Target);
        // List<CaseInput> uniqueInputs = new ArrayList<CaseInput>(set);
        //
        // return uniqueInputs.toArray(new CaseInput[0]);
        return inputList2Target.toArray(new CaseInput[0]);
    }

    private CaseParameter[] cloneCaseParameters(int parentCaseId, int targetCaseId, CaseParameter[] params,
            Session session) throws Exception {
        List<CaseParameter> params2Target = new ArrayList<CaseParameter>();

        for (int i = 0; i < params.length; i++) {
            CaseParameter tempParam = (CaseParameter) DeepCopy.copy(params[i]);
            CaseParameter parentParameter = getParentCaseParameters4SensitivityCase(parentCaseId, params[i], session);
            boolean modifiedFromParent = false;

            if (parentParameter != null) {
                tempParam.setParentCaseId(parentCaseId);

                if (tempParam.getEnvVar() == null) {
                    tempParam.setEnvVar(parentParameter.getEnvVar());
                    modifiedFromParent = true;
                }

                if (tempParam.getType() == null) {
                    tempParam.setType(parentParameter.getType());
                    modifiedFromParent = true;
                }

                if (tempParam.getValue() == null || tempParam.getValue().trim().isEmpty()) {
                    tempParam.setValue(parentParameter.getValue());
                    modifiedFromParent = true;
                }

                if (tempParam.getPurpose() == null || tempParam.getPurpose().trim().isEmpty()) {
                    tempParam.setPurpose(parentParameter.getPurpose());
                    modifiedFromParent = true;
                }

                if (modifiedFromParent)
                    tempParam.setLastModifiedDate(parentParameter.getLastModifiedDate());
            } else {
                tempParam.setParentCaseId(params[i].getCaseID());
            }

            tempParam.setCaseID(targetCaseId);
            params2Target.add(tempParam);
        }

        /*
         * NOTE: If two parameters are very similar in the template, they potentially could be identical after they are
         * updated from the parent. This makes sure that we do not try to add 2 identical parameters. GOTCHA: If we need
         * both parameters, maybe we need to check for this and copy the original parameters without updating from
         * parent.
         */
        // TreeSet<CaseParameter> set = new TreeSet<CaseParameter>(params2Target);
        // List<CaseParameter> uniqueParameters = new ArrayList<CaseParameter>(set);
        //
        // return uniqueParameters.toArray(new CaseParameter[0]);
        return params2Target.toArray(new CaseParameter[0]);
    }

    public synchronized String validateNLInputs(int caseId) throws EmfException {
        String noLocalValues = "";
        try {
            CaseInput[] inputList = getCaseInputs(caseId);
            if (inputList == null)
                return noLocalValues;

            for (CaseInput input : inputList) {
                if (!input.isLocal() && input.getDataset() == null) {
                    noLocalValues += getInputValues(input) + "\n";
                }
            }
        } catch (RuntimeException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return noLocalValues;
    }

    public synchronized String validateNLParameters(int caseId) throws EmfException {
        String noLocalValues = "";
        CaseParameter[] paraList = getCaseParameters(caseId);
        for (CaseParameter par : paraList) {
            if (!par.isLocal() && par.getValue().trim().isEmpty()) {
                noLocalValues += getParamValues(par) + "\n";
            }
        }
        return noLocalValues;
    }

    private String getInputValues(CaseInput input) {
        String Value = (input.getEnvtVars() == null ? "" : input.getEnvtVars().getName()) + "; "
                + (input.getSector() == null ? "All sectors" : input.getSector().getName()) + "; "
                + getJobName(input.getCaseJobID()) + "; " + input.getName();
        return Value;
    }

    private String getParamValues(CaseParameter parameter) {
        String Value = (parameter.getEnvVar() == null ? "" : parameter.getEnvVar().getName()) + "; "
                + (parameter.getSector() == null ? "All sectors" : parameter.getSector().getName()) + "; "
                + getJobName(parameter.getJobId()) + "; " + parameter.getName();
        return Value;
    }

    private String getJobName(int jobId) {
        CaseJob job = dao.getCaseJob(jobId);
        if (job == null)
            return "";

        return job.getName();
    }

    public synchronized Case[] getSensitivityCases(int parentCaseId) throws EmfException {
        Session session = sessionFactory.getSession();
        Exception exc = null;
        Case parent = null;

        try {
            List<Case> cases = dao.getSensitivityCases(parentCaseId, session);
            return cases.toArray(new Case[0]);
        } catch (Exception e) {
            exc = e;
            parent = dao.getCase(parentCaseId, session);
        } finally {
            session.close();

            if (exc != null) {
                exc.printStackTrace();
                log.error("Could not get all sensitivity cases.", exc);
                throw new EmfException("Could not get all sensitivity cases for parent case"
                        + (parent == null ? "." : " (" + parent.getName() + ")."));
            }
        }

        return null;
    }

    public synchronized String[] getJobGroups(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return dao.getJobGroups(caseId, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all job groups info.", e);
            throw new EmfException("Could not get all job groups info.");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    private CaseInput[] getValidCaseInputs4SensitivityCase(int caseId, int[] jobIds, CaseJob[] jobs, Session session) {
        String query = "SELECT obj.id from " + CaseInput.class.getSimpleName() + " as obj WHERE obj.caseID = " + caseId
                + " AND ((obj.caseJobID = 0 AND obj.sector.id is null)"
                + getAndOrClause(jobIds, "obj.caseJobID", getSectorIds(jobs), "obj.sector.id") + ")";

        if (DebugLevels.DEBUG_9)
            log.warn(query);

        List<?> ids = session.createQuery(query).list();
        List<CaseInput> inputs = new ArrayList<CaseInput>();

        for (Iterator<?> iter = ids.iterator(); iter.hasNext();) {
            Integer id = (Integer) iter.next();
            inputs.add(dao.getCaseInput(id, session));
        }

        return inputs.toArray(new CaseInput[0]);
    }

    private CaseParameter[] getValidCaseParameters4SensitivityCase(int caseId, int[] jobIds, CaseJob[] jobs,
            Session session) {
        String query = "SELECT obj.id from " + CaseParameter.class.getSimpleName() + " as obj WHERE obj.caseID = "
                + caseId + " AND ((obj.jobId = 0 AND obj.sector.id is null)"
                + getAndOrClause(jobIds, "obj.jobId", getSectorIds(jobs), "obj.sector.id") + ")";

        if (DebugLevels.DEBUG_9)
            log.warn(query);

        List<?> ids = session.createQuery(query).list();

        List<CaseParameter> params = new ArrayList<CaseParameter>();

        for (Iterator<?> iter = ids.iterator(); iter.hasNext();)
            params.add(dao.getCaseParameter((Integer) iter.next(), session));

        return params.toArray(new CaseParameter[0]);
    }

    private int[] getSectorIds(CaseJob[] jobs) {
        int[] sectorIds = new int[jobs.length];

        for (int i = 0; i < jobs.length; i++) {
            Sector sector = jobs[i].getSector();

            if (sector == null)
                sectorIds[i] = -1;
            else
                sectorIds[i] = sector.getId();
        }

        return sectorIds;
    }

    private String getAndOrClause(int[] jobIds, String jobIdString, int[] sectorIds, String sectorIdString) {
        StringBuffer sb = new StringBuffer();
        int numIDs = jobIds.length;

        if (numIDs < 1)
            return "";

        // NOTE: the following implementation reflects this logic:
        // If you select job1 which is sector1, the logic for selecting the appropriate parameters from the template is:
        // (All job AND All sectors) OR (all jobs AND sector = sector1) OR (job = job1 AND All sectors) OR (job=job1 AND
        // sector = sector1)
        // You have to also make sure this doesn't fail if the sector of the job is All sectors.

        for (int i = 0; i < numIDs; i++) {
            if (sectorIds[i] != -1)
                sb.append(" OR (" + jobIdString + " = 0 AND " + sectorIdString + " = " + sectorIds[i] + ")" + " OR ("
                        + jobIdString + " = " + jobIds[i] + " AND " + sectorIdString + " is null)" + " OR ("
                        + jobIdString + " = " + jobIds[i] + " AND " + sectorIdString + " = " + sectorIds[i] + ")");
            else
                sb.append(" OR (" + jobIdString + " = " + jobIds[i] + " AND " + sectorIdString + " is null)");
        }

        return sb.toString();
    }

    // NOTE: the following implementation reflects this logic:
    //        
    // same env variable as param1 AND ((all jobs and all sectors) OR (all jobs and same sector as param1))
    //
    // If you get multiple parameters from that query, then you match with same sector.
    //
    // example:
    // template (name, env variable, sector, job) parent (name, env variable, sector, job)
    // PARAM1, EV1,all sectors, all jobs PARAM1,EV1,all sectors,all jobs
    // PARAM2,EV1,sector1, all jobs PARAM2,EV1,sector1, all jobs
    // PARAM3,EV1,all sectors, job1 PARAM3, EV1, sector2,all jobs
    // PARAM4,EV1,sector2,all jobs
    // PARAM5,EV1,sector3,all jobs
    //
    // So, the matching would be:
    // template, parent
    // 1,1
    // 2,2
    // 3,1
    // 4,3
    // 5,1

    private CaseParameter getParentCaseParameters4SensitivityCase(int caseId, CaseParameter param, Session session) {
        ParameterEnvVar env = param.getEnvVar();
        Sector sector = param.getSector();
        CaseParameter theParameter = null;

        String query = "SELECT obj.id from " + CaseParameter.class.getSimpleName() + " as obj WHERE obj.caseID = "
                + caseId + " AND obj.envVar.id = " + env.getId() + " AND ((obj.jobId = 0 AND obj.sector.id is null)"
                + " OR (obj.jobId = 0 AND obj.sector.id " + (sector == null ? "is null" : "= " + sector.getId()) + "))";

        if (DebugLevels.DEBUG_9)
            log.warn(query);

        List<?> ids = session.createQuery(query).list();

        for (Iterator<?> iter = ids.iterator(); iter.hasNext();) {
            theParameter = dao.getCaseParameter((Integer) iter.next(), session);

            if (sector != null && sector.equals(theParameter.getSector()))
                return theParameter;
        }

        return theParameter;
    }

    private CaseInput getParentCaseInputs4SensitivityCase(int caseId, CaseInput input, Session session) {
        InputEnvtVar env = input.getEnvtVars();

        if (env == null)
            return null; // NOTE: this policy is to be determined.

        Sector sector = input.getSector();
        CaseInput theInput = null;

        String query = "SELECT obj.id from " + CaseInput.class.getSimpleName() + " as obj WHERE obj.caseID = " + caseId
                + " AND obj.envtVars.id = " + env.getId() + " AND ((obj.caseJobID = 0 AND obj.sector.id is null)"
                + " OR (obj.caseJobID = 0 AND obj.sector.id " + (sector == null ? "is null" : "= " + sector.getId())
                + "))";

        if (DebugLevels.DEBUG_9)
            log.warn(query);

        List<?> ids = session.createQuery(query).list();

        for (Iterator<?> iter = ids.iterator(); iter.hasNext();) {
            Integer id = (Integer) iter.next();
            theInput = dao.getCaseInput(id, session);

            if (sector != null && sector.equals(theInput.getSector()))
                return theInput;
        }

        return theInput;
    }

    public void printCase(String folder, int caseId) throws EmfException {
        Session session = sessionFactory.getSession();
        Case currentCase = null;

        try {
            currentCase = dao.getCase(caseId, session);
            if (currentCase == null)
                throw new EmfException("Cannot retrieve current case.");

            File exportDir = new File(folder);
            if (!exportDir.canWrite()) {
                throw new EmfException("EMF cannot write to folder " + folder);
            }
            List<CaseJob> jobs = dao.getCaseJobs(caseId);
            List<CaseInput> inputs = dao.getCaseInputs(caseId, session);
            List<CaseParameter> parameters = dao.getCaseParameters(caseId, session);

            String prefix = currentCase.getName() + "_" + currentCase.getAbbreviation().getName() + "_";
            prefix = CustomStringTools.replaceNoneLetterDigit(prefix, '_');
            String sumParamFile = prefix + "Summary_Parameters.csv";
            String inputsFile = prefix + "Inputs.csv";
            String jobsFile = prefix + "Jobs.csv";

            printCaseSumParams(currentCase, parameters, jobs, folder, sumParamFile);
            printCaseInputs(inputs, jobs, folder, inputsFile, session);
            printCaseJobs(jobs, folder, jobsFile, session);
        } catch (Exception e) {
            log.error("Could not export case "
                    + (currentCase == null ? " (id = " + caseId + ")." : currentCase.getName() + "."), e);
            throw new EmfException("Could not export case: "
            // AME: this info makes the message too long to see it all in the window
                    // + (currentCase == null ? " (id = " + caseId + "). " : currentCase.getName() + ". ")
                    + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    private synchronized void printCaseSumParams(Case currentCase, List<CaseParameter> parameters, List<CaseJob> jobs,
            String folder, String sumParamFile) throws IOException {
        String ls = System.getProperty("line.separator");
        String model = (currentCase.getModel() == null) ? "" : currentCase.getModel().getName();
        String modelRegion = (currentCase.getModelingRegion() == null) ? "" : currentCase.getModelingRegion().getName();
        String gridName = (currentCase.getGrid() == null) ? "" : currentCase.getGrid().getName();
        String gridResolution = (currentCase.getGridResolution() == null) ? "" : currentCase.getGridResolution()
                .getName();
        String dstrModel = (currentCase.getAirQualityModel() == null) ? "" : currentCase.getAirQualityModel().getName();
        String speciation = (currentCase.getSpeciation() == null) ? "" : currentCase.getSpeciation().getName();
        String metYear = (currentCase.getMeteorlogicalYear() == null) ? "" : currentCase.getMeteorlogicalYear()
                .getName();
        String startDate = (currentCase.getStartDate() == null) ? "" : CustomDateFormat
                .format_MM_DD_YYYY_HH_mm(currentCase.getStartDate());
        String endDate = (currentCase.getEndDate() == null) ? "" : CustomDateFormat.format_MM_DD_YYYY_HH_mm(currentCase
                .getEndDate());

        String summary = "\"#EMF_CASE_NAME="
                + clean(currentCase.getName())
                + "\""
                + ls
                + "\"#EMF_CASE_ABBREVIATION="
                + clean(currentCase.getAbbreviation() == null ? "" : currentCase.getAbbreviation().getName())
                + "\""
                + ls
                + "\"#EMF_CASE_DESCRIPTION="
                + clean(currentCase.getDescription() == null ? "" : currentCase.getDescription())
                + "\""
                + ls
                + "\"#EMF_CASE_CATEGORY="
                + clean(currentCase.getCaseCategory() == null ? "" : currentCase.getCaseCategory().getName())
                + "\""
                + ls
                + "\"#EMF_PROJECT="
                + clean(currentCase.getProject() == null ? "" : currentCase.getProject().getName())
                + "\""
                + ls
                + "\"#EMF_SECTORS="
                + clean(getSectors(currentCase.getSectors()))
                + "\""
                + ls
                + "\"#EMF_CASE_COPIED_FROM="
                + clean(currentCase.getTemplateUsed())
                + "\""
                + ls
                + "\"#EMF_LAST_MODIFIED="
                + clean(currentCase.getLastModifiedBy() == null ? "" : currentCase.getLastModifiedBy().getName())
                + " on "
                + CustomDateFormat.format_MM_DD_YYYY_HH_mm(currentCase.getLastModifiedDate())
                + "\""
                + ls
                + "\"#EMF_IS_FINAL="
                + currentCase.getIsFinal()
                + " \""
                + ls
                + "\"#EMF_IS_TEMPLATE="
                + currentCase.isCaseTemplate()
                + "\""
                + ls
                + "Tab,Parameter,Order,Envt. Var.,Sector,Job,Program,Value,Type,Reqd?,Local?,Last Modified,Notes,Purpose"
                + ls + "Summary,Model to Run,0,MODEL_LABEL,All sectors,All jobs for sector,All programs,\""
                + clean(model) + "\",String,TRUE,TRUE,,," + ls
                + "Summary,Model Version,0,MODEL_LABEL,All sectors,All jobs for sector,All programs,\""
                + clean(currentCase.getModelVersion()) + "\",String,TRUE,TRUE,,," + ls
                + "Summary,Modeling Region,0,,All sectors,All jobs for sector,All programs,\"" + clean(modelRegion)
                + "\",String,TRUE,TRUE,,," + ls
                + "Summary,Grid Name,0,IOAPI_GRIDNAME_1,All sectors,All jobs for sector,All programs,\""
                + clean(gridName) + "\",String,TRUE,TRUE,,," + ls
                + "Summary,Grid Resolution,0,EMF_GRID,All sectors,All jobs for sector,All programs,\""
                + clean(gridResolution) + "\",String,TRUE,TRUE,,," + ls
                + "Summary,Met Layers,0,,All sectors,All jobs for sector,All programs," + currentCase.getNumMetLayers()
                + ",Integer,TRUE,TRUE,,," + ls
                + "Summary,Emission Layers,0,,All sectors,All jobs for sector,All programs,"
                + currentCase.getNumEmissionsLayers() + ",Integer,TRUE,TRUE,,," + ls
                + "Summary,Downstream Model,0,EMF_AQM,All sectors,All jobs for sector,All programs,\""
                + clean(dstrModel) + "\",String,TRUE,TRUE,,," + ls
                + "Summary,Speciation,0,EMF_SPC,All sectors,All jobs for sector,All programs,\"" + clean(speciation)
                + "\",String,TRUE,TRUE,,," + ls
                + "Summary,Meteorological Year,0,,All sectors,All jobs for sector,All programs," + metYear
                + ",String,TRUE,TRUE,,," + ls
                + "Summary,Base Year,0,BASE_YEAR,All sectors,All jobs for sector,All programs,"
                + currentCase.getBaseYear() + ",String,TRUE,TRUE,,," + ls
                + "Summary,Future Year,0,FUTURE_YEAR,All sectors,All jobs for sector,All programs,"
                + currentCase.getFutureYear() + ",String,TRUE,TRUE,,," + ls
                + "Summary,Start Date & Time,0,EPI_STDATE_TIME,All sectors,All jobs for sector,All programs,"
                + startDate + ",Date,TRUE,TRUE,,," + ls
                + "Summary,End Date & Time,0,EPI_ENDATE_TIME,All sectors,All jobs for sector,All programs," + endDate
                + ",Date,TRUE,TRUE,,," + ls;

        StringBuffer sb = new StringBuffer(summary);

        for (Iterator<CaseParameter> iter = parameters.iterator(); iter.hasNext();) {
            CaseParameter param = iter.next();
            String name = param.getName();
            String order = param.getOrder() + "";
            String envVar = param.getEnvVar() == null ? "" : param.getEnvVar() + "";
            String sector = (param.getSector() == null) ? "All sectors" : param.getSector() + "";
            String job = getJobName(param.getJobId(), jobs);
            String prog = param.getProgram() == null ? "" : param.getProgram() + "";
            String value = param.getValue() == null ? "" : param.getValue();
            String type = param.getType() == null ? "" : param.getType() + "";
            String reqrd = param.isRequired() + "";
            String local = param.isLocal() + "";
            String lstMod = param.getLastModifiedDate() == null ? "" : CustomDateFormat.format_MM_DD_YYYY_HH_mm(param
                    .getLastModifiedDate());
            String notes = param.getNotes() == null ? "" : param.getNotes();
            String purpose = param.getPurpose() == null ? "" : param.getPurpose();

            sb.append("Parameters,\"" + clean(name) + "\"," + order + ",\"" + clean(envVar) + "\",\"" + clean(sector)
                    + "\",\"" + clean(job) + "\",\"" + clean(prog) + "\",\"" + clean(value) + "\"," + clean(type) + ","
                    + reqrd + "," + local + "," + lstMod + ",\"" + clean(notes) + "\",\"" + clean(purpose) + "\"" + ls);
        }

        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(folder, sumParamFile))));
        writer.println(sb.toString());
        writer.close();
    }

    private String getSectors(Sector[] sectors) {
        StringBuffer sb = new StringBuffer();

        for (Sector sector : sectors)
            sb.append(sector.getName() + "&");

        int lastAmp = sb.lastIndexOf("&");

        return lastAmp < 0 ? sb.toString() : sb.toString().substring(0, lastAmp);
    }

    private String getJobName(int jobId, List<CaseJob> jobs) {
        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();

            if (jobId == 0)
                return "All jobs for sector";

            if (jobId == job.getId())
                return job.getName();
        }

        return "";
    }

    private String getDependsOnJobsString(DependentJob[] dependentJobs, Session session) {
        StringBuffer sb = new StringBuffer();

        for (DependentJob job : dependentJobs) {
            CaseJob depJob = dao.getCaseJob(job.getJobId(), session);
            sb.append(depJob != null ? depJob.getName() + "&" : "");
        }

        int lastAmp = sb.lastIndexOf("&");

        return lastAmp < 0 ? sb.toString() : sb.toString().substring(0, lastAmp);
    }

    private synchronized void printCaseInputs(List<CaseInput> inputs, List<CaseJob> jobs, String folder,
            String inputsFile, Session session) throws IOException {
        String ls = System.getProperty("line.separator");
        String columns = "Tab,Inputname,Envt Variable,Sector,Job,Program,Dataset,Version,QA status,DS Type,Reqd?,Local?,Subdir,Last Modified,Parentcase"
                + ls;

        StringBuffer sb = new StringBuffer(columns);

        for (Iterator<CaseInput> iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = iter.next();
            String name = input.getName();
            String envVar = input.getEnvtVars() == null ? "" : input.getEnvtVars() + "";
            String sector = (input.getSector() == null) ? "All sectors" : input.getSector() + "";
            String job = getJobName(input.getCaseJobID(), jobs);
            String prog = input.getProgram() == null ? "" : input.getProgram() + "";
            String dsName = input.getDataset() == null ? "" : input.getDataset().getName();
            String dsVersion = input.getVersion() == null ? "" : input.getVersion().getVersion() + "";
            String qaStatus = "";
            String dsType = input.getDatasetType() == null ? "" : input.getDatasetType().getName();
            String reqrd = input.isRequired() + "";
            String local = input.isLocal() + "";
            String subdir = input.getSubdirObj() == null ? "" : input.getSubdirObj() + "";
            String lstMod = input.getLastModifiedDate() == null ? "" : CustomDateFormat.format_MM_DD_YYYY_HH_mm(input
                    .getLastModifiedDate());
            Case parent = (input.getParentCaseId() > 0) ? dao.getCase(input.getParentCaseId(), session) : null;
            String parentName = parent != null ? parent.getName() : "";

            sb.append("Inputs,\"" + clean(name) + "\",\"" + clean(envVar) + "\",\"" + clean(sector) + "\",\""
                    + clean(job) + "\",\"" + clean(prog) + "\",\"" + clean(dsName) + "\"," + dsVersion + ","
                    + clean(qaStatus) + ",\"" + clean(dsType) + "\"," + reqrd + "," + local + "," + clean(subdir) + ","
                    + lstMod + ",\"" + clean(parentName) + "\"" + ls);
        }

        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(folder, inputsFile))));
        writer.println(sb.toString());
        writer.close();
    }

    private synchronized void printCaseJobs(List<CaseJob> jobs, String folder, String jobsFile, Session session)
            throws IOException {
        String ls = System.getProperty("line.separator");
        String columns = "Tab,JobName,Order,Sector,RunStatus,StartDate,CompletionDate,Executable,Arguments,Path,QueueOptions,JobGroup,Local,QueueID,User,Host,Notes,Purpose,DependsOn"
                + ls;

        StringBuffer sb = new StringBuffer(columns);

        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            String name = job.getName();
            String order = job.getOrder() + "";
            String sector = (job.getSector() == null) ? "All sectors" : job.getSector() + "";
            String status = job.getRunstatus() == null ? "" : job.getRunstatus() + "";
            String start = job.getRunStartDate() == null ? "" : CustomDateFormat.format_MM_DD_YYYY_HH_mm(job
                    .getRunStartDate());
            String end = job.getRunCompletionDate() == null ? "" : CustomDateFormat.format_MM_DD_YYYY_HH_mm(job
                    .getRunCompletionDate());
            String exec = job.getExecutable() == null ? "" : job.getExecutable() + "";
            String args = job.getArgs() == null ? "" : job.getArgs();
            String path = job.getPath() == null ? "" : job.getPath();
            String qOptns = job.getQueOptions() == null ? "" : job.getQueOptions();
            String jobGrp = job.getJobGroup();
            String local = job.isLocal() + "";
            String qId = job.getIdInQueue() == null ? "" : job.getIdInQueue();
            String user = job.getUser() == null ? "" : job.getUser().getName();
            String host = job.getHost() == null ? "" : job.getHost() + "";
            String notes = job.getRunNotes() == null ? "" : job.getRunNotes();
            String purpose = job.getPurpose() == null ? "" : job.getPurpose();
            String dependsOn = getDependsOnJobsString(job.getDependentJobs(), session);

            sb.append("Jobs,\"" + clean(name) + "\"," + order + ",\"" + clean(sector) + "\"," + clean(status) + ","
                    + start + "," + end + "," + clean(exec) + ",\"" + clean(args) + "\"," + clean(path) + ",\""
                    + clean(qOptns) + "\",\"" + clean(jobGrp) + "\"," + local + ",\"" + clean(qId) + "\",\""
                    + clean(user) + "\", " + clean(host) + ",\"" + clean(notes) + "\",\"" + clean(purpose) + "\",\""
                    + clean(dependsOn) + "\"" + ls);
        }

        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(folder, jobsFile))));
        writer.println(sb.toString());
        writer.close();
    }

    private String clean(String toClean) {
        if (toClean == null || toClean.trim().isEmpty())
            return "";

        String temp = toClean.replace('"', '\'');

        return temp.replaceAll("\\\\", "/");
    }

    public synchronized String loadCMAQCase(String path, int jobId, int caseId, User user) throws EmfException {
        File logFile = new File(path);

        if (!logFile.exists())
            throw new EmfException("CMAQ log file doesn't exist: " + path + ".");

        if (!logFile.isFile())
            throw new EmfException("Please specify a valid log file.");

        if (!logFile.canRead())
            throw new EmfException("CMAQ log file is not readable by Tomcat: " + path + ".");

        List<CaseParameter> paramObjects = getValidParameters(jobId, caseId);
        List<String> paramEnvs = getEnvVars(paramObjects);
        List<String> withSumEnvs = combineSumEnvs(paramEnvs);

        if (withSumEnvs == null || withSumEnvs.size() == 0)
            throw new EmfException("No valid parameters selected to load.");

        EMFCaseFile caseFile = new CMAQLogFile(logFile);
        caseFile.read(withSumEnvs);

        StringBuffer sb = new StringBuffer();

        String lineSep = System.getProperty("line.separator");

        try {
            resetSummaryValues(withSumEnvs, caseFile, sb, lineSep, caseId);
        } catch (Exception e) {
            throw new EmfException("Error parsing summary info: " + e.getMessage());
        }

        int numLoaded = resetParameterValues(user, paramObjects, caseFile, sb, lineSep);

        String msg = numLoaded + " parameter value" + (numLoaded > 1 ? "s" : "") + " loaded." + lineSep;

        return msg + sb.toString() + caseFile.getMessages();
    }

    private List<String> combineSumEnvs(List<String> paramEnvs) {
        if (paramEnvs == null)
            paramEnvs = new ArrayList<String>();

        if (!paramEnvs.contains("MODEL_LABEL"))
            paramEnvs.add("MODEL_LABEL");

        if (!paramEnvs.contains("IOAPI_GRIDNAME_1"))
            paramEnvs.add("IOAPI_GRIDNAME_1");

        if (!paramEnvs.contains("EMF_GRID"))
            paramEnvs.add("EMF_GRID");

        if (!paramEnvs.contains("EMF_AQM"))
            paramEnvs.add("EMF_AQM");

        if (!paramEnvs.contains("EMF_SPC"))
            paramEnvs.add("EMF_SPC");

        if (!paramEnvs.contains("BASE_YEAR"))
            paramEnvs.add("BASE_YEAR");

        if (!paramEnvs.contains("FUTURE_YEAR"))
            paramEnvs.add("FUTURE_YEAR");

        if (!paramEnvs.contains("EPI_STDATE_TIME"))
            paramEnvs.add("EPI_STDATE_TIME");

        if (!paramEnvs.contains("EPI_ENDATE_TIME"))
            paramEnvs.add("EPI_ENDATE_TIME");

        return paramEnvs;
    }

    private void resetSummaryValues(List<String> paramEnvs, EMFCaseFile caseFile, StringBuffer sb, String lineSep,
            int caseId) throws Exception {
        Case caze = this.getCase(caseId);
        Session session = sessionFactory.getSession();

        for (Iterator<String> iter = paramEnvs.iterator(); iter.hasNext();) {
            String envVar = iter.next();
            String value = caseFile.getAttributeValue(envVar);

            if (value == null)
                continue;

            if (envVar.toUpperCase().equals("MODEL_LABEL")) {
                String origName = (caze.getModel() == null ? null : caze.getModel().getName());
                String origVersion = caze.getModelVersion();

                ModelToRun model = new ModelToRun();
                String version = setModelNVersion(model, value);
                model = addModelToRun(model);

                if (!model.getName().isEmpty() && !model.getName().equalsIgnoreCase(origName)) {
                    caze.setModel(model);
                    sb.append("WARNING: model -- value replaced (previous: " + origName + ")" + lineSep);
                }

                if (!version.isEmpty() && !version.equalsIgnoreCase(origVersion)) {
                    caze.setModelVersion(version);
                    sb.append("WARNING: version -- value replaced (previous: " + origVersion + ")" + lineSep);
                }
            }

            if (envVar.toUpperCase().equals("IOAPI_GRIDNAME_1")) {
                Grid grid = caze.getGrid();

                if (grid != null && grid.getName() != null && grid.getName().trim().equalsIgnoreCase(value))
                    continue;

                if (grid != null && grid.getName() != null)
                    sb.append("WARNING: grid -- value replaced (previous: " + grid.getName() + ")" + lineSep);

                grid = new Grid();
                grid.setName(value);
                grid = this.addGrid(grid);

                caze.setGrid(grid);
            }

            if (envVar.toUpperCase().equals("EMF_GRID")) {
                GridResolution resltn = caze.getGridResolution();

                if (resltn != null && resltn.getName() != null && resltn.getName().trim().equalsIgnoreCase(value))
                    continue;

                if (resltn != null && resltn.getName() != null)
                    sb.append("WARNING: grid resolution -- value replaced (previous: " + resltn.getName() + ")"
                            + lineSep);

                resltn = new GridResolution();
                resltn.setName(value);
                resltn = this.addGridResolution(resltn);

                caze.setGridResolution(resltn);
            }

            if (envVar.toUpperCase().equals("EMF_AQM")) {
                AirQualityModel aqm = caze.getAirQualityModel();

                if (aqm != null && aqm.getName() != null && aqm.getName().trim().equalsIgnoreCase(value))
                    continue;

                if (aqm != null && aqm.getName() != null)
                    sb.append("WARNING: air quality model -- value replaced (previous: " + aqm.getName() + ")"
                            + lineSep);

                aqm = new AirQualityModel();
                aqm.setName(value);
                aqm = this.addAirQualityModel(aqm);

                caze.setAirQualityModel(aqm);
            }

            if (envVar.toUpperCase().equals("EMF_SPC")) {
                Speciation spec = caze.getSpeciation();

                if (spec != null && spec.getName() != null && spec.getName().trim().equalsIgnoreCase(value))
                    continue;

                if (spec != null && spec.getName() != null)
                    sb.append("WARNING: speciation -- value replaced (previous: " + spec.getName() + ")" + lineSep);

                spec = new Speciation();
                spec.setName(value);
                spec = this.addSpeciation(spec);

                caze.setSpeciation(spec);
            }

            if (envVar.toUpperCase().equals("BASE_YEAR")) {
                int baseyr = caze.getBaseYear();

                sb.append("WARNING: base year -- value replaced (previous: " + baseyr + ")" + lineSep);

                caze.setBaseYear(Integer.parseInt(value.trim()));
            }

            if (envVar.toUpperCase().equals("FUTURE_YEAR")) {
                int futureyr = caze.getFutureYear();

                sb.append("WARNING: base year -- value replaced (previous: " + futureyr + ")" + lineSep);

                caze.setFutureYear(Integer.parseInt(value.trim()));
            }

            if (envVar.toUpperCase().equals("EPI_STDATE_TIME")) {
                Date start = caze.getStartDate();

                if (start != null) {
                    String date = CustomDateFormat.format_MM_DD_YYYY_HH_mm(start);
                    sb.append("WARNING: start date -- value replaced (previous: " + date + ")" + lineSep);
                }

                caze.setStartDate(start);
            }

            if (envVar.toUpperCase().equals("EPI_ENDATE_TIME")) {
                Date end = caze.getEndDate();

                if (end != null) {
                    String date = CustomDateFormat.format_MM_DD_YYYY_HH_mm(end);
                    sb.append("WARNING: end date -- value replaced (previous: " + date + ")" + lineSep);
                }

                caze.setStartDate(end);
            }
        }

        try {
            dao.updateWithLock(caze, session);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    private String setModelNVersion(ModelToRun model, String value) throws Exception {
        // NOTE: parsing mechanism in synch with the log file reader
        if (value == null || value.trim().isEmpty())
            return null;

        try {
            value = value.trim();
            int comma = value.indexOf(',');
            String part1 = value;
            String part2 = "";
            String version = "";

            if (comma > 0)
                part1 = value.substring(0, comma).trim();

            if (comma > 0 && comma < value.length() - 1)
                part2 = value.substring(comma + 1, value.length()).trim();

            if ((!part1.isEmpty() && Character.isDigit(part1.charAt(0)))
                    || (part1.toUpperCase().startsWith("V") && Character.isDigit(part1.charAt(1)))) {
                version = part1;
                model.setName(part2);
                return version;
            }

            model.setName(part1);
            return part2;
        } catch (Exception e) {
            log.error("Error parsing case model and version string.", e);
            throw e;
        }
    }

    private int resetParameterValues(User user, List<CaseParameter> paramObjects, EMFCaseFile caseFile,
            StringBuffer sb, String lineSep) throws EmfException {
        int numLoaded = 0;

        for (Iterator<CaseParameter> iter = paramObjects.iterator(); iter.hasNext();) {
            CaseParameter param = iter.next();
            ParameterEnvVar envVar = param.getEnvVar();

            if (envVar == null)
                continue;

            String value = caseFile.getAttributeValue(envVar.getName());
            String existingVal = param.getValue();

            if (value == null || value.isEmpty() || value.equals(existingVal))
                continue;

            if (existingVal != null && !existingVal.trim().isEmpty())
                sb.append("WARNING: parameter \'" + param.getName() + "\'--value replaced (previous: " + existingVal
                        + ")" + lineSep);

            param.setValue(value);
            updateCaseParameter(user, param);
            numLoaded++;
        }

        return numLoaded;
    }

    private synchronized List<CaseParameter> getValidParameters(int jobId, int caseId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            CaseJob job = dao.getCaseJob(jobId, session);
            Sector sector = (job == null) ? null : job.getSector();

            List<CaseParameter> jobSpecParams = dao.getCaseParametersByJobId(caseId, jobId, session);
            List<CaseParameter> sectorSpecParams = (sector != null) ? dao.getCaseParametersBySector(caseId, sector,
                    session) : dao.getCaseParametersForAllSectors(caseId, session);
            List<CaseParameter> params4AllSectorsAllJobs = dao.getCaseParametersForAllSectorsAllJobs(caseId, session);

            return selectValidParameters(jobSpecParams, sectorSpecParams, params4AllSectorsAllJobs);
        } catch (Exception e) {
            log.error("Error reading case parameters for case id = " + caseId + " and job id = " + jobId + ".", e);
            throw new EmfException("Error reading case parameters for case id = " + caseId + " and job id = " + jobId
                    + ".");
        } finally {
            if (session != null && session.isOpen())
                session.close();
        }
    }

    private List<CaseParameter> selectValidParameters(List<CaseParameter> jobSpecParams,
            List<CaseParameter> sectorSpecParams, List<CaseParameter> params4AllSectorsAllJobs) {
        List<String> paramEnvs = getEnvVars(jobSpecParams);

        for (Iterator<CaseParameter> iter = sectorSpecParams.iterator(); iter.hasNext();) {
            CaseParameter param = iter.next();
            ParameterEnvVar envVar = param.getEnvVar();
            int paramJobId = param.getJobId();

            if (!paramEnvs.contains(envVar.getName()) && paramJobId == 0) {
                jobSpecParams.add(param);
                paramEnvs.add(envVar.getName());
            }
        }

        for (Iterator<CaseParameter> iter = params4AllSectorsAllJobs.iterator(); iter.hasNext();) {
            CaseParameter param = iter.next();
            ParameterEnvVar envVar = param.getEnvVar();

            if (!paramEnvs.contains(envVar.getName())) {
                jobSpecParams.add(param);
                paramEnvs.add(envVar.getName());
            }
        }

        return jobSpecParams;
    }

    private synchronized List<String> getEnvVars(List<CaseParameter> paramObjects) {
        List<String> envs = new ArrayList<String>();

        for (Iterator<CaseParameter> iter = paramObjects.iterator(); iter.hasNext();) {
            ParameterEnvVar env = iter.next().getEnvVar();

            if (env != null && env.getName() != null && !env.getName().trim().isEmpty())
                envs.add(env.getName());
        }

        return envs;
    }

    public synchronized void updateRelatedModels(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<CaseParameter> params = dao.getCaseParameters(caseId, session);
            Case caze = dao.getCase(caseId, session);
            ModelToRun model = caze.getModel();

            if (model == null)
                return;

            int modelId = model.getId();

            for (Iterator<CaseParameter> iter = params.iterator(); iter.hasNext();) {
                CaseParameter param = iter.next();
                ParameterName name = param.getParameterName();
                ParameterEnvVar envar = param.getEnvVar();
                name.setModelToRunId(modelId);
                envar.setModelToRunId(modelId);
                param.setParameterName(name);
                param.setEnvVar(envar);
                dao.updateCaseParameter(param, session);
            }

            // NOTE: need to update input name and env var also
        } catch (Exception e) {
            log.error("Error reading case parameters for case id = " + caseId + ".", e);
            throw new EmfException("Error reading case parameters for case id = " + caseId + ".");
        } finally {
            if (session != null && session.isOpen())
                session.close();
        }
    }

}
