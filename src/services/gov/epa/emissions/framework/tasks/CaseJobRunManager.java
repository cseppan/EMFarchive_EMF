package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.framework.services.casemanagement.CaseJobTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CaseJobRunManager extends RunManager {
    private static Log log = LogFactory.getLog(CaseJobRunManager.class);
    private static CaseJobRunManager ref;

    private CaseJobRunManager() {
        super();
        log.info("CaseJobRunManager constructor");
        if (DebugLevels.DEBUG_1)
            System.out.println("Export Task Manager created @@@@@ THREAD ID: " + Thread.currentThread().getId());

        refCount++;
        if (DebugLevels.DEBUG_4)
            System.out.println("Task Manager created refCount= " + refCount);
        if (DebugLevels.DEBUG_4)
            System.out.println("Priority Blocking queue created? " + !(taskQueue == null));

        threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, threadPoolQueue);
        if (DebugLevels.DEBUG_4)
            System.out.println("ThreadPool created? " + !(threadPool == null));
        if (DebugLevels.DEBUG_4)
            System.out.println("Initial # of jobs in Thread Pool: " + threadPool.getPoolSize());

    }

    public static RunManager getCaseJobRunManager() {
        if (ref == null)
            ref = new CaseJobRunManager();
        return ref;
    }

    public static synchronized void addTasks(ArrayList<Runnable> tasks) {
        RunManager.resetIdleTime();
        taskQueue.addAll(tasks);

        synchronized (RunManager.runTable) {
            if (RunManager.runTable.size() == 0) {
                CaseJobRunManager.processTaskQueue();
            }
        }// synchronized
    }
 
    public synchronized static void callBackFromThread(String taskId, String submitterId, String status, String mesg) {
        if (DebugLevels.DEBUG_2)
            System.out.println("CaseJobRunManager refCount= " + refCount);
        if (DebugLevels.DEBUG_2)
            System.out.println("%%%% CaseJobRunManager reports that Task# " + taskId + " for submitter= " + submitterId
                    + " completed with status= " + status + " and message= " + mesg);
        Iterator<TaskSubmitter> iter = submitters.iterator();
        while (iter.hasNext()) {
            TaskSubmitter submitter = iter.next();
            if (submitterId.equals(submitter.getSubmitterId())) {
                if (DebugLevels.DEBUG_2)
                    System.out.println(">>@@ Found a submitter in the taskmanager collection of submitters");
                submitter.callbackFromRunManager(taskId, status, mesg);
            }
        }
        CaseJobRunManager.processTaskQueue();
    }

  public static synchronized void processTaskQueue() {
      if (CaseJobRunManager.taskQueue.size()>0){
          CaseJobTask cjt = (CaseJobTask) taskQueue.peek();
          System.out.println("Found a CaseJobTask: " + cjt.taskId);
          System.out.println("Submitting CJT to threadpool for execution");
          threadPool.execute(cjt);
          System.out.println("Submitted CJT for execution");
      }
      
  }
    
