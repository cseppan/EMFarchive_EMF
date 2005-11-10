package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.services.Page;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

public class PageData extends AbstractTableData {

    private InternalSource source;

    private List rows;

    public PageData(InternalSource source, Page page) {
        this.source = source;
        this.rows = createRows(page);
    }

    public String[] columns() {
        String[] cols = source.getCols();
        List result = new ArrayList();
        // ignore first col - dataset id, not for display
        for (int i = 1; i < cols.length; i++)
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
        Record[] records = page.getRecords();

        for (int i = 0; i < records.length; i++) {
            String[] values = values(records[i]);
            Row row = new ViewableRow(records[i], values);
            rows.add(row);
        }

        return rows;
    }

    private String[] values(Record record) {
        List values = new ArrayList();
        int[] colIndexes = colIndexes();
        for (int i = 0; i < colIndexes.length; i++)
            values.add(record.token(colIndexes[i]));

        return (String[]) values.toArray(new String[0]);
    }

    private int[] colIndexes() {
        IntList indexes = new ArrayIntList();
        // ignore first col - dataset id, not for display
        for (int i = 1; i <= colsCount(); i++)
            indexes.add(i);

        return indexes.toArray();
    }

    private int colsCount() {
        return columns().length;
    }
}
