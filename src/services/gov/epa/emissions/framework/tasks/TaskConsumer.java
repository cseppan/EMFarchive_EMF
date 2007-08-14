package gov.epa.emissions.framework.tasks;

import java.util.TimerTask;

public class TaskConsumer extends TimerTask {

    /**
     * 
     */
    public TaskConsumer() {
        // No arg constructor
    }

    public TaskConsumer(ExportTaskManager tm) {
        if (DebugLevels.DEBUG_1)
            System.out.println("Export Task Consumer created @@@@@ in THREAD ID: " + Thread.currentThread().getId());

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */

    public void run() {
        if (DebugLevels.DEBUG_0)
            System.out.println("Task Consumer is awake and processing");
        if (DebugLevels.DEBUG_0)
            System.out.println("Export Task Consumer RUNNING @@@@@ THREAD ID: " + Thread.currentThread().getId());

        synchronized (TaskManager.runTable) {
            if (TaskManager.runTable.size() == 0) {
                ExportTaskManager.processTaskQueue();
            }
        }// synchronized

        if (DebugLevels.DEBUG_1)
            System.out.println("Consumer done processing...Timer timing out and sleeping");

    }

    @Override
    protected void finalize() throws Throwable {
        this.cancel();
        super.finalize();
    }

}
