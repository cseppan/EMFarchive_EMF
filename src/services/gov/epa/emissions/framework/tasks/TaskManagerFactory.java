package gov.epa.emissions.framework.tasks;

public class TaskManagerFactory {
	private static TaskManagerFactory ref;
	
	private TaskManagerFactory(){
		System.out.println();
	}
	
	// Singleton factory method
	public static synchronized TaskManagerFactory getTaskManagerFactory() {
		if (ref == null)
			// it's ok, we can call this constructor
			ref = new TaskManagerFactory();
		return ref;
	}

	public static synchronized ExportTaskManager getExportTaskManager(){
		return (ExportTaskManager) ExportTaskManager.getExportTaskManager();
	}
	
    public static synchronized CaseJobTaskManager getCaseJobTaskManager(){
        return (CaseJobTaskManager) CaseJobTaskManager.getCaseJobTaskManager();
    }
	
}
