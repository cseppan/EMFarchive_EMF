package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;
import gov.epa.emissions.framework.ui.SelectableEmfTableData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EditablePage extends AbstractTableData implements SelectableEmfTableData {
    private List rows;

    private int datasetId;

    private Version version;

    private ChangeSet changeset;

    private TableMetadata tableMetadata;

    public EditablePage(int datasetId, Version version, Page page, TableMetadata tableMetadata) {
        this.datasetId = datasetId;
        this.version = version;
        this.tableMetadata = tableMetadata;
        this.rows = createRows(page);

        changeset = new ChangeSet();
        changeset.setVersion(version);
    }

    public String[] columns() {
        List list = new ArrayList();
        list.add("Select");
        ColumnMetaData[] cols = tableMetadata.getCols();
        for (int i = 4; i < cols.length; i++) {// FIXME: have to add record id and verson related columns
            list.add(cols[i].getName());
        }

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
        for (int i = 4; i < tableMetadata.getCols().length; i++) {
            // FIXME: have to add record id and verson related columns
            tokens.add(null);
        }
        record.setTokens(tokens.toArray());

        rows.add(row(record));
        changeset.addNew(record);
    }

    public void removeSelected() {
        VersionedRecord[] record = getSelected();

        remove(record);
        changeset.addDeleted(record);
    }

    public Class getColumnClass(int col) {
        return col == 0 ? Boolean.class : classType(col);
    }

    private Class classType(int col) {
        String type = tableMetadata.getCols()[col+3].getType(); //FIXME: remove the addition after adding version adn record id columns
        try {
            return Class.forName(type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ChangeSet changeset() {
        return changeset;
    }

    public TableMetadata getTableMetadata() {
        return tableMetadata;
    }

}
