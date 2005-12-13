package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.framework.client.data.Keywords;
import gov.epa.emissions.framework.client.meta.KeyValueRowSource;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;
import gov.epa.emissions.framework.ui.SelectableEmfTableData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EditablePageTableData extends AbstractTableData implements SelectableEmfTableData {
    private List rows;

    private Keywords masterKeywords;

    private String[] cols;

    private Page page;

    public EditablePageTableData(String[] cols, Page page) {
        this.cols = cols;
        this.page = page;
        this.rows = createRows(page);
    }

    public String[] columns() {
        List list = new ArrayList();
        list.add("Select");
        list.addAll(Arrays.asList(cols));

        return (String[]) list.toArray(new String[0]);
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return true;
    }

    private List createRows(Page page) {
        List rows = new ArrayList();
        VersionedRecord[] records = page.getRecords();

        for (int i = 0; i < records.length; i++) {
            EditableRow row = row(records[i]);
            rows.add(row);
        }

        return rows;
    }

    void remove(KeyVal keyValue) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            KeyVal source = (KeyVal) row.source();
            if (source == keyValue) {
                rows.remove(row);
                return;
            }
        }
    }

    private EditableRow row(VersionedRecord record) {
        RowSource source = new PageDataRowSource(record);
        return new EditableRow(source);
    }

    public void setValueAt(Object value, int row, int col) {
        if (!isEditable(col))
            return;

        EditableRow editableRow = (EditableRow) rows.get(row);
        editableRow.setValueAt(value, col);
    }

    private KeyVal[] getSelected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            KeyValueRowSource rowSource = (KeyValueRowSource) row.rowSource();
            if (rowSource.isSelected())
                selected.add(rowSource.source());
        }

        return (KeyVal[]) selected.toArray(new KeyVal[0]);
    }

    public void remove(KeyVal[] values) {
        for (int i = 0; i < values.length; i++)
            remove(values[i]);
    }

    public KeyVal[] sources() {
        List sources = sourcesList();
        return (KeyVal[]) sources.toArray(new KeyVal[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            KeyValueRowSource rowSource = (KeyValueRowSource) row.rowSource();
            sources.add(rowSource.source());
        }
        return sources;
    }

    public void addBlankRow() {
        //TODO: source the template w/ same dataset id, and ??
        rows.add(row(new VersionedRecord()));
    }

    public void removeSelected() {
        remove(getSelected());
    }

    public Class getColumnClass(int col) {
        return col == 0 ? Boolean.class : String.class;
    }
}
