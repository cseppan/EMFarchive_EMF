package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class NonEditablePageData extends AbstractTableData {

    private List rows;

    private String[] cols;

    public NonEditablePageData(Page page, String[] cols) {
        this.cols = cols;
        this.rows = createRows(page);
    }

    public Class getColumnClass(int col) {
        // TODO: need to use InternalSource + other? to lookup correct Class
        return String.class;
    }

    public String[] columns() {
        List result = new ArrayList();
        for (int i = 0; i < cols.length; i++)
            result.add(cols[i]);

        return (String[]) result.toArray(new String[0]);
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(Page page) {
        List rows = new ArrayList();
        VersionedRecord[] records = page.getRecords();

        for (int i = 0; i < records.length; i++) {
            String[] values = values(records[i]);
            Row row = new ViewableRow(records[i], values);
            rows.add(row);
        }

        return rows;
    }

    private String[] values(VersionedRecord record) {
        return record.getTokens();
    }

}
