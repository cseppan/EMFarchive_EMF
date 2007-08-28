package gov.epa.emissions.framework.tasks;
import java.util.ArrayList;
import java.util.Date;


public class CaseJobSumitter implements TaskSubmitter {
    private static int svcCount = 0;

    private String svcLabel = null;

    public String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    protected String submitterId;

    protected ArrayList<Runnable> caseJobTasks = null;

    public CaseJobSumitter() {
        myTag();
        submitterId = svcLabel;
        System.out.println("CaseJobSubmitter myTag called: " + submitterId);
        caseJobTasks = new ArrayList<Runnable>();

    }

    public void addTasksToSubmitter(ArrayList<Runnable> tasksForSubmitter) {
        System.out.println("CaseJobSubmitter::addTasksToSubmitter Total number of tasks= " + tasksForSubmitter.size());
        this.submitTasksToRunManager(submitterId, tasksForSubmitter);
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
        return this.submitterId;
    }

    public int getTaskCount() {
        // NOTE Auto-generated method stub
        return 0;
    }

    public synchronized void submitTasksToRunManager(String submitterId, ArrayList<Runnable> tasks) {
        if (DebugLevels.DEBUG_0)
            System.out.println("In submitter::submitTasksToTaskManager= " + this.getSubmitterId());
        if (DebugLevels.DEBUG_0)
            System.out.println("In submitter::submitTasksToTaskManager # of elements in param array= " + tasks.size());

        if (DebugLevels.DEBUG_0)
            System.out.println("Submitter::exportTasks before ADD: " + this.submitterId + " has task count= "
                    + this.caseJobTasks.size());
//        if (DebugLevels.DEBUG_0)
//            System.out.println("SUBMITTER::submittedtasks before ADD: " + this.submitterId + " has task count= "
//                    + this.submittedTasks.size());

        RunManagerFactory.getCaseJobRunManager().addTasks(tasks);

        // FIXME: May not need to do this next step since submitted Table is uptodate
//        submittedTasks.addAll(tasks);

        // Remove all tasks from exportTasks and keep it available for new submissions if necessary
        caseJobTasks.removeAll(tasks);

        if (DebugLevels.DEBUG_0)
            System.out.println("Submitter::submitTasksToRunManager after ADD: " + this.submitterId + " has task count= "
                    + this.caseJobTasks.size());

//        if (DebugLevels.DEBUG_0)
//            System.out.println("SUBMITTER::submittedtasks after ADD: " + this.submitterId + " has task count= "
//                    + this.submittedTasks.size());

    }


    public void updateStatus(Runnable task) {
        // NOTE Auto-generated method stub
        
    }


}
