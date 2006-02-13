package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.SelectableRow;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class NotesTableData extends AbstractTableData {

    private List rows;

    private Note[] values;

    public NotesTableData(Note[] values) {
        this.values = values;
        this.rows = createRows(values);
    }

    public String[] columns() {
        return new String[] { "Select", "Name", "Type", "Details", "References", "Creator", "Date" };
    }

    public Class getColumnClass(int col) {
        if (col == 0)
            return Boolean.class;
        if (col == 6)
            return Date.class;
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public void add(Note version) {
        rows.add(row(version));
    }

    private List createRows(Note[] values) {
        List rows = new ArrayList();
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i]));

        return rows;
    }

    private Row row(Note note) {
        return new SelectableRow(new NotesRowSource(note));
    }

    public boolean isEditable(int col) {
        return (col == 0) ? true : false;
    }

    public Note[] selected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            SelectableRow row = (SelectableRow) iter.next();
            if (row.isSelected())
                selected.add(row.source());
        }

        return (Note[]) selected.toArray(new Note[0]);
    }

    public Note[] getValues() {
        return values;
    }

}
