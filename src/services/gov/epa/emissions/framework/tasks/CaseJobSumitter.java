package gov.epa.emissions.framework.tasks;
import java.util.ArrayList;


public class CaseJobSumitter implements TaskSubmitter {

    public void addTasksToSubmitter(ArrayList<Runnable> tasksForSubmitter) {
        // NOTE Auto-generated method stub
        
    }

    public void callbackFromRunManager(String taskId, String status, String mesg) {
        // NOTE Auto-generated method stub
        
    }

    public void cancelTasks(ArrayList<Runnable> tasks) {
        // NOTE Auto-generated method stub
        
    }

    public void deregisterSubmitterFromRunManager(TaskSubmitter ts) {
        // NOTE Auto-generated method stub
        
    }

    public String getSubmitterId() {
        // NOTE Auto-generated method stub
        return null;
    }

    public int getTaskCount() {
        // NOTE Auto-generated method stub
        return 0;
    }

    public void submitTasksToRunManager(String submitterId, ArrayList<Runnable> tasks) {
        // NOTE Auto-generated method stub
        
    }

    public void updateStatus(Runnable task) {
        // NOTE Auto-generated method stub
        
    }


}
