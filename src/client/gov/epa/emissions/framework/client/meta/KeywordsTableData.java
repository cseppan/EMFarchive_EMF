package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.ui.AbstractEmfTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;
import gov.epa.emissions.framework.ui.SelectableEmfTableData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KeywordsTableData extends AbstractEmfTableData implements SelectableEmfTableData {
    private List rows;

    private Keyword[] keywords;

    public KeywordsTableData(KeyVal[] values, Keyword[] keywords) {
        this.keywords = keywords;
        this.rows = createRows(values, keywords);
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

    private List createRows(KeyVal[] values, Keyword[] keywords) {
        List rows = new ArrayList();
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i], keywords));

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

    private EditableRow row(KeyVal keyValue, Keyword[] keywords) {
        RowSource source = new KeyValueRowSource(keyValue, keywords);
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
        System.out.println("adding blank row...");
        KeyVal keyVal = new KeyVal();
        keyVal.setKeyword(new Keyword(""));
        keyVal.setValue("");

        rows.add(row(keyVal, keywords));
    }

    public void removeSelected() {
        remove(getSelected());
    }
}
