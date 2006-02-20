package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotesTableData extends AbstractTableData {

    private List rows;

    private Note[] values;

    private List additions;

    public NotesTableData(Note[] values) {
        this.values = values;
        this.rows = createRows(values);
        this.additions = new ArrayList();
    }

    public String[] columns() {
        return new String[] { "Summary", "Type", "Version", "Creator", "Date", "References", "Details" };
    }

    public Class getColumnClass(int col) {
        if (col == 2)
            return Long.class;
        if (col == 4)
            return Date.class;
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public void add(Note note) {
        additions.add(note);
        rows.add(row(note));
    }

    private List createRows(Note[] values) {
        List rows = new ArrayList();
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i]));

        return rows;
    }

    private Row row(Note note) {
        return new ViewableRow(new NotesRowSource(note));
    }

    public boolean isEditable(int col) {
        return false;
    }

    public Note[] getValues() {
        return values;
    }

    public Note[] additions() {
        return (Note[]) additions.toArray(new Note[0]);
    }

}
