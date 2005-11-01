package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.ui.AbstractEmfTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;
import gov.epa.emissions.framework.ui.SelectableEmfTableData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//FIXME: very similar to SectorCriteriaTableData. Refactor
public class DatasetTypeKeywordsTableData extends AbstractEmfTableData implements SelectableEmfTableData {
    private List rows;

    public DatasetTypeKeywordsTableData(String[] keywords) {
        this.rows = createRows(keywords);
    }

    public String[] columns() {
        return new String[] { "Select", "Keyword" };
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return true;
    }

    public void add(String keyword) {
        rows.add(row(keyword));
    }

    public void setValueAt(Object value, int row, int col) {
        if (!isEditable(col))
            return;

        EditableRow editableRow = (EditableRow) rows.get(row);
        editableRow.setValueAt(value, col);
    }

    public String[] sources() {
        List sources = sourcesList();
        return (String[]) sources.toArray(new String[0]);
    }

    public void addBlankRow() {
        add("");
    }

    public void removeSelected() {
        remove(getSelected());
    }

    private List createRows(String[] keywords) {
        List rows = new ArrayList();
        for (int i = 0; i < keywords.length; i++)
            rows.add(row(keywords[i]));

        return rows;
    }

    void remove(String keyword) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            String source = (String) row.source();
            if (source == keyword) {
                rows.remove(row);
                return;
            }
        }
    }

    private EditableRow row(String keyword) {
        RowSource source = new DatasetTypeKeywordRowSource(keyword);
        return new EditableRow(source);
    }

    private String[] getSelected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            DatasetTypeKeywordRowSource rowSource = (DatasetTypeKeywordRowSource) row.rowSource();
            if (rowSource.isSelected())
                selected.add(rowSource.source());
        }

        return (String[]) selected.toArray(new String[0]);
    }

    private void remove(String[] keywords) {
        for (int i = 0; i < keywords.length; i++)
            remove(keywords[i]);
    }

    private List sourcesList() {
        List sources = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            sources.add(row.source());
        }
        return sources;
    }

}
