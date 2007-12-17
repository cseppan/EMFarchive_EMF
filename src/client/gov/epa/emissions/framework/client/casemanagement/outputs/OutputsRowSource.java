package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.ui.RowSource;

public class OutputsRowSource implements RowSource {
    private CaseOutput source;

    private String[] datasetValues;
    
    private CaseJob job; 

    public OutputsRowSource(CaseOutput source, EmfSession session) {
        this.source = source;
        this.datasetValues = getDatasetValues(source, session);
        this.job = getCaseJob(source, session);
    }


    public Object[] values() {
        if (datasetValues == null)
            return null;
        
        return new Object[] { getOutputName(source), getJobName(source), getSector(source), getDatasetProperty("name"),
                getDatasetProperty("datasetType"), getStatus(source), getDatasetProperty("creator"),
                getDatasetProperty("createdDateTime"), getExecName(source), getMessage(source) };
    }

    private String getMessage(CaseOutput source) {
        String msg=null;
        String message=source.getMessage();
        if(message.length()>20) {
            msg=message.substring(0,19);
            return msg;
        }
        return message;
    }


    private Object getExecName(CaseOutput output) {
        return output.getExecName() != null ? output.getExecName() : "";
    }

    private Object getStatus(CaseOutput output) {
        return output.getStatus() != null ? output.getStatus() : "";
    }

    private String getDatasetProperty(String property) {
        String value = null;

        for (String values : datasetValues) {
            if (values.startsWith(property))
                value = values.substring(values.indexOf(",") + 1);
        }
        return value;
    }

    private String getOutputName(CaseOutput output) {
        return output.getName() != null ? output.getName() : "";
    }

    private String getJobName(CaseOutput output) {
        String jobName = null;
        if (job != null)
            jobName = job.getName();
        return jobName;
    }

    private String getSector(CaseOutput output) {
        String sectorName=null; 
        if (job != null)
        sectorName=job.getSector().getName();
        return sectorName;
    }

    public void setValueAt(int column, Object val) {
        // NOTE Auto-generated method stub

    }

    public Object source() {
        return source;
    }

    public void validate(int rowNumber) throws EmfException {
        // NOTE Auto-generated method stub
        throw new EmfException("Under construction.");
    }

    private String[] getDatasetValues(CaseOutput output, EmfSession session) {
        String[] values = null;
        
        try {
            values = session.dataService().getDatasetValues(new Integer(output.getDatasetId()));
        } catch (Exception e) {
            return null;
        }
        
        return values;
    }
    
   private CaseJob getCaseJob(CaseOutput output, EmfSession session) {
        try {
            job =session.caseService().getCaseJob(output.getJobId());
        } catch (EmfException e) {
            return null; 
        }
        return job;
    }
}
