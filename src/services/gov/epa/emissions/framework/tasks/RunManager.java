package gov.epa.emissions.framework.tasks;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class RunManager {
    static int refCount = 0;

    protected final int poolSize = 4;

    protected final int maxPoolSize = 4;

    protected final long keepAliveTime = 60;

    protected static long idleTime = 0;

    protected static TaskConsumer consumer = null;

    protected static Timer taskManagerTimer = null;

    protected static ArrayList<TaskSubmitter> submitters = new ArrayList<TaskSubmitter>();

    protected static ThreadPoolExecutor threadPool = null;

    // PBQ is the queue for submitting jobs
    protected final static BlockingQueue<Runnable> taskQueue = new PriorityBlockingQueue<Runnable>();

    protected final static ArrayBlockingQueue<Runnable> threadPoolQueue = new ArrayBlockingQueue<Runnable>(5);

    protected final static Hashtable<String, Task> runTable = new Hashtable<String, Task>();

    protected final static Hashtable<String, Task> waitTable = new Hashtable<String, Task>();

    public synchronized static int getSizeofTaskQueue() {
        return taskQueue.size();
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

    public synchronized static void resetIdleTime() {
        idleTime = 0;
    }

    public static long getIdleTime() {
        return idleTime;
    }

    public static synchronized void setIdleTime(long idleTime) {
        RunManager.idleTime = idleTime;
    }

    @Override
    protected synchronized void finalize() throws Throwable {
        if (DebugLevels.DEBUG_0) System.out.println("Finalizing RunManager # of taskmanagers= " + RunManager.refCount);

        RunManager.shutDown();
        //consumer.finalize();
        super.finalize();
    }

    // clone not supported needs to be added
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();

    }

}
