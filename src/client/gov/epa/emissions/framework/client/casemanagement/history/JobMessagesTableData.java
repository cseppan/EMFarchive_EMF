package gov.epa.emissions.framework.client.casemanagement.history;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.ui.ChangeableTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JobMessagesTableData extends ChangeableTableData {
    private List<Row> rows;

    private JobMessage[] values;
    
    private EmfSession session;
    
    public JobMessagesTableData(JobMessage[] values, EmfSession session) {
        this.values = values;
        this.session = session;
        this.rows = createRows(values);
    }

    public String[] columns() {
        return new String[] {"Job", "Exec. Path", "Exec. Name", "Period", "Message",  
                "Message Type", "Status", "Remote User", "Exec. Mod. Date", "Received Date"};
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public void add(JobMessage msg) {
        rows.add(row(msg));
        refresh();
    }

    private List<Row> createRows(JobMessage[] values) {
        List<Row> rows = new ArrayList<Row>();
        
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i]));

        return rows;
    }

    private Row row(JobMessage msg) {
        return new ViewableRow(msg,
                new Object[] { getJob(msg), msg.getExecPath(), msg.getExecName(),
                msg.getPeriod(), msg.getMessage(), msg.getMessageType(),
                msg.getStatus(), msg.getRemoteUser(), 
                EmfDateFormat.format_YYYY_MM_DD_HH_MM(msg.getExecModifiedDate()),
                EmfDateFormat.format_YYYY_MM_DD_HH_MM(msg.getReceivedTime())});
    }
    
    private String getJob(JobMessage msg) {
        try {
            CaseJob job = session.caseService().getCaseJob(msg.getJobId());
            return job.getName();
        } catch (EmfException e) {
            e.printStackTrace();
            return "";
        }
    }

    public boolean isEditable(int col) {
        return false;
    }

    public JobMessage[] getValues() {
        return values;
    }
    
    public void remove(JobMessage[] msgs) {
        for (int i = 0; i < msgs.length; i++)
            remove(msgs[i]);
    }
    
    private void remove(JobMessage msg) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            JobMessage source = (JobMessage) row.source();
            if (source == msg) {
                rows.remove(row);
                return;
            }
        }
    }
    
    private void refresh() {
        this.rows = createRows(sources());
    }

    public JobMessage[] sources() {
        List<JobMessage> sources = sourcesList();
        return sources.toArray(new JobMessage[0]);
    }

    private List<JobMessage> sourcesList() {
        List<JobMessage> sources = new ArrayList<JobMessage>();
        
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add((JobMessage)row.source());
        }

        return sources;
    }
    
}
