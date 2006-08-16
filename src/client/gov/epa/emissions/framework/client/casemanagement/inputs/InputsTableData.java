package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.ui.ChangeableTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InputsTableData extends ChangeableTableData {

    private List rows;

    private List showables;
    
    private List notShows;

    private CaseInput[] values;

    public InputsTableData(CaseInput[] values) {
        this.showables = new ArrayList();
        this.notShows = new ArrayList();
        this.values = values;
        this.rows = createRows(values);
    }

    public String[] columns() {
        return new String[] { "Input", "Sector", "Program", "Envt. Var", "Dataset", "Version", "QA Status", "DS Type",
                "Reqd?", "Show?", "Sub Dir" };
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public void add(CaseInput input) {
        addShowList(input);
        rows.add(row(input));
        notifyChanges();
    }

    private void addShowList(CaseInput input) {
        if (input.isShow())
            showables.add(row(input));
        else
            notShows.add(row(input));
    }

    private List createRows(CaseInput[] values) {
        List rows = new ArrayList();
        
        for (int i = 0; i < values.length; i++) {
            rows.add(row(values[i]));
            addShowList(values[i]);
        }

        return rows;
    }

    private Row row(CaseInput input) {
        return new ViewableRow(new InputsRowSource(input));
    }

    public boolean isEditable(int col) {
        return true;
    }

    public CaseInput[] getValues() {
        return values;
    }
    
    private void removeFromList(CaseInput input, List list) {
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            CaseInput source = (CaseInput) row.source();
            if (source == input)
                list.remove(row);
            
            return;
        }
    }
    
    public void remove(CaseInput[] values) {
        for (int i = 0; i < values.length; i++)
            removeFromList(values[i], rows);
        
        for (int i = 0; i < values.length; i++)
            removeFromList(values[i], showables);
        
        for (int i = 0; i < values.length; i++)
            removeFromList(values[i], notShows);
    }


    public void refreshShowables() { //FIXME: needs to be modified to show showables while
        CaseInput[] inputs = sources(); //keep the notShows record
        clearLists();
        this.rows = createRows(inputs);
    }

    public void refresh() {
        CaseInput[] inputs = sources();
        clearLists();
        this.rows = createRows(inputs);
    }

    public CaseInput[] sources() {
        List sources = sourcesList();
        return (CaseInput[]) sources.toArray(new CaseInput[0]);
    }

    public CaseInput[] showSources() {
        List sources = showSourcesList();
        return (CaseInput[]) sources.toArray(new CaseInput[0]);
    }
    
    private List sourcesList() {
        List sources = new ArrayList();
        List total = getTotal();
        
        for (Iterator iter = total.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    private List getTotal() {
        List total = new ArrayList();
        total.addAll(showables);
        total.addAll(notShows);
        
        return total;
    }

    private List showSourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = showables.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }
        
        return sources;
    }
    
    private void clearLists() {
        this.showables.clear();
        this.notShows.clear();
    }

}
