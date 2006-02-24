package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewablePage extends AbstractTableData {

    private List rows;

    private TableMetadata tableMetadata;

    public ViewablePage(TableMetadata tableMetadata, Page page) {
        this.tableMetadata = tableMetadata;
        this.rows = createRows(page);
    }

    public Class getColumnClass(int col) {
        // ColumnMetaData[] cols = tableMetadata.getCols();
        // return classType(cols[col].getType());
        return String.class;
    }

    /*
     * private Class classType(String type) { try { return Class.forName(type); } catch (ClassNotFoundException e) {
     * throw new RuntimeException(e.getMessage());//TODO: exception what we should do } }
     */
    public String[] columns() {
        List result = new ArrayList();
        ColumnMetaData[] cols = tableMetadata.getCols();
        for (int i = 0; i < cols.length; i++)
            result.add(cols[i].getName());

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
            Object[] values = values(records[i]);
            Row row = new ViewableRow(records[i], values);
            rows.add(row);
        }

        return rows;
    }

    private Object[] values(VersionedRecord record) {
        List allTokens = new ArrayList();
        allTokens.add(new Integer("" + record.getRecordId()));
        allTokens.add(new Long("" + record.getDatasetId()));
        allTokens.add(new Long("" + record.getVersion()));
        allTokens.add(record.getDeleteVersions());

        Object[] tokens = record.getTokens();
        allTokens.addAll(Arrays.asList(tokens));
        return allTokens.toArray();
    }
}
