package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.exim.ManagedImportService;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.TaskManagerFactory;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CaseAssistServiceImpl implements CaseAssistService {
    private static Log log = LogFactory.getLog(CaseAssistServiceImpl.class);

    private static int svcCount = 0;

    private String svcLabel = null;

    private CaseDAO dao;

    private ManagedImportService importService;

    public String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbFactory;

    public CaseAssistServiceImpl() {
        this(HibernateSessionFactory.get(), DbServerFactory.get());
    }

    public CaseAssistServiceImpl(HibernateSessionFactory sessionFactory, DbServerFactory dbFactory) {
        this.sessionFactory = sessionFactory;
        this.dbFactory = dbFactory;
        this.dao = new CaseDAO(sessionFactory);
        if (DebugLevels.DEBUG_0)
            System.out.println("CaseAsistServiceImpl::getCaseService  Is sessionFactory null? "
                    + (sessionFactory == null));

        myTag();
        if (DebugLevels.DEBUG_0)
            System.out.println(myTag());

    }

    @Override
    protected void finalize() throws Throwable {
        this.sessionFactory = null;
        
        if (DebugLevels.DEBUG_0)
            System.out.println("CaseAssistService: garbage collected. sessionFactory = null? "
                    + (sessionFactory == null));
        
        super.finalize();
    }

    public synchronized String printStatusCaseJobTaskManager() throws EmfException {
        return TaskManagerFactory.getCaseJobTaskManager(sessionFactory).getStatusOfWaitAndRunTable();
    }

    public synchronized void registerOutputs(CaseOutput[] outputs, String[] jobKeys) throws EmfException {
        if (DebugLevels.DEBUG_14)
            System.out.println("Start registering case outputs. " +  new Date());
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
        
        if (DebugLevels.DEBUG_14)
            System.out.println("Finished registering case outputs. " +  new Date());
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

    public synchronized void recordJobMessages(JobMessage[] msgs, String[] keys) throws EmfException {
        int msgLength = msgs.length;

        if (msgs.length != keys.length) {
            throw new EmfException("Error recording job messages: Number of job messages (" + msgs.length
                    + ") doesn't match number of job keys (" + keys.length + ")");
        }

        for (int i = 0; i < msgLength; i++)
            recordJobMessage(msgs[i], keys[i]);
    }

    public synchronized void recordJobMessage(JobMessage message, String jobKey) throws EmfException {
        if (DebugLevels.DEBUG_14)
            System.out.println("Start recording job messages. " + message.getMessage() + " " + new Date());
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
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new EmfException("Error recording job messages: " + e.getMessage());
        }
        
        if (DebugLevels.DEBUG_14)
            System.out.println("Finished record job message. " + message.getMessage() + " " + new Date());
    }

    private synchronized CaseJob getJobFromKey(String jobKey) throws EmfException {
        CaseJob job = dao.getCaseJob(jobKey);

        if (job == null)
            throw new EmfException("Error recording job messages: No jobs found associated with job key: " + jobKey);

        return job;
    }

}