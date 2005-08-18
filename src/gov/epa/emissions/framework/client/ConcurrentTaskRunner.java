package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.TaskRunner;

//TODO: use JDK 1.5 concurrency utilties to test 
public class ConcurrentTaskRunner implements TaskRunner {

    private final Object mutex = new Object();

    private boolean alive = false;

    // FIXME: use jdk 1.5 concurrent utlities for synchronization
    public void start(Runnable runnable) {
        synchronized (mutex) {
            alive = true;

            while (alive) {
                // FIXME: use thread pool to run
                new Thread(runnable).start();
                try {
                    mutex.wait(500);
                } catch (InterruptedException e) {
                    alive = false;
                }
            }
        }

    }

    public void stop() {
        synchronized (mutex) {
            this.alive = false;
            mutex.notify();
        }
    }

}
