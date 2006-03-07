package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EditablePage extends AbstractEditableTableData implements SelectableTableData {
    
    private List rows;

    private int datasetId;

    private Version version;

    private ChangeSet changeset;

    private TableMetadata tableMetadata;

    private String[] columnNames;

    private Class []columnClasses;

    public EditablePage(int datasetId, Version version, Page page, TableMetadata tableMetadata) {
        this.datasetId = datasetId;
        this.version = version;
        this.tableMetadata = tableMetadata;
        this.rows = createRows(page);

        changeset = new ChangeSet();
        changeset.setVersion(version);
        columnNames = setupColumns();
        columnClasses = columnClasses();
    }

    public String[] columns() {
        return columnNames;
    }

    private String[] setupColumns() {
        List list = new ArrayList();
        list.add("Select");
        ColumnMetaData[] cols = tableMetadata.getCols();
        for (int i = 4; i < cols.length; i++) {
            list.add(cols[i].getName());
        }
        list.add(cols[0].getName());  //record_id
        list.add(cols[2].getName());  //version

        return (String[]) list.toArray(new String[0]);
    }
    
    private Class[] columnClasses(){
        List classes = new ArrayList();
        classes.add(Boolean.class);
        for (int i = 1; i < columnNames.length; i++) {//first column is 'Select'
            ColumnMetaData data = tableMetadata.columnMetadata(columnNames[i]);
            classes.add(classType(data.getType()));
        }
        return (Class[]) classes.toArray(new Class[0]);
    }

    private Class classType(String type) {
        try {
            return Class.forName(type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        int size = columnNames.length;
        return (col < size - 2) ? true : false;
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

    public VersionedRecord[] getSelected() {
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

    public void addBlankRow(int row) {
        VersionedRecord record = new VersionedRecord();
        record.setDatasetId(datasetId);
        record.setVersion(version.getVersion());
        record.setDeleteVersions("");
        List tokens = new ArrayList();
        //0-3 are record id and version related columns 
        for (int i = 4; i < tableMetadata.getCols().length; i++) {
            tokens.add(null);
        }
        record.setTokens(tokens.toArray());

        rows.add(row, row(record));
        changeset.addNew(record);
    }


    public void removeSelected() {
        VersionedRecord[] record = getSelected();

        remove(record);
        changeset.addDeleted(record);
    }

    public Class getColumnClass(int col) {
        return columnClasses[col];
    }
    
    public ChangeSet changeset() {
        return changeset;
    }

    public TableMetadata getTableMetadata() {
        return tableMetadata;
    }

}
