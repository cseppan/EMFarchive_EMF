package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.casemanagement.CaseDAO;
import gov.epa.emissions.framework.services.casemanagement.CaseJobTask;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.DependentJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CaseJobTaskManager implements TaskManager {
    private static Log log = LogFactory.getLog(CaseJobTaskManager.class);

    private static CaseDAO caseDAO = null;

    private static StatusDAO statusDAO = null;

    private static CaseJobTaskManager ref;

    private static int refCount = 0;

    private final int poolSize = 4;

    private final int maxPoolSize = 4;

    private final long keepAliveTime = 60;

    private static ArrayList<TaskSubmitter> submitters = new ArrayList<TaskSubmitter>();

    private static ThreadPoolExecutor threadPool = null;

    // PBQ is the queue for submitting jobs
    private static BlockingQueue<Runnable> taskQueue = new PriorityBlockingQueue<Runnable>();

    private static ArrayBlockingQueue<Runnable> threadPoolQueue = new ArrayBlockingQueue<Runnable>(5);

    private static Hashtable<String, Task> runTable = new Hashtable<String, Task>();

    private static Hashtable<String, Task> waitTable = new Hashtable<String, Task>();

    public static synchronized int getSizeofTaskQueue() {
        return taskQueue.size();
    }

    public static synchronized int getSizeofWaitTable() {
        return waitTable.size();
    }

    public static synchronized int getSizeofRunTable() {
        return runTable.size();
    }

    public static synchronized void shutDown() {
        if (DebugLevels.DEBUG_1)
            System.out.println("Shutdown called on Task Manager");
        taskQueue.clear();
        threadPoolQueue.clear();
        threadPool.shutdownNow();
    }

    public static synchronized void removeTask(Runnable task) {
        taskQueue.remove(task);
    }

    public static synchronized void removeTasks(ArrayList<?> tasks) {
        taskQueue.removeAll(tasks);
    }

    public static synchronized void registerTaskSubmitter(TaskSubmitter ts) {
        submitters.add(ts);
    }

    public static synchronized void deregisterSubmitter(TaskSubmitter ts) {
        if (DebugLevels.DEBUG_1)
            System.out.println("DeREGISTERED SUBMITTER: " + ts.getSubmitterId() + " Confirm task count= "
                    + ts.getTaskCount());
        submitters.remove(ts);
    }

    public synchronized void finalize() throws Throwable {
        if (DebugLevels.DEBUG_0)
            System.out.println("Finalizing TaskManager # of taskmanagers= " + refCount);

        shutDown();
    }

    // clone not supported needs to be added
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    private CaseJobTaskManager(HibernateSessionFactory sessionFactory) {
        super();
        log.info("CaseJobTaskManager constructor");
        if (DebugLevels.DEBUG_0)
            System.out.println("CaseJob Task Manager created @@@@@ THREAD ID: " + Thread.currentThread().getId());

        refCount++;
        if (DebugLevels.DEBUG_4)
            System.out.println("CaseJobTask Manager created refCount= " + refCount);
        if (DebugLevels.DEBUG_4)
            System.out.println("Priority Blocking queue created? " + !(taskQueue == null));

        threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, threadPoolQueue);
        if (DebugLevels.DEBUG_4)
            System.out.println("ThreadPool created? " + !(threadPool == null));
        if (DebugLevels.DEBUG_4)
            System.out.println("Initial # of jobs in Thread Pool: " + threadPool.getPoolSize());

        caseDAO = new CaseDAO(sessionFactory);
        statusDAO = new StatusDAO(sessionFactory);

        // FIXME: Remove the next line after statusDAO is used in this class
        if (DebugLevels.DEBUG_9)
            System.out.println("Dummy: " + statusDAO.getClass().getName());
    }

    public static synchronized CaseJobTaskManager getCaseJobTaskManager(HibernateSessionFactory sessionFactory) {
        if (ref == null)
            ref = new CaseJobTaskManager(sessionFactory);
        return ref;
    }

    /**
     * Add a CaseJobTask to the PBQ
     * 
     * @throws EmfException
     */
    public static synchronized void addTask(CaseJobTask task) throws EmfException {
        if (DebugLevels.DEBUG_0)
            System.out.println("IN CaseJobTaskManager::add task= " + task.getTaskId() + " for job= " + task.getJobId());
        taskQueue.add(task);
        if (DebugLevels.DEBUG_0)
            System.out.println("IN CaseJobTaskManager size of task Queue= " + taskQueue.size());

        // Process the TaskQueue
        processTaskQueue();

    }

    public static synchronized void addTasks(ArrayList<Runnable> tasks) throws EmfException {

        if (DebugLevels.DEBUG_0)
            System.out.println("IN CaseJobTaskManager number of tasks received= " + tasks.size());
        taskQueue.addAll(tasks);
        if (DebugLevels.DEBUG_0)
            System.out.println("IN CaseJobTaskManager size of task Queue= " + taskQueue.size());

        synchronized (runTable) {
            if (threadPool.getCorePoolSize() - runTable.size() > 0) {
                processTaskQueue();
            }
        }// synchronized
    }

    public static synchronized void callBackFromThread(String taskId, String submitterId, String status, String mesg)
            throws EmfException {
        if (DebugLevels.DEBUG_2)
            System.out.println("CaseJobTaskManager::callBackFromThread  refCount= " + refCount);
        if (DebugLevels.DEBUG_2)
            System.out.println("%%%% CaseJobTaskManager reports that Task# " + taskId + " for submitter= "
                    + submitterId + " completed with status= " + status + " and message= " + mesg);

        if (DebugLevels.DEBUG_0)
            System.out.println("***BELOW*** In callback the sizes are ***BELOW***");
        if (DebugLevels.DEBUG_0)
            System.out.println("Size of PBQ taskQueue: " + getSizeofTaskQueue());
        if (DebugLevels.DEBUG_0)
            System.out.println("Size of WAIT TABLE: " + getSizeofWaitTable());
        if (DebugLevels.DEBUG_0)
            System.out.println("Size of RUN TABLE: " + getSizeofRunTable());
        if (DebugLevels.DEBUG_0)
            System.out.println("***ABOVE*** In callback the sizes are shown ***ABOVE***");

        updateRunStatus(taskId, status);

        if (DebugLevels.DEBUG_0)
            System.out.println("After Task removed Size of RUN TABLE: " + getSizeofRunTable());

        processTaskQueue();
    }

    private static void updateRunStatus(String taskId, String status) throws EmfException {
        System.out.println("CaseJobTaskManager::updateRunStatus: " + taskId + " status= " + status);

        CaseJobTask cjt = null;
        String jobStatus = "";

        try {
            if ((status.equals("completed")) || (status.equals("failed"))) {
                System.out.println("CaseJobTaskManager::updateRunStatus:  job completed or failed");
                synchronized (runTable) {
                    cjt = (CaseJobTask) runTable.get(taskId);

                    runTable.remove(taskId);
                }
            }

            if (status.equals("export failed")) {
                System.out.println("CaseJobTaskManager::updateRunStatus:  export failed");
                synchronized (waitTable) {
                    System.out.println("Export Failed");
                    cjt = (CaseJobTask) waitTable.get(taskId);
                    System.out.println("CaseJobTask Id for failed exports = " + cjt.getJobId());
                    System.out.println("CaseJobTask Id for failed exports = " + cjt.getTaskId());
                    System.out.println("Size of the waitTable before remove: " + waitTable.size());
                    waitTable.remove(taskId);
                    System.out.println("Size of the waitTable after remove: " + waitTable.size());
                }
            }

            if (status.equals("export succeeded")) {
                System.out.println("CaseJobTaskManager::updateRunStatus:  export success");
                synchronized (waitTable) {
                    cjt = (CaseJobTask) waitTable.get(taskId);
                }
            }

            // update the run status in the Case_CaseJobs
            int jid = cjt.getJobId();

            CaseJob caseJob = caseDAO.getCaseJob(jid);

            if (DebugLevels.DEBUG_9)
                System.out.println("In CaseJobTaskManager::updateRunStatus for jobId= " + jid
                        + " Is the CaseJob null? " + (caseJob == null));

            if (status.equals("completed")) {
                System.out.println("CaseJobTaskManager::updateRunStatus:  job Status is completed jobStatus=Submitted");
                jobStatus = "Submitted";
                caseJob.setRunStartDate(new Date());
            }

            if (status.equals("failed")) {
                System.out.println("CaseJobTaskManager::updateRunStatus:  jobStatus is Failed jobStatus=Failed");
                jobStatus = "Failed";
                caseJob.setRunCompletionDate(new Date());
            }

            if (status.equals("export succeeded")) {
                System.out.println("CaseJobTaskManager::updateRunStatus:  export Succeeded jobStatus=Waiting");
                jobStatus = "Waiting";
                caseJob.setRunStartDate(new Date());
            }

            if (status.equals("export failed")) {
                System.out.println("CaseJobTaskManager::updateRunStatus:  export FAILED jobStatus=FAILED");
                jobStatus = "Failed";
                caseJob.setRunStartDate(new Date());
            }

            JobRunStatus jrStat = caseDAO.getJobRunStatuse(jobStatus);
            caseJob.setRunstatus(jrStat);

            caseDAO.updateCaseJob(caseJob);
        } catch (Exception e) {
            System.out.println("^^^^^^^^^^^^^^");
            e.printStackTrace();

            System.out.println("^^^^^^^^^^^^^^");
            throw new EmfException(e.getMessage());
        }
    }

    public static synchronized void processTaskQueue() throws EmfException {
        testAndSetWaitingTasksDependencies();

        int threadsAvail = -99;
        
        if (DebugLevels.DEBUG_0)
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        if (DebugLevels.DEBUG_0)
            System.out.println(">>>>> BEGIN CaseJobTaskManager::processTaskQueue() *** " + new Date());
        if (DebugLevels.DEBUG_0)
            System.out.println("Size of PBQ taskQueue: " + getSizeofTaskQueue());
        if (DebugLevels.DEBUG_0)
            System.out.println("Size of WAIT TABLE: " + getSizeofWaitTable());
        if (DebugLevels.DEBUG_0)
            System.out.println("Size of RUN TABLE: " + getSizeofRunTable());

        if (DebugLevels.DEBUG_0)
            System.out.println("Before processing the taskQueue");
        if (DebugLevels.DEBUG_0)
            System.out.println("SIZE OF TASKQUEUE: " + getSizeofTaskQueue());

        boolean done = false;
        while (!done) {
            if (getSizeofTaskQueue() == 0) {
                if (DebugLevels.DEBUG_0)
                    System.out.println("#tasks in taskQueue == 0?? Breaking out of taskQueue TEST loop: ");
                done = true;
            } else {
                if (DebugLevels.DEBUG_0)
                    System.out.println("Before Peek into taskQueue: " + getSizeofTaskQueue());
                if (taskQueue.peek() != null) {
                    if (DebugLevels.DEBUG_0)
                        System.out.println("Peak into taskQueue has an object in head: " + getSizeofTaskQueue());

                    try {
                        CaseJobTask nextTask = (CaseJobTask) taskQueue.take();
                        if (DebugLevels.DEBUG_9)
                            System.out
                                    .println("&&&&& In CaseJobTaskManager::processQueue pop the queue the type of TASK objects nextTask is : "
                                            + nextTask.getClass().getName());

                        // number of threads available before inspecting the priority blocking queue
                        synchronized (threadPool) {
                            // threadsAvail = threadPool.getCorePoolSize() - threadPool.getActiveCount();
                            threadsAvail = threadPool.getCorePoolSize() - runTable.size();

                        }// synchronized
                        if (DebugLevels.DEBUG_0)
                            System.out.println("Number of threads available before taskQueue PDQ: " + threadsAvail);

                        if (threadsAvail == 0) {
                            synchronized (waitTable) {
                                waitTable.put(nextTask.getTaskId(), nextTask);
                            }// synchronized
                        } else {
                            if (nextTask.isReady()) {

                                // add to runTable
                                synchronized (runTable) {
                                    runTable.put(nextTask.getTaskId(), nextTask);
                                }// synchronized

                                // runTask and decrement threadsAvail
                                threadPool.execute(nextTask);
                                // synchronized (threadPool) {
                                // threadsAvail--;
                                // }// synchronized
                            } else {
                                synchronized (waitTable) {
                                    waitTable.put(nextTask.getTaskId(), nextTask);
                                }// synchronized
                            }

                        }
                    } catch (InterruptedException e) {

                        e.printStackTrace();
                    }
                }
            }// more tasks in taskQueue

        }// while not done

        if (DebugLevels.DEBUG_0)
            System.out.println("After processing the taskQueue");

        if (DebugLevels.DEBUG_0)
            System.out.println("Size of PBQ taskQueue: " + getSizeofTaskQueue());
        if (DebugLevels.DEBUG_0)
            System.out.println("Size of WAIT TABLE: " + getSizeofWaitTable());
        if (DebugLevels.DEBUG_0)
            System.out.println("Size of RUN TABLE: " + getSizeofRunTable());

        if (DebugLevels.DEBUG_0)
            System.out.println("Before processing the WAIT TABLE");

        // Copy the waitTable to tempWaitTable
        Collection<Task> waitTasks = null;
        Hashtable<String, Task> tempWaitTable = new Hashtable<String, Task>();
        synchronized (waitTable) {
            waitTasks = waitTable.values();
            Iterator<Task> copyIter = waitTasks.iterator();
            while (copyIter.hasNext()) {
                Task copyTask = copyIter.next();
                if (DebugLevels.DEBUG_9)
                    System.out
                            .println("&&&&& In CaseJobTaskManager::processQueue process waitTasks the type of TASK object: "
                                    + copyTask.getClass().getName());

                tempWaitTable.put(copyTask.getTaskId(), copyTask);
            }
        }// synchronized (waitTable)

        // Reuse the waittasks collection
        waitTasks = tempWaitTable.values();

        if (DebugLevels.DEBUG_0)
            System.out.println("Number of waitTasks acquired from waitTable: " + waitTasks.size());

        // iterate over the tasks in the waitTable and find as many that can
        // be run in all available threads

        Iterator<Task> iter = waitTasks.iterator();
        while (iter.hasNext()) {

            // number of threads available before inspecting the waiting list
            synchronized (threadPool) {
                threadsAvail = threadPool.getCorePoolSize() - runTable.size();
            }

            if (DebugLevels.DEBUG_0)
                System.out.println("Number of threads available before waiting list-table: " + threadsAvail);

            if (threadsAvail > 0) {
                CaseJobTask tsk = (CaseJobTask) iter.next();

                if (DebugLevels.DEBUG_9)
                    System.out
                            .println("&&&&& In CaseJobTaskManager::processQueue run a waitTasks the type of TASK object: "
                                    + tsk.getClass().getName());
                System.out.println(tsk.taskId);

                if (DebugLevels.DEBUG_0)
                    System.out.println("Is the caseJobTask null? " + (tsk == null));
                if (DebugLevels.DEBUG_0)
                    System.out.println("Is this task ready? " + tsk.isReady());

                // look at this waitTable element and see if it isReady()==true
                if (tsk.isReady()) {
                    if (DebugLevels.DEBUG_0)
                        System.out.println("WAIT TABLE Before Moving Task from WAIT to RUN: " + waitTable.size());
                    if (DebugLevels.DEBUG_0)
                        System.out.println("RUN TABLE Before Moving Task from WAIT to RUN: " + runTable.size());
                    if (DebugLevels.DEBUG_0)
                        System.out.println("#THREADS Before Moving Task from WAIT to RUN: " + threadsAvail);

                    // remove from waitTable
                    synchronized (waitTable) {
                        waitTable.remove(tsk.getTaskId());
                    }// synchronized (wait Table)

                    // add to runTable
                    synchronized (runTable) {
                        runTable.put(tsk.getTaskId(), tsk);
                    }// synchronized (runTable)

                    // runTask and decrement threadsAvail
                    synchronized (threadPool) {
                        threadPool.execute(tsk);
                    }// synchronized (wait Table)

                    synchronized (threadPool) {
                        threadsAvail = threadPool.getCorePoolSize() - runTable.size();
                    }

                    if (DebugLevels.DEBUG_0)
                        System.out.println("WAITTABLE After Moving Task from WAIT to RUN: " + waitTable.size());
                    if (DebugLevels.DEBUG_0)
                        System.out.println("RUNTABLE After Moving Task from WAIT to RUN: " + runTable.size());
                    if (DebugLevels.DEBUG_0)
                        System.out.println("#THREADS After Moving Task from WAIT to RUN: " + threadsAvail);

                }

            } else {
                if (DebugLevels.DEBUG_0)
                    System.out.println("#THREADS == 0?? Breaking out of WAIT TEST loop: " + threadsAvail);
                break;

            }
        }// while wait Tasks iterator

        if (DebugLevels.DEBUG_0)
            System.out.println(">>>>> END CaseJobTaskManager::processTaskQueue() *** " + new Date());
        if (DebugLevels.DEBUG_0)
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

    }

    public static void callBackFromExportJobSubmitter(String cjtId, String status, String mesg) throws EmfException {
        CaseJobTask cjt = null;

        System.out.println("CaseJobTaskManager::callBackFromExportJobSubmitter for caseJobTask= " + cjtId + " status= "
                + status + " and message= " + mesg);
        synchronized (waitTable) {
            if (DebugLevels.DEBUG_9)
                System.out.println("Size of the wait Table: " + waitTable.size());
            cjt = (CaseJobTask) waitTable.get(cjtId);
            if (DebugLevels.DEBUG_9)
                System.out.println("For cjtId=" + cjtId + " Is the CaseJobTaksk null? " + (cjt == null));
        }

        if (cjt != null) {

            if (status.equals("completed")) {
                cjt.setExportsSuccess(true);
                updateRunStatus(cjtId, "export succeeded");
            }

            if (status.equals("failed")) {
                cjt.setExportsSuccess(false);
                updateRunStatus(cjtId, "export failed");
            }

        }

        processTaskQueue();

    }

    /**
     * Loop over all the waiting tasks. Test and set the dependencies of each task in the waitTable
     * 
     */
    private static synchronized void testAndSetWaitingTasksDependencies() throws EmfException {

        //Get all the waiting CaseJobTasks in the waitTable
        Collection<Task> allWaitTasks = waitTable.values();
        Iterator<Task> iter = allWaitTasks.iterator();

        //Loop over all the waiting CaseJobTasks tasks
        while (iter.hasNext()) {
            
            CaseJobTask cjt = (CaseJobTask) iter.next();

            // For this CaseJobTask if dependencies have not been set yet
            if (!(cjt.isDependenciesSet())) {
                // get the caseJob
                CaseJob caseJob = caseDAO.getCaseJob(cjt.getJobId());
                // get the dependents of this caseJob
                DependentJob[] dependJobs = caseJob.getDependentJobs();

                // the caseJob has no dependencies therefore set to True
                if ((dependJobs == null) || (dependJobs.length == 0)) {
                    cjt.setDependenciesSet(true);
                } else {
                    // this CaseJob has dependents
//                    ArrayList<CaseJob> allDependentCaseJobs = new ArrayList<CaseJob>();

                    int nonFinalD = 0;
                    int foNSD = 0;
                    int compD = 0;

                    int TotalD = dependJobs.length;

                    // Loop over dependetJobs and add the corresponding CaseJob to the allDependentCaseJobs
                    for (int i = 0; i < dependJobs.length; i++) {
                        DependentJob dJob = dependJobs[i];

                        CaseJob dcj = caseDAO.getCaseJob(dJob.getJobId());
                        JobRunStatus jrs = dcj.getRunstatus();

                        String status = jrs.getName();

                        if ((status.equals("Not Started")) || (status.equals("Failed"))) {
                            foNSD++;
                        } else if (status.equals("Completed")) {
                            compD++;
                        } else {
                            nonFinalD++;
                        }

//                        allDependentCaseJobs.add(dcj);

                    }// for dependentJobs

                    // If none of the jobs are in a non-Final state
                    if (nonFinalD == 0) {
                        // all our jobs are in a final=completed state
                        if (compD == TotalD) {
                            cjt.setDependenciesSet(true);
                        } else {
                            // We have a parent job that has atleast one dependent job that has failed or
                            // not started therefore send an error message to the user's status window
                            // log a failed jobstatus to the casejobs table
                            // and remove the corresponding casejobtask from the waitTable
                            User user = caseJob.getUser();

                            // set the CaseJob jobstatus (casejob table) to Failed
                            updateRunStatus(cjt.getTaskId(), "failed");

                            String message = "Job name= " + cjt.getJobName()
                                    + " failed due to at least one dependent jobs state = Failed or Not Started";

                            // set the status in the user's status window
                            setStatus(user, statusDAO, message);

                            // now remove the job with bad dependencies from the waitTable
                            synchronized (waitTable) {
                                waitTable.remove(cjt.getTaskId());
                            }

                        }
                    }

                }// CJT had dependents
            }// cjt dependencies was false


        }// loop over all waiting tasks

    }

    protected static synchronized void setStatus(User user, StatusDAO statusServices, String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Export");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusServices.add(endStatus);
    }

    /**
     *  There was a change in the status of a waiting job so process the Queue 
     */
    public static synchronized void callBackFromJobRunServer()
            throws EmfException {

        processTaskQueue();
    }
}
