package gov.epa.emissions.framework.client.meta.logs;

import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;

public class LogsTableData extends AbstractTableData {

    private List rows;

    public LogsTableData(AccessLog[] logs) {
        this.rows = createRows(logs);
    }

    public String[] columns() {
        return new String[] { "User", "Dataset Name", "Version", 
                "Access Date", "Start Date", "End Date", "Lines Exported", 
                "Time Reqrd (ms)", "Description", "Export Location" };
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(AccessLog[] logs) {
        List rows = new ArrayList();

        for (int i = 0; i < logs.length; i++) {
            AccessLog log = logs[i];
            Object[] values = { log.getUsername(), log.getDatasetname(),
                    log.getVersion(), log.getTimestamp(), log.getStartdate(),
                    log.getEnddate(), log.getLinesExported() + "", 
                    log.getTimereqrd() + "", log.getDescription(),
                    log.getFolderPath() };

            Row row = new ViewableRow(log, values);
            rows.add(row);
        }

        return rows;
    }

}
