package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;
import gov.epa.emissions.framework.ui.SelectableEmfTableData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//FIXME: very similar to SectorCriteriaTableData. Refactor
public class KeywordsTableData extends AbstractTableData implements SelectableEmfTableData {
    private List rows;

    private Keywords masterKeywords;

    public KeywordsTableData(Keyword[] keywordsList, Keywords masterKeywords) {
        this.masterKeywords = masterKeywords;
        this.rows = createRows(keywordsList, masterKeywords);
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

    void add(String keyword) {
        rows.add(row(new Keyword(keyword), masterKeywords));
    }

    public Keyword[] sources() {
        List sources = sourcesList();
        return (Keyword[]) sources.toArray(new Keyword[0]);
    }

    public void addBlankRow() {
        add("");
    }

    public void removeSelected() {
        remove(getSelected());
    }

    private List createRows(Keyword[] keywordsList, Keywords masterKeywords) {
        List rows = new ArrayList();
        for (int i = 0; i < keywordsList.length; i++)
            rows.add(row(keywordsList[i], masterKeywords));

        return rows;
    }

    void remove(Keyword keyword) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            Keyword source = (Keyword) row.source();
            if (source == keyword) {
                rows.remove(row);
                return;
            }
        }
    }

    private EditableRow row(Keyword keyword, Keywords masterKeywords) {
        RowSource source = new KeywordRowSource(keyword, masterKeywords);
        return new EditableRow(source);
    }

    private Keyword[] getSelected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            KeywordRowSource rowSource = (KeywordRowSource) row.rowSource();
            if (rowSource.isSelected())
                selected.add(rowSource.source());
        }

        return (Keyword[]) selected.toArray(new Keyword[0]);
    }

    private void remove(Keyword[] keywords) {
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

    public Class getColumnClass(int col) {
        if (col == 0)
            return Boolean.class;

        return String.class;
    }

}
