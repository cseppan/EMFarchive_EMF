package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.exim.Row;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.LoggingServices;
import gov.epa.emissions.framework.ui.AbstractEmfTableData;

import java.util.ArrayList;
import java.util.List;

public class AccessLogTableData extends AbstractEmfTableData {

    private List rows;

    public AccessLogTableData(EmfDataset dataset, LoggingServices services) throws EmfException {
        // TODO: should use lazy load ?
        this.rows = createRows(services.getAccessLogs(dataset.getDatasetid()));
    }

    public String[] columns() {
        return new String[] { "User", "Date", "Version", "Description", "Export Location" };
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
            Object[] values = { log.getUsername(), log.getTimestamp(), log.getVersion(), log.getDescription(),
                    log.getFolderPath() };

            Row row = new Row(log, values);
            rows.add(row);
        }

        return rows;
    }

}