//    public static synchronized void processTaskQueue() {
//        int threadsAvail = -99;
//
//        if (DebugLevels.DEBUG_0)
//            System.out.println("*** BEGIN CaseJobRunManager::processTaskQueue() *** " + new Date());
//        if (DebugLevels.DEBUG_0)
//            System.out.println("Size of PBQ taskQueue: " + CaseJobRunManager.taskQueue.size());
//        if (DebugLevels.DEBUG_0)
//            System.out.println("Size of WAIT TABLE: " + CaseJobRunManager.waitTable.size());
//        if (DebugLevels.DEBUG_0)
//            System.out.println("Size of RUN TABLE: " + CaseJobRunManager.runTable.size());
//
//        if (DebugLevels.DEBUG_0)
//            System.out.println("Number of tasks left in queue: " + CaseJobRunManager.getSizeofTaskQueue());
//
//        if (DebugLevels.DEBUG_4)
//            System.out.println("# of tasks in Thread Pool size: " + threadPool.getPoolSize());
//        if (DebugLevels.DEBUG_5)
//            System.out.println("Active Thread Count= " + threadPool.getActiveCount());
//        if (DebugLevels.DEBUG_5)
//            System.out.println("Core pool size: " + threadPool.getCorePoolSize());
//        if (DebugLevels.DEBUG_5)
//            System.out.println("Maximum pool size= " + threadPool.getMaximumPoolSize());
//        if (DebugLevels.DEBUG_5)
//            System.out.println("Threads available for processing= "
//                    + (threadPool.getCorePoolSize() - threadPool.getPoolSize()));
//        if (DebugLevels.DEBUG_5)
//            System.out.println("Completed task count: " + threadPool.getCompletedTaskCount());
//        if (DebugLevels.DEBUG_5)
//            System.out.println("TASK COUNT: " + threadPool.getTaskCount());
//        if (DebugLevels.DEBUG_3)
//            System.out.println("ACTIVE TASK COUNT: " + threadPool.getActiveCount());
//
//        if (DebugLevels.DEBUG_1)
//            System.out.println("SIZE OF waiting list-table: " + waitTable.size());
//
//        // iterate over the tasks in the waitTable and find as many that can
//        // be run in all available threads
//        Collection<Task> waitTasks = waitTable.values();
//        if (DebugLevels.DEBUG_0)
//            System.out.println("Number of waitTasks acquired from waitTable: " + waitTasks.size());
//
//        Iterator<Task> iter = waitTasks.iterator();
//
//        while (iter.hasNext()) {
//            // number of threads available before inspecting the waiting list
//            // synchronized (threadPool) {
//            //threadsAvail = threadPool.getCorePoolSize() - threadPool.getActiveCount();
//            threadsAvail = threadPool.getCorePoolSize() - runTable.size();
//            if (DebugLevels.DEBUG_0)
//                System.out.println("Number of threads available before waiting list-table: " + threadsAvail);
//            // }
//
//            if (threadsAvail > 0) {
//                CaseJobTask tsk = (CaseJobTask) iter.next();
//
//                // look at this waitTable element and see if it is exportEquivalent (same Absolute Path)
//                // to any of the tasks currently in the runTable
//                // synchronized (waitTable) {
//                if (notEqualsToAnyRunTask(tsk)) {
//                    if (DebugLevels.DEBUG_0)
//                        System.out.println("WAIT TABLE Before Moving Task from WAIT to RUN: " + waitTable.size());
//                    if (DebugLevels.DEBUG_0)
//                        System.out.println("RUN TABLE Before Moving Task from WAIT to RUN: " + runTable.size());
//                    if (DebugLevels.DEBUG_0)
//                        System.out.println("#THREADS Before Moving Task from WAIT to RUN: " + threadsAvail);
//
//                    // remove from waitTable
//                    waitTable.remove(tsk.getTaskId());
//
//                    // add to runTable
//                    runTable.put(tsk.getTaskId(), tsk);
//
//                    // runTask and decrement threadsAvail
//                    threadPool.execute(tsk);
////                    threadsAvail--;
//
//                    if (DebugLevels.DEBUG_0)
//                        System.out.println("WAITTABLE After Moving Task from WAIT to RUN: " + waitTable.size());
//                    if (DebugLevels.DEBUG_0)
//                        System.out.println("RUNTABLE After Moving Task from WAIT to RUN: " + runTable.size());
//                    if (DebugLevels.DEBUG_0)
//                        System.out.println("#THREADS After Moving Task from WAIT to RUN: " + threadsAvail);
//
//                }
//                // }// synchronized (...)
//
//            } else {
//                if (DebugLevels.DEBUG_0)
//                    System.out.println("#THREADS == 0?? Breaking out of WAIT TEST loop: " + threadsAvail);
//                break;
//            }
//
//        }
//
//        if (DebugLevels.DEBUG_0)
//            System.out.println("SIZE OF TASKQUEUE: " + CaseJobRunManager.getSizeofTaskQueue());
//        boolean done = false;
//        while (!done) {
//            if (RunManager.taskQueue.size() == 0) {
//                if (DebugLevels.DEBUG_0)
//                    System.out.println("#tasks in taskQueue == 0?? Breaking out of taskQueue TEST loop: ");
//                done = true;
//            } else {
//                if (DebugLevels.DEBUG_0)
//                    System.out.println("Before Peak into taskQueue: " + taskQueue.size());
//                if (taskQueue.peek() != null) {
//                    if (DebugLevels.DEBUG_0)
//                        System.out.println("Peak into taskQueue has an object in head: " + taskQueue.size());
//
//                    try {
//                        CaseJobTask nextTask = (CaseJobTask) taskQueue.take();
//                        // number of threads available before inspecting the priority blocking queue
//                        // synchronized (threadPool) {
////                        threadsAvail = threadPool.getCorePoolSize() - threadPool.getActiveCount();
//                        threadsAvail = threadPool.getCorePoolSize() - runTable.size();
//
//                        // }// synchronized
//                        if (DebugLevels.DEBUG_0)
//                            System.out.println("Number of threads available before taskQueue PDQ: " + threadsAvail);
//
//                        if (threadsAvail == 0) {
//                            // synchronized (waitTable) {
//                            waitTable.put(nextTask.getTaskId(), nextTask);
//                            // }// synchronized
//                        } else {
//                            if (notEqualsToAnyRunTask(nextTask)) {
//
//                                // add to runTable
//                                // synchronized (runTable) {
//                                runTable.put(nextTask.getTaskId(), nextTask);
//                                // }// synchronized
//
//                                // runTask and decrement threadsAvail
//                                threadPool.execute(nextTask);
//                                // synchronized (threadPool) {
//                                //threadsAvail--;
//                                // }// synchronized
//                            } else {
//                                // synchronized (waitTable) {
//                                waitTable.put(nextTask.getTaskId(), nextTask);
//                                // }// synchronized
//                            }
//
//                        }
//                    } catch (InterruptedException e) {
//
//                        e.printStackTrace();
//                    }
//                }
//            }// more tasks in taskQueue
//
//        }// while not done
//
//        // Now do other processing and maintenance work
//        // if taskQueue is empty then this processing will be the only thing that happens
//        // during this callback
//
//        // TODO:
//
//        if (DebugLevels.DEBUG_0)
//            System.out.println("*** END CaseJobRunManager::processTaskQueue() *** " + new Date());
//
//    }

//    private static boolean notEqualsToAnyRunTask(CaseJobTask nextTask) {
//        // NOTE Auto-generated method stub
//        return false;
//    }


}
