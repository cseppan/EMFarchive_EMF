package gov.epa.emissions.commons.io.importer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReferenceTableTypes implements TableTypes {

    private List list() {
        List list = new ArrayList();
        list.add(new TableType(DatasetTypes.REFERENCE, ReferenceTable.types(), null));

        return list;
    }

    public TableType type(String datasetType) {
        for (Iterator iter = list().iterator(); iter.hasNext();) {
            TableType type = (TableType) iter.next();
            if (type.getDatasetType().equals(datasetType))
                return type;
        }

        return null;
    }

}
