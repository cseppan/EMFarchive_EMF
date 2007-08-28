package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.security.User;
//import gov.epa.emissions.framework.services.basic.RemoteCommand;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.Task;

//import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CaseJobTask extends Task {
    private static Log log = LogFactory.getLog(CaseJobTask.class);

    private User user = null;

 //   private String runRedirect = ">&"; // shell specific redirect

    String logFile = null;
    String jobFile = null;
    String queueOptions = null;
    String hostName = null;

    int jobId;
    String jobName = null;
    int caseId;
        
    public CaseJobTask(int jobId, int caseId, User user) {
        super();
        createId();
        if (DebugLevels.DEBUG_1)
            System.out.println(">>>> " + createId());
        log.info("created a CaseJobTask: " + this.taskId);
        this.user = user;
        this.jobId=jobId;
        this.caseId=caseId;
    }

    public void run() {
        System.out.println("@@@@ CASE job task RUNNING jobId= " + jobId + " jobName= " + jobName + " caseId= " + caseId + " CaseJobTask id= " + this.getTaskId() + " now running in Thread id= " + Thread.currentThread().getId());
//        String executionStr = null;
//        String qOptions = this.queueOptions;
//        
//        // Create an execution string and submit job to the queue,
//        // if the key word $EMF_JOBLOG is in the queue options,
//        // replace w/ log file
//        String queueOptionsLog = qOptions.replace("$EMF_JOBLOG", this.logFile);
//
//        if (queueOptionsLog.equals("")) {
//            executionStr = this.jobFile;
//        } else {
//            executionStr = queueOptionsLog + " " + this.jobFile;
//        }
//
//        /*
//         * execute the job script Note if hostname is localhost this is done locally w/o ssh and stdout and stderr is
//         * redirected to the log. This redirect is currently shell specific (should generalize) if hostname is not
//         * localhost it is through ssh
//         */
//        String username = this.user.getUsername();
//        try{
//            if (hostName.equals("localhost")) {
//                // execute on local machine
//                executionStr = executionStr + " " + this.runRedirect + " " + this.logFile;
//      
//                RemoteCommand.executeLocal(executionStr);
//                            
//            } else {
//                // execute on remote machine and log stdout
//                InputStream inStream = RemoteCommand.execute(username, hostName, executionStr);
//                
//                String outTitle = "stdout from (" + hostName + "): " + executionStr;
//                RemoteCommand.logStdout(outTitle, inStream);
//                
//                // capture PBSqueueId and send back to case job submitter
//                //TODO:
//            }
//            
//        }catch(Exception e){
//            log.error("Error executing job file: " + jobFile + " Execution string= " + executionStr);
//            e.printStackTrace();
//        }
// 
    }

    public void setRunRedirect(String runRedirect) {
//        this.runRedirect = runRedirect;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public void setJobFile(String jobFile) {
        this.jobFile = jobFile;
    }

    public void setQueueOptions(String queueOptions) {
        this.queueOptions = queueOptions;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }


}
