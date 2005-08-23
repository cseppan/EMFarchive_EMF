package gov.epa.emissions.framework;

//TODO: use JDK 1.5 concurrency utilties to test 
public class ConcurrentTaskRunner implements TaskRunner {

    private final Object mutex = new Object();

    private boolean alive = false;

    private TaskDelegate delegate;

    // FIXME: use jdk 1.5 concurrent utlities for synchronization
    public void start(Runnable task) {
        alive = true;
        this.delegate = new TaskDelegate(task);
        Thread thread = new Thread(delegate);// FIXME: use thread pool
        thread.start();
    }

    public void stop() {
        synchronized (mutex) {
            this.alive = false;
            mutex.notify();
        }
    }

    public class TaskDelegate implements Runnable {

        private Runnable task;

        public TaskDelegate(Runnable task) {
            this.task = task;
        }

        public void run() {
            synchronized (mutex) {
                while (alive) {
                    task.run();
                    try {
                        // TODO: what's a reasonable polling time ? 
                        mutex.wait(5000);
                    } catch (InterruptedException e) {
                        alive = false;
                    }
                }
            }
        }

    }
}
