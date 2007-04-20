package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.ui.RowSource;

public class CaseJobsRowSource implements RowSource {

    private CaseJob job;

    public CaseJobsRowSource(CaseJob source) {
        this.job = source;
    }

    public Object[] values() {
        return new Object[] { getJobName(job), getJobNum(job), 
                getSectorName(job), getExecutableName(job),
                getVersion(job), getArgs(job), getOrder(job),
                getRunStatus(job), getPath(job), getQOpt(job),
                getIDInQ(job), getStartDate(job), getCompleteDate(job),
                getUser(job), getRunLog(job), getHost(job), getPurpose(job) };
    }
    
    private String getJobName(CaseJob job) {
        return (job.getName() == null) ? "" : job.getName();
    }
    
    private Float getJobNum(CaseJob job) {
        return new Float(job.getJobNo());
    }
    
    private String getSectorName(CaseJob job) {
        return (job.getSector() == null) ? "All sectors" : job.getSector().getName();
    }
    
    private String getExecutableName(CaseJob job) {
        return (job.getExecutable() == null) ? "" : job.getExecutable()[0].getName();
    }
    
    private Integer getVersion(CaseJob job) {
        return new Integer(job.getVersion());
    }
    
    private String getArgs(CaseJob job) {
        return job.getArgs();
    }
    
    private Integer getOrder(CaseJob job) {
        return new Integer(job.getOrder());
    }
    
    private String getRunStatus(CaseJob job) {
        return (job.getRunstatus() == null) ? "" : job.getRunstatus().getName();
    }
    
    private String getPath(CaseJob job) {
        return job.getPath();
    }
    
    private Integer getIDInQ(CaseJob job) {
        return new Integer(job.getIdInQueue());
    }

    private String getQOpt(CaseJob job) {
        return job.getQueOptions();
    }
    
    private String getStartDate(CaseJob job) {
        return (job.getRunStartDate() == null) ? "" : EmfDateFormat.format_YYYY_MM_DD_HH_MM(job.getRunStartDate());
    }

    private String getCompleteDate(CaseJob job) {
        return (job.getRunCompletionDate() == null) ? "" : EmfDateFormat.format_YYYY_MM_DD_HH_MM(job.getRunCompletionDate());
    }
    
    private String getPurpose(CaseJob job) {
        return job.getPurpose();
    }

    private String getHost(CaseJob job) {
        return (job.getHost() == null) ? "" : job.getHost().getName();
    }

    private String getRunLog(CaseJob job) {
        return job.getRunLog();
    }

    private String getUser(CaseJob job) {
        return (job.getUser() == null) ? "" : job.getUser().getName();
    }
    
    public Object source() {
        return job;
    }

    public void validate(int rowNumber) {// No Op
    }

    public void setValueAt(int column, Object val) {// No Op
    }
}