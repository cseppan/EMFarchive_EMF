package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.ui.AbstractEmfTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;

public class AccessLogTableData extends AbstractEmfTableData {

    private List rows;

    public AccessLogTableData(AccessLog[] logs) {
        this.rows = createRows(logs);
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

            Row row = new ViewableRow(log, values);
            rows.add(row);
        }

        return rows;
    }

}
