package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.ui.RowSource;

public class OutputsRowSource implements RowSource {
    private CaseOutput source;

    private EmfSession session;

    private String[] datasetValues;

    public OutputsRowSource(CaseOutput source, EmfSession session) {
        this.source = source;
        this.session = session;
        this.datasetValues = getDatasetValues(source, session);
    }

    public Object[] values() {
        if (datasetValues == null)
            return null;
        
        return new Object[] { getOutputName(source), getJobName(source), getSector(source), getDatasetProperty("name"),
                getDatasetProperty("datasetType"), getStatus(source), getDatasetProperty("creator"),
                getDatasetProperty("createdDateTime"), getExecName(source) };
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

        try {
            CaseJob job = session.caseService().getCaseJob(output.getJobId());

            if (job != null)
                jobName = job.getName();
        } catch (EmfException e) {
            e.printStackTrace();
        }
        
        return jobName;
    }

    private String getSector(CaseOutput output) {
        return "";
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
}
