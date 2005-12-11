package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VersionsTableData extends AbstractTableData {

    private List rows;

    public VersionsTableData(Version[] values) {
        this.rows = createRows(values);
    }

    public String[] columns() {
        // TODO: convert these into 'TableColumn' (w/ properties: name, editable)
        return new String[] { "Select", "Name", "Version", "Base", "Date" };
    }

    public Class getColumnClass(int col) {
        if (col == 0)
            return Boolean.class;

        return String.class;
    }

    public List rows() {
        return rows;
    }

    private List createRows(Version[] values) {
        List rows = new ArrayList();
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i]));

        return rows;
    }

    private EditableRow row(Version version) {
        RowSource source = new VersionRowSource(version);
        return new EditableRow(source);
    }

    public boolean isEditable(int col) {
        return (col == 0) ? true : false;
    }

    public Version[] selected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            VersionRowSource rowSource = (VersionRowSource) row.rowSource();
            if (rowSource.isSelected())
                selected.add(rowSource.source());
        }

        return (Version[]) selected.toArray(new Version[0]);
    }

}
