package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.data.Keywords;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;
import gov.epa.emissions.framework.ui.SelectableEmfTableData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EditableKeyValueTableData extends AbstractTableData implements SelectableEmfTableData {
    private List rows;

    private Keywords masterKeywords;

    public EditableKeyValueTableData(KeyVal[] values, Keywords masterKeywords) {
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
        RowSource source = new EditableKeyValueRowSource(keyValue, keywords);
        return new EditableRow(source);
    }

    private KeyVal[] getSelected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            EditableKeyValueRowSource rowSource = (EditableKeyValueRowSource) row.rowSource();
            if (rowSource.isSelected())
                selected.add(rowSource.source());
        }

        return (KeyVal[]) selected.toArray(new KeyVal[0]);
    }

    public void remove(KeyVal[] values) {
        for (int i = 0; i < values.length; i++)
            remove(values[i]);
    }

    public KeyVal[] sources() throws EmfException {
        List sources = sourcesList();
        return (KeyVal[]) sources.toArray(new KeyVal[0]);
    }

    private List sourcesList() throws EmfException {
        List sources = new ArrayList();
        int rowNumber = 0;
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            rowNumber++;
            EditableRow row = (EditableRow) iter.next();
            EditableKeyValueRowSource rowSource = (EditableKeyValueRowSource) row.rowSource();
            rowSource.validate(rowNumber);
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
