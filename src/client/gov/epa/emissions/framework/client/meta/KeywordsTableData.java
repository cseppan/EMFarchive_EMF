package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.EmfKeyVal;
import gov.epa.emissions.framework.ui.AbstractEmfTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;
import gov.epa.emissions.framework.ui.SelectableEmfTableData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KeywordsTableData extends AbstractEmfTableData implements SelectableEmfTableData {
    private List rows;

    public KeywordsTableData(EmfKeyVal[] values) {
        this.rows = createRows(values);
    }

    public String[] columns() {
        return new String[] { "Select", "Keyword", "Value" };
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return true;
    }

    private List createRows(EmfKeyVal[] values) {
        List rows = new ArrayList();
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i]));

        return rows;
    }

    void remove(EmfKeyVal keyValue) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            EmfKeyVal source = (EmfKeyVal) row.source();
            if (source == keyValue) {
                rows.remove(row);
                return;
            }
        }
    }

    public void add(EmfKeyVal keyValue) {
        rows.add(row(keyValue));
    }

    private EditableRow row(EmfKeyVal keyValue) {
        RowSource source = new KeyValueRowSource(keyValue);
        return new EditableRow(source);
    }

    public void setValueAt(Object value, int row, int col) {
        if (!isEditable(col))
            return;

        EditableRow editableRow = (EditableRow) rows.get(row);
        editableRow.setValueAt(value, col);
    }

    private EmfKeyVal[] getSelected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            KeyValueRowSource rowSource = (KeyValueRowSource) row.rowSource();
            if (rowSource.isSelected())
                selected.add(rowSource.source());
        }

        return (EmfKeyVal[]) selected.toArray(new EmfKeyVal[0]);
    }

    public void remove(EmfKeyVal[] values) {
        for (int i = 0; i < values.length; i++)
            remove(values[i]);
    }

    public EmfKeyVal[] sources() {
        List sources = sourcesList();
        return (EmfKeyVal[]) sources.toArray(new EmfKeyVal[0]);
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
        EmfKeyVal entry = new EmfKeyVal();
        entry.setKeyword("");
        entry.setValue("");

        add(entry);
    }

    public void removeSelected() {
        remove(getSelected());
    }
}
