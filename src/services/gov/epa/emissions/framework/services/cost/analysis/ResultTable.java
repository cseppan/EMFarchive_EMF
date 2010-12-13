package gov.epa.emissions.framework.services.cost.analysis;

import gov.epa.emissions.commons.ForBugs;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class ResultTable {
    
    private DataTable delegate;
    
    public ResultTable(String table, Datasource datasource) {
        EmfDataset dataset = new EmfDataset();
        
        if ( ForBugs.FIX_BUG3555) {
            String newName = table;
            if ( newName != null) {
                newName = newName.trim();
            }
            dataset.setName(newName);
        } else {
            dataset.setName(table);
        }
        
        this.delegate = new DataTable(dataset, datasource);
    }
    
    public String name() {
        return delegate.name();
    }
    
    public void create(String table, TableFormat tableFormat) throws Exception {
        delegate.create(table, tableFormat);
    }

    public void create(TableFormat tableFormat) throws Exception {
        delegate.create(name(), tableFormat);
    }

    public void drop(String table) throws Exception {
        delegate.drop(table);
    }

    public void drop() throws Exception {
        delegate.drop();
    }

    public boolean exists(String table) throws Exception {
        return delegate.exists(table);
    }
    
}
