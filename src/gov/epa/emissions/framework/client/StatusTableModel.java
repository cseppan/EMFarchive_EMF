package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.gui.TableHeader;
import gov.epa.emissions.framework.services.Status;

import javax.swing.table.AbstractTableModel;

public class StatusTableModel extends AbstractTableModel {

    private Status[] status;

    private TableHeader header;

    public StatusTableModel() {
        this.header = new TableHeader(new String[] { "Username", "Message Type", "Message", "Timestamp" });
    }

    public int getRowCount() {
        return status.length;
    }

    public int getColumnCount() {
        return 4;
    }

    public String getColumnName(int i) {
        return header.columnName(i);
    }

    public Object getValueAt(int arg0, int arg1) {
        return null;
    }

    public void refresh(Status[] status) {
        this.status = status;
    }

}
