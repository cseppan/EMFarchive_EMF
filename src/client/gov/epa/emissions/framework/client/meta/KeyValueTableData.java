package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.client.data.Keywords;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;
import gov.epa.emissions.framework.ui.SelectableEmfTableData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KeyValueTableData extends AbstractTableData implements SelectableEmfTableData {
    private List rows;

    private Keywords masterKeywords;

    public KeyValueTableData(KeyVal[] values, Keywords masterKeywords) {
        this.masterKeywords = masterKeywords;
        this.rows = createRows(values, masterKeywords);
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

    private List createRows(KeyVal[] values, Keywords masterKeywords) {
        List rows = new ArrayList();
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i], masterKeywords));

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

    private EditableRow row(KeyVal keyValue, Keywords keywords) {
        RowSource source = new KeyValueRowSource(keyValue, keywords);
        return new EditableRow(source);
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
        KeyVal keyVal = new KeyVal();
        keyVal.setKeyword(new Keyword(""));
        keyVal.setValue("");

        rows.add(row(keyVal, masterKeywords));
    }

    public void removeSelected() {
        remove(getSelected());
    }

    public Class getColumnClass(int col) {
        if (col == 0)
            return Boolean.class;

        return String.class;
    }
}
