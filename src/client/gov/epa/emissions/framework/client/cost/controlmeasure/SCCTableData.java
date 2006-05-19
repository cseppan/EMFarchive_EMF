package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.InlineEditableTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SCCTableData extends AbstractEditableTableData implements InlineEditableTableData {

    private List rows;

    public SCCTableData(String[] sccs) {
        rows = createRows(sccs);
    }

    private List createRows(String[] sccs) {
        List rows = new ArrayList();
        for (int i = 0; i < sccs.length; i++) {
            Row row = row(sccs[i]);
            rows.add(row);
        }
        return rows;
    }

    private Row row(String scc) {
        RowSource source = new CMSCCRowSource(scc);
        Row row = new EditableRow(source);
        return row;
    }

    public String[] columns() {
        return new String[] {"Select", "Name" };
    }

    public Class getColumnClass(int col) {
        if (col == 0)
            return Boolean.class;

        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return true;
    }

    public void addBlankRow() {
        Row row = row("");
        rows.add(row);
    }

    public void removeSelected() {
        remove(getSelected());
    }

    private String[] getSelected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            CMSCCRowSource rowSource = (CMSCCRowSource) row.rowSource();
            if (rowSource.isSelected())
                selected.add(rowSource.source());
        }

        return (String[]) selected.toArray(new String[0]);
    }

    private void remove(String[] sccs) {
        for (int i = 0; i < sccs.length; i++) {
            remove(sccs[i]);
        }
    }

    public void remove(String scc) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            String source = (String) row.source();
            if (source == scc) {
                rows.remove(row);
                return;
            }
        }
    }

    // FIXME: removed eventually once we obtained the sccs using the referece table
    public class CMSCCRowSource implements RowSource {

        private String source;

        private Boolean selected;

        public CMSCCRowSource(String scc) {
            this.source = scc;
            this.selected = Boolean.FALSE;
        }

        public Object[] values() {
            return new Object[] { selected, source };
        }

        public void setValueAt(int column, Object val) {
            if (column == 0) {
                selected = (Boolean) val;
            } else {
                this.source = (String) val;
            }
        }

        public Object source() {
            return source;
        }

        public boolean isSelected() {
            return selected.booleanValue();
        }

        public void validate(int rowNumber) throws EmfException {
            if (source.length() == 0) {
                throw new EmfException("Empty scc at row " + rowNumber);
            }
        }
    }

}
