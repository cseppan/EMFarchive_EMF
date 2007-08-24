package gov.epa.emissions.framework.tasks;

public class RunManagerFactory {
	private static RunManagerFactory ref;
	
	private RunManagerFactory(){
		System.out.println();
	}
	
	// Singleton factory method
	public static synchronized RunManagerFactory getTaskManagerFactory() {
		if (ref == null)
			// it's ok, we can call this constructor
			ref = new RunManagerFactory();
		return ref;
	}

	public static synchronized ExportTaskRunManager getExportTaskRunManager(){
		return (ExportTaskRunManager) ExportTaskRunManager.getExportTaskRunManager();
	}
	
    public static synchronized CaseJobRunManager getCaseJobRunManager(){
        return (CaseJobRunManager) CaseJobRunManager.getCaseJobRunManager();
    }
	
}
