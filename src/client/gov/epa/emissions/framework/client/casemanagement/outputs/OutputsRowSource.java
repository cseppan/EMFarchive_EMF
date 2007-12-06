package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.ui.RowSource;

public class OutputsRowSource implements RowSource {
    private CaseOutput output;

    private EmfSession session;

    private String[] datasetValues;

    public OutputsRowSource(CaseOutput source, EmfSession session) throws EmfException {
        this.output = source;
        this.session = session;
        this.datasetValues = getDatasetValues(output, session);
    }

    public Object[] values() {
        return new Object[] { getOutputName(output), getJobName(output), getSector(output), getDatasetProperty("name"),
                getDatasetProperty("datasetType"), getStatus(output), getDatasetProperty("creator"),
                getDatasetProperty("createdDateTime"), getExecName(output) };
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
        // NOTE Auto-generated method stub
        return null;
    }

    public void validate(int rowNumber) throws EmfException {
        // NOTE Auto-generated method stub
        throw new EmfException("Under construction.");
    }

    private String[] getDatasetValues(CaseOutput output, EmfSession session) throws EmfException {
        return session.dataService().getDatasetValues(new Integer(output.getDatasetId()));
    }
}
