package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseDAO;
import gov.epa.emissions.framework.services.casemanagement.CaseJobTask;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
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
import org.hibernate.Session;

public class CaseJobTaskManager implements TaskManager {
    private static Log log = LogFactory.getLog(CaseJobTaskManager.class);

    private static HibernateSessionFactory sessionFactory = null;

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

        // this.sessionFactory=sessionFactory;

    }

    public static synchronized CaseJobTaskManager getCaseJobTaskManager(HibernateSessionFactory sessionFactory) {
        if (ref == null)
            ref = new CaseJobTaskManager(sessionFactory);
        return ref;
    }

    public static synchronized void addTasks(ArrayList<Runnable> tasks) {
        // TaskManager.resetIdleTime();
        Iterator iter = tasks.iterator();
        while (iter.hasNext()) {
            Task tsk = (Task) iter.next();
            if (DebugLevels.DEBUG_9)
                System.out.println("&&&&& In CaseJobTaskManager::addTasks the types of TASK objects coming in are: "
                        + tsk.getClass().getName());
        }

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

        CaseJobTask cjt = null;
        Session session = sessionFactory.getSession();
        CaseDAO dao = new CaseDAO();
        String jobStatus = "";

        try {
            if ((status.equals("completed")) || (status.equals("failed"))) {

                synchronized (runTable) {
                    cjt = (CaseJobTask) runTable.get(taskId);

                    runTable.remove(taskId);
                }
            }

            if (status.equals("export failed")) {
                synchronized (waitTable) {
                    cjt = (CaseJobTask) waitTable.get(taskId);
                    waitTable.remove(taskId);
                }
            }

            if (status.equals("export succeeded")) {
                synchronized (waitTable) {
                    cjt = (CaseJobTask) waitTable.get(taskId);
                }
            }

            // update the run status in the Case_CaseJobs
            int jid = cjt.getJobId();
            CaseJob caseJob = dao.getCaseJob(jid, session);

            if (status.equals("completed")) {
                jobStatus = "Submitted";
                caseJob.setRunStartDate(new Date());
            }

            if (status.equals("failed")) {
                jobStatus = "Failed";
                caseJob.setRunCompletionDate(new Date());
            }

            if (status.equals("export succeeded")) {
                jobStatus = "Waiting";
                caseJob.setRunStartDate(new Date());
            }

            JobRunStatus jrStat = dao.getJobRunStatuse(jobStatus, session);
            caseJob.setRunstatus(jrStat);

            dao.updateCaseJob(caseJob, session);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public static synchronized void processTaskQueue() {
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

            // FIXME: expand the dependencies logic with Alexis
            cjt.setDependenciesSet(true);

        }

        processTaskQueue();

    }

}
