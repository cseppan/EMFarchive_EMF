package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.framework.services.casemanagement.CaseJobTask;

import java.util.Collection;
import java.util.Date;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CaseJobTaskManager extends TaskManager {
    private static Log log = LogFactory.getLog(CaseJobTaskManager.class);

    private static CaseJobTaskManager ref;

    private CaseJobTaskManager() {
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

    }

    public static TaskManager getCaseJobTaskManager() {
        if (ref == null)
            ref = new CaseJobTaskManager();
        return ref;
    }

    public static synchronized void addTasks(ArrayList<Runnable> tasks) {
        // TaskManager.resetIdleTime();
        System.out.println("IN CaseJobTaskManager number of tasks received= " + tasks.size());
        taskQueue.addAll(tasks);
        System.out.println("IN CaseJobTaskManager size of task Queue= " + taskQueue.size());

        synchronized (CaseJobTaskManager.runTable) {
            if (threadPool.getCorePoolSize() - runTable.size() > 0) {
                CaseJobTaskManager.processTaskQueue();
            }
        }// synchronized
    }

    public synchronized static void callBackFromThread(String taskId, String submitterId, String status, String mesg) {
        if (DebugLevels.DEBUG_2)
            System.out.println("CaseJobTaskManager::callBackFromThread  refCount= " + refCount);
        if (DebugLevels.DEBUG_2)
            System.out.println("%%%% CaseJobTaskManager reports that Task# " + taskId + " for submitter= "
                    + submitterId + " completed with status= " + status + " and message= " + mesg);

        if (DebugLevels.DEBUG_0)
            System.out.println("***BELOW*** In callback the sizes are ***BELOW***");
        if (DebugLevels.DEBUG_0)
            System.out.println("Size of PBQ taskQueue: " + CaseJobTaskManager.getSizeofTaskQueue());
        if (DebugLevels.DEBUG_0)
            System.out.println("Size of WAIT TABLE: " + CaseJobTaskManager.getSizeofWaitTable());
        if (DebugLevels.DEBUG_0)
            System.out.println("Size of RUN TABLE: " + CaseJobTaskManager.getSizeofRunTable());
        if (DebugLevels.DEBUG_0)
            System.out.println("***ABOVE*** In callback the sizes are shown ***ABOVE***");
        runTable.remove(taskId);
        if (DebugLevels.DEBUG_0)
            System.out.println("After Task removed Size of RUN TABLE: " + CaseJobTaskManager.getSizeofRunTable());

        CaseJobTaskManager.processTaskQueue();
    }

    public static synchronized void processTaskQueue() {
        int threadsAvail = -99;
        if (DebugLevels.DEBUG_0)
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        if (DebugLevels.DEBUG_0)
            System.out.println(">>>>> BEGIN CaseJobTaskManager::processTaskQueue() *** " + new Date());
        if (DebugLevels.DEBUG_0)
            System.out.println("Size of PBQ taskQueue: " + CaseJobTaskManager.getSizeofTaskQueue());
        if (DebugLevels.DEBUG_0)
            System.out.println("Size of WAIT TABLE: " + CaseJobTaskManager.getSizeofWaitTable());
        if (DebugLevels.DEBUG_0)
            System.out.println("Size of RUN TABLE: " + CaseJobTaskManager.getSizeofRunTable());

        // if (DebugLevels.DEBUG_4)
        // System.out.println("# of tasks in Thread Pool size: " + threadPool.getPoolSize());
        //
        // if (DebugLevels.DEBUG_5)
        // System.out.println("Active Thread Count= " + threadPool.getActiveCount());
        //        
        // if (DebugLevels.DEBUG_5)
        // System.out.println("Core pool size: " + threadPool.getCorePoolSize());
        //        
        // if (DebugLevels.DEBUG_5)
        // System.out.println("Maximum pool size= " + threadPool.getMaximumPoolSize());
        //        
        // if (DebugLevels.DEBUG_5)
        // System.out.println("Threads available for processing= "
        // + (threadPool.getCorePoolSize() - threadPool.getPoolSize()));
        //        
        // if (DebugLevels.DEBUG_5)
        // System.out.println("Completed task count: " + threadPool.getCompletedTaskCount());
        //        
        // if (DebugLevels.DEBUG_5)
        // System.out.println("TASK COUNT: " + threadPool.getTaskCount());
        //        
        // if (DebugLevels.DEBUG_3)
        // System.out.println("ACTIVE TASK COUNT: " + threadPool.getActiveCount());

        if (DebugLevels.DEBUG_0)
            System.out.println("Before processing the taskQueue");
        if (DebugLevels.DEBUG_0)
            System.out.println("SIZE OF TASKQUEUE: " + CaseJobTaskManager.getSizeofTaskQueue());

        boolean done = false;
        while (!done) {
            if (CaseJobTaskManager.getSizeofTaskQueue() == 0) {
                if (DebugLevels.DEBUG_0)
                    System.out.println("#tasks in taskQueue == 0?? Breaking out of taskQueue TEST loop: ");
                done = true;
            } else {
                if (DebugLevels.DEBUG_0)
                    System.out.println("Before Peak into taskQueue: " + CaseJobTaskManager.getSizeofTaskQueue());
                if (taskQueue.peek() != null) {
                    if (DebugLevels.DEBUG_0)
                        System.out.println("Peak into taskQueue has an object in head: "
                                + CaseJobTaskManager.getSizeofTaskQueue());

                    try {
                        CaseJobTask nextTask = (CaseJobTask) taskQueue.take();
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
            System.out.println("Size of PBQ taskQueue: " + CaseJobTaskManager.getSizeofTaskQueue());
        if (DebugLevels.DEBUG_0)
            System.out.println("Size of WAIT TABLE: " + CaseJobTaskManager.getSizeofWaitTable());
        if (DebugLevels.DEBUG_0)
            System.out.println("Size of RUN TABLE: " + CaseJobTaskManager.getSizeofRunTable());

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
                System.out.println(tsk.taskId);
                if (DebugLevels.DEBUG_0)
                    System.out.println("Is the caseJobTask null? " + (tsk == null));
                if (DebugLevels.DEBUG_0)
                    System.out.println("Is this task ready? " + tsk.isReady());

                // look at this waitTable element and see if it isReady()==true
                if (true){
                //                if (tsk.isReady()) {
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

    public static void callBackFromExportJobSubmitter(String cjtId, String status, String mesg) {

        System.out.println("CaseJobTaskManager::callBackFromExportJobSubmitter for caseJobTask= " + cjtId + " status= "
                + status + " and message= " + mesg);
        CaseJobTask cjt = (CaseJobTask) waitTable.get(cjtId);

        if (status.equals("completed"))
            cjt.setExportsSuccess(true);
        if (status.equals("failed"))
            cjt.setExportsSuccess(false);

        // FIXME: expand the dependencies logic with Alexis
        cjt.setDependenciesSet(true);

        
        CaseJobTaskManager.processTaskQueue();

    }

}
