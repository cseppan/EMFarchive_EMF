package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.RemoteCommand;
import gov.epa.emissions.framework.tasks.CaseJobTaskManager;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CaseJobTask extends Task {
    
    private static Log log = LogFactory.getLog(CaseJobTask.class);

    private User user = null;

    private int numDepends=0;
    
    // private String runRedirect = ">&"; // shell specific redirect

    private String jobFileContent = null;

    private String logFile = null;

    private String jobFile = null;

    private String queueOptions = null;

    private String hostName = null;

    private int jobId;

    private String jobName = null;

    private int caseId;

    private String exportTaskSubmitterId = null;

    boolean exportsSuccess = false;

    boolean dependenciesSet = false;

    private String jobkey;

    private String runRedirect = ">&"; // shell specific redirect

    public String getJobkey() {
        return jobkey;
    }

    public void setJobkey(String jobkey) {
        this.jobkey = jobkey;
    }

    public boolean isExportsSuccess() {
        return exportsSuccess;
    }

    public void setExportsSuccess(boolean exportsSuccess) {
        this.exportsSuccess = exportsSuccess;
    }

    public boolean isDependenciesSet() {
        return dependenciesSet;
    }

    public void setDependenciesSet(boolean dependenciesSet) {
        this.dependenciesSet = dependenciesSet;
    }

    public String getExportTaskSubmitterId() {
        return exportTaskSubmitterId;
    }

    public void setExportTaskSubmitterId(String exportTaskSubmitterId) {
        this.exportTaskSubmitterId = exportTaskSubmitterId;
    }

    public CaseJobTask(int jobId, int caseId, User user) {
        super();
        createId();
        if (DebugLevels.DEBUG_1)
            System.out.println(">>>> " + createId());
        this.user = user;
        this.jobId = jobId;
        this.caseId = caseId;
        log.info("created a CaseJobTask: " + this.taskId + " for caseId: " + this.caseId);
    }

    @Override
    public boolean isReady() {

        // Modify the algorithm to use all parameters that indicate rediness
        // Initial algorithm is simple. CJT isReadyFlag at the get-go
        this.isReadyFlag = this.dependenciesSet && this.exportsSuccess;

        return this.isReadyFlag;
    }

    public void run() {
        String status = null;
        String mesg = null;

        System.out.println("@@@@ CASE job task RUNNING jobId= " + jobId + " jobName= " + jobName + " caseId= " + caseId
                + " CaseJobTask id= " + this.getTaskId() + " now running in Thread id= "
                + Thread.currentThread().getId());

//        String status = "completed";
//        String mesg = " was pseudo successfull";

        try {
            this.createJobFile();
            status = "completed";
        } catch (Exception e) {
            log.error("Exception while creating JobFile when running CaseJobTask. See stacktrace for details");
            e.printStackTrace();
            
            status = "failed";
            mesg="Failed to create  job script: " + this.jobFile;
        }

        // Create an execution string and submit job to the queue,
        // if the key word $EMF_JOBLOG is in the queue options,
        // replace w/ log file

        String executionStr = null;
        String qOptions = this.queueOptions;
        String queueOptionsLog = qOptions.replace("$EMF_JOBLOG", this.logFile);

        if (queueOptionsLog.equals("")) {
            executionStr = this.jobFile;
        } else {
            executionStr = queueOptionsLog + " " + this.jobFile;
        }

        /*
         * execute the job script Note if hostname is localhost this is done locally w/o ssh and stdout and stderr is
         * redirected to the log. This redirect is currently shell specific (should generalize) if hostname is not
         * localhost it is through ssh
         */
        String username = this.user.getUsername();
        try {
            if (hostName.equals("localhost")) {
                // execute on local machine
                executionStr = executionStr + " " + this.runRedirect + " " + this.logFile;
                RemoteCommand.executeLocal(executionStr);

            } else {
                // execute on remote machine and log stdout
                InputStream inStream = RemoteCommand.execute(username, hostName, executionStr);

                String outTitle = "stdout from (" + hostName + "): " + executionStr;
                RemoteCommand.logStdout(outTitle, inStream);

                // capture PBSqueueId and send back to case job submitter
                // TODO:
            }

            status="completed";

        } catch (Exception e) {
            log.error("Error executing job file: " + jobFile + " Execution string= " + executionStr);
            e.printStackTrace();
            status = "failed";
            mesg="Failed to submit job to Host: " + hostName + " for job: " + this.jobFile;

        }


        try {
            CaseJobTaskManager.callBackFromThread(this.taskId, this.submitterId, status, mesg);
        } catch (EmfException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Write out the contents of the string (jobFileContent) to the file (jobFile)
     * 
     * @throws EmfException
     */
    private void createJobFile() throws EmfException {
        FileOutputStream out; // declare a file output object
        PrintStream p; // declare a print stream object
        File outFile = null;

        try {
            outFile = new File(jobFile);

            // Create a new file output stream
            // connected to jobFile
            out = new FileOutputStream(outFile);

            // Connect print stream to the output stream
            p = new PrintStream(out);

            p.println(this.jobFileContent);
            p.flush();
            p.close();

            // Make script executable
            outFile.setExecutable(true, false);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e);
            throw new EmfException("In createJobFile: Error writing jobFile: " + jobFile);
        }
    }

    public void setRunRedirect(String runRedirect) {
        // this.runRedirect = runRedirect;
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

    public void setJobFileContent(String jobFileContent) {
        this.jobFileContent = jobFileContent;
    }

    // ***********************************************************
    // FIXME: After code is working remove everything below
    public String getJobFileContent() {
        return jobFileContent;
    }

    public String getLogFile() {
        return logFile;
    }

    public String getJobFile() {
        return jobFile;
    }

    public String getQueueOptions() {
        return queueOptions;
    }

    public String getHostName() {
        return hostName;
    }
    // FIXME: After code is working remove everything ABOVE
    // ***********************************************************

    public int getNumDepends() {
        return numDepends;
    }

    public void setNumDepends(int numDepends) {
        this.numDepends = numDepends;
    }


    @Override
    public int compareTo(Object o) {
        CaseJobTask second = (CaseJobTask) o;
       int thisDependsSize = this.numDepends;
       int secondDependsSize = second.getNumDepends();
       
       if (thisDependsSize < secondDependsSize){
           return -1;
       }else if (secondDependsSize < thisDependsSize){
           return 1;
       }
       return 0;
    }


}
