package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;
import gov.epa.emissions.framework.ui.SelectableEmfTableData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EditablePage extends AbstractTableData implements SelectableEmfTableData {
    private List rows;

    private String[] cols;

    private int datasetId;

    private Version version;

    private ChangeSet changeset;

    public EditablePage(int datasetId, Version version, Page page, String[] cols) {
        this.datasetId = datasetId;
        this.version = version;
        this.cols = cols;
        this.rows = createRows(page);

        changeset = new ChangeSet();
        changeset.setVersion(version);
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

    void remove(VersionedRecord record) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            VersionedRecord source = (VersionedRecord) row.source();
            if (source == record) {
                rows.remove(row);
                return;
            }
        }
    }

    private EditableRow row(VersionedRecord record) {
        RowSource source = new EditablePageRowSource(record);
        return new EditableRow(source);
    }

    public void setValueAt(Object value, int row, int col) {
        if (!isEditable(col))
            return;

        super.setValueAt(value, row, col);

        if (col != 0) // not Select
            addUpdated(row);
    }

    private void addUpdated(int row) {
        EditableRow editableRow = (EditableRow) rows.get(row);
        VersionedRecord record = (VersionedRecord) editableRow.source();

        if (changeset.containsNew(record)) {// ignore changes to new records
            return;
        }

        if (!changeset.containsUpdated(record)) {
            changeset.addUpdated(record);
        }
    }

    private VersionedRecord[] getSelected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            EditablePageRowSource rowSource = (EditablePageRowSource) row.rowSource();
            if (rowSource.isSelected())
                selected.add(rowSource.source());
        }

        return (VersionedRecord[]) selected.toArray(new VersionedRecord[0]);
    }

    public void remove(VersionedRecord[] values) {
        for (int i = 0; i < values.length; i++)
            remove(values[i]);
    }

    public VersionedRecord[] sources() {
        List sources = sourcesList();
        return (VersionedRecord[]) sources.toArray(new VersionedRecord[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            EditablePageRowSource rowSource = (EditablePageRowSource) row.rowSource();
            sources.add(rowSource.source());
        }
        return sources;
    }

    public void addBlankRow() {
        VersionedRecord record = new VersionedRecord();
        record.setDatasetId(datasetId);
        record.setVersion(version.getVersion());
        record.setDeleteVersions("");
        List tokens = new ArrayList();
        for (int i = 0; i < cols.length; i++)
            tokens.add("");
        record.setTokens((String[]) tokens.toArray(new String[0]));

        rows.add(row(record));
        changeset.addNew(record);
    }

    public void removeSelected() {
        VersionedRecord[] record = getSelected();

        remove(record);
        changeset.addDeleted(record);
    }

    public Class getColumnClass(int col) {
        return col == 0 ? Boolean.class : String.class;
    }

    public ChangeSet changeset() {
        return changeset;
    }

}
