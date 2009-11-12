package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CMReferenceTableData extends AbstractEditableTableData {

    private List<ViewableRow> rows;

    public CMReferenceTableData(Reference[] references) {
        rows = createRows(references);
    }

    private List<ViewableRow> createRows(Reference[] references) {
        
        rows = new ArrayList<ViewableRow>();
        for (Reference reference : references) {
            rows.add(row(reference));
        }
        
        return rows;
    }

    private ViewableRow row(Reference reference) {

        Object[] values = { reference.getId(), reference.getDescription() };
        return new ViewableRow(reference, values);
    }

    public String[] columns() {
        return new String[] { "ID", "Description" };
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public List rows() {
        return this.rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

    public Reference[] sources() {
        return sourcesList().toArray(new Reference[0]);
    }

    private List<Reference> sourcesList() {

        List<Reference> sources = new ArrayList<Reference>();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add((Reference) row.source());
        }

        return sources;
    }

    public boolean contains(Reference reference) {
        return this.sourcesList().contains(reference);
    }
    
    public void add(Reference reference) {
        rows.add(row(reference));
    }

    private void remove(Reference reference) {

        for (Iterator iter = rows.iterator(); iter.hasNext();) {

            ViewableRow row = (ViewableRow) iter.next();
            Reference source = (Reference) row.source();

            if (source.equals(reference)) {

                rows.remove(row);
                return;
            }
        }
    }

    public void remove(List<Reference> references) {

        for (Reference reference : references) {
            this.remove(reference);
        }
    }
}
